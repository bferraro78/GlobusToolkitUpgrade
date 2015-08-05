suppressPackageStartupMessages(library(randomForest))
load("garli_recent.rf")
garli_estimate_one <- read.table("6225512540.2017662805852528_estimate",header=T)
garli.pred <- predict(garli.rf, garli_estimate_one[1,])
cat('ESTIMATE: ',as.integer(garli.pred[1]),'\n',sep="")
