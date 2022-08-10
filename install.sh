#!/usr/bin/env bash

# Script working if the directories match the directories from the docker container
# If you are not using Docker, make sure to run the file after followed the dockerfile steps from https://github.com/yami2200/polyglot-language-server

echo "Copying Tree sitter files into Jsitter ..."
cp ../tree-sitter/* ../jsitter/native/tree-sitter/
echo "Copied successfully !"

