#!/bin/sh
#********************************************
# GENERIC SCRIPT FOR RUNNING JAVA CODE 
#********************************************
#
SCRIPT_HOME=`dirname $0`
JAR_NAME=$SCRIPT_HOME/../lib/j4ml-clas12-0.9-SNAPSHOT-jar-with-dependencies.jar
CLASS_NAME=j4ml.clas12.process.TrackDataExtractor
JAVA_OPTS=-Xms2048m
java -cp $JAR_NAME $CLASS_NAME $*


