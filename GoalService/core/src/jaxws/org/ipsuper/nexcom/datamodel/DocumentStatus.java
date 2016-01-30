//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-3354 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.06.10 at 12:38:13 PM IST 
//


package org.ipsuper.nexcom.datamodel;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for DocumentStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DocumentStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Received"/>
 *     &lt;enumeration value="Accepted"/>
 *     &lt;enumeration value="Not accepted"/>
 *     &lt;enumeration value="Approved"/>
 *     &lt;enumeration value="Not approved"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum DocumentStatus {

    @XmlEnumValue("Accepted")
    ACCEPTED("Accepted"),
    @XmlEnumValue("Approved")
    APPROVED("Approved"),
    @XmlEnumValue("Not accepted")
    NOT_ACCEPTED("Not accepted"),
    @XmlEnumValue("Not approved")
    NOT_APPROVED("Not approved"),
    @XmlEnumValue("Received")
    RECEIVED("Received");
    private final String value;

    DocumentStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DocumentStatus fromValue(String v) {
        for (DocumentStatus c: DocumentStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}