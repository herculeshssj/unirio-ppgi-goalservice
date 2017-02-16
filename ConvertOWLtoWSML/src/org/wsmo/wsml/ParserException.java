/**
 * ParserException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.wsmo.wsml;

public class ParserException  extends org.apache.axis.AxisFault  implements java.io.Serializable {
    private int errorLine;

    private int errorPos;

    private java.lang.String expectedToken;

    private java.lang.String foundToken;

    private java.lang.String message1;

    public ParserException() {
    }

    public ParserException(
           int errorLine,
           int errorPos,
           java.lang.String expectedToken,
           java.lang.String foundToken,
           java.lang.String message1) {
        this.errorLine = errorLine;
        this.errorPos = errorPos;
        this.expectedToken = expectedToken;
        this.foundToken = foundToken;
        this.message1 = message1;
    }


    /**
     * Gets the errorLine value for this ParserException.
     * 
     * @return errorLine
     */
    public int getErrorLine() {
        return errorLine;
    }


    /**
     * Sets the errorLine value for this ParserException.
     * 
     * @param errorLine
     */
    public void setErrorLine(int errorLine) {
        this.errorLine = errorLine;
    }


    /**
     * Gets the errorPos value for this ParserException.
     * 
     * @return errorPos
     */
    public int getErrorPos() {
        return errorPos;
    }


    /**
     * Sets the errorPos value for this ParserException.
     * 
     * @param errorPos
     */
    public void setErrorPos(int errorPos) {
        this.errorPos = errorPos;
    }


    /**
     * Gets the expectedToken value for this ParserException.
     * 
     * @return expectedToken
     */
    public java.lang.String getExpectedToken() {
        return expectedToken;
    }


    /**
     * Sets the expectedToken value for this ParserException.
     * 
     * @param expectedToken
     */
    public void setExpectedToken(java.lang.String expectedToken) {
        this.expectedToken = expectedToken;
    }


    /**
     * Gets the foundToken value for this ParserException.
     * 
     * @return foundToken
     */
    public java.lang.String getFoundToken() {
        return foundToken;
    }


    /**
     * Sets the foundToken value for this ParserException.
     * 
     * @param foundToken
     */
    public void setFoundToken(java.lang.String foundToken) {
        this.foundToken = foundToken;
    }


    /**
     * Gets the message1 value for this ParserException.
     * 
     * @return message1
     */
    public java.lang.String getMessage1() {
        return message1;
    }


    /**
     * Sets the message1 value for this ParserException.
     * 
     * @param message1
     */
    public void setMessage1(java.lang.String message1) {
        this.message1 = message1;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ParserException)) return false;
        ParserException other = (ParserException) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.errorLine == other.getErrorLine() &&
            this.errorPos == other.getErrorPos() &&
            ((this.expectedToken==null && other.getExpectedToken()==null) || 
             (this.expectedToken!=null &&
              this.expectedToken.equals(other.getExpectedToken()))) &&
            ((this.foundToken==null && other.getFoundToken()==null) || 
             (this.foundToken!=null &&
              this.foundToken.equals(other.getFoundToken()))) &&
            ((this.message1==null && other.getMessage1()==null) || 
             (this.message1!=null &&
              this.message1.equals(other.getMessage1())));
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
        _hashCode += getErrorLine();
        _hashCode += getErrorPos();
        if (getExpectedToken() != null) {
            _hashCode += getExpectedToken().hashCode();
        }
        if (getFoundToken() != null) {
            _hashCode += getFoundToken().hashCode();
        }
        if (getMessage1() != null) {
            _hashCode += getMessage1().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ParserException.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://wsml.wsmo.org", "ParserException"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorLine");
        elemField.setXmlName(new javax.xml.namespace.QName("", "errorLine"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorPos");
        elemField.setXmlName(new javax.xml.namespace.QName("", "errorPos"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("expectedToken");
        elemField.setXmlName(new javax.xml.namespace.QName("", "expectedToken"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("foundToken");
        elemField.setXmlName(new javax.xml.namespace.QName("", "foundToken"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("message1");
        elemField.setXmlName(new javax.xml.namespace.QName("", "message"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
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


    /**
     * Writes the exception data to the faultDetails
     */
    public void writeDetails(javax.xml.namespace.QName qname, org.apache.axis.encoding.SerializationContext context) throws java.io.IOException {
        context.serialize(qname, null, this);
    }
}
