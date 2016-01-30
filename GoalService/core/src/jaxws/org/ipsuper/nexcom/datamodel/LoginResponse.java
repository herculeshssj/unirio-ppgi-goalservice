//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-3354 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.06.10 at 12:38:13 PM IST 
//


package org.ipsuper.nexcom.datamodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LoginResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LoginResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Confirmation" type="{http://ip-super.org/usecase/nexcom/}DocumentStatus"/>
 *         &lt;element name="GoldClass" type="{http://ip-super.org/usecase/nexcom/}QoSParameters"/>
 *         &lt;element name="SilverClass" type="{http://ip-super.org/usecase/nexcom/}QoSParameters"/>
 *         &lt;element name="BestEffort" type="{http://ip-super.org/usecase/nexcom/}QoSParameters"/>
 *         &lt;element name="ttID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoginResponse", propOrder = {
    "confirmation",
    "goldClass",
    "silverClass",
    "bestEffort",
    "ttID"
})
public class LoginResponse {

    @XmlElement(name = "Confirmation", required = true)
    protected DocumentStatus confirmation;
    @XmlElement(name = "GoldClass", required = true)
    protected QoSParameters goldClass;
    @XmlElement(name = "SilverClass", required = true)
    protected QoSParameters silverClass;
    @XmlElement(name = "BestEffort", required = true)
    protected QoSParameters bestEffort;
    @XmlElement(required = true)
    protected String ttID;

    /**
     * Gets the value of the confirmation property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentStatus }
     *     
     */
    public DocumentStatus getConfirmation() {
        return confirmation;
    }

    /**
     * Sets the value of the confirmation property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentStatus }
     *     
     */
    public void setConfirmation(DocumentStatus value) {
        this.confirmation = value;
    }

    /**
     * Gets the value of the goldClass property.
     * 
     * @return
     *     possible object is
     *     {@link QoSParameters }
     *     
     */
    public QoSParameters getGoldClass() {
        return goldClass;
    }

    /**
     * Sets the value of the goldClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link QoSParameters }
     *     
     */
    public void setGoldClass(QoSParameters value) {
        this.goldClass = value;
    }

    /**
     * Gets the value of the silverClass property.
     * 
     * @return
     *     possible object is
     *     {@link QoSParameters }
     *     
     */
    public QoSParameters getSilverClass() {
        return silverClass;
    }

    /**
     * Sets the value of the silverClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link QoSParameters }
     *     
     */
    public void setSilverClass(QoSParameters value) {
        this.silverClass = value;
    }

    /**
     * Gets the value of the bestEffort property.
     * 
     * @return
     *     possible object is
     *     {@link QoSParameters }
     *     
     */
    public QoSParameters getBestEffort() {
        return bestEffort;
    }

    /**
     * Sets the value of the bestEffort property.
     * 
     * @param value
     *     allowed object is
     *     {@link QoSParameters }
     *     
     */
    public void setBestEffort(QoSParameters value) {
        this.bestEffort = value;
    }

    /**
     * Gets the value of the ttID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTtID() {
        return ttID;
    }

    /**
     * Sets the value of the ttID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTtID(String value) {
        this.ttID = value;
    }

}
