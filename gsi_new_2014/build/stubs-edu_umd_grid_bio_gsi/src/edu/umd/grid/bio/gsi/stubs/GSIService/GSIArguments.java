/**
 * GSIArguments.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Mar 01, 2007 (10:42:15 CST) WSDL2Java emitter.
 */

package edu.umd.grid.bio.gsi.stubs.GSIService;

public class GSIArguments  implements java.io.Serializable {
    private java.lang.Integer replicates;

    private java.lang.Integer numPerms;

    private java.lang.Integer treeNumber;

    private java.lang.String outputFile;

    private java.lang.String assignmentFile;

    private java.lang.Integer replicates2;

    private java.lang.String treeFile;

    private java.lang.String jobname;

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

    private java.lang.String workingDir;

    public GSIArguments() {
    }

    public GSIArguments(
           java.lang.Integer replicates,
           java.lang.Integer numPerms,
           java.lang.Integer treeNumber,
           java.lang.String outputFile,
           java.lang.String assignmentFile,
           java.lang.Integer replicates2,
           java.lang.String treeFile,
           java.lang.String jobname,
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
           java.lang.String workingDir) {
           this.replicates = replicates;
           this.numPerms = numPerms;
           this.treeNumber = treeNumber;
           this.outputFile = outputFile;
           this.assignmentFile = assignmentFile;
           this.replicates2 = replicates2;
           this.treeFile = treeFile;
           this.jobname = jobname;
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
           this.workingDir = workingDir;
    }


    /**
     * Gets the replicates value for this GSIArguments.
     * 
     * @return replicates
     */
    public java.lang.Integer getReplicates() {
        return replicates;
    }


    /**
     * Sets the replicates value for this GSIArguments.
     * 
     * @param replicates
     */
    public void setReplicates(java.lang.Integer replicates) {
        this.replicates = replicates;
    }


    /**
     * Gets the numPerms value for this GSIArguments.
     * 
     * @return numPerms
     */
    public java.lang.Integer getNumPerms() {
        return numPerms;
    }


    /**
     * Sets the numPerms value for this GSIArguments.
     * 
     * @param numPerms
     */
    public void setNumPerms(java.lang.Integer numPerms) {
        this.numPerms = numPerms;
    }


    /**
     * Gets the treeNumber value for this GSIArguments.
     * 
     * @return treeNumber
     */
    public java.lang.Integer getTreeNumber() {
        return treeNumber;
    }


    /**
     * Sets the treeNumber value for this GSIArguments.
     * 
     * @param treeNumber
     */
    public void setTreeNumber(java.lang.Integer treeNumber) {
        this.treeNumber = treeNumber;
    }


    /**
     * Gets the outputFile value for this GSIArguments.
     * 
     * @return outputFile
     */
    public java.lang.String getOutputFile() {
        return outputFile;
    }


    /**
     * Sets the outputFile value for this GSIArguments.
     * 
     * @param outputFile
     */
    public void setOutputFile(java.lang.String outputFile) {
        this.outputFile = outputFile;
    }


    /**
     * Gets the assignmentFile value for this GSIArguments.
     * 
     * @return assignmentFile
     */
    public java.lang.String getAssignmentFile() {
        return assignmentFile;
    }


    /**
     * Sets the assignmentFile value for this GSIArguments.
     * 
     * @param assignmentFile
     */
    public void setAssignmentFile(java.lang.String assignmentFile) {
        this.assignmentFile = assignmentFile;
    }


    /**
     * Gets the replicates2 value for this GSIArguments.
     * 
     * @return replicates2
     */
    public java.lang.Integer getReplicates2() {
        return replicates2;
    }


    /**
     * Sets the replicates2 value for this GSIArguments.
     * 
     * @param replicates2
     */
    public void setReplicates2(java.lang.Integer replicates2) {
        this.replicates2 = replicates2;
    }


    /**
     * Gets the treeFile value for this GSIArguments.
     * 
     * @return treeFile
     */
    public java.lang.String getTreeFile() {
        return treeFile;
    }


    /**
     * Sets the treeFile value for this GSIArguments.
     * 
     * @param treeFile
     */
    public void setTreeFile(java.lang.String treeFile) {
        this.treeFile = treeFile;
    }


    /**
     * Gets the jobname value for this GSIArguments.
     * 
     * @return jobname
     */
    public java.lang.String getJobname() {
        return jobname;
    }


    /**
     * Sets the jobname value for this GSIArguments.
     * 
     * @param jobname
     */
    public void setJobname(java.lang.String jobname) {
        this.jobname = jobname;
    }


    /**
     * Gets the sharedFiles value for this GSIArguments.
     * 
     * @return sharedFiles
     */
    public java.lang.String[] getSharedFiles() {
        return sharedFiles;
    }


    /**
     * Sets the sharedFiles value for this GSIArguments.
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
     * Gets the perJobArguments value for this GSIArguments.
     * 
     * @return perJobArguments
     */
    public java.lang.String[] getPerJobArguments() {
        return perJobArguments;
    }


    /**
     * Sets the perJobArguments value for this GSIArguments.
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
     * Gets the perJobFiles value for this GSIArguments.
     * 
     * @return perJobFiles
     */
    public java.lang.String[] getPerJobFiles() {
        return perJobFiles;
    }


