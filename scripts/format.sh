#!/bin/bash

npm install -g @htmlacademy/editorconfig-cli
IFS=$'\n' read -rd '' -a FILES <<< "$(git --no-pager diff --name-only HEAD "$(git merge-base HEAD master)")"
ERROR=""
for i in "${FILES[@]}"
do
    if [[ "$i" == *".java"* || "$i" == *".json"* || "$i" == *".mcmeta"* ]]
    then
        OUTPUT=$(editorconfig-cli -e ../.editorconfig "../$i" 2>&1)
        if [[ $? != 0 ]]
        then
            ERROR="error"
            IFS=$'\n' read -rd '' -a LINES <<< "$OUTPUT"
            for f in "${LINES[@]}"
            do
                echo "$f" | awk '/^(Line).*|^(File).*/'
            done
        fi
    fi
done

if [[ "$ERROR" != "" ]]
then
    echo "Found files with improper formatting"
    exit 1
else
    echo "No formatting issues"
fi
