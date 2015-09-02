#!/bin/bash
GSBL_CONFIG_DIR=/opt/gsbl-config
export GSBL_CONFIG_DIR
cd GT4.2/garli/trunk
rm -rf build/
source setClassPathArginine.sh
./globus-build-service.sh GARLI
