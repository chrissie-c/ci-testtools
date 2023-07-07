#!/bin/bash
#
# Show all of the labels and the nodes that have them
# (Basically, invert the node/label DB)
# Needs to be run on the Jenkins host


HOME=~jenkins
declare -A FUNCTION

nodelist=$(echo $(ls -1 $HOME/nodes) | tr '\n' ' ')

for n in $nodelist
do
    labels=$(cat $HOME/nodes/$n/config.xml | grep label | sed -e 's/<label>//g' -e 's/<\/label>//g')
    for i in $labels
    do
        FUNCTION["$i"]+="$n "
    done
done

for i in "${!FUNCTION[@]}"
do
    echo "$i = ${FUNCTION[$i]}"
    echo
done
