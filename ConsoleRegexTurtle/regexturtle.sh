#!/bin/bash
#Executes the command-line version of RegextTurtle; automatically sets the JAVA VM memory size based on the available system memory
MEMDATA=$(free -m | grep Mem:)
ARR=($MEMDATA)
MEMSYSTEM=${ARR[1]}
MAXMEM=$(( MEMSYSTEM-512 ))
XMSMEM=$(( MAXMEM/2 ))
echo "System memory:"$MEMSYSTEM "Mbytes"
echo "RegexTurtle is going to use this amount of the system memory:"$MAXMEM "Mbytes" 
java -Xmx${MAXMEM}M -Xms${XMSMEM}M -jar "ConsoleRegexTurtle.jar" $@ 