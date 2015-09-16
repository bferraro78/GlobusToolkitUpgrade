#!/bin/bash
GSBL_CONFIG_DIR=/opt/gsbl-config
export GSBL_CONFIG_DIR
cd GSBL/
gradle build
cd ../
gradle build
