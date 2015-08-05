library(randomForest)
rf_matrix <- read.table("matrix",header=T)

# Mike said the formula version was less memory efficient; however, we're still using it so that prediction works with existing code for the time being
# reducing the number of trees in the forest from 10000 to 5000 to avoid using too much memory
garli.rf <- randomForest(runtime ~ .,data=rf_matrix,ntree=5000)

# an alternative way to build a randomForest; however, this does not currently work with the prediction code
#garli.rf <- randomForest(x=rf_matrix[ , names(rf_matrix) != "runtime"], y=rf_matrix[ , "runtime"], data=rf_matrix, ntree=5000)

save(garli.rf, file="garli.rf_new")
