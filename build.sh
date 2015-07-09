#!/bin/bash
GLOBUS_LOCATION=/export/work/globus-4.2.0/
export GLOBUS_LOCATION
cd GT4.2/garli/trunk
rm -rf build/
./globus-build-service.sh GARLI
