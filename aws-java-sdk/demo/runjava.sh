#!/bin/bash
#mvn compile exec:java -Dexec.mainClass="com.example.BoxToS3"
mvn clean package
mvn exec:java -Dexec.mainClass="com.example.BoxToS3"
