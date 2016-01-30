package org.wsmx.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
/**
 * Wrapper around two strings. 
 * Used as data type for JAX-WS.
 * 
 * @author maxher
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stringPair", 
		namespace = "http://www.wsmx.org/jaxws/entrypointtypes",
		propOrder = {"first","second"})
public class StringPair {
	
	@XmlElement(name = "first", required=true)  
	String first;
	@XmlElement(name = "second", required=true) 
	String second;
	
	public String getFirst() {
		return first;
	}
	public void setFirst(String first) {
		this.first = first;
	}
	public String getSecond() {
		return second;
	}
	public void setSecond(String second) {
		this.second = second;
	}
}
