fcvalues <- read.table("convergence_folddifference.txt")
fcvalues <- fcvalues[[1]]
pdf(file="convergence_folddifference.pdf")
plot(c(1:length(fcvalues)), fcvalues, type="o",xlab="days", ylab="average fold-difference", main="average fold-difference between estimated and actual runtime", sub="(accounting started 2012-07-20)")
dev.off()
rdvalues <- read.table("convergence_reldiff.txt")
rdvalues <- rdvalues[[1]]
pdf(file="convergence_reldiff.pdf")
plot(c(1:length(rdvalues)), rdvalues, type="o",xlab="days", ylab="average relative difference", main="average relative difference between estimated and actual runtime", sub="(accounting started 2012-07-20)")
dev.off()
