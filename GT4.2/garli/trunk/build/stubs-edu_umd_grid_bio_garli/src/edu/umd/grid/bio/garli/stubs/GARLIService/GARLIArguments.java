/**
 * GARLIArguments.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Mar 01, 2007 (10:42:15 CST) WSDL2Java emitter.
 */

package edu.umd.grid.bio.garli.stubs.GARLIService;

public class GARLIArguments implements java.io.Serializable {
	private java.lang.Integer replicates;

	private java.lang.String ratematrix;

	private java.lang.Boolean outputphyliptree;

	private java.lang.Double randnniweight;

	private java.lang.String ofprefix;

	private java.lang.Double limsprweight;

	private java.lang.Integer numratecats;

	private java.lang.Double uniqueswapbias;

	private java.lang.Integer treerejectionthreshold;

	private java.lang.String numtaxa;

	private java.lang.String modelsdata;

	private java.lang.Double startoptprec;

	private java.lang.Boolean optimizeinputonly;

	private java.lang.Boolean refinestart;

	private java.lang.Boolean subsetspecificrates;

	private java.lang.Double topoweight;

	private java.lang.String constraintfile;

	private java.lang.Integer replicates2;

	private java.lang.Integer nindivs;

	private java.lang.String streefname_userdata;

	private java.lang.Integer profilingjob;

	private java.lang.String analysistype;

	private java.lang.Double distanceswapbias;

	private java.lang.Double brlenweight;

	private java.lang.Double minoptprec;

	private java.lang.Double modweight;

	private java.lang.String outgroup;

	private java.lang.Integer numberofprecreductions;

	private java.lang.Double selectionintensity;

	private java.lang.Boolean outputsitelikelihoods;

	private java.lang.Integer limsprrange;

	private java.lang.Boolean collapsebranches;

	private java.lang.String geneticcode;

	private java.lang.String configFile;

	private java.lang.Integer gammashapebrlen;

	private java.lang.Integer searchreps;

	private java.lang.Double randsprweight;

	private java.lang.Boolean novalidate;

	private java.lang.String partitionsdata;

	private java.lang.Boolean inferinternalstateprobs;

	private java.lang.String streefname;

	private java.lang.Boolean linkmodels;

	private java.lang.String uniquepatterns;

	private java.lang.String invariantsites;

	private java.lang.String datafname;

	private java.lang.String actualmemory;

	private java.lang.String ratehetmodel;

	private java.lang.Double resampleproportion;

	private java.lang.String statefrequencies;

	private java.lang.Integer meanbrlenmuts;

	private java.lang.String jobname;

	private java.lang.Integer gammashapemodel;

	private java.lang.String datatype;

	private java.lang.Integer attachmentspertaxon;

	private java.lang.String[] sharedFiles;

	private java.lang.String[] perJobArguments;

	private java.lang.String[] perJobFiles;

	private java.lang.String symlinks;

	private java.lang.String[] inputFiles;

	private java.lang.String[] outputFiles;

	private java.lang.String owner;

	private java.lang.String schedulerOverride;

	private java.lang.String appName;

	private java.lang.String jobName;

	private java.land.String jobID;  // Added for GT6.

	private java.lang.String workingDir;

	public GARLIArguments() {
	}

<<<<<<< HEAD
<<<<<<< HEAD
    public GARLIArguments(
           java.lang.Integer replicates,
           java.lang.String ratematrix,
           java.lang.Boolean outputphyliptree,
           java.lang.Double randnniweight,
           java.lang.String ofprefix,
           java.lang.Double limsprweight,
           java.lang.Integer numratecats,
           java.lang.Double uniqueswapbias,
           java.lang.Integer treerejectionthreshold,
           java.lang.String numtaxa,
           java.lang.String modelsdata,
           java.lang.Double startoptprec,
           java.lang.Boolean optimizeinputonly,
           java.lang.Boolean refinestart,
           java.lang.Boolean subsetspecificrates,
           java.lang.Double topoweight,
           java.lang.String constraintfile,
           java.lang.Integer replicates2,
           java.lang.Integer nindivs,
           java.lang.String streefname_userdata,
           java.lang.Integer profilingjob,
           java.lang.String analysistype,
           java.lang.Double distanceswapbias,
           java.lang.Double brlenweight,
           java.lang.Double minoptprec,
           java.lang.Double modweight,
           java.lang.String outgroup,
           java.lang.Integer numberofprecreductions,
           java.lang.Double selectionintensity,
           java.lang.Boolean outputsitelikelihoods,
           java.lang.Integer limsprrange,
           java.lang.Boolean collapsebranches,
           java.lang.String geneticcode,
           java.lang.String configFile,
           java.lang.Integer gammashapebrlen,
           java.lang.Integer searchreps,
           java.lang.Double randsprweight,
           java.lang.Boolean novalidate,
           java.lang.String partitionsdata,
           java.lang.Boolean inferinternalstateprobs,
           java.lang.String streefname,
           java.lang.Boolean linkmodels,
           java.lang.String uniquepatterns,
           java.lang.String invariantsites,
           java.lang.String datafname,
           java.lang.String actualmemory,
           java.lang.String ratehetmodel,
           java.lang.Double resampleproportion,
           java.lang.String statefrequencies,
           java.lang.Integer meanbrlenmuts,
           java.lang.String jobname,
           java.lang.Integer gammashapemodel,
           java.lang.String datatype,
           java.lang.Integer attachmentspertaxon,
           java.lang.String[] sharedFiles,
           java.lang.String[] perJobArguments,
           java.lang.String[] perJobFiles,
           java.lang.String symlinks,
           java.lang.String[] inputFiles,
           java.lang.String[] outputFiles,
           java.lang.String owner,
           java.lang.String schedulerOverride,
           java.lang.String appName,
           java.lang.String jobName,
           java.land.String jobID,
           java.lang.String workingDir) {
           this.replicates = replicates;
           this.ratematrix = ratematrix;
           this.outputphyliptree = outputphyliptree;
           this.randnniweight = randnniweight;
           this.ofprefix = ofprefix;
           this.limsprweight = limsprweight;
           this.numratecats = numratecats;
           this.uniqueswapbias = uniqueswapbias;
           this.treerejectionthreshold = treerejectionthreshold;
           this.numtaxa = numtaxa;
           this.modelsdata = modelsdata;
           this.startoptprec = startoptprec;
           this.optimizeinputonly = optimizeinputonly;
           this.refinestart = refinestart;
           this.subsetspecificrates = subsetspecificrates;
           this.topoweight = topoweight;
           this.constraintfile = constraintfile;
           this.replicates2 = replicates2;
           this.nindivs = nindivs;
           this.streefname_userdata = streefname_userdata;
           this.profilingjob = profilingjob;
           this.analysistype = analysistype;
           this.distanceswapbias = distanceswapbias;
           this.brlenweight = brlenweight;
           this.minoptprec = minoptprec;
           this.modweight = modweight;
           this.outgroup = outgroup;
           this.numberofprecreductions = numberofprecreductions;
           this.selectionintensity = selectionintensity;
           this.outputsitelikelihoods = outputsitelikelihoods;
           this.limsprrange = limsprrange;
           this.collapsebranches = collapsebranches;
           this.geneticcode = geneticcode;
           this.configFile = configFile;
           this.gammashapebrlen = gammashapebrlen;
           this.searchreps = searchreps;
           this.randsprweight = randsprweight;
           this.novalidate = novalidate;
           this.partitionsdata = partitionsdata;
           this.inferinternalstateprobs = inferinternalstateprobs;
           this.streefname = streefname;
           this.linkmodels = linkmodels;
           this.uniquepatterns = uniquepatterns;
           this.invariantsites = invariantsites;
           this.datafname = datafname;
           this.actualmemory = actualmemory;
           this.ratehetmodel = ratehetmodel;
           this.resampleproportion = resampleproportion;
           this.statefrequencies = statefrequencies;
           this.meanbrlenmuts = meanbrlenmuts;
           this.jobname = jobname;
           this.gammashapemodel = gammashapemodel;
           this.datatype = datatype;
           this.attachmentspertaxon = attachmentspertaxon;
           this.sharedFiles = sharedFiles;
           this.perJobArguments = perJobArguments;
           this.perJobFiles = perJobFiles;
           this.symlinks = symlinks;
           this.inputFiles = inputFiles;
           this.outputFiles = outputFiles;
           this.owner = owner;
           this.schedulerOverride = schedulerOverride;
           this.appName = appName;
           this.jobName = jobName;
           this.jobID = jobID; // JobID added
           this.workingDir = workingDir;
    }


    /**
     * Gets the replicates value for this GARLIArguments.
     * 
     * @return replicates
     */
    public java.lang.Integer getReplicates() {
        return replicates;
    }


    /**
     * Sets the replicates value for this GARLIArguments.
     * 
     * @param replicates
     */
    public void setReplicates(java.lang.Integer replicates) {
        this.replicates = replicates;
    }


    /**
     * Gets the ratematrix value for this GARLIArguments.
     * 
     * @return ratematrix
     */
    public java.lang.String getRatematrix() {
        return ratematrix;
    }


    /**
     * Sets the ratematrix value for this GARLIArguments.
     * 
     * @param ratematrix
     */
    public void setRatematrix(java.lang.String ratematrix) {
        this.ratematrix = ratematrix;
    }


    /**
     * Gets the outputphyliptree value for this GARLIArguments.
     * 
     * @return outputphyliptree
     */
    public java.lang.Boolean getOutputphyliptree() {
        return outputphyliptree;
    }


    /**
     * Sets the outputphyliptree value for this GARLIArguments.
     * 
     * @param outputphyliptree
     */
    public void setOutputphyliptree(java.lang.Boolean outputphyliptree) {
        this.outputphyliptree = outputphyliptree;
    }


    /**
     * Gets the randnniweight value for this GARLIArguments.
     * 
     * @return randnniweight
     */
    public java.lang.Double getRandnniweight() {
        return randnniweight;
    }


    /**
     * Sets the randnniweight value for this GARLIArguments.
     * 
     * @param randnniweight
     */
    public void setRandnniweight(java.lang.Double randnniweight) {
        this.randnniweight = randnniweight;
    }


    /**
     * Gets the ofprefix value for this GARLIArguments.
     * 
     * @return ofprefix
     */
    public java.lang.String getOfprefix() {
        return ofprefix;
    }


    /**
     * Sets the ofprefix value for this GARLIArguments.
     * 
     * @param ofprefix
     */
    public void setOfprefix(java.lang.String ofprefix) {
        this.ofprefix = ofprefix;
    }


    /**
     * Gets the limsprweight value for this GARLIArguments.
     * 
     * @return limsprweight
     */
    public java.lang.Double getLimsprweight() {
        return limsprweight;
    }


    /**
     * Sets the limsprweight value for this GARLIArguments.
     * 
     * @param limsprweight
     */
    public void setLimsprweight(java.lang.Double limsprweight) {
        this.limsprweight = limsprweight;
    }


    /**
     * Gets the numratecats value for this GARLIArguments.
     * 
     * @return numratecats
     */
    public java.lang.Integer getNumratecats() {
        return numratecats;
    }


    /**
     * Sets the numratecats value for this GARLIArguments.
     * 
     * @param numratecats
     */
    public void setNumratecats(java.lang.Integer numratecats) {
        this.numratecats = numratecats;
    }


    /**
     * Gets the uniqueswapbias value for this GARLIArguments.
     * 
     * @return uniqueswapbias
     */
    public java.lang.Double getUniqueswapbias() {
        return uniqueswapbias;
    }


    /**
     * Sets the uniqueswapbias value for this GARLIArguments.
     * 
     * @param uniqueswapbias
     */
    public void setUniqueswapbias(java.lang.Double uniqueswapbias) {
        this.uniqueswapbias = uniqueswapbias;
    }


    /**
     * Gets the treerejectionthreshold value for this GARLIArguments.
     * 
     * @return treerejectionthreshold
     */
    public java.lang.Integer getTreerejectionthreshold() {
        return treerejectionthreshold;
    }


    /**
     * Sets the treerejectionthreshold value for this GARLIArguments.
     * 
     * @param treerejectionthreshold
     */
    public void setTreerejectionthreshold(java.lang.Integer treerejectionthreshold) {
        this.treerejectionthreshold = treerejectionthreshold;
    }


    /**
     * Gets the numtaxa value for this GARLIArguments.
     * 
     * @return numtaxa
     */
    public java.lang.String getNumtaxa() {
        return numtaxa;
    }


    /**
     * Sets the numtaxa value for this GARLIArguments.
     * 
     * @param numtaxa
     */
    public void setNumtaxa(java.lang.String numtaxa) {
        this.numtaxa = numtaxa;
    }


    /**
     * Gets the modelsdata value for this GARLIArguments.
     * 
     * @return modelsdata
     */
    public java.lang.String getModelsdata() {
        return modelsdata;
    }


    /**
     * Sets the modelsdata value for this GARLIArguments.
     * 
     * @param modelsdata
     */
    public void setModelsdata(java.lang.String modelsdata) {
        this.modelsdata = modelsdata;
    }


    /**
     * Gets the startoptprec value for this GARLIArguments.
     * 
     * @return startoptprec
     */
    public java.lang.Double getStartoptprec() {
        return startoptprec;
    }


    /**
     * Sets the startoptprec value for this GARLIArguments.
     * 
     * @param startoptprec
     */
    public void setStartoptprec(java.lang.Double startoptprec) {
        this.startoptprec = startoptprec;
    }


    /**
     * Gets the optimizeinputonly value for this GARLIArguments.
     * 
     * @return optimizeinputonly
     */
    public java.lang.Boolean getOptimizeinputonly() {
        return optimizeinputonly;
    }


    /**
     * Sets the optimizeinputonly value for this GARLIArguments.
     * 
     * @param optimizeinputonly
     */
    public void setOptimizeinputonly(java.lang.Boolean optimizeinputonly) {
        this.optimizeinputonly = optimizeinputonly;
    }


    /**
     * Gets the refinestart value for this GARLIArguments.
     * 
     * @return refinestart
     */
    public java.lang.Boolean getRefinestart() {
        return refinestart;
    }


    /**
     * Sets the refinestart value for this GARLIArguments.
     * 
     * @param refinestart
     */
    public void setRefinestart(java.lang.Boolean refinestart) {
        this.refinestart = refinestart;
    }


    /**
     * Gets the subsetspecificrates value for this GARLIArguments.
     * 
     * @return subsetspecificrates
     */
    public java.lang.Boolean getSubsetspecificrates() {
        return subsetspecificrates;
    }


    /**
     * Sets the subsetspecificrates value for this GARLIArguments.
     * 
     * @param subsetspecificrates
     */
    public void setSubsetspecificrates(java.lang.Boolean subsetspecificrates) {
        this.subsetspecificrates = subsetspecificrates;
    }


    /**
     * Gets the topoweight value for this GARLIArguments.
     * 
     * @return topoweight
     */
    public java.lang.Double getTopoweight() {
        return topoweight;
    }


    /**
     * Sets the topoweight value for this GARLIArguments.
     * 
     * @param topoweight
     */
    public void setTopoweight(java.lang.Double topoweight) {
        this.topoweight = topoweight;
    }


    /**
     * Gets the constraintfile value for this GARLIArguments.
     * 
     * @return constraintfile
     */
    public java.lang.String getConstraintfile() {
        return constraintfile;
    }


