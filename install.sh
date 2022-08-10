#!/usr/bin/env bash

# Script working if the directories match the directories from the docker container
# If you are not using Docker, make sure to run the file after followed the dockerfile steps from https://github.com/yami2200/polyglot-language-server

echo "Copying Tree sitter files into Jsitter ..."
cp -r ../tree-sitter/* ../jsitter/native/tree-sitter/
echo "Copied successfully !"

echo "Edit CMakeLists of Jsitter ..."
sed -i '22iadd_library(tsjavascript SHARED grammars/tree-sitter-javascript/src/parser.c  grammars/tree-sitter-javascript/src/scanner.c)\nadd_library(tspython SHARED grammars/tree-sitter-python/src/parser.c grammars/tree-sitter-python/src/scanner.cc)' ../jsitter/native/CMakeLists.txt
echo "Edited successfully !"

