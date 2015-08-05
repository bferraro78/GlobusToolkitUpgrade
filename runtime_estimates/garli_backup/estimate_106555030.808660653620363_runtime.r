suppressPackageStartupMessages(library(randomForest))
load("garli.rf")
garli_estimate_one <- read.table("106555030.808660653620363_estimate",header=T)
garli.pred <- predict(garli.rf, garli_estimate_one[1,])
cat('ESTIMATE: ',as.integer(garli.pred[1]),'\n',sep="")