    /**
     * Sets the constraintfile value for this GARLIArguments.
     * 
     * @param constraintfile
     */
    public void setConstraintfile(java.lang.String constraintfile) {
        this.constraintfile = constraintfile;
    }


    /**
     * Gets the replicates2 value for this GARLIArguments.
     * 
     * @return replicates2
     */
    public java.lang.Integer getReplicates2() {
        return replicates2;
    }


    /**
     * Sets the replicates2 value for this GARLIArguments.
     * 
     * @param replicates2
     */
    public void setReplicates2(java.lang.Integer replicates2) {
        this.replicates2 = replicates2;
    }


    /**
     * Gets the nindivs value for this GARLIArguments.
     * 
     * @return nindivs
     */
    public java.lang.Integer getNindivs() {
        return nindivs;
    }


    /**
     * Sets the nindivs value for this GARLIArguments.
     * 
     * @param nindivs
     */
    public void setNindivs(java.lang.Integer nindivs) {
        this.nindivs = nindivs;
    }


    /**
     * Gets the streefname_userdata value for this GARLIArguments.
     * 
     * @return streefname_userdata
     */
    public java.lang.String getStreefname_userdata() {
        return streefname_userdata;
    }


    /**
     * Sets the streefname_userdata value for this GARLIArguments.
     * 
     * @param streefname_userdata
     */
    public void setStreefname_userdata(java.lang.String streefname_userdata) {
        this.streefname_userdata = streefname_userdata;
    }


    /**
     * Gets the profilingjob value for this GARLIArguments.
     * 
     * @return profilingjob
     */
    public java.lang.Integer getProfilingjob() {
        return profilingjob;
    }


    /**
     * Sets the profilingjob value for this GARLIArguments.
     * 
     * @param profilingjob
     */
    public void setProfilingjob(java.lang.Integer profilingjob) {
        this.profilingjob = profilingjob;
    }


    /**
     * Gets the analysistype value for this GARLIArguments.
     * 
     * @return analysistype
     */
    public java.lang.String getAnalysistype() {
        return analysistype;
    }


    /**
     * Sets the analysistype value for this GARLIArguments.
     * 
     * @param analysistype
     */
    public void setAnalysistype(java.lang.String analysistype) {
        this.analysistype = analysistype;
    }


    /**
     * Gets the distanceswapbias value for this GARLIArguments.
     * 
     * @return distanceswapbias
     */
    public java.lang.Double getDistanceswapbias() {
        return distanceswapbias;
    }


    /**
     * Sets the distanceswapbias value for this GARLIArguments.
     * 
     * @param distanceswapbias
     */
    public void setDistanceswapbias(java.lang.Double distanceswapbias) {
        this.distanceswapbias = distanceswapbias;
    }


    /**
     * Gets the brlenweight value for this GARLIArguments.
     * 
     * @return brlenweight
     */
    public java.lang.Double getBrlenweight() {
        return brlenweight;
    }


    /**
     * Sets the brlenweight value for this GARLIArguments.
     * 
     * @param brlenweight
     */
    public void setBrlenweight(java.lang.Double brlenweight) {
        this.brlenweight = brlenweight;
    }


    /**
     * Gets the minoptprec value for this GARLIArguments.
     * 
     * @return minoptprec
     */
    public java.lang.Double getMinoptprec() {
        return minoptprec;
    }


    /**
     * Sets the minoptprec value for this GARLIArguments.
     * 
     * @param minoptprec
     */
    public void setMinoptprec(java.lang.Double minoptprec) {
        this.minoptprec = minoptprec;
    }


    /**
     * Gets the modweight value for this GARLIArguments.
     * 
     * @return modweight
     */
    public java.lang.Double getModweight() {
        return modweight;
    }


    /**
     * Sets the modweight value for this GARLIArguments.
     * 
     * @param modweight
     */
    public void setModweight(java.lang.Double modweight) {
        this.modweight = modweight;
    }


    /**
     * Gets the outgroup value for this GARLIArguments.
     * 
     * @return outgroup
     */
    public java.lang.String getOutgroup() {
        return outgroup;
    }


    /**
     * Sets the outgroup value for this GARLIArguments.
     * 
     * @param outgroup
     */
    public void setOutgroup(java.lang.String outgroup) {
        this.outgroup = outgroup;
    }


    /**
     * Gets the numberofprecreductions value for this GARLIArguments.
     * 
     * @return numberofprecreductions
     */
    public java.lang.Integer getNumberofprecreductions() {
        return numberofprecreductions;
    }


    /**
     * Sets the numberofprecreductions value for this GARLIArguments.
     * 
     * @param numberofprecreductions
     */
    public void setNumberofprecreductions(java.lang.Integer numberofprecreductions) {
        this.numberofprecreductions = numberofprecreductions;
    }


    /**
     * Gets the selectionintensity value for this GARLIArguments.
     * 
     * @return selectionintensity
     */
    public java.lang.Double getSelectionintensity() {
        return selectionintensity;
    }


    /**
     * Sets the selectionintensity value for this GARLIArguments.
     * 
     * @param selectionintensity
     */
    public void setSelectionintensity(java.lang.Double selectionintensity) {
        this.selectionintensity = selectionintensity;
    }


    /**
     * Gets the outputsitelikelihoods value for this GARLIArguments.
     * 
     * @return outputsitelikelihoods
     */
    public java.lang.Boolean getOutputsitelikelihoods() {
        return outputsitelikelihoods;
    }


    /**
     * Sets the outputsitelikelihoods value for this GARLIArguments.
     * 
     * @param outputsitelikelihoods
     */
    public void setOutputsitelikelihoods(java.lang.Boolean outputsitelikelihoods) {
        this.outputsitelikelihoods = outputsitelikelihoods;
    }


    /**
     * Gets the limsprrange value for this GARLIArguments.
     * 
     * @return limsprrange
     */
    public java.lang.Integer getLimsprrange() {
        return limsprrange;
    }


    /**
     * Sets the limsprrange value for this GARLIArguments.
     * 
     * @param limsprrange
     */
    public void setLimsprrange(java.lang.Integer limsprrange) {
        this.limsprrange = limsprrange;
    }


    /**
     * Gets the collapsebranches value for this GARLIArguments.
     * 
     * @return collapsebranches
     */
    public java.lang.Boolean getCollapsebranches() {
        return collapsebranches;
    }


    /**
     * Sets the collapsebranches value for this GARLIArguments.
     * 
     * @param collapsebranches
     */
    public void setCollapsebranches(java.lang.Boolean collapsebranches) {
        this.collapsebranches = collapsebranches;
    }


    /**
     * Gets the geneticcode value for this GARLIArguments.
     * 
     * @return geneticcode
     */
    public java.lang.String getGeneticcode() {
        return geneticcode;
    }


    /**
     * Sets the geneticcode value for this GARLIArguments.
     * 
     * @param geneticcode
     */
    public void setGeneticcode(java.lang.String geneticcode) {
        this.geneticcode = geneticcode;
    }


    /**
     * Gets the configFile value for this GARLIArguments.
     * 
     * @return configFile
     */
    public java.lang.String getConfigFile() {
        return configFile;
    }


    /**
     * Sets the configFile value for this GARLIArguments.
     * 
     * @param configFile
     */
    public void setConfigFile(java.lang.String configFile) {
        this.configFile = configFile;
    }


    /**
     * Gets the gammashapebrlen value for this GARLIArguments.
     * 
     * @return gammashapebrlen
     */
    public java.lang.Integer getGammashapebrlen() {
        return gammashapebrlen;
    }


    /**
     * Sets the gammashapebrlen value for this GARLIArguments.
     * 
     * @param gammashapebrlen
     */
    public void setGammashapebrlen(java.lang.Integer gammashapebrlen) {
        this.gammashapebrlen = gammashapebrlen;
    }


    /**
     * Gets the searchreps value for this GARLIArguments.
     * 
     * @return searchreps
     */
    public java.lang.Integer getSearchreps() {
        return searchreps;
    }


    /**
     * Sets the searchreps value for this GARLIArguments.
     * 
     * @param searchreps
     */
    public void setSearchreps(java.lang.Integer searchreps) {
        this.searchreps = searchreps;
    }


    /**
     * Gets the randsprweight value for this GARLIArguments.
     * 
     * @return randsprweight
     */
    public java.lang.Double getRandsprweight() {
        return randsprweight;
    }


    /**
     * Sets the randsprweight value for this GARLIArguments.
     * 
     * @param randsprweight
     */
    public void setRandsprweight(java.lang.Double randsprweight) {
        this.randsprweight = randsprweight;
    }


    /**
     * Gets the novalidate value for this GARLIArguments.
     * 
     * @return novalidate
     */
    public java.lang.Boolean getNovalidate() {
        return novalidate;
    }


    /**
     * Sets the novalidate value for this GARLIArguments.
     * 
     * @param novalidate
     */
    public void setNovalidate(java.lang.Boolean novalidate) {
        this.novalidate = novalidate;
    }


    /**
     * Gets the partitionsdata value for this GARLIArguments.
     * 
     * @return partitionsdata
     */
    public java.lang.String getPartitionsdata() {
        return partitionsdata;
    }


    /**
     * Sets the partitionsdata value for this GARLIArguments.
     * 
     * @param partitionsdata
     */
    public void setPartitionsdata(java.lang.String partitionsdata) {
        this.partitionsdata = partitionsdata;
    }


    /**
     * Gets the inferinternalstateprobs value for this GARLIArguments.
     * 
     * @return inferinternalstateprobs
     */
    public java.lang.Boolean getInferinternalstateprobs() {
        return inferinternalstateprobs;
    }


    /**
     * Sets the inferinternalstateprobs value for this GARLIArguments.
     * 
     * @param inferinternalstateprobs
     */
    public void setInferinternalstateprobs(java.lang.Boolean inferinternalstateprobs) {
        this.inferinternalstateprobs = inferinternalstateprobs;
    }


    /**
     * Gets the streefname value for this GARLIArguments.
     * 
     * @return streefname
     */
    public java.lang.String getStreefname() {
        return streefname;
    }


    /**
     * Sets the streefname value for this GARLIArguments.
     * 
     * @param streefname
     */
    public void setStreefname(java.lang.String streefname) {
        this.streefname = streefname;
    }


    /**
     * Gets the linkmodels value for this GARLIArguments.
     * 
     * @return linkmodels
     */
    public java.lang.Boolean getLinkmodels() {
        return linkmodels;
    }


    /**
     * Sets the linkmodels value for this GARLIArguments.
     * 
     * @param linkmodels
     */
    public void setLinkmodels(java.lang.Boolean linkmodels) {
        this.linkmodels = linkmodels;
    }


    /**
     * Gets the uniquepatterns value for this GARLIArguments.
     * 
     * @return uniquepatterns
     */
    public java.lang.String getUniquepatterns() {
        return uniquepatterns;
    }


    /**
     * Sets the uniquepatterns value for this GARLIArguments.
     * 
     * @param uniquepatterns
     */
    public void setUniquepatterns(java.lang.String uniquepatterns) {
        this.uniquepatterns = uniquepatterns;
    }


    /**
     * Gets the invariantsites value for this GARLIArguments.
     * 
     * @return invariantsites
     */
    public java.lang.String getInvariantsites() {
        return invariantsites;
    }


    /**
     * Sets the invariantsites value for this GARLIArguments.
     * 
     * @param invariantsites
     */
    public void setInvariantsites(java.lang.String invariantsites) {
        this.invariantsites = invariantsites;
    }


    /**
     * Gets the datafname value for this GARLIArguments.
     * 
     * @return datafname
     */
    public java.lang.String getDatafname() {
        return datafname;
    }


    /**
     * Sets the datafname value for this GARLIArguments.
     * 
     * @param datafname
     */
    public void setDatafname(java.lang.String datafname) {
        this.datafname = datafname;
    }


    /**
     * Gets the actualmemory value for this GARLIArguments.
     * 
     * @return actualmemory
     */
    public java.lang.String getActualmemory() {
        return actualmemory;
    }


    /**
     * Sets the actualmemory value for this GARLIArguments.
     * 
     * @param actualmemory
     */
    public void setActualmemory(java.lang.String actualmemory) {
        this.actualmemory = actualmemory;
    }


    /**
     * Gets the ratehetmodel value for this GARLIArguments.
     * 
     * @return ratehetmodel
     */
    public java.lang.String getRatehetmodel() {
        return ratehetmodel;
    }


    /**
     * Sets the ratehetmodel value for this GARLIArguments.
     * 
     * @param ratehetmodel
     */
    public void setRatehetmodel(java.lang.String ratehetmodel) {
        this.ratehetmodel = ratehetmodel;
    }


    /**
     * Gets the resampleproportion value for this GARLIArguments.
     * 
     * @return resampleproportion
     */
    public java.lang.Double getResampleproportion() {
        return resampleproportion;
    }


    /**
     * Sets the resampleproportion value for this GARLIArguments.
     * 
     * @param resampleproportion
     */
    public void setResampleproportion(java.lang.Double resampleproportion) {
        this.resampleproportion = resampleproportion;
    }


    /**
     * Gets the statefrequencies value for this GARLIArguments.
     * 
     * @return statefrequencies
     */
    public java.lang.String getStatefrequencies() {
        return statefrequencies;
    }


    /**
     * Sets the statefrequencies value for this GARLIArguments.
     * 
     * @param statefrequencies
     */
    public void setStatefrequencies(java.lang.String statefrequencies) {
        this.statefrequencies = statefrequencies;
    }


    /**
     * Gets the meanbrlenmuts value for this GARLIArguments.
     * 
     * @return meanbrlenmuts
     */
    public java.lang.Integer getMeanbrlenmuts() {
        return meanbrlenmuts;
    }


    /**
     * Sets the meanbrlenmuts value for this GARLIArguments.
     * 
     * @param meanbrlenmuts
     */
    public void setMeanbrlenmuts(java.lang.Integer meanbrlenmuts) {
        this.meanbrlenmuts = meanbrlenmuts;
    }


    /**
     * Gets the jobname value for this GARLIArguments.
     * 
     * @return jobname
     */
    public java.lang.String getJobname() {
        return jobname;
    }


    /**
     * Sets the jobname value for this GARLIArguments.
     * 
     * @param jobname
     */
    public void setJobname(java.lang.String jobname) {
        this.jobname = jobname;
    }


    /**
     * Gets the gammashapemodel value for this GARLIArguments.
     * 
     * @return gammashapemodel
     */
    public java.lang.Integer getGammashapemodel() {
        return gammashapemodel;
    }


    /**
     * Sets the gammashapemodel value for this GARLIArguments.
     * 
     * @param gammashapemodel
     */
    public void setGammashapemodel(java.lang.Integer gammashapemodel) {
        this.gammashapemodel = gammashapemodel;
    }


    /**
     * Gets the datatype value for this GARLIArguments.
     * 
     * @return datatype
     */
    public java.lang.String getDatatype() {
        return datatype;
    }


    /**
     * Sets the datatype value for this GARLIArguments.
     * 
     * @param datatype
     */
    public void setDatatype(java.lang.String datatype) {
        this.datatype = datatype;
    }


