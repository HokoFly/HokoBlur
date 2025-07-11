#!/bin/bash

./gradlew clean
./gradlew hoko-blur:assemble
./gradlew hoko-blur:publishToMavenCentral
