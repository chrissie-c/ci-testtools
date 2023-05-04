// Program to generate Jenkinsfile from a template and a list
// of axes and nodes

// ./make_jf [-v] -a jenkins-axes.txt -t Jenkinsfile.template
// writes to stdout, verbose stuff to stderr


use std::collections::HashMap;
use std::fs::File;
use std::io;
use std::io::BufRead;
use std::path::Path;
use structopt::StructOpt;
use chrono::prelude::*;

// Command-line options
#[derive(Debug, StructOpt)]
#[structopt(name = "make-jf", about = "Make axes in a pipeline Jenksfile")]
/// do it
struct Opt {
    #[structopt(short = "v", long = "verbose", help = "Print verbose debugging info")]
    verbose: bool,

    #[structopt(short = "t", long = "template", help = "Template Jenkinsfile")]
    template_filename: String,

    #[structopt(short = "a", long = "axies", help = "List of axes")]
    axes_filename: String,
}

// Utility iterator for reading lines from a file
fn read_lines<P>(filename: P) -> io::Result<io::Lines<io::BufReader<File>>>
where
    P: AsRef<Path>,
{
    let file = File::open(filename)?;
    Ok(io::BufReader::new(file).lines())
}

// Read the axes file and populate the list of axes and all nodes
fn read_axes_file(
    opt: &Opt,
    filename: &String,
) -> Result<(HashMap<String, Vec<String>>, Vec<String>), std::io::Error> {
    let mut axis_map = HashMap::<String, Vec<String>>::new();
    let mut allnodes = Vec::<String>::new();

    match read_lines(filename) {
        Ok(lines) => {
            for l in lines.into_iter().flatten() {
                if l.trim_start().chars().next().unwrap_or('#') != '#' {
                    // Get a Vec of the axis + nodes
                    let mut nodes = Vec::<String>::new();
                    let mut num = 0;
                    let mut axis = String::new();
                    for s in l.trim().split([' ', ':']) {
                        if num == 0 {
                            axis = s.to_string();
                            num += 1;
                        } else {
                            nodes.push(s.to_string());
                            allnodes.push(s.to_string());
                        }
                    }
                    // Put them in order for neatness sake
                    nodes.sort();

                    if opt.verbose {
                        eprintln!("axis: {axis}: {nodes:?}");
                    }
                    axis_map.insert(axis, nodes);
                }
            }
        }
        Err(e) => return Err(e),
    }

    // Deduplicate the allnodes vector
    allnodes.sort();
    allnodes.dedup();
    Ok((axis_map, allnodes))
}

fn replace_with_all_nodes(line: &str, template_text: &str, allnodes: &Vec<String>) -> String {
    // Turn allnodes into a string
    let mut allstring = String::new();
    let mut first = true;
    for s in allnodes {
        if !first {
            allstring += ", ";
        }
        allstring += format!("'{s}'").as_str();
        first = false;
    }

    line.replace(template_text, &allstring.to_owned())
}

fn replace_with_all_functions(
    line: &str,
    template_text: &str,
    axes: &HashMap<String, Vec<String>>,
) -> String {
    // Turn axes into a string
    let mut allstring = String::new();
    let mut first = true;
    for f in axes.keys() {
        if !first {
            allstring += ", ";
        }
        allstring += format!("'{f}'").as_str();
        first = false;
    }

    line.replace(template_text, &allstring.to_owned())
}

// Replace template string with all of the nodes from allnodes that are NOT mentioned in axis_nodes
fn replace_with_excludes(
    line: &str,
    template_text: &str,
    axis_nodes: &[String],
    allnodes: &Vec<String>,
) -> String {
    let mut first = true;
    let mut exclude_string = String::new();

    for n in allnodes {
        if !axis_nodes.contains(n) {
            if !first {
                exclude_string += ", ";
            }
            exclude_string += format!("'{n}'").as_str();
            first = false
        }
    }
    line.replace(template_text, &exclude_string.to_owned())
}

fn main() -> Result<(), io::Error> {
    // Get command-line options
    let opt = Opt::from_args();

    // Parse the options file
    let (axes, allnodes) = read_axes_file(&opt, &opt.axes_filename)?;

    // Header
    // Get current date
    let today: DateTime<Local> = Local::now();
    println!("// Generated on {}-{:02}-{:02} {:02}:{:02}", today.year(), today.month(), today.day(), today.hour(), today.minute());

    // Rewrite the template file
    if let Ok(lines) = read_lines(opt.template_filename) {
        for l in lines.into_iter().flatten() {
            let mut found = false;

            // Look for strings
            if l.find("<<PLATFORMS>>").unwrap_or(0) > 0 {
                let newline = replace_with_all_nodes(&l, "<<PLATFORMS>>", &allnodes);
                println!("{newline}");
                found = true;
            } else if l.find("<<FUNCTIONS>>").unwrap_or(0) > 0 {
                let newline = replace_with_all_functions(&l, "<<FUNCTIONS>>", &axes);
                println!("{newline}");
                found = true;
            }
            // Do the excludes
            else {
                for (f, axis_nodes) in &axes {
                    let lookfor = format!("{}{}{}", "<<EXCLUDES-", f, ">>");
                    if l.find(lookfor.as_str()).unwrap_or(0) > 0 {
                        let exline = replace_with_excludes(&l, &lookfor, axis_nodes, &allnodes);
                        println!("{exline}");
                        found = true;
                    }
                }
            }
            // Just a normal line
            if !found {
                println!("{l}");
            }
        }
    }

    Ok(())
}