    /**
     * Gets the attachmentspertaxon value for this GARLIArguments.
     * 
     * @return attachmentspertaxon
     */
    public java.lang.Integer getAttachmentspertaxon() {
        return attachmentspertaxon;
    }


    /**
     * Sets the attachmentspertaxon value for this GARLIArguments.
     * 
     * @param attachmentspertaxon
     */
    public void setAttachmentspertaxon(java.lang.Integer attachmentspertaxon) {
        this.attachmentspertaxon = attachmentspertaxon;
    }


    /**
     * Gets the sharedFiles value for this GARLIArguments.
     * 
     * @return sharedFiles
     */
    public java.lang.String[] getSharedFiles() {
        return sharedFiles;
    }


    /**
     * Sets the sharedFiles value for this GARLIArguments.
     * 
     * @param sharedFiles
     */
    public void setSharedFiles(java.lang.String[] sharedFiles) {
        this.sharedFiles = sharedFiles;
    }

    public java.lang.String getSharedFiles(int i) {
        return this.sharedFiles[i];
    }

    public void setSharedFiles(int i, java.lang.String _value) {
        this.sharedFiles[i] = _value;
    }


    /**
     * Gets the perJobArguments value for this GARLIArguments.
     * 
     * @return perJobArguments
     */
    public java.lang.String[] getPerJobArguments() {
        return perJobArguments;
    }


    /**
     * Sets the perJobArguments value for this GARLIArguments.
     * 
     * @param perJobArguments
     */
    public void setPerJobArguments(java.lang.String[] perJobArguments) {
        this.perJobArguments = perJobArguments;
    }

    public java.lang.String getPerJobArguments(int i) {
        return this.perJobArguments[i];
    }

    public void setPerJobArguments(int i, java.lang.String _value) {
        this.perJobArguments[i] = _value;
    }


    /**
     * Gets the perJobFiles value for this GARLIArguments.
     * 
     * @return perJobFiles
     */
    public java.lang.String[] getPerJobFiles() {
        return perJobFiles;
    }


    /**
     * Sets the perJobFiles value for this GARLIArguments.
     * 
     * @param perJobFiles
     */
    public void setPerJobFiles(java.lang.String[] perJobFiles) {
        this.perJobFiles = perJobFiles;
    }

    public java.lang.String getPerJobFiles(int i) {
        return this.perJobFiles[i];
    }

    public void setPerJobFiles(int i, java.lang.String _value) {
        this.perJobFiles[i] = _value;
    }


    /**
     * Gets the symlinks value for this GARLIArguments.
     * 
     * @return symlinks
     */
    public java.lang.String getSymlinks() {
        return symlinks;
    }


    /**
     * Sets the symlinks value for this GARLIArguments.
     * 
     * @param symlinks
     */
    public void setSymlinks(java.lang.String symlinks) {
        this.symlinks = symlinks;
    }


    /**
     * Gets the inputFiles value for this GARLIArguments.
     * 
     * @return inputFiles
     */
    public java.lang.String[] getInputFiles() {
        return inputFiles;
    }


    /**
     * Sets the inputFiles value for this GARLIArguments.
     * 
     * @param inputFiles
     */
    public void setInputFiles(java.lang.String[] inputFiles) {
        this.inputFiles = inputFiles;
    }

    public java.lang.String getInputFiles(int i) {
        return this.inputFiles[i];
    }

    public void setInputFiles(int i, java.lang.String _value) {
        this.inputFiles[i] = _value;
    }


    /**
     * Gets the outputFiles value for this GARLIArguments.
     * 
     * @return outputFiles
     */
    public java.lang.String[] getOutputFiles() {
        return outputFiles;
    }


    /**
     * Sets the outputFiles value for this GARLIArguments.
     * 
     * @param outputFiles
     */
    public void setOutputFiles(java.lang.String[] outputFiles) {
        this.outputFiles = outputFiles;
    }

    public java.lang.String getOutputFiles(int i) {
        return this.outputFiles[i];
    }

    public void setOutputFiles(int i, java.lang.String _value) {
        this.outputFiles[i] = _value;
    }


    /**
     * Gets the owner value for this GARLIArguments.
     * 
     * @return owner
     */
    public java.lang.String getOwner() {
        return owner;
    }


    /**
     * Sets the owner value for this GARLIArguments.
     * 
     * @param owner
     */
    public void setOwner(java.lang.String owner) {
        this.owner = owner;
    }


    /**
     * Gets the schedulerOverride value for this GARLIArguments.
     * 
     * @return schedulerOverride
     */
    public java.lang.String getSchedulerOverride() {
        return schedulerOverride;
    }


    /**
     * Sets the schedulerOverride value for this GARLIArguments.
     * 
     * @param schedulerOverride
     */
    public void setSchedulerOverride(java.lang.String schedulerOverride) {
        this.schedulerOverride = schedulerOverride;
    }


    /**
     * Gets the appName value for this GARLIArguments.
     * 
     * @return appName
     */
    public java.lang.String getAppName() {
        return appName;
    }


    /**
     * Sets the appName value for this GARLIArguments.
     * 
     * @param appName
     */
    public void setAppName(java.lang.String appName) {
        this.appName = appName;
    }


    /**
     * Gets the jobName value for this GARLIArguments.
     * 
     * @return jobName
     */
    public java.lang.String getJobName() {
        return jobName;
    }


    /**
     * Sets the jobName value for this GARLIArguments.
     * 
     * @param jobName
     */
    public void setJobName(java.lang.String jobName) {
        this.jobName = jobName;
    }

    /**
     * Sets the jobID value for this GARLIArguments.
     * 
     * @param jobID
     */
    public void setjobID(java.lang.String jobID) {
        this.jobID = jobID;
    }

    /**
     * Gets the jobID value for this GARLIArguments.
     * 
     * @param jobID
     */
    public void getjobID() {
        return jobID;
    }



    /**
     * Gets the workingDir value for this GARLIArguments.
     * 
     * @return workingDir
     */
    public java.lang.String getWorkingDir() {
        return workingDir;
    }


