/**
 * CreateResourceResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Mar 01, 2007 (10:42:15 CST) WSDL2Java emitter.
 */

package edu.umd.umiacs.cummings.GSBL.GT42GSBLFactoryService_wsdl;

public class CreateResourceResponse  implements java.io.Serializable {
    private org.w3.www._2005._08.addressing.EndpointReferenceType endpointReference;

    public CreateResourceResponse() {
    }

    public CreateResourceResponse(
           org.w3.www._2005._08.addressing.EndpointReferenceType endpointReference) {
           this.endpointReference = endpointReference;
    }


    /**
     * Gets the endpointReference value for this CreateResourceResponse.
     * 
     * @return endpointReference
     */
    public org.w3.www._2005._08.addressing.EndpointReferenceType getEndpointReference() {
        return endpointReference;
    }


    /**
     * Sets the endpointReference value for this CreateResourceResponse.
     * 
     * @param endpointReference
     */
    public void setEndpointReference(org.w3.www._2005._08.addressing.EndpointReferenceType endpointReference) {
        this.endpointReference = endpointReference;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateResourceResponse)) return false;
        CreateResourceResponse other = (CreateResourceResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.endpointReference==null && other.getEndpointReference()==null) || 
             (this.endpointReference!=null &&
              this.endpointReference.equals(other.getEndpointReference())));
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
        if (getEndpointReference() != null) {
            _hashCode += getEndpointReference().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CreateResourceResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://cummings.umiacs.umd.edu/GSBL/GT42GSBLFactoryService.wsdl", ">createResourceResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("endpointReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
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
