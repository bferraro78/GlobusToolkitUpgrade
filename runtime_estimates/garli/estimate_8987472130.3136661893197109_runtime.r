suppressPackageStartupMessages(library(randomForest))
load("garli_recent.rf")
garli_estimate_one <- read.table("8987472130.3136661893197109_estimate",header=T)
garli.pred <- predict(garli.rf, garli_estimate_one[1,])
cat('ESTIMATE: ',as.integer(garli.pred[1]),'\n',sep="")
