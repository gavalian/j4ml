#!/bin/sh
#************************************
# CLEANUP AND DIRECTORY CREATION
rm -rf j4ml-1.0 j4ml-1.0.tar.gz
mkdir j4ml-1.0
#************************************
cp -r bin j4ml-1.0/.
cp -r etc j4ml-1.0/.
mkdir -p j4ml-1.0/lib
cp ../j4ml-deepnetts/target/*with-dependencies.jar j4ml-1.0/lib/.
cp ../j4ml-clas12/target/*with-dependencies.jar j4ml-1.0/lib/.
tar -cf j4ml-1.0.tar j4ml-1.0
gzip j4ml-1.0.tar
