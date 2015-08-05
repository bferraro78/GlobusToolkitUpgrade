library(randomForest)
rf_matrix <- read.table("garli_matrix_new_input",header=T)
garli.rf <- randomForest(runtime ~ .,data=rf_matrix,ntree=10000)
save(garli.rf, file="garli.rf_new")
