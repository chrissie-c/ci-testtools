#!/bin/bash
#
# Takes a template Jenkinsfile (in $1) and adds
# the node axes for voting,nonvoting and staticcheck
#
HOME=~jenkins

getnodelist() {
	nodelist=$(echo $(ls -1 $HOME/nodes) | tr '\n' ' ')
}

add_to_functions() {
	labels=$(cat $HOME/nodes/$1/config.xml | grep label | sed -e 's/<label>//g' -e 's/<\/label>//g')
	added="0"
	anvil="0"

	# Check for anvil nodes first - so we can exclude them
	for i in $labels
	do
	    case $i in
		anvil*)
		    anvil="1"
		    ;;
	    esac
	done

	# Now do the main functions
	if [ "$anvil" = "0" ]
	then
	    for i in $labels
	    do
		case $i in
		    voting)
			FUNCTION["voting"]+=$1
			added=1
			;;
		    nonvoting)
			FUNCTION["nonvoting"]+=$1
			added=1
			;;
		    coverity)
			FUNCTION["coverity"]+=$1
			added=1
			;;
		esac
	    done

	    # Add all used nodes to the matrix nodelist
	    if [ "$added" = "1" ]
	    then
		if [ -n "${ALLNODES}" ]
		then
		    ALLNODES+=", "
		fi
		ALLNODES+="'$1'"
	    fi
	fi
}

if [ -z "$1" ]
then
    echo "usage $0 [template-jenkinsfile]"
    exit
fi

if [ ! -f "$1" ]
then
    echo "template '$1' not found" 
    exit
fi



getnodelist
declare -A EXCLUDES
declare -A FUNCTION

# Build the function/node list
for i in $nodelist
do
    add_to_functions $i
done

# Build the excludes list for the Jenkinsfile
for i in voting nonvoting staticcheck
do
    if [ -n "${ALLFUNCS}" ]
    then
	ALLFUNCS+=","
    fi
    ALLFUNCS+="'"${i}"'"

    EXCLUDES[$i]=""
    for n in $ALLNODES
    do
	node=`echo $n|tr -d ",' "`
	echo "${FUNCTION[$i]}" | grep "$node" > /dev/null 2>/dev/null
	if [ $? != 0 ]
	then
	    if [ -n "${EXCLUDES[$i]}" ]
	    then
		EXCLUDES[$i]+=", "
	    fi
	    EXCLUDES[$i]+="'$node'"
	fi
    done
done

# Build the new Jenkinsfile
sed < $1 -e "s/<<PLATFORMS>>/$ALLNODES/" \
    -e"s/<<FUNCTIONS>>/$ALLFUNCS/" \
    -e"s/<<EXCLUDES-voting>>/${EXCLUDES['voting']}/" \
    -e"s/<<EXCLUDES-staticcheck>>/${EXCLUDES['staticcheck']}/" \
    -e"s/<<EXCLUDES-nonvoting>>/${EXCLUDES['nonvoting']}/"