    /**
     * Sets the workingDir value for this GARLIArguments.
     * 
     * @param workingDir
     */
    public void setWorkingDir(java.lang.String workingDir) {
        this.workingDir = workingDir;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GARLIArguments)) return false;
        GARLIArguments other = (GARLIArguments) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.replicates==null && other.getReplicates()==null) || 
             (this.replicates!=null &&
              this.replicates.equals(other.getReplicates()))) &&
            ((this.ratematrix==null && other.getRatematrix()==null) || 
             (this.ratematrix!=null &&
              this.ratematrix.equals(other.getRatematrix()))) &&
            ((this.outputphyliptree==null && other.getOutputphyliptree()==null) || 
             (this.outputphyliptree!=null &&
              this.outputphyliptree.equals(other.getOutputphyliptree()))) &&
            ((this.randnniweight==null && other.getRandnniweight()==null) || 
             (this.randnniweight!=null &&
              this.randnniweight.equals(other.getRandnniweight()))) &&
            ((this.ofprefix==null && other.getOfprefix()==null) || 
             (this.ofprefix!=null &&
              this.ofprefix.equals(other.getOfprefix()))) &&
            ((this.limsprweight==null && other.getLimsprweight()==null) || 
             (this.limsprweight!=null &&
              this.limsprweight.equals(other.getLimsprweight()))) &&
            ((this.numratecats==null && other.getNumratecats()==null) || 
             (this.numratecats!=null &&
              this.numratecats.equals(other.getNumratecats()))) &&
            ((this.uniqueswapbias==null && other.getUniqueswapbias()==null) || 
             (this.uniqueswapbias!=null &&
              this.uniqueswapbias.equals(other.getUniqueswapbias()))) &&
            ((this.treerejectionthreshold==null && other.getTreerejectionthreshold()==null) || 
             (this.treerejectionthreshold!=null &&
              this.treerejectionthreshold.equals(other.getTreerejectionthreshold()))) &&
            ((this.numtaxa==null && other.getNumtaxa()==null) || 
             (this.numtaxa!=null &&
              this.numtaxa.equals(other.getNumtaxa()))) &&
            ((this.modelsdata==null && other.getModelsdata()==null) || 
             (this.modelsdata!=null &&
              this.modelsdata.equals(other.getModelsdata()))) &&
            ((this.startoptprec==null && other.getStartoptprec()==null) || 
             (this.startoptprec!=null &&
              this.startoptprec.equals(other.getStartoptprec()))) &&
            ((this.optimizeinputonly==null && other.getOptimizeinputonly()==null) || 
             (this.optimizeinputonly!=null &&
              this.optimizeinputonly.equals(other.getOptimizeinputonly()))) &&
            ((this.refinestart==null && other.getRefinestart()==null) || 
             (this.refinestart!=null &&
              this.refinestart.equals(other.getRefinestart()))) &&
            ((this.subsetspecificrates==null && other.getSubsetspecificrates()==null) || 
             (this.subsetspecificrates!=null &&
              this.subsetspecificrates.equals(other.getSubsetspecificrates()))) &&
            ((this.topoweight==null && other.getTopoweight()==null) || 
             (this.topoweight!=null &&
              this.topoweight.equals(other.getTopoweight()))) &&
            ((this.constraintfile==null && other.getConstraintfile()==null) || 
             (this.constraintfile!=null &&
              this.constraintfile.equals(other.getConstraintfile()))) &&
            ((this.replicates2==null && other.getReplicates2()==null) || 
             (this.replicates2!=null &&
              this.replicates2.equals(other.getReplicates2()))) &&
            ((this.nindivs==null && other.getNindivs()==null) || 
             (this.nindivs!=null &&
              this.nindivs.equals(other.getNindivs()))) &&
            ((this.streefname_userdata==null && other.getStreefname_userdata()==null) || 
             (this.streefname_userdata!=null &&
              this.streefname_userdata.equals(other.getStreefname_userdata()))) &&
            ((this.profilingjob==null && other.getProfilingjob()==null) || 
             (this.profilingjob!=null &&
              this.profilingjob.equals(other.getProfilingjob()))) &&
            ((this.analysistype==null && other.getAnalysistype()==null) || 
             (this.analysistype!=null &&
              this.analysistype.equals(other.getAnalysistype()))) &&
            ((this.distanceswapbias==null && other.getDistanceswapbias()==null) || 
             (this.distanceswapbias!=null &&
              this.distanceswapbias.equals(other.getDistanceswapbias()))) &&
            ((this.brlenweight==null && other.getBrlenweight()==null) || 
             (this.brlenweight!=null &&
              this.brlenweight.equals(other.getBrlenweight()))) &&
            ((this.minoptprec==null && other.getMinoptprec()==null) || 
             (this.minoptprec!=null &&
              this.minoptprec.equals(other.getMinoptprec()))) &&
            ((this.modweight==null && other.getModweight()==null) || 
             (this.modweight!=null &&
              this.modweight.equals(other.getModweight()))) &&
            ((this.outgroup==null && other.getOutgroup()==null) || 
             (this.outgroup!=null &&
              this.outgroup.equals(other.getOutgroup()))) &&
            ((this.numberofprecreductions==null && other.getNumberofprecreductions()==null) || 
             (this.numberofprecreductions!=null &&
              this.numberofprecreductions.equals(other.getNumberofprecreductions()))) &&
            ((this.selectionintensity==null && other.getSelectionintensity()==null) || 
             (this.selectionintensity!=null &&
              this.selectionintensity.equals(other.getSelectionintensity()))) &&
            ((this.outputsitelikelihoods==null && other.getOutputsitelikelihoods()==null) || 
             (this.outputsitelikelihoods!=null &&
              this.outputsitelikelihoods.equals(other.getOutputsitelikelihoods()))) &&
            ((this.limsprrange==null && other.getLimsprrange()==null) || 
             (this.limsprrange!=null &&
              this.limsprrange.equals(other.getLimsprrange()))) &&
            ((this.collapsebranches==null && other.getCollapsebranches()==null) || 
             (this.collapsebranches!=null &&
              this.collapsebranches.equals(other.getCollapsebranches()))) &&
            ((this.geneticcode==null && other.getGeneticcode()==null) || 
             (this.geneticcode!=null &&
              this.geneticcode.equals(other.getGeneticcode()))) &&
            ((this.configFile==null && other.getConfigFile()==null) || 
             (this.configFile!=null &&
              this.configFile.equals(other.getConfigFile()))) &&
            ((this.gammashapebrlen==null && other.getGammashapebrlen()==null) || 
             (this.gammashapebrlen!=null &&
              this.gammashapebrlen.equals(other.getGammashapebrlen()))) &&
            ((this.searchreps==null && other.getSearchreps()==null) || 
             (this.searchreps!=null &&
              this.searchreps.equals(other.getSearchreps()))) &&
            ((this.randsprweight==null && other.getRandsprweight()==null) || 
             (this.randsprweight!=null &&
              this.randsprweight.equals(other.getRandsprweight()))) &&
            ((this.novalidate==null && other.getNovalidate()==null) || 
             (this.novalidate!=null &&
              this.novalidate.equals(other.getNovalidate()))) &&
            ((this.partitionsdata==null && other.getPartitionsdata()==null) || 
             (this.partitionsdata!=null &&
              this.partitionsdata.equals(other.getPartitionsdata()))) &&
            ((this.inferinternalstateprobs==null && other.getInferinternalstateprobs()==null) || 
             (this.inferinternalstateprobs!=null &&
              this.inferinternalstateprobs.equals(other.getInferinternalstateprobs()))) &&
            ((this.streefname==null && other.getStreefname()==null) || 
             (this.streefname!=null &&
              this.streefname.equals(other.getStreefname()))) &&
            ((this.linkmodels==null && other.getLinkmodels()==null) || 
             (this.linkmodels!=null &&
              this.linkmodels.equals(other.getLinkmodels()))) &&
            ((this.uniquepatterns==null && other.getUniquepatterns()==null) || 
             (this.uniquepatterns!=null &&
              this.uniquepatterns.equals(other.getUniquepatterns()))) &&
            ((this.invariantsites==null && other.getInvariantsites()==null) || 
             (this.invariantsites!=null &&
              this.invariantsites.equals(other.getInvariantsites()))) &&
            ((this.datafname==null && other.getDatafname()==null) || 
             (this.datafname!=null &&
              this.datafname.equals(other.getDatafname()))) &&
            ((this.actualmemory==null && other.getActualmemory()==null) || 
             (this.actualmemory!=null &&
              this.actualmemory.equals(other.getActualmemory()))) &&
            ((this.ratehetmodel==null && other.getRatehetmodel()==null) || 
             (this.ratehetmodel!=null &&
              this.ratehetmodel.equals(other.getRatehetmodel()))) &&
            ((this.resampleproportion==null && other.getResampleproportion()==null) || 
             (this.resampleproportion!=null &&
              this.resampleproportion.equals(other.getResampleproportion()))) &&
            ((this.statefrequencies==null && other.getStatefrequencies()==null) || 
             (this.statefrequencies!=null &&
              this.statefrequencies.equals(other.getStatefrequencies()))) &&
            ((this.meanbrlenmuts==null && other.getMeanbrlenmuts()==null) || 
             (this.meanbrlenmuts!=null &&
              this.meanbrlenmuts.equals(other.getMeanbrlenmuts()))) &&
            ((this.jobname==null && other.getJobname()==null) || 
             (this.jobname!=null &&
              this.jobname.equals(other.getJobname()))) &&
            ((this.gammashapemodel==null && other.getGammashapemodel()==null) || 
             (this.gammashapemodel!=null &&
              this.gammashapemodel.equals(other.getGammashapemodel()))) &&
            ((this.datatype==null && other.getDatatype()==null) || 
             (this.datatype!=null &&
              this.datatype.equals(other.getDatatype()))) &&
            ((this.attachmentspertaxon==null && other.getAttachmentspertaxon()==null) || 
             (this.attachmentspertaxon!=null &&
              this.attachmentspertaxon.equals(other.getAttachmentspertaxon()))) &&
            ((this.sharedFiles==null && other.getSharedFiles()==null) || 
             (this.sharedFiles!=null &&
              java.util.Arrays.equals(this.sharedFiles, other.getSharedFiles()))) &&
            ((this.perJobArguments==null && other.getPerJobArguments()==null) || 
             (this.perJobArguments!=null &&
              java.util.Arrays.equals(this.perJobArguments, other.getPerJobArguments()))) &&
            ((this.perJobFiles==null && other.getPerJobFiles()==null) || 
             (this.perJobFiles!=null &&
              java.util.Arrays.equals(this.perJobFiles, other.getPerJobFiles()))) &&
            ((this.symlinks==null && other.getSymlinks()==null) || 
             (this.symlinks!=null &&
              this.symlinks.equals(other.getSymlinks()))) &&
            ((this.inputFiles==null && other.getInputFiles()==null) || 
             (this.inputFiles!=null &&
              java.util.Arrays.equals(this.inputFiles, other.getInputFiles()))) &&
            ((this.outputFiles==null && other.getOutputFiles()==null) || 
             (this.outputFiles!=null &&
              java.util.Arrays.equals(this.outputFiles, other.getOutputFiles()))) &&
            ((this.owner==null && other.getOwner()==null) || 
             (this.owner!=null &&
              this.owner.equals(other.getOwner()))) &&
            ((this.schedulerOverride==null && other.getSchedulerOverride()==null) || 
             (this.schedulerOverride!=null &&
              this.schedulerOverride.equals(other.getSchedulerOverride()))) &&
            ((this.appName==null && other.getAppName()==null) || 
             (this.appName!=null &&
              this.appName.equals(other.getAppName()))) &&
            ((this.jobName==null && other.getJobName()==null) || 
             (this.jobName!=null &&
              this.jobName.equals(other.getJobName()))) &&
            ((this.workingDir==null && other.getWorkingDir()==null) || 
             (this.workingDir!=null &&
              this.workingDir.equals(other.getWorkingDir())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getReplicates() != null) {
            _hashCode += getReplicates().hashCode();
        }
        if (getRatematrix() != null) {
            _hashCode += getRatematrix().hashCode();
        }
        if (getOutputphyliptree() != null) {
            _hashCode += getOutputphyliptree().hashCode();
        }
        if (getRandnniweight() != null) {
            _hashCode += getRandnniweight().hashCode();
        }
        if (getOfprefix() != null) {
            _hashCode += getOfprefix().hashCode();
        }
        if (getLimsprweight() != null) {
            _hashCode += getLimsprweight().hashCode();
        }
        if (getNumratecats() != null) {
            _hashCode += getNumratecats().hashCode();
        }
        if (getUniqueswapbias() != null) {
            _hashCode += getUniqueswapbias().hashCode();
        }
        if (getTreerejectionthreshold() != null) {
            _hashCode += getTreerejectionthreshold().hashCode();
        }
        if (getNumtaxa() != null) {
            _hashCode += getNumtaxa().hashCode();
        }
        if (getModelsdata() != null) {
            _hashCode += getModelsdata().hashCode();
        }
        if (getStartoptprec() != null) {
            _hashCode += getStartoptprec().hashCode();
        }
        if (getOptimizeinputonly() != null) {
            _hashCode += getOptimizeinputonly().hashCode();
        }
        if (getRefinestart() != null) {
            _hashCode += getRefinestart().hashCode();
        }
        if (getSubsetspecificrates() != null) {
            _hashCode += getSubsetspecificrates().hashCode();
        }
        if (getTopoweight() != null) {
            _hashCode += getTopoweight().hashCode();
        }
        if (getConstraintfile() != null) {
            _hashCode += getConstraintfile().hashCode();
        }
        if (getReplicates2() != null) {
            _hashCode += getReplicates2().hashCode();
        }
        if (getNindivs() != null) {
            _hashCode += getNindivs().hashCode();
        }
        if (getStreefname_userdata() != null) {
            _hashCode += getStreefname_userdata().hashCode();
        }
        if (getProfilingjob() != null) {
            _hashCode += getProfilingjob().hashCode();
        }
        if (getAnalysistype() != null) {
            _hashCode += getAnalysistype().hashCode();
        }
        if (getDistanceswapbias() != null) {
            _hashCode += getDistanceswapbias().hashCode();
        }
        if (getBrlenweight() != null) {
            _hashCode += getBrlenweight().hashCode();
        }
        if (getMinoptprec() != null) {
            _hashCode += getMinoptprec().hashCode();
        }
        if (getModweight() != null) {
            _hashCode += getModweight().hashCode();
        }
        if (getOutgroup() != null) {
            _hashCode += getOutgroup().hashCode();
        }
        if (getNumberofprecreductions() != null) {
            _hashCode += getNumberofprecreductions().hashCode();
        }
        if (getSelectionintensity() != null) {
            _hashCode += getSelectionintensity().hashCode();
        }
        if (getOutputsitelikelihoods() != null) {
            _hashCode += getOutputsitelikelihoods().hashCode();
        }
        if (getLimsprrange() != null) {
            _hashCode += getLimsprrange().hashCode();
        }
        if (getCollapsebranches() != null) {
            _hashCode += getCollapsebranches().hashCode();
        }
        if (getGeneticcode() != null) {
            _hashCode += getGeneticcode().hashCode();
        }
        if (getConfigFile() != null) {
            _hashCode += getConfigFile().hashCode();
        }
        if (getGammashapebrlen() != null) {
            _hashCode += getGammashapebrlen().hashCode();
        }
        if (getSearchreps() != null) {
            _hashCode += getSearchreps().hashCode();
        }
        if (getRandsprweight() != null) {
            _hashCode += getRandsprweight().hashCode();
        }
        if (getNovalidate() != null) {
            _hashCode += getNovalidate().hashCode();
        }
        if (getPartitionsdata() != null) {
            _hashCode += getPartitionsdata().hashCode();
        }
        if (getInferinternalstateprobs() != null) {
            _hashCode += getInferinternalstateprobs().hashCode();
        }
        if (getStreefname() != null) {
            _hashCode += getStreefname().hashCode();
        }
        if (getLinkmodels() != null) {
            _hashCode += getLinkmodels().hashCode();
        }
        if (getUniquepatterns() != null) {
            _hashCode += getUniquepatterns().hashCode();
        }
        if (getInvariantsites() != null) {
            _hashCode += getInvariantsites().hashCode();
        }
        if (getDatafname() != null) {
            _hashCode += getDatafname().hashCode();
        }
        if (getActualmemory() != null) {
            _hashCode += getActualmemory().hashCode();
        }
        if (getRatehetmodel() != null) {
            _hashCode += getRatehetmodel().hashCode();
        }
        if (getResampleproportion() != null) {
            _hashCode += getResampleproportion().hashCode();
        }
        if (getStatefrequencies() != null) {
            _hashCode += getStatefrequencies().hashCode();
        }
        if (getMeanbrlenmuts() != null) {
            _hashCode += getMeanbrlenmuts().hashCode();
        }
        if (getJobname() != null) {
            _hashCode += getJobname().hashCode();
        }
        if (getGammashapemodel() != null) {
            _hashCode += getGammashapemodel().hashCode();
        }
        if (getDatatype() != null) {
            _hashCode += getDatatype().hashCode();
        }
        if (getAttachmentspertaxon() != null) {
            _hashCode += getAttachmentspertaxon().hashCode();
        }
        if (getSharedFiles() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSharedFiles());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSharedFiles(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getPerJobArguments() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPerJobArguments());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPerJobArguments(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getPerJobFiles() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPerJobFiles());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPerJobFiles(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getSymlinks() != null) {
            _hashCode += getSymlinks().hashCode();
        }
        if (getInputFiles() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getInputFiles());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getInputFiles(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getOutputFiles() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getOutputFiles());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getOutputFiles(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getOwner() != null) {
            _hashCode += getOwner().hashCode();
        }
        if (getSchedulerOverride() != null) {
            _hashCode += getSchedulerOverride().hashCode();
        }
        if (getAppName() != null) {
            _hashCode += getAppName().hashCode();
        }
        if (getJobName() != null) {
            _hashCode += getJobName().hashCode();
        }
        if (getWorkingDir() != null) {
            _hashCode += getWorkingDir().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GARLIArguments.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.umd.edu/namespaces/grid/bio/GARLIService", "GARLIArguments"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("replicates");
        elemField.setXmlName(new javax.xml.namespace.QName("", "replicates"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ratematrix");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ratematrix"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("outputphyliptree");
        elemField.setXmlName(new javax.xml.namespace.QName("", "outputphyliptree"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("randnniweight");
        elemField.setXmlName(new javax.xml.namespace.QName("", "randnniweight"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ofprefix");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ofprefix"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("limsprweight");
        elemField.setXmlName(new javax.xml.namespace.QName("", "limsprweight"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numratecats");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numratecats"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("uniqueswapbias");
        elemField.setXmlName(new javax.xml.namespace.QName("", "uniqueswapbias"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("treerejectionthreshold");
        elemField.setXmlName(new javax.xml.namespace.QName("", "treerejectionthreshold"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numtaxa");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numtaxa"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modelsdata");
        elemField.setXmlName(new javax.xml.namespace.QName("", "modelsdata"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("startoptprec");
        elemField.setXmlName(new javax.xml.namespace.QName("", "startoptprec"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("optimizeinputonly");
        elemField.setXmlName(new javax.xml.namespace.QName("", "optimizeinputonly"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("refinestart");
        elemField.setXmlName(new javax.xml.namespace.QName("", "refinestart"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subsetspecificrates");
        elemField.setXmlName(new javax.xml.namespace.QName("", "subsetspecificrates"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("topoweight");
        elemField.setXmlName(new javax.xml.namespace.QName("", "topoweight"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("constraintfile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "constraintfile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("replicates");
        elemField.setXmlName(new javax.xml.namespace.QName("", "replicates"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nindivs");
        elemField.setXmlName(new javax.xml.namespace.QName("", "nindivs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("streefname_userdata");
        elemField.setXmlName(new javax.xml.namespace.QName("", "streefname_userdata"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("profilingjob");
        elemField.setXmlName(new javax.xml.namespace.QName("", "profilingjob"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("analysistype");
        elemField.setXmlName(new javax.xml.namespace.QName("", "analysistype"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("distanceswapbias");
        elemField.setXmlName(new javax.xml.namespace.QName("", "distanceswapbias"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("brlenweight");
        elemField.setXmlName(new javax.xml.namespace.QName("", "brlenweight"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("minoptprec");
        elemField.setXmlName(new javax.xml.namespace.QName("", "minoptprec"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modweight");
        elemField.setXmlName(new javax.xml.namespace.QName("", "modweight"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("outgroup");
        elemField.setXmlName(new javax.xml.namespace.QName("", "outgroup"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numberofprecreductions");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numberofprecreductions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("selectionintensity");
        elemField.setXmlName(new javax.xml.namespace.QName("", "selectionintensity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("outputsitelikelihoods");
        elemField.setXmlName(new javax.xml.namespace.QName("", "outputsitelikelihoods"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("limsprrange");
        elemField.setXmlName(new javax.xml.namespace.QName("", "limsprrange"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("collapsebranches");
        elemField.setXmlName(new javax.xml.namespace.QName("", "collapsebranches"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("geneticcode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "geneticcode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("configFile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "configFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("gammashapebrlen");
        elemField.setXmlName(new javax.xml.namespace.QName("", "gammashapebrlen"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("searchreps");
        elemField.setXmlName(new javax.xml.namespace.QName("", "searchreps"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("randsprweight");
        elemField.setXmlName(new javax.xml.namespace.QName("", "randsprweight"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("novalidate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "novalidate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("partitionsdata");
        elemField.setXmlName(new javax.xml.namespace.QName("", "partitionsdata"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("inferinternalstateprobs");
        elemField.setXmlName(new javax.xml.namespace.QName("", "inferinternalstateprobs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("streefname");
        elemField.setXmlName(new javax.xml.namespace.QName("", "streefname"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("linkmodels");
        elemField.setXmlName(new javax.xml.namespace.QName("", "linkmodels"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("uniquepatterns");
        elemField.setXmlName(new javax.xml.namespace.QName("", "uniquepatterns"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("invariantsites");
        elemField.setXmlName(new javax.xml.namespace.QName("", "invariantsites"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("datafname");
        elemField.setXmlName(new javax.xml.namespace.QName("", "datafname"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actualmemory");
        elemField.setXmlName(new javax.xml.namespace.QName("", "actualmemory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ratehetmodel");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ratehetmodel"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resampleproportion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "resampleproportion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("statefrequencies");
        elemField.setXmlName(new javax.xml.namespace.QName("", "statefrequencies"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("meanbrlenmuts");
        elemField.setXmlName(new javax.xml.namespace.QName("", "meanbrlenmuts"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobname");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jobname"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("gammashapemodel");
        elemField.setXmlName(new javax.xml.namespace.QName("", "gammashapemodel"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("datatype");
        elemField.setXmlName(new javax.xml.namespace.QName("", "datatype"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attachmentspertaxon");
        elemField.setXmlName(new javax.xml.namespace.QName("", "attachmentspertaxon"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sharedFiles");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sharedFiles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("perJobArguments");
        elemField.setXmlName(new javax.xml.namespace.QName("", "perJobArguments"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("perJobFiles");
        elemField.setXmlName(new javax.xml.namespace.QName("", "perJobFiles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("symlinks");
        elemField.setXmlName(new javax.xml.namespace.QName("", "symlinks"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("inputFiles");
        elemField.setXmlName(new javax.xml.namespace.QName("", "inputFiles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("outputFiles");
        elemField.setXmlName(new javax.xml.namespace.QName("", "outputFiles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("owner");
        elemField.setXmlName(new javax.xml.namespace.QName("", "owner"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("schedulerOverride");
        elemField.setXmlName(new javax.xml.namespace.QName("", "schedulerOverride"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("appName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "appName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jobName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workingDir");
        elemField.setXmlName(new javax.xml.namespace.QName("", "workingDir"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }
=======
=======
>>>>>>> a10ad43c27f9fe4ae9b295e0f6d35188164f953a
	public GARLIArguments(
			java.lang.Integer replicates,
			java.lang.String ratematrix,
			java.lang.Boolean outputphyliptree,
			java.lang.Double randnniweight,
			java.lang.String ofprefix,
			java.lang.Double limsprweight,
			java.lang.Integer numratecats,
			java.lang.Double uniqueswapbias,
			java.lang.Integer treerejectionthreshold,
			java.lang.String numtaxa,
			java.lang.String modelsdata,
			java.lang.Double startoptprec,
			java.lang.Boolean optimizeinputonly,
			java.lang.Boolean refinestart,
			java.lang.Boolean subsetspecificrates,
			java.lang.Double topoweight,
			java.lang.String constraintfile,
			java.lang.Integer replicates2,
			java.lang.Integer nindivs,
			java.lang.String streefname_userdata,
			java.lang.Integer profilingjob,
			java.lang.String analysistype,
			java.lang.Double distanceswapbias,
			java.lang.Double brlenweight,
			java.lang.Double minoptprec,
			java.lang.Double modweight,
			java.lang.String outgroup,
			java.lang.Integer numberofprecreductions,
			java.lang.Double selectionintensity,
			java.lang.Boolean outputsitelikelihoods,
			java.lang.Integer limsprrange,
			java.lang.Boolean collapsebranches,
			java.lang.String geneticcode,
			java.lang.String configFile,
			java.lang.Integer gammashapebrlen,
			java.lang.Integer searchreps,
			java.lang.Double randsprweight,
			java.lang.Boolean novalidate,
			java.lang.String partitionsdata,
			java.lang.Boolean inferinternalstateprobs,
			java.lang.String streefname,
			java.lang.Boolean linkmodels,
			java.lang.String uniquepatterns,
			java.lang.String invariantsites,
			java.lang.String datafname,
			java.lang.String actualmemory,
			java.lang.String ratehetmodel,
			java.lang.Double resampleproportion,
			java.lang.String statefrequencies,
			java.lang.Integer meanbrlenmuts,
			java.lang.String jobname,
			java.lang.Integer gammashapemodel,
			java.lang.String datatype,
			java.lang.Integer attachmentspertaxon,
			java.lang.String[] sharedFiles,
			java.lang.String[] perJobArguments,
			java.lang.String[] perJobFiles,
			java.lang.String symlinks,
			java.lang.String[] inputFiles,
			java.lang.String[] outputFiles,
			java.lang.String owner,
			java.lang.String schedulerOverride,
			java.lang.String appName,
			java.lang.String jobName,
			java.land.String jobID,
			java.lang.String workingDir) {
		this.replicates = replicates;
		this.ratematrix = ratematrix;
		this.outputphyliptree = outputphyliptree;
		this.randnniweight = randnniweight;
		this.ofprefix = ofprefix;
		this.limsprweight = limsprweight;
		this.numratecats = numratecats;
		this.uniqueswapbias = uniqueswapbias;
		this.treerejectionthreshold = treerejectionthreshold;
		this.numtaxa = numtaxa;
		this.modelsdata = modelsdata;
		this.startoptprec = startoptprec;
		this.optimizeinputonly = optimizeinputonly;
		this.refinestart = refinestart;
		this.subsetspecificrates = subsetspecificrates;
		this.topoweight = topoweight;
		this.constraintfile = constraintfile;
		this.replicates2 = replicates2;
		this.nindivs = nindivs;
		this.streefname_userdata = streefname_userdata;
		this.profilingjob = profilingjob;
		this.analysistype = analysistype;
		this.distanceswapbias = distanceswapbias;
		this.brlenweight = brlenweight;
		this.minoptprec = minoptprec;
		this.modweight = modweight;
		this.outgroup = outgroup;
		this.numberofprecreductions = numberofprecreductions;
		this.selectionintensity = selectionintensity;
		this.outputsitelikelihoods = outputsitelikelihoods;
		this.limsprrange = limsprrange;
		this.collapsebranches = collapsebranches;
		this.geneticcode = geneticcode;
		this.configFile = configFile;
		this.gammashapebrlen = gammashapebrlen;
		this.searchreps = searchreps;
		this.randsprweight = randsprweight;
		this.novalidate = novalidate;
		this.partitionsdata = partitionsdata;
		this.inferinternalstateprobs = inferinternalstateprobs;
		this.streefname = streefname;
		this.linkmodels = linkmodels;
		this.uniquepatterns = uniquepatterns;
		this.invariantsites = invariantsites;
		this.datafname = datafname;
		this.actualmemory = actualmemory;
		this.ratehetmodel = ratehetmodel;
		this.resampleproportion = resampleproportion;
		this.statefrequencies = statefrequencies;
		this.meanbrlenmuts = meanbrlenmuts;
		this.jobname = jobname;
		this.gammashapemodel = gammashapemodel;
		this.datatype = datatype;
		this.attachmentspertaxon = attachmentspertaxon;
		this.sharedFiles = sharedFiles;
		this.perJobArguments = perJobArguments;
		this.perJobFiles = perJobFiles;
		this.symlinks = symlinks;
		this.inputFiles = inputFiles;
		this.outputFiles = outputFiles;
		this.owner = owner;
		this.schedulerOverride = schedulerOverride;
		this.appName = appName;
		this.jobName = jobName;
		this.jobID = jobID;  // Added for GT6.
		this.workingDir = workingDir;
	}


	/**
	 * Gets the replicates value for this GARLIArguments.
	 * 
	 * @return replicates
	 */
	public java.lang.Integer getReplicates() {
		return replicates;
	}


	/**
	 * Sets the replicates value for this GARLIArguments.
	 * 
	 * @param replicates
	 */
	public void setReplicates(java.lang.Integer replicates) {
		this.replicates = replicates;
	}


	/**
	 * Gets the ratematrix value for this GARLIArguments.
	 * 
	 * @return ratematrix
	 */
	public java.lang.String getRatematrix() {
		return ratematrix;
	}


	/**
	 * Sets the ratematrix value for this GARLIArguments.
	 * 
	 * @param ratematrix
	 */
	public void setRatematrix(java.lang.String ratematrix) {
		this.ratematrix = ratematrix;
	}


	/**
	 * Gets the outputphyliptree value for this GARLIArguments.
	 * 
	 * @return outputphyliptree
	 */
	public java.lang.Boolean getOutputphyliptree() {
		return outputphyliptree;
	}


	/**
	 * Sets the outputphyliptree value for this GARLIArguments.
	 * 
	 * @param outputphyliptree
	 */
	public void setOutputphyliptree(java.lang.Boolean outputphyliptree) {
		this.outputphyliptree = outputphyliptree;
	}


	/**
	 * Gets the randnniweight value for this GARLIArguments.
	 * 
	 * @return randnniweight
	 */
	public java.lang.Double getRandnniweight() {
		return randnniweight;
	}


	/**
	 * Sets the randnniweight value for this GARLIArguments.
	 * 
	 * @param randnniweight
	 */
	public void setRandnniweight(java.lang.Double randnniweight) {
		this.randnniweight = randnniweight;
	}


	/**
	 * Gets the ofprefix value for this GARLIArguments.
	 * 
	 * @return ofprefix
	 */
	public java.lang.String getOfprefix() {
		return ofprefix;
	}


	/**
	 * Sets the ofprefix value for this GARLIArguments.
	 * 
	 * @param ofprefix
	 */
	public void setOfprefix(java.lang.String ofprefix) {
		this.ofprefix = ofprefix;
	}


	/**
	 * Gets the limsprweight value for this GARLIArguments.
	 * 
	 * @return limsprweight
	 */
	public java.lang.Double getLimsprweight() {
		return limsprweight;
	}


	/**
	 * Sets the limsprweight value for this GARLIArguments.
	 * 
	 * @param limsprweight
	 */
	public void setLimsprweight(java.lang.Double limsprweight) {
		this.limsprweight = limsprweight;
	}


	/**
	 * Gets the numratecats value for this GARLIArguments.
	 * 
	 * @return numratecats
	 */
	public java.lang.Integer getNumratecats() {
		return numratecats;
	}


	/**
	 * Sets the numratecats value for this GARLIArguments.
	 * 
	 * @param numratecats
	 */
	public void setNumratecats(java.lang.Integer numratecats) {
		this.numratecats = numratecats;
	}


	/**
	 * Gets the uniqueswapbias value for this GARLIArguments.
	 * 
	 * @return uniqueswapbias
	 */
	public java.lang.Double getUniqueswapbias() {
		return uniqueswapbias;
	}


	/**
	 * Sets the uniqueswapbias value for this GARLIArguments.
	 * 
	 * @param uniqueswapbias
	 */
	public void setUniqueswapbias(java.lang.Double uniqueswapbias) {
		this.uniqueswapbias = uniqueswapbias;
	}


	/**
	 * Gets the treerejectionthreshold value for this GARLIArguments.
	 * 
	 * @return treerejectionthreshold
	 */
	public java.lang.Integer getTreerejectionthreshold() {
		return treerejectionthreshold;
	}


	/**
	 * Sets the treerejectionthreshold value for this GARLIArguments.
	 * 
	 * @param treerejectionthreshold
	 */
	public void setTreerejectionthreshold(java.lang.Integer treerejectionthreshold) {
		this.treerejectionthreshold = treerejectionthreshold;
	}


	/**
	 * Gets the numtaxa value for this GARLIArguments.
	 * 
	 * @return numtaxa
	 */
	public java.lang.String getNumtaxa() {
		return numtaxa;
	}


	/**
	 * Sets the numtaxa value for this GARLIArguments.
	 * 
	 * @param numtaxa
	 */
	public void setNumtaxa(java.lang.String numtaxa) {
		this.numtaxa = numtaxa;
	}


	/**
	 * Gets the modelsdata value for this GARLIArguments.
	 * 
	 * @return modelsdata
	 */
	public java.lang.String getModelsdata() {
		return modelsdata;
	}


	/**
	 * Sets the modelsdata value for this GARLIArguments.
	 * 
	 * @param modelsdata
	 */
	public void setModelsdata(java.lang.String modelsdata) {
		this.modelsdata = modelsdata;
	}


	/**
	 * Gets the startoptprec value for this GARLIArguments.
	 * 
	 * @return startoptprec
	 */
	public java.lang.Double getStartoptprec() {
		return startoptprec;
	}


	/**
	 * Sets the startoptprec value for this GARLIArguments.
	 * 
	 * @param startoptprec
	 */
	public void setStartoptprec(java.lang.Double startoptprec) {
		this.startoptprec = startoptprec;
	}


	/**
	 * Gets the optimizeinputonly value for this GARLIArguments.
	 * 
	 * @return optimizeinputonly
	 */
	public java.lang.Boolean getOptimizeinputonly() {
		return optimizeinputonly;
	}


	/**
	 * Sets the optimizeinputonly value for this GARLIArguments.
	 * 
	 * @param optimizeinputonly
	 */
	public void setOptimizeinputonly(java.lang.Boolean optimizeinputonly) {
		this.optimizeinputonly = optimizeinputonly;
	}


	/**
	 * Gets the refinestart value for this GARLIArguments.
	 * 
	 * @return refinestart
	 */
	public java.lang.Boolean getRefinestart() {
		return refinestart;
	}


	/**
	 * Sets the refinestart value for this GARLIArguments.
	 * 
	 * @param refinestart
	 */
	public void setRefinestart(java.lang.Boolean refinestart) {
		this.refinestart = refinestart;
	}


	/**
	 * Gets the subsetspecificrates value for this GARLIArguments.
	 * 
	 * @return subsetspecificrates
	 */
	public java.lang.Boolean getSubsetspecificrates() {
		return subsetspecificrates;
	}


	/**
	 * Sets the subsetspecificrates value for this GARLIArguments.
	 * 
	 * @param subsetspecificrates
	 */
	public void setSubsetspecificrates(java.lang.Boolean subsetspecificrates) {
		this.subsetspecificrates = subsetspecificrates;
	}


	/**
	 * Gets the topoweight value for this GARLIArguments.
	 * 
	 * @return topoweight
	 */
	public java.lang.Double getTopoweight() {
		return topoweight;
	}


	/**
	 * Sets the topoweight value for this GARLIArguments.
	 * 
	 * @param topoweight
	 */
	public void setTopoweight(java.lang.Double topoweight) {
		this.topoweight = topoweight;
	}


	/**
	 * Gets the constraintfile value for this GARLIArguments.
	 * 
	 * @return constraintfile
	 */
	public java.lang.String getConstraintfile() {
		return constraintfile;
	}


	/**
	 * Sets the constraintfile value for this GARLIArguments.
	 * 
	 * @param constraintfile
	 */
	public void setConstraintfile(java.lang.String constraintfile) {
		this.constraintfile = constraintfile;
	}


	/**
	 * Gets the replicates2 value for this GARLIArguments.
	 * 
	 * @return replicates2
	 */
	public java.lang.Integer getReplicates2() {
		return replicates2;
	}


	/**
	 * Sets the replicates2 value for this GARLIArguments.
	 * 
	 * @param replicates2
	 */
	public void setReplicates2(java.lang.Integer replicates2) {
		this.replicates2 = replicates2;
	}


	/**
	 * Gets the nindivs value for this GARLIArguments.
	 * 
	 * @return nindivs
	 */
	public java.lang.Integer getNindivs() {
		return nindivs;
	}


	/**
	 * Sets the nindivs value for this GARLIArguments.
	 * 
	 * @param nindivs
	 */
	public void setNindivs(java.lang.Integer nindivs) {
		this.nindivs = nindivs;
	}


	/**
	 * Gets the streefname_userdata value for this GARLIArguments.
	 * 
	 * @return streefname_userdata
	 */
	public java.lang.String getStreefname_userdata() {
		return streefname_userdata;
	}


	/**
	 * Sets the streefname_userdata value for this GARLIArguments.
	 * 
	 * @param streefname_userdata
	 */
	public void setStreefname_userdata(java.lang.String streefname_userdata) {
		this.streefname_userdata = streefname_userdata;
	}


	/**
	 * Gets the profilingjob value for this GARLIArguments.
	 * 
	 * @return profilingjob
	 */
	public java.lang.Integer getProfilingjob() {
		return profilingjob;
	}


	/**
	 * Sets the profilingjob value for this GARLIArguments.
	 * 
	 * @param profilingjob
	 */
	public void setProfilingjob(java.lang.Integer profilingjob) {
		this.profilingjob = profilingjob;
	}


	/**
	 * Gets the analysistype value for this GARLIArguments.
	 * 
	 * @return analysistype
	 */
	public java.lang.String getAnalysistype() {
		return analysistype;
	}


	/**
	 * Sets the analysistype value for this GARLIArguments.
	 * 
	 * @param analysistype
	 */
	public void setAnalysistype(java.lang.String analysistype) {
		this.analysistype = analysistype;
	}


	/**
	 * Gets the distanceswapbias value for this GARLIArguments.
	 * 
	 * @return distanceswapbias
	 */
	public java.lang.Double getDistanceswapbias() {
		return distanceswapbias;
	}


	/**
	 * Sets the distanceswapbias value for this GARLIArguments.
	 * 
	 * @param distanceswapbias
	 */
	public void setDistanceswapbias(java.lang.Double distanceswapbias) {
		this.distanceswapbias = distanceswapbias;
	}


	/**
	 * Gets the brlenweight value for this GARLIArguments.
	 * 
	 * @return brlenweight
	 */
	public java.lang.Double getBrlenweight() {
		return brlenweight;
	}


	/**
	 * Sets the brlenweight value for this GARLIArguments.
	 * 
	 * @param brlenweight
	 */
	public void setBrlenweight(java.lang.Double brlenweight) {
		this.brlenweight = brlenweight;
	}


	/**
	 * Gets the minoptprec value for this GARLIArguments.
	 * 
	 * @return minoptprec
	 */
	public java.lang.Double getMinoptprec() {
		return minoptprec;
	}


	/**
	 * Sets the minoptprec value for this GARLIArguments.
	 * 
	 * @param minoptprec
	 */
	public void setMinoptprec(java.lang.Double minoptprec) {
		this.minoptprec = minoptprec;
	}


	/**
	 * Gets the modweight value for this GARLIArguments.
	 * 
	 * @return modweight
	 */
	public java.lang.Double getModweight() {
		return modweight;
	}


	/**
	 * Sets the modweight value for this GARLIArguments.
	 * 
	 * @param modweight
	 */
	public void setModweight(java.lang.Double modweight) {
		this.modweight = modweight;
	}


	/**
	 * Gets the outgroup value for this GARLIArguments.
	 * 
	 * @return outgroup
	 */
	public java.lang.String getOutgroup() {
		return outgroup;
	}


	/**
	 * Sets the outgroup value for this GARLIArguments.
	 * 
	 * @param outgroup
	 */
	public void setOutgroup(java.lang.String outgroup) {
		this.outgroup = outgroup;
	}


	/**
	 * Gets the numberofprecreductions value for this GARLIArguments.
	 * 
	 * @return numberofprecreductions
	 */
	public java.lang.Integer getNumberofprecreductions() {
		return numberofprecreductions;
	}


	/**
	 * Sets the numberofprecreductions value for this GARLIArguments.
	 * 
	 * @param numberofprecreductions
	 */
	public void setNumberofprecreductions(java.lang.Integer numberofprecreductions) {
		this.numberofprecreductions = numberofprecreductions;
	}


	/**
	 * Gets the selectionintensity value for this GARLIArguments.
	 * 
	 * @return selectionintensity
	 */
	public java.lang.Double getSelectionintensity() {
		return selectionintensity;
	}


	/**
	 * Sets the selectionintensity value for this GARLIArguments.
	 * 
	 * @param selectionintensity
	 */
	public void setSelectionintensity(java.lang.Double selectionintensity) {
		this.selectionintensity = selectionintensity;
	}


	/**
	 * Gets the outputsitelikelihoods value for this GARLIArguments.
	 * 
	 * @return outputsitelikelihoods
	 */
	public java.lang.Boolean getOutputsitelikelihoods() {
		return outputsitelikelihoods;
	}


	/**
	 * Sets the outputsitelikelihoods value for this GARLIArguments.
	 * 
	 * @param outputsitelikelihoods
	 */
	public void setOutputsitelikelihoods(java.lang.Boolean outputsitelikelihoods) {
		this.outputsitelikelihoods = outputsitelikelihoods;
	}


	/**
	 * Gets the limsprrange value for this GARLIArguments.
	 * 
	 * @return limsprrange
	 */
	public java.lang.Integer getLimsprrange() {
		return limsprrange;
	}


	/**
	 * Sets the limsprrange value for this GARLIArguments.
	 * 
	 * @param limsprrange
	 */
	public void setLimsprrange(java.lang.Integer limsprrange) {
		this.limsprrange = limsprrange;
	}


	/**
	 * Gets the collapsebranches value for this GARLIArguments.
	 * 
	 * @return collapsebranches
	 */
	public java.lang.Boolean getCollapsebranches() {
		return collapsebranches;
	}


	/**
	 * Sets the collapsebranches value for this GARLIArguments.
	 * 
	 * @param collapsebranches
	 */
	public void setCollapsebranches(java.lang.Boolean collapsebranches) {
		this.collapsebranches = collapsebranches;
	}


	/**
	 * Gets the geneticcode value for this GARLIArguments.
	 * 
	 * @return geneticcode
	 */
	public java.lang.String getGeneticcode() {
		return geneticcode;
	}


	/**
	 * Sets the geneticcode value for this GARLIArguments.
	 * 
	 * @param geneticcode
	 */
	public void setGeneticcode(java.lang.String geneticcode) {
		this.geneticcode = geneticcode;
	}


	/**
	 * Gets the configFile value for this GARLIArguments.
	 * 
	 * @return configFile
	 */
	public java.lang.String getConfigFile() {
		return configFile;
	}


	/**
	 * Sets the configFile value for this GARLIArguments.
	 * 
	 * @param configFile
	 */
	public void setConfigFile(java.lang.String configFile) {
		this.configFile = configFile;
	}


	/**
	 * Gets the gammashapebrlen value for this GARLIArguments.
	 * 
	 * @return gammashapebrlen
	 */
	public java.lang.Integer getGammashapebrlen() {
		return gammashapebrlen;
	}


	/**
	 * Sets the gammashapebrlen value for this GARLIArguments.
	 * 
	 * @param gammashapebrlen
	 */
	public void setGammashapebrlen(java.lang.Integer gammashapebrlen) {
		this.gammashapebrlen = gammashapebrlen;
	}


	/**
	 * Gets the searchreps value for this GARLIArguments.
	 * 
	 * @return searchreps
	 */
	public java.lang.Integer getSearchreps() {
		return searchreps;
	}


	/**
	 * Sets the searchreps value for this GARLIArguments.
	 * 
	 * @param searchreps
	 */
	public void setSearchreps(java.lang.Integer searchreps) {
		this.searchreps = searchreps;
	}


	/**
	 * Gets the randsprweight value for this GARLIArguments.
	 * 
	 * @return randsprweight
	 */
	public java.lang.Double getRandsprweight() {
		return randsprweight;
	}


	/**
	 * Sets the randsprweight value for this GARLIArguments.
	 * 
	 * @param randsprweight
	 */
	public void setRandsprweight(java.lang.Double randsprweight) {
		this.randsprweight = randsprweight;
	}


	/**
	 * Gets the novalidate value for this GARLIArguments.
	 * 
	 * @return novalidate
	 */
	public java.lang.Boolean getNovalidate() {
		return novalidate;
	}


	/**
	 * Sets the novalidate value for this GARLIArguments.
	 * 
	 * @param novalidate
	 */
	public void setNovalidate(java.lang.Boolean novalidate) {
		this.novalidate = novalidate;
	}


	/**
	 * Gets the partitionsdata value for this GARLIArguments.
	 * 
	 * @return partitionsdata
	 */
	public java.lang.String getPartitionsdata() {
		return partitionsdata;
	}


	/**
	 * Sets the partitionsdata value for this GARLIArguments.
	 * 
	 * @param partitionsdata
	 */
	public void setPartitionsdata(java.lang.String partitionsdata) {
		this.partitionsdata = partitionsdata;
	}


	/**
	 * Gets the inferinternalstateprobs value for this GARLIArguments.
	 * 
	 * @return inferinternalstateprobs
	 */
	public java.lang.Boolean getInferinternalstateprobs() {
		return inferinternalstateprobs;
	}


	/**
	 * Sets the inferinternalstateprobs value for this GARLIArguments.
	 * 
	 * @param inferinternalstateprobs
	 */
	public void setInferinternalstateprobs(java.lang.Boolean inferinternalstateprobs) {
		this.inferinternalstateprobs = inferinternalstateprobs;
	}


	/**
	 * Gets the streefname value for this GARLIArguments.
	 * 
	 * @return streefname
	 */
	public java.lang.String getStreefname() {
		return streefname;
	}


	/**
	 * Sets the streefname value for this GARLIArguments.
	 * 
	 * @param streefname
	 */
	public void setStreefname(java.lang.String streefname) {
		this.streefname = streefname;
	}


	/**
	 * Gets the linkmodels value for this GARLIArguments.
	 * 
	 * @return linkmodels
	 */
	public java.lang.Boolean getLinkmodels() {
		return linkmodels;
	}


	/**
	 * Sets the linkmodels value for this GARLIArguments.
	 * 
	 * @param linkmodels
	 */
	public void setLinkmodels(java.lang.Boolean linkmodels) {
		this.linkmodels = linkmodels;
	}


	/**
	 * Gets the uniquepatterns value for this GARLIArguments.
	 * 
	 * @return uniquepatterns
	 */
	public java.lang.String getUniquepatterns() {
		return uniquepatterns;
	}


	/**
	 * Sets the uniquepatterns value for this GARLIArguments.
	 * 
	 * @param uniquepatterns
	 */
	public void setUniquepatterns(java.lang.String uniquepatterns) {
		this.uniquepatterns = uniquepatterns;
	}


	/**
	 * Gets the invariantsites value for this GARLIArguments.
	 * 
	 * @return invariantsites
	 */
	public java.lang.String getInvariantsites() {
		return invariantsites;
	}


	/**
	 * Sets the invariantsites value for this GARLIArguments.
	 * 
	 * @param invariantsites
	 */
	public void setInvariantsites(java.lang.String invariantsites) {
		this.invariantsites = invariantsites;
	}


	/**
	 * Gets the datafname value for this GARLIArguments.
	 * 
	 * @return datafname
	 */
	public java.lang.String getDatafname() {
		return datafname;
	}


	/**
	 * Sets the datafname value for this GARLIArguments.
	 * 
	 * @param datafname
	 */
	public void setDatafname(java.lang.String datafname) {
		this.datafname = datafname;
	}


	/**
	 * Gets the actualmemory value for this GARLIArguments.
	 * 
	 * @return actualmemory
	 */
	public java.lang.String getActualmemory() {
		return actualmemory;
	}


	/**
	 * Sets the actualmemory value for this GARLIArguments.
	 * 
	 * @param actualmemory
	 */
	public void setActualmemory(java.lang.String actualmemory) {
		this.actualmemory = actualmemory;
	}


	/**
	 * Gets the ratehetmodel value for this GARLIArguments.
	 * 
	 * @return ratehetmodel
	 */
	public java.lang.String getRatehetmodel() {
		return ratehetmodel;
	}


	/**
	 * Sets the ratehetmodel value for this GARLIArguments.
	 * 
	 * @param ratehetmodel
	 */
	public void setRatehetmodel(java.lang.String ratehetmodel) {
		this.ratehetmodel = ratehetmodel;
	}


	/**
	 * Gets the resampleproportion value for this GARLIArguments.
	 * 
	 * @return resampleproportion
	 */
	public java.lang.Double getResampleproportion() {
		return resampleproportion;
	}


	/**
	 * Sets the resampleproportion value for this GARLIArguments.
	 * 
	 * @param resampleproportion
	 */
	public void setResampleproportion(java.lang.Double resampleproportion) {
		this.resampleproportion = resampleproportion;
	}


	/**
	 * Gets the statefrequencies value for this GARLIArguments.
	 * 
	 * @return statefrequencies
	 */
	public java.lang.String getStatefrequencies() {
		return statefrequencies;
	}


	/**
	 * Sets the statefrequencies value for this GARLIArguments.
	 * 
	 * @param statefrequencies
	 */
	public void setStatefrequencies(java.lang.String statefrequencies) {
		this.statefrequencies = statefrequencies;
	}


	/**
	 * Gets the meanbrlenmuts value for this GARLIArguments.
	 * 
	 * @return meanbrlenmuts
	 */
	public java.lang.Integer getMeanbrlenmuts() {
		return meanbrlenmuts;
	}


	/**
	 * Sets the meanbrlenmuts value for this GARLIArguments.
	 * 
	 * @param meanbrlenmuts
	 */
	public void setMeanbrlenmuts(java.lang.Integer meanbrlenmuts) {
		this.meanbrlenmuts = meanbrlenmuts;
	}


	/**
	 * Gets the jobname value for this GARLIArguments.
	 * 
	 * @return jobname
	 */
	public java.lang.String getJobname() {
		return jobname;
	}


	/**
	 * Sets the jobname value for this GARLIArguments.
	 * 
	 * @param jobname
	 */
	public void setJobname(java.lang.String jobname) {
		this.jobname = jobname;
	}


	/**
	 * Gets the gammashapemodel value for this GARLIArguments.
	 * 
	 * @return gammashapemodel
	 */
	public java.lang.Integer getGammashapemodel() {
		return gammashapemodel;
	}


	/**
	 * Sets the gammashapemodel value for this GARLIArguments.
	 * 
	 * @param gammashapemodel
	 */
	public void setGammashapemodel(java.lang.Integer gammashapemodel) {
		this.gammashapemodel = gammashapemodel;
	}


	/**
	 * Gets the datatype value for this GARLIArguments.
	 * 
	 * @return datatype
	 */
	public java.lang.String getDatatype() {
		return datatype;
	}


	/**
	 * Sets the datatype value for this GARLIArguments.
	 * 
	 * @param datatype
	 */
	public void setDatatype(java.lang.String datatype) {
		this.datatype = datatype;
	}


	/**
	 * Gets the attachmentspertaxon value for this GARLIArguments.
	 * 
	 * @return attachmentspertaxon
	 */
	public java.lang.Integer getAttachmentspertaxon() {
		return attachmentspertaxon;
	}


	/**
	 * Sets the attachmentspertaxon value for this GARLIArguments.
	 * 
	 * @param attachmentspertaxon
	 */
	public void setAttachmentspertaxon(java.lang.Integer attachmentspertaxon) {
		this.attachmentspertaxon = attachmentspertaxon;
	}


	/**
	 * Gets the sharedFiles value for this GARLIArguments.
	 * 
	 * @return sharedFiles
	 */
	public java.lang.String[] getSharedFiles() {
		return sharedFiles;
	}


	/**
	 * Sets the sharedFiles value for this GARLIArguments.
	 * 
	 * @param sharedFiles
	 */
	public void setSharedFiles(java.lang.String[] sharedFiles) {
		this.sharedFiles = sharedFiles;
	}

	public java.lang.String getSharedFiles(int i) {
		return this.sharedFiles[i];
	}

	public void setSharedFiles(int i, java.lang.String _value) {
		this.sharedFiles[i] = _value;
	}


	/**
	 * Gets the perJobArguments value for this GARLIArguments.
	 * 
	 * @return perJobArguments
	 */
	public java.lang.String[] getPerJobArguments() {
		return perJobArguments;
	}


	/**
	 * Sets the perJobArguments value for this GARLIArguments.
	 * 
	 * @param perJobArguments
	 */
	public void setPerJobArguments(java.lang.String[] perJobArguments) {
		this.perJobArguments = perJobArguments;
	}

	public java.lang.String getPerJobArguments(int i) {
		return this.perJobArguments[i];
	}

	public void setPerJobArguments(int i, java.lang.String _value) {
		this.perJobArguments[i] = _value;
	}


	/**
	 * Gets the perJobFiles value for this GARLIArguments.
	 * 
	 * @return perJobFiles
	 */
	public java.lang.String[] getPerJobFiles() {
		return perJobFiles;
	}


	/**
	 * Sets the perJobFiles value for this GARLIArguments.
	 * 
	 * @param perJobFiles
	 */
	public void setPerJobFiles(java.lang.String[] perJobFiles) {
		this.perJobFiles = perJobFiles;
	}

	public java.lang.String getPerJobFiles(int i) {
		return this.perJobFiles[i];
	}

	public void setPerJobFiles(int i, java.lang.String _value) {
		this.perJobFiles[i] = _value;
	}


	/**
	 * Gets the symlinks value for this GARLIArguments.
	 * 
	 * @return symlinks
	 */
	public java.lang.String getSymlinks() {
		return symlinks;
	}


	/**
	 * Sets the symlinks value for this GARLIArguments.
	 * 
	 * @param symlinks
	 */
	public void setSymlinks(java.lang.String symlinks) {
		this.symlinks = symlinks;
	}


	/**
	 * Gets the inputFiles value for this GARLIArguments.
	 * 
	 * @return inputFiles
	 */
	public java.lang.String[] getInputFiles() {
		return inputFiles;
	}


	/**
	 * Sets the inputFiles value for this GARLIArguments.
	 * 
	 * @param inputFiles
	 */
	public void setInputFiles(java.lang.String[] inputFiles) {
		this.inputFiles = inputFiles;
	}

	public java.lang.String getInputFiles(int i) {
		return this.inputFiles[i];
	}

	public void setInputFiles(int i, java.lang.String _value) {
		this.inputFiles[i] = _value;
	}


	/**
	 * Gets the outputFiles value for this GARLIArguments.
	 * 
	 * @return outputFiles
	 */
	public java.lang.String[] getOutputFiles() {
		return outputFiles;
	}


	/**
	 * Sets the outputFiles value for this GARLIArguments.
	 * 
	 * @param outputFiles
	 */
	public void setOutputFiles(java.lang.String[] outputFiles) {
		this.outputFiles = outputFiles;
	}

	public java.lang.String getOutputFiles(int i) {
		return this.outputFiles[i];
	}

	public void setOutputFiles(int i, java.lang.String _value) {
		this.outputFiles[i] = _value;
	}


	/**
	 * Gets the owner value for this GARLIArguments.
	 * 
	 * @return owner
	 */
	public java.lang.String getOwner() {
		return owner;
	}


	/**
	 * Sets the owner value for this GARLIArguments.
	 * 
	 * @param owner
	 */
	public void setOwner(java.lang.String owner) {
		this.owner = owner;
	}


	/**
	 * Gets the schedulerOverride value for this GARLIArguments.
	 * 
	 * @return schedulerOverride
	 */
	public java.lang.String getSchedulerOverride() {
		return schedulerOverride;
	}


	/**
	 * Sets the schedulerOverride value for this GARLIArguments.
	 * 
	 * @param schedulerOverride
	 */
	public void setSchedulerOverride(java.lang.String schedulerOverride) {
		this.schedulerOverride = schedulerOverride;
	}


	/**
	 * Gets the appName value for this GARLIArguments.
	 * 
	 * @return appName
	 */
	public java.lang.String getAppName() {
		return appName;
	}


	/**
	 * Sets the appName value for this GARLIArguments.
	 * 
	 * @param appName
	 */
	public void setAppName(java.lang.String appName) {
		this.appName = appName;
	}


	/**
	 * Gets the jobName value for this GARLIArguments.
	 * 
	 * @return jobName
	 */
	public java.lang.String getJobName() {
		return jobName;
	}


	/**
	 * Sets the jobName value for this GARLIArguments.
	 * 
	 * @param jobName
	 */
	public void setJobName(java.lang.String jobName) {
		this.jobName = jobName;
	}


	/**
	 * Gets the jobID value for this GARLIArguments.
	 * 
	 * @return jobID
	 */
	public java.lang.String getjobID() {
		return jobID;
	}


	/**
	 * Sets the jobID value for this GARLIArguments.
	 * 
	 * @param jobID
	 */
	public void setjobID(java.lang.String jobID) {
		this.jobID = jobID;
	}


	/**
	 * Gets the workingDir value for this GARLIArguments.
	 * 
	 * @return workingDir
	 */
	public java.lang.String getWorkingDir() {
		return workingDir;
	}


	/**
	 * Sets the workingDir value for this GARLIArguments.
	 * 
	 * @param workingDir
	 */
	public void setWorkingDir(java.lang.String workingDir) {
		this.workingDir = workingDir;
	}

	private java.lang.Object __equalsCalc = null;
	public synchronized boolean equals(java.lang.Object obj) {
		if (!(obj instanceof GARLIArguments)) return false;
		GARLIArguments other = (GARLIArguments) obj;
		if (obj == null) return false;
		if (this == obj) return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true && 
				((this.replicates==null && other.getReplicates()==null) || 
				 (this.replicates!=null &&
				  this.replicates.equals(other.getReplicates()))) &&
				((this.ratematrix==null && other.getRatematrix()==null) || 
				 (this.ratematrix!=null &&
				  this.ratematrix.equals(other.getRatematrix()))) &&
				((this.outputphyliptree==null && other.getOutputphyliptree()==null) || 
				 (this.outputphyliptree!=null &&
				  this.outputphyliptree.equals(other.getOutputphyliptree()))) &&
				((this.randnniweight==null && other.getRandnniweight()==null) || 
				 (this.randnniweight!=null &&
				  this.randnniweight.equals(other.getRandnniweight()))) &&
				((this.ofprefix==null && other.getOfprefix()==null) || 
				 (this.ofprefix!=null &&
				  this.ofprefix.equals(other.getOfprefix()))) &&
				((this.limsprweight==null && other.getLimsprweight()==null) || 
				 (this.limsprweight!=null &&
				  this.limsprweight.equals(other.getLimsprweight()))) &&
				((this.numratecats==null && other.getNumratecats()==null) || 
				 (this.numratecats!=null &&
				  this.numratecats.equals(other.getNumratecats()))) &&
				((this.uniqueswapbias==null && other.getUniqueswapbias()==null) || 
				 (this.uniqueswapbias!=null &&
				  this.uniqueswapbias.equals(other.getUniqueswapbias()))) &&
				((this.treerejectionthreshold==null && other.getTreerejectionthreshold()==null) || 
				 (this.treerejectionthreshold!=null &&
				  this.treerejectionthreshold.equals(other.getTreerejectionthreshold()))) &&
				((this.numtaxa==null && other.getNumtaxa()==null) || 
				 (this.numtaxa!=null &&
				  this.numtaxa.equals(other.getNumtaxa()))) &&
				((this.modelsdata==null && other.getModelsdata()==null) || 
				 (this.modelsdata!=null &&
				  this.modelsdata.equals(other.getModelsdata()))) &&
				((this.startoptprec==null && other.getStartoptprec()==null) || 
				 (this.startoptprec!=null &&
				  this.startoptprec.equals(other.getStartoptprec()))) &&
				((this.optimizeinputonly==null && other.getOptimizeinputonly()==null) || 
				 (this.optimizeinputonly!=null &&
				  this.optimizeinputonly.equals(other.getOptimizeinputonly()))) &&
				((this.refinestart==null && other.getRefinestart()==null) || 
				 (this.refinestart!=null &&
				  this.refinestart.equals(other.getRefinestart()))) &&
				((this.subsetspecificrates==null && other.getSubsetspecificrates()==null) || 
				 (this.subsetspecificrates!=null &&
				  this.subsetspecificrates.equals(other.getSubsetspecificrates()))) &&
				((this.topoweight==null && other.getTopoweight()==null) || 
				 (this.topoweight!=null &&
				  this.topoweight.equals(other.getTopoweight()))) &&
				((this.constraintfile==null && other.getConstraintfile()==null) || 
				 (this.constraintfile!=null &&
				  this.constraintfile.equals(other.getConstraintfile()))) &&
				((this.replicates2==null && other.getReplicates2()==null) || 
				 (this.replicates2!=null &&
				  this.replicates2.equals(other.getReplicates2()))) &&
				((this.nindivs==null && other.getNindivs()==null) || 
				 (this.nindivs!=null &&
				  this.nindivs.equals(other.getNindivs()))) &&
				((this.streefname_userdata==null && other.getStreefname_userdata()==null) || 
				 (this.streefname_userdata!=null &&
				  this.streefname_userdata.equals(other.getStreefname_userdata()))) &&
				((this.profilingjob==null && other.getProfilingjob()==null) || 
				 (this.profilingjob!=null &&
				  this.profilingjob.equals(other.getProfilingjob()))) &&
				((this.analysistype==null && other.getAnalysistype()==null) || 
				 (this.analysistype!=null &&
				  this.analysistype.equals(other.getAnalysistype()))) &&
				((this.distanceswapbias==null && other.getDistanceswapbias()==null) || 
				 (this.distanceswapbias!=null &&
				  this.distanceswapbias.equals(other.getDistanceswapbias()))) &&
				((this.brlenweight==null && other.getBrlenweight()==null) || 
				 (this.brlenweight!=null &&
				  this.brlenweight.equals(other.getBrlenweight()))) &&
				((this.minoptprec==null && other.getMinoptprec()==null) || 
				 (this.minoptprec!=null &&
				  this.minoptprec.equals(other.getMinoptprec()))) &&
				((this.modweight==null && other.getModweight()==null) || 
				 (this.modweight!=null &&
				  this.modweight.equals(other.getModweight()))) &&
				((this.outgroup==null && other.getOutgroup()==null) || 
				 (this.outgroup!=null &&
				  this.outgroup.equals(other.getOutgroup()))) &&
				((this.numberofprecreductions==null && other.getNumberofprecreductions()==null) || 
				 (this.numberofprecreductions!=null &&
				  this.numberofprecreductions.equals(other.getNumberofprecreductions()))) &&
				((this.selectionintensity==null && other.getSelectionintensity()==null) || 
				 (this.selectionintensity!=null &&
				  this.selectionintensity.equals(other.getSelectionintensity()))) &&
				((this.outputsitelikelihoods==null && other.getOutputsitelikelihoods()==null) || 
				 (this.outputsitelikelihoods!=null &&
				  this.outputsitelikelihoods.equals(other.getOutputsitelikelihoods()))) &&
				((this.limsprrange==null && other.getLimsprrange()==null) || 
				 (this.limsprrange!=null &&
				  this.limsprrange.equals(other.getLimsprrange()))) &&
				((this.collapsebranches==null && other.getCollapsebranches()==null) || 
				 (this.collapsebranches!=null &&
				  this.collapsebranches.equals(other.getCollapsebranches()))) &&
				((this.geneticcode==null && other.getGeneticcode()==null) || 
				 (this.geneticcode!=null &&
				  this.geneticcode.equals(other.getGeneticcode()))) &&
				((this.configFile==null && other.getConfigFile()==null) || 
				 (this.configFile!=null &&
				  this.configFile.equals(other.getConfigFile()))) &&
				((this.gammashapebrlen==null && other.getGammashapebrlen()==null) || 
				 (this.gammashapebrlen!=null &&
				  this.gammashapebrlen.equals(other.getGammashapebrlen()))) &&
				((this.searchreps==null && other.getSearchreps()==null) || 
				 (this.searchreps!=null &&
				  this.searchreps.equals(other.getSearchreps()))) &&
				((this.randsprweight==null && other.getRandsprweight()==null) || 
				 (this.randsprweight!=null &&
				  this.randsprweight.equals(other.getRandsprweight()))) &&
				((this.novalidate==null && other.getNovalidate()==null) || 
				 (this.novalidate!=null &&
				  this.novalidate.equals(other.getNovalidate()))) &&
				((this.partitionsdata==null && other.getPartitionsdata()==null) || 
				 (this.partitionsdata!=null &&
				  this.partitionsdata.equals(other.getPartitionsdata()))) &&
				((this.inferinternalstateprobs==null && other.getInferinternalstateprobs()==null) || 
				 (this.inferinternalstateprobs!=null &&
				  this.inferinternalstateprobs.equals(other.getInferinternalstateprobs()))) &&
				((this.streefname==null && other.getStreefname()==null) || 
				 (this.streefname!=null &&
				  this.streefname.equals(other.getStreefname()))) &&
				((this.linkmodels==null && other.getLinkmodels()==null) || 
				 (this.linkmodels!=null &&
				  this.linkmodels.equals(other.getLinkmodels()))) &&
				((this.uniquepatterns==null && other.getUniquepatterns()==null) || 
				 (this.uniquepatterns!=null &&
				  this.uniquepatterns.equals(other.getUniquepatterns()))) &&
				((this.invariantsites==null && other.getInvariantsites()==null) || 
				 (this.invariantsites!=null &&
				  this.invariantsites.equals(other.getInvariantsites()))) &&
				((this.datafname==null && other.getDatafname()==null) || 
				 (this.datafname!=null &&
				  this.datafname.equals(other.getDatafname()))) &&
				((this.actualmemory==null && other.getActualmemory()==null) || 
				 (this.actualmemory!=null &&
				  this.actualmemory.equals(other.getActualmemory()))) &&
				((this.ratehetmodel==null && other.getRatehetmodel()==null) || 
				 (this.ratehetmodel!=null &&
				  this.ratehetmodel.equals(other.getRatehetmodel()))) &&
				((this.resampleproportion==null && other.getResampleproportion()==null) || 
				 (this.resampleproportion!=null &&
				  this.resampleproportion.equals(other.getResampleproportion()))) &&
				((this.statefrequencies==null && other.getStatefrequencies()==null) || 
				 (this.statefrequencies!=null &&
				  this.statefrequencies.equals(other.getStatefrequencies()))) &&
				((this.meanbrlenmuts==null && other.getMeanbrlenmuts()==null) || 
				 (this.meanbrlenmuts!=null &&
				  this.meanbrlenmuts.equals(other.getMeanbrlenmuts()))) &&
				((this.jobname==null && other.getJobname()==null) || 
				 (this.jobname!=null &&
				  this.jobname.equals(other.getJobname()))) &&
				((this.gammashapemodel==null && other.getGammashapemodel()==null) || 
				 (this.gammashapemodel!=null &&
				  this.gammashapemodel.equals(other.getGammashapemodel()))) &&
				((this.datatype==null && other.getDatatype()==null) || 
				 (this.datatype!=null &&
				  this.datatype.equals(other.getDatatype()))) &&
				((this.attachmentspertaxon==null && other.getAttachmentspertaxon()==null) || 
				 (this.attachmentspertaxon!=null &&
				  this.attachmentspertaxon.equals(other.getAttachmentspertaxon()))) &&
				((this.sharedFiles==null && other.getSharedFiles()==null) || 
				 (this.sharedFiles!=null &&
				  java.util.Arrays.equals(this.sharedFiles, other.getSharedFiles()))) &&
				((this.perJobArguments==null && other.getPerJobArguments()==null) || 
				 (this.perJobArguments!=null &&
				  java.util.Arrays.equals(this.perJobArguments, other.getPerJobArguments()))) &&
				((this.perJobFiles==null && other.getPerJobFiles()==null) || 
				 (this.perJobFiles!=null &&
				  java.util.Arrays.equals(this.perJobFiles, other.getPerJobFiles()))) &&
				((this.symlinks==null && other.getSymlinks()==null) || 
				 (this.symlinks!=null &&
				  this.symlinks.equals(other.getSymlinks()))) &&
				((this.inputFiles==null && other.getInputFiles()==null) || 
				 (this.inputFiles!=null &&
				  java.util.Arrays.equals(this.inputFiles, other.getInputFiles()))) &&
				((this.outputFiles==null && other.getOutputFiles()==null) || 
				 (this.outputFiles!=null &&
				  java.util.Arrays.equals(this.outputFiles, other.getOutputFiles()))) &&
				((this.owner==null && other.getOwner()==null) || 
				 (this.owner!=null &&
				  this.owner.equals(other.getOwner()))) &&
				((this.schedulerOverride==null && other.getSchedulerOverride()==null) || 
				 (this.schedulerOverride!=null &&
				  this.schedulerOverride.equals(other.getSchedulerOverride()))) &&
				((this.appName==null && other.getAppName()==null) || 
				 (this.appName!=null &&
				  this.appName.equals(other.getAppName()))) &&
				((this.jobName==null && other.getJobName()==null) || 
				 (this.jobName!=null &&
				  this.jobName.equals(other.getJobName()))) &&
				((this.jobID==null && other.getJobID()==null) ||  // Added for GT6.
				 (this.jobID!=null &&
				  this.jobID.equals(other.getJobID()))) &&
				((this.workingDir==null && other.getWorkingDir()==null) || 
				 (this.workingDir!=null &&
				  this.workingDir.equals(other.getWorkingDir())));
		__equalsCalc = null;
		return _equals;
	}

	private boolean __hashCodeCalc = false;
	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1;
		if (getReplicates() != null) {
			_hashCode += getReplicates().hashCode();
		}
		if (getRatematrix() != null) {
			_hashCode += getRatematrix().hashCode();
		}
		if (getOutputphyliptree() != null) {
			_hashCode += getOutputphyliptree().hashCode();
		}
		if (getRandnniweight() != null) {
			_hashCode += getRandnniweight().hashCode();
		}
		if (getOfprefix() != null) {
			_hashCode += getOfprefix().hashCode();
		}
		if (getLimsprweight() != null) {
			_hashCode += getLimsprweight().hashCode();
		}
		if (getNumratecats() != null) {
			_hashCode += getNumratecats().hashCode();
		}
		if (getUniqueswapbias() != null) {
			_hashCode += getUniqueswapbias().hashCode();
		}
		if (getTreerejectionthreshold() != null) {
			_hashCode += getTreerejectionthreshold().hashCode();
		}
		if (getNumtaxa() != null) {
			_hashCode += getNumtaxa().hashCode();
		}
		if (getModelsdata() != null) {
			_hashCode += getModelsdata().hashCode();
		}
		if (getStartoptprec() != null) {
			_hashCode += getStartoptprec().hashCode();
		}
		if (getOptimizeinputonly() != null) {
			_hashCode += getOptimizeinputonly().hashCode();
		}
		if (getRefinestart() != null) {
			_hashCode += getRefinestart().hashCode();
		}
		if (getSubsetspecificrates() != null) {
			_hashCode += getSubsetspecificrates().hashCode();
		}
		if (getTopoweight() != null) {
			_hashCode += getTopoweight().hashCode();
		}
		if (getConstraintfile() != null) {
			_hashCode += getConstraintfile().hashCode();
		}
		if (getReplicates2() != null) {
			_hashCode += getReplicates2().hashCode();
		}
		if (getNindivs() != null) {
			_hashCode += getNindivs().hashCode();
		}
		if (getStreefname_userdata() != null) {
			_hashCode += getStreefname_userdata().hashCode();
		}
		if (getProfilingjob() != null) {
			_hashCode += getProfilingjob().hashCode();
		}
		if (getAnalysistype() != null) {
			_hashCode += getAnalysistype().hashCode();
		}
		if (getDistanceswapbias() != null) {
			_hashCode += getDistanceswapbias().hashCode();
		}
		if (getBrlenweight() != null) {
			_hashCode += getBrlenweight().hashCode();
		}
		if (getMinoptprec() != null) {
			_hashCode += getMinoptprec().hashCode();
		}
		if (getModweight() != null) {
			_hashCode += getModweight().hashCode();
		}
		if (getOutgroup() != null) {
			_hashCode += getOutgroup().hashCode();
		}
		if (getNumberofprecreductions() != null) {
			_hashCode += getNumberofprecreductions().hashCode();
		}
		if (getSelectionintensity() != null) {
			_hashCode += getSelectionintensity().hashCode();
		}
		if (getOutputsitelikelihoods() != null) {
			_hashCode += getOutputsitelikelihoods().hashCode();
		}
		if (getLimsprrange() != null) {
			_hashCode += getLimsprrange().hashCode();
		}
		if (getCollapsebranches() != null) {
			_hashCode += getCollapsebranches().hashCode();
		}
		if (getGeneticcode() != null) {
			_hashCode += getGeneticcode().hashCode();
		}
		if (getConfigFile() != null) {
			_hashCode += getConfigFile().hashCode();
		}
		if (getGammashapebrlen() != null) {
			_hashCode += getGammashapebrlen().hashCode();
		}
		if (getSearchreps() != null) {
			_hashCode += getSearchreps().hashCode();
		}
		if (getRandsprweight() != null) {
			_hashCode += getRandsprweight().hashCode();
		}
		if (getNovalidate() != null) {
			_hashCode += getNovalidate().hashCode();
		}
		if (getPartitionsdata() != null) {
			_hashCode += getPartitionsdata().hashCode();
		}
		if (getInferinternalstateprobs() != null) {
			_hashCode += getInferinternalstateprobs().hashCode();
		}
		if (getStreefname() != null) {
			_hashCode += getStreefname().hashCode();
		}
		if (getLinkmodels() != null) {
			_hashCode += getLinkmodels().hashCode();
		}
		if (getUniquepatterns() != null) {
			_hashCode += getUniquepatterns().hashCode();
		}
		if (getInvariantsites() != null) {
			_hashCode += getInvariantsites().hashCode();
		}
		if (getDatafname() != null) {
			_hashCode += getDatafname().hashCode();
		}
		if (getActualmemory() != null) {
			_hashCode += getActualmemory().hashCode();
		}
		if (getRatehetmodel() != null) {
			_hashCode += getRatehetmodel().hashCode();
		}
		if (getResampleproportion() != null) {
			_hashCode += getResampleproportion().hashCode();
		}
		if (getStatefrequencies() != null) {
			_hashCode += getStatefrequencies().hashCode();
		}
		if (getMeanbrlenmuts() != null) {
			_hashCode += getMeanbrlenmuts().hashCode();
		}
		if (getJobname() != null) {
			_hashCode += getJobname().hashCode();
		}
		if (getGammashapemodel() != null) {
			_hashCode += getGammashapemodel().hashCode();
		}
		if (getDatatype() != null) {
			_hashCode += getDatatype().hashCode();
		}
		if (getAttachmentspertaxon() != null) {
			_hashCode += getAttachmentspertaxon().hashCode();
		}
		if (getSharedFiles() != null) {
			for (int i=0;
					i<java.lang.reflect.Array.getLength(getSharedFiles());
					i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getSharedFiles(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getPerJobArguments() != null) {
			for (int i=0;
					i<java.lang.reflect.Array.getLength(getPerJobArguments());
					i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getPerJobArguments(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getPerJobFiles() != null) {
			for (int i=0;
					i<java.lang.reflect.Array.getLength(getPerJobFiles());
					i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getPerJobFiles(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getSymlinks() != null) {
			_hashCode += getSymlinks().hashCode();
		}
		if (getInputFiles() != null) {
			for (int i=0;
					i<java.lang.reflect.Array.getLength(getInputFiles());
					i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getInputFiles(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getOutputFiles() != null) {
			for (int i=0;
					i<java.lang.reflect.Array.getLength(getOutputFiles());
					i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getOutputFiles(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getOwner() != null) {
			_hashCode += getOwner().hashCode();
		}
		if (getSchedulerOverride() != null) {
			_hashCode += getSchedulerOverride().hashCode();
		}
		if (getAppName() != null) {
			_hashCode += getAppName().hashCode();
		}
		if (getJobName() != null) {
			_hashCode += getJobName().hashCode();
		}
		if (getJobID() != null) {
			_hashCode += getJobID().hashCode();
		}
		if (getWorkingDir() != null) {
			_hashCode += getWorkingDir().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc =
	new org.apache.axis.description.TypeDesc(GARLIArguments.class, true);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.umd.edu/namespaces/grid/bio/GARLIService", "GARLIArguments"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("replicates");
		elemField.setXmlName(new javax.xml.namespace.QName("", "replicates"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("ratematrix");
		elemField.setXmlName(new javax.xml.namespace.QName("", "ratematrix"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("outputphyliptree");
		elemField.setXmlName(new javax.xml.namespace.QName("", "outputphyliptree"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("randnniweight");
		elemField.setXmlName(new javax.xml.namespace.QName("", "randnniweight"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("ofprefix");
		elemField.setXmlName(new javax.xml.namespace.QName("", "ofprefix"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("limsprweight");
		elemField.setXmlName(new javax.xml.namespace.QName("", "limsprweight"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("numratecats");
		elemField.setXmlName(new javax.xml.namespace.QName("", "numratecats"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("uniqueswapbias");
		elemField.setXmlName(new javax.xml.namespace.QName("", "uniqueswapbias"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("treerejectionthreshold");
		elemField.setXmlName(new javax.xml.namespace.QName("", "treerejectionthreshold"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("numtaxa");
		elemField.setXmlName(new javax.xml.namespace.QName("", "numtaxa"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("modelsdata");
		elemField.setXmlName(new javax.xml.namespace.QName("", "modelsdata"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("startoptprec");
		elemField.setXmlName(new javax.xml.namespace.QName("", "startoptprec"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("optimizeinputonly");
		elemField.setXmlName(new javax.xml.namespace.QName("", "optimizeinputonly"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("refinestart");
		elemField.setXmlName(new javax.xml.namespace.QName("", "refinestart"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("subsetspecificrates");
		elemField.setXmlName(new javax.xml.namespace.QName("", "subsetspecificrates"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("topoweight");
		elemField.setXmlName(new javax.xml.namespace.QName("", "topoweight"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("constraintfile");
		elemField.setXmlName(new javax.xml.namespace.QName("", "constraintfile"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("replicates");
		elemField.setXmlName(new javax.xml.namespace.QName("", "replicates"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("nindivs");
		elemField.setXmlName(new javax.xml.namespace.QName("", "nindivs"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("streefname_userdata");
		elemField.setXmlName(new javax.xml.namespace.QName("", "streefname_userdata"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("profilingjob");
		elemField.setXmlName(new javax.xml.namespace.QName("", "profilingjob"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("analysistype");
		elemField.setXmlName(new javax.xml.namespace.QName("", "analysistype"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("distanceswapbias");
		elemField.setXmlName(new javax.xml.namespace.QName("", "distanceswapbias"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("brlenweight");
		elemField.setXmlName(new javax.xml.namespace.QName("", "brlenweight"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("minoptprec");
		elemField.setXmlName(new javax.xml.namespace.QName("", "minoptprec"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("modweight");
		elemField.setXmlName(new javax.xml.namespace.QName("", "modweight"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("outgroup");
		elemField.setXmlName(new javax.xml.namespace.QName("", "outgroup"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("numberofprecreductions");
		elemField.setXmlName(new javax.xml.namespace.QName("", "numberofprecreductions"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("selectionintensity");
		elemField.setXmlName(new javax.xml.namespace.QName("", "selectionintensity"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("outputsitelikelihoods");
		elemField.setXmlName(new javax.xml.namespace.QName("", "outputsitelikelihoods"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("limsprrange");
		elemField.setXmlName(new javax.xml.namespace.QName("", "limsprrange"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("collapsebranches");
		elemField.setXmlName(new javax.xml.namespace.QName("", "collapsebranches"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("geneticcode");
		elemField.setXmlName(new javax.xml.namespace.QName("", "geneticcode"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("configFile");
		elemField.setXmlName(new javax.xml.namespace.QName("", "configFile"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("gammashapebrlen");
		elemField.setXmlName(new javax.xml.namespace.QName("", "gammashapebrlen"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("searchreps");
		elemField.setXmlName(new javax.xml.namespace.QName("", "searchreps"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("randsprweight");
		elemField.setXmlName(new javax.xml.namespace.QName("", "randsprweight"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("novalidate");
		elemField.setXmlName(new javax.xml.namespace.QName("", "novalidate"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("partitionsdata");
		elemField.setXmlName(new javax.xml.namespace.QName("", "partitionsdata"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("inferinternalstateprobs");
		elemField.setXmlName(new javax.xml.namespace.QName("", "inferinternalstateprobs"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("streefname");
		elemField.setXmlName(new javax.xml.namespace.QName("", "streefname"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("linkmodels");
		elemField.setXmlName(new javax.xml.namespace.QName("", "linkmodels"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("uniquepatterns");
		elemField.setXmlName(new javax.xml.namespace.QName("", "uniquepatterns"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("invariantsites");
		elemField.setXmlName(new javax.xml.namespace.QName("", "invariantsites"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("datafname");
		elemField.setXmlName(new javax.xml.namespace.QName("", "datafname"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("actualmemory");
		elemField.setXmlName(new javax.xml.namespace.QName("", "actualmemory"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("ratehetmodel");
		elemField.setXmlName(new javax.xml.namespace.QName("", "ratehetmodel"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("resampleproportion");
		elemField.setXmlName(new javax.xml.namespace.QName("", "resampleproportion"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("statefrequencies");
		elemField.setXmlName(new javax.xml.namespace.QName("", "statefrequencies"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("meanbrlenmuts");
		elemField.setXmlName(new javax.xml.namespace.QName("", "meanbrlenmuts"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("jobname");
		elemField.setXmlName(new javax.xml.namespace.QName("", "jobname"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("gammashapemodel");
		elemField.setXmlName(new javax.xml.namespace.QName("", "gammashapemodel"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("datatype");
		elemField.setXmlName(new javax.xml.namespace.QName("", "datatype"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("attachmentspertaxon");
		elemField.setXmlName(new javax.xml.namespace.QName("", "attachmentspertaxon"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("sharedFiles");
		elemField.setXmlName(new javax.xml.namespace.QName("", "sharedFiles"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("perJobArguments");
		elemField.setXmlName(new javax.xml.namespace.QName("", "perJobArguments"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("perJobFiles");
		elemField.setXmlName(new javax.xml.namespace.QName("", "perJobFiles"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("symlinks");
		elemField.setXmlName(new javax.xml.namespace.QName("", "symlinks"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("inputFiles");
		elemField.setXmlName(new javax.xml.namespace.QName("", "inputFiles"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("outputFiles");
		elemField.setXmlName(new javax.xml.namespace.QName("", "outputFiles"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setMinOccurs(0);
		elemField.setNillable(false);
		elemField.setMaxOccursUnbounded(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("owner");
		elemField.setXmlName(new javax.xml.namespace.QName("", "owner"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("schedulerOverride");
		elemField.setXmlName(new javax.xml.namespace.QName("", "schedulerOverride"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("appName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "appName"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("jobName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "jobName"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("jobID");
		elemField.setXmlName(new javax.xml.namespace.QName("", "jobID"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("workingDir");
		elemField.setXmlName(new javax.xml.namespace.QName("", "workingDir"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc() {
		return typeDesc;
	}

	/**
	 * Get Custom Serializer
	 */
	public static org.apache.axis.encoding.Serializer getSerializer(
			java.lang.String mechType, 
			java.lang.Class _javaType,  
			javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanSerializer(
				_javaType, _xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(
			java.lang.String mechType, 
			java.lang.Class _javaType,  
			javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanDeserializer(
				_javaType, _xmlType, typeDesc);
	}
<<<<<<< HEAD
>>>>>>> a10ad43c27f9fe4ae9b295e0f6d35188164f953a
=======
>>>>>>> a10ad43c27f9fe4ae9b295e0f6d35188164f953a

}
