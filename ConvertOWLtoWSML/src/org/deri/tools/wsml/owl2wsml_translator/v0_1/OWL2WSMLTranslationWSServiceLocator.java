/**
 * OWL2WSMLTranslationWSServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.deri.tools.wsml.owl2wsml_translator.v0_1;

public class OWL2WSMLTranslationWSServiceLocator extends org.apache.axis.client.Service implements org.deri.tools.wsml.owl2wsml_translator.v0_1.OWL2WSMLTranslationWSService {

    public OWL2WSMLTranslationWSServiceLocator() {
    }


    public OWL2WSMLTranslationWSServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public OWL2WSMLTranslationWSServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for owl2wsmlTranslation
    private java.lang.String owl2wsmlTranslation_address = "http://tools.deri.org/wsml/owl2wsml-translator/v0.1/services/owl2wsmlTranslation";

    public java.lang.String getowl2wsmlTranslationAddress() {
        return owl2wsmlTranslation_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String owl2wsmlTranslationWSDDServiceName = "owl2wsmlTranslation";

    public java.lang.String getowl2wsmlTranslationWSDDServiceName() {
        return owl2wsmlTranslationWSDDServiceName;
    }

    public void setowl2wsmlTranslationWSDDServiceName(java.lang.String name) {
        owl2wsmlTranslationWSDDServiceName = name;
    }

    public org.deri.tools.wsml.owl2wsml_translator.v0_1.OWL2WSMLTranslationWS getowl2wsmlTranslation() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(owl2wsmlTranslation_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getowl2wsmlTranslation(endpoint);
    }

    public org.deri.tools.wsml.owl2wsml_translator.v0_1.OWL2WSMLTranslationWS getowl2wsmlTranslation(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.deri.tools.wsml.owl2wsml_translator.v0_1.Owl2WsmlTranslationSoapBindingStub _stub = new org.deri.tools.wsml.owl2wsml_translator.v0_1.Owl2WsmlTranslationSoapBindingStub(portAddress, this);
            _stub.setPortName(getowl2wsmlTranslationWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setowl2wsmlTranslationEndpointAddress(java.lang.String address) {
        owl2wsmlTranslation_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.deri.tools.wsml.owl2wsml_translator.v0_1.OWL2WSMLTranslationWS.class.isAssignableFrom(serviceEndpointInterface)) {
                org.deri.tools.wsml.owl2wsml_translator.v0_1.Owl2WsmlTranslationSoapBindingStub _stub = new org.deri.tools.wsml.owl2wsml_translator.v0_1.Owl2WsmlTranslationSoapBindingStub(new java.net.URL(owl2wsmlTranslation_address), this);
                _stub.setPortName(getowl2wsmlTranslationWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("owl2wsmlTranslation".equals(inputPortName)) {
            return getowl2wsmlTranslation();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tools.deri.org/wsml/owl2wsml-translator/v0.1/", "OWL2WSMLTranslationWSService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tools.deri.org/wsml/owl2wsml-translator/v0.1/", "owl2wsmlTranslation"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("owl2wsmlTranslation".equals(portName)) {
            setowl2wsmlTranslationEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
