#!/bin/bash

allnodes="" # Get this from Jenkins
# this line for testing only
allnodes="'debian13-x86-64', 'debian14-sparc', 'fedora37-x86-64', 'freebsd13-x86-64', 'rhel7-arm7l', 'rhel7-x86-64', 'rhel8-x86-64'"

# read the file and build the INCLUDES list
declare -a line
declare -A FUNCTION
declare -A EXCLUDES
ALLFUNCS=""
while read -a line
do
    if [ "${line[0]:0:1}" != "#" ] # Ignore comments
    then
	label=${line[0]}
	if [ -n "$ALLFUNCS" ]
	then
	    ALLFUNCS+=", "
	fi
	ALLFUNCS+="'$label'"
	FUNCTION[$label]=""
	for i in ${!line[@]}
	do
	    if [ $i != 0 ]
	    then
		FUNCTION[$label]+="${line[$i]} "
	    fi
	done
	echo "$label = ${FUNCTION[$label]}"
   fi
done < $1

# Build the excludes list *sigh*
for i in ${!FUNCTION[@]}
do
    EXCLUDES[$i]=""
    for n in $allnodes
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

sed < $2 -e "s/<<PLATFORMS>>/$allnodes/" \
    -e"s/<<FUNCTIONS>>/$ALLFUNCS/" \
    -e"s/<<EXCLUDES-voting>>/${EXCLUDES['voting']}/" \
    -e"s/<<EXCLUDES-staticcheck>>/${EXCLUDES['staticcheck']}/" \
    -e"s/<<EXCLUDES-nonvoting>>/${EXCLUDES['nonvoting']}/"
