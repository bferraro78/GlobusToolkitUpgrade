#!/bin/bash
GSBL_CONFIG_DIR=/opt/gsbl-config
export GSBL_CONFIG_DIR
cd GSBL/
rm -rf build/
gradle build
cd ../
rm -rf build/
gradle build
