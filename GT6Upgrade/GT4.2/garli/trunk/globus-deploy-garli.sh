#!/bin/bash
cd $GLOBUS_LOCATION
cd -
globus-stop-container-detached
globus-undeploy-gar edu_umd_grid_bio_garli
globus-deploy-gar edu_umd_grid_bio_garli.gar
cd -
./start-stop start
cd -