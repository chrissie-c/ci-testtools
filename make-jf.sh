#!/bin/bash
#
# Takes a template Jenkinsfile (in $1) and adds
# the node axes for voting,nonvoting and staticcheck
#


getnodelist() {
	nodelist=$(echo $(ls -1 $HOME/nodes) | tr '\n' ' ')
}

add_to_functions() {
	labels=$(cat $HOME/nodes/$1/config.xml | grep label | sed -e 's/<label>//g' -e 's/<\/label>//g')

	for i in $labels
	do
	    case $i in
		voting)
		    FUNCTION["voting"]+=$1
		    ;;
		non-voting)
		    FUNCTION["nonvoting"]+=$1
		    ;;
		coverity)
		    FUNCTION["coverity"]+=$1
		    ;;
	    esac
	done
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
ALLFUNCS="voting,nonvoting,staticcheck,cleanup"

for i in $nodelist
do
    add_to_functions $i
done

# Build the excludes list for the Jenkinsfile
for i in "voting" "nonvoting" "staticcheck"
do
    EXCLUDES[$i]=""
    for n in $nodelist
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

sed < $1 -e "s/<<PLATFORMS>>/$nodelist/" \
    -e"s/<<FUNCTIONS>>/$ALLFUNCS/" \
    -e"s/<<EXCLUDES-voting>>/${EXCLUDES['voting']}/" \
    -e"s/<<EXCLUDES-staticcheck>>/${EXCLUDES['staticcheck']}/" \
    -e"s/<<EXCLUDES-nonvoting>>/${EXCLUDES['nonvoting']}/"
