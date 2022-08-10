#!/usr/bin/env bash

# Script working if the directories match the directories from the docker container
# If you are not using Docker, make sure to run the file after followed the dockerfile steps from https://github.com/yami2200/polyglot-language-server

echo "Copying Tree sitter files into Jsitter ..."
cp -r ../tree-sitter/* ../jsitter/native/tree-sitter/
echo "Copied successfully !"

echo "Edit CMakeLists of Jsitter ..."
sed -i '20iadd_library(tsjavascript SHARED grammars/tree-sitter-javascript/src/parser.c  grammars/tree-sitter-javascript/src/scanner.c)\nadd_library(tspython SHARED grammars/tree-sitter-python/src/parser.c grammars/tree-sitter-python/src/scanner.cc)\ntarget_link_libraries(jsitter tsgo tsjava tsjavascript tspython)\n#[[' ../jsitter/native/CMakeLists.txt
echo ']]' >> ../jsitter/native/CMakeLists.txt
echo "Edited successfully !"

echo "Copying Tree sitter Python, javascript, java, go into jsitter/native/grammars ..."
cp -r ../tree-sitter-python ../jsitter/native/grammars/
cp -r ../tree-sitter-javascript ../jsitter/native/grammars/
cp -r ../tree-sitter-go ../jsitter/native/grammars/
cp -r ../tree-sitter-java ../jsitter/native/grammars/
echo "Copied successfully !"

../jsitter/make.sh
cd ../jsitter/
mvn install

cd ..

mkdir ./PolyglotAST/src/main/java/com/resources/
mkdir ./PolyglotAST/src/main/java/com/resources/linux-x86-64/
cp ./jsitter/native/build/linux-x86-64/*.so ./PolyglotAST/src/main/java/com/resources/
cp ./jsitter/native/build/linux-x86-64/*.so ./PolyglotAST/src/main/java/com/resources/linux-x86-64/