#!/bin/bash
#Script to flag all the files in a directory (given as first argument to the script). It creates new file with the example flagged as true/false if they match/doesn't match at least one regex in the PATTERNS file.
#The files flagged are only the ones with doesn't end in ".flag".

JAR="../dist/Random_Regex_Finch.jar"
JAR_SUPER_CSV="../lib/super-csv/super-csv-2.1.0.jar"
JAR_LANG_COMMONS="../lib/commons-lang3-3.1/commons-lang3-3.1.jar"
#use StringMaker to generate patterns.dat
PATTERNS="patterns.dat"

if [[ $# != 1 ]]; then
	echo "Set the directory as argument: $0 <dir>";
	echo "Rememeber to check JAR files and other options inside the script.";
	exit;
fi
parallelInstalled=1;
type parallel >/dev/null 2>&1 || { parallelInstalled=0; echo >&2 "Install parallel to improve performance."; } 


if [[ $parallelInstalled -eq 0 ]]; then
	for file in $1/*;
		do
			if [[ -f $file && ! $file =~ .*\.flag$ ]] ; then
				echo $file	
				java -classpath "$JAR":"$JAR_SUPER_CSV":"$JAR_LANG_COMMONS" it.units.inginf.rrf.pcreutils.FlagRE "$PATTERNS" $file 
			fi
		done
	else
		find $1/ -type f -not -name "*.flag" | parallel --verbose java -classpath "$JAR":"$JAR_SUPER_CSV":"$JAR_LANG_COMMONS" it.units.inginf.rrf.pcreutils.FlagRE "$PATTERNS" {} 
fi


