package com.isoco.dip.adapter;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

/**
 * 
 * @author Darek Kleczek
 *
 */

public class XMLTranslator {

	protected static Logger logger = Logger.getLogger(XMLTranslator.class);
	
	public String doTransformation(String initialXML, InputStream xsltFile){

		String wsmlFile = new String();
		StringWriter writer = new StringWriter();
		try
		{
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t1 = tf.newTransformer(new StreamSource(xsltFile));
			t1.transform(new StreamSource(new StringReader(initialXML)), new StreamResult(writer));
			//write the results of the transformation to the wsml document
			wsmlFile = writer.toString();
			
		}
		catch(Exception e)
		{
			logger.debug(e.getMessage());
		}
				
		return wsmlFile;
	}
}
