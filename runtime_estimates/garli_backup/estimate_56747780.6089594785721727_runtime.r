suppressPackageStartupMessages(library(randomForest))
load("garli.rf")
garli_estimate_one <- read.table("56747780.6089594785721727_estimate",header=T)
garli.pred <- predict(garli.rf, garli_estimate_one[1,])
cat('ESTIMATE: ',as.integer(garli.pred[1]),'\n',sep="")