    /**
     * Sets the perJobFiles value for this GSIArguments.
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
     * Gets the symlinks value for this GSIArguments.
     * 
     * @return symlinks
     */
    public java.lang.String getSymlinks() {
        return symlinks;
    }


    /**
     * Sets the symlinks value for this GSIArguments.
     * 
     * @param symlinks
     */
    public void setSymlinks(java.lang.String symlinks) {
        this.symlinks = symlinks;
    }


    /**
     * Gets the inputFiles value for this GSIArguments.
     * 
     * @return inputFiles
     */
    public java.lang.String[] getInputFiles() {
        return inputFiles;
    }


    /**
     * Sets the inputFiles value for this GSIArguments.
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
     * Gets the outputFiles value for this GSIArguments.
     * 
     * @return outputFiles
     */
    public java.lang.String[] getOutputFiles() {
        return outputFiles;
    }


    /**
     * Sets the outputFiles value for this GSIArguments.
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
     * Gets the owner value for this GSIArguments.
     * 
     * @return owner
     */
    public java.lang.String getOwner() {
        return owner;
    }


    /**
     * Sets the owner value for this GSIArguments.
     * 
     * @param owner
     */
    public void setOwner(java.lang.String owner) {
        this.owner = owner;
    }


    /**
     * Gets the schedulerOverride value for this GSIArguments.
     * 
     * @return schedulerOverride
     */
    public java.lang.String getSchedulerOverride() {
        return schedulerOverride;
    }


    /**
     * Sets the schedulerOverride value for this GSIArguments.
     * 
     * @param schedulerOverride
     */
    public void setSchedulerOverride(java.lang.String schedulerOverride) {
        this.schedulerOverride = schedulerOverride;
    }


    /**
     * Gets the appName value for this GSIArguments.
     * 
     * @return appName
     */
    public java.lang.String getAppName() {
        return appName;
    }


    /**
     * Sets the appName value for this GSIArguments.
     * 
     * @param appName
     */
    public void setAppName(java.lang.String appName) {
        this.appName = appName;
    }


    /**
     * Gets the jobName value for this GSIArguments.
     * 
     * @return jobName
     */
    public java.lang.String getJobName() {
        return jobName;
    }


    /**
     * Sets the jobName value for this GSIArguments.
     * 
     * @param jobName
     */
    public void setJobName(java.lang.String jobName) {
        this.jobName = jobName;
    }


    /**
     * Gets the workingDir value for this GSIArguments.
     * 
     * @return workingDir
     */
    public java.lang.String getWorkingDir() {
        return workingDir;
    }


    /**
     * Sets the workingDir value for this GSIArguments.
     * 
     * @param workingDir
     */
    public void setWorkingDir(java.lang.String workingDir) {
        this.workingDir = workingDir;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GSIArguments)) return false;
        GSIArguments other = (GSIArguments) obj;
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
            ((this.numPerms==null && other.getNumPerms()==null) || 
             (this.numPerms!=null &&
              this.numPerms.equals(other.getNumPerms()))) &&
            ((this.treeNumber==null && other.getTreeNumber()==null) || 
             (this.treeNumber!=null &&
              this.treeNumber.equals(other.getTreeNumber()))) &&
            ((this.outputFile==null && other.getOutputFile()==null) || 
             (this.outputFile!=null &&
              this.outputFile.equals(other.getOutputFile()))) &&
            ((this.assignmentFile==null && other.getAssignmentFile()==null) || 
             (this.assignmentFile!=null &&
              this.assignmentFile.equals(other.getAssignmentFile()))) &&
            ((this.replicates2==null && other.getReplicates2()==null) || 
             (this.replicates2!=null &&
              this.replicates2.equals(other.getReplicates2()))) &&
            ((this.treeFile==null && other.getTreeFile()==null) || 
             (this.treeFile!=null &&
              this.treeFile.equals(other.getTreeFile()))) &&
            ((this.jobname==null && other.getJobname()==null) || 
             (this.jobname!=null &&
              this.jobname.equals(other.getJobname()))) &&
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
        if (getNumPerms() != null) {
            _hashCode += getNumPerms().hashCode();
        }
        if (getTreeNumber() != null) {
            _hashCode += getTreeNumber().hashCode();
        }
        if (getOutputFile() != null) {
            _hashCode += getOutputFile().hashCode();
        }
        if (getAssignmentFile() != null) {
            _hashCode += getAssignmentFile().hashCode();
        }
        if (getReplicates2() != null) {
            _hashCode += getReplicates2().hashCode();
        }
        if (getTreeFile() != null) {
            _hashCode += getTreeFile().hashCode();
        }
        if (getJobname() != null) {
            _hashCode += getJobname().hashCode();
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
        new org.apache.axis.description.TypeDesc(GSIArguments.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.umd.edu/namespaces/grid/bio/GSIService", "GSIArguments"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("replicates");
        elemField.setXmlName(new javax.xml.namespace.QName("", "replicates"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numPerms");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numPerms"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("treeNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("", "treeNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("outputFile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "outputFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("assignmentFile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "assignmentFile"));
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
        elemField.setFieldName("treeFile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "treeFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobname");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jobname"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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

}
