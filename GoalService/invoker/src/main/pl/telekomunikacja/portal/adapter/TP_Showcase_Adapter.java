package pl.telekomunikacja.portal.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import javax.xml.soap.SOAPElement;

import org.apache.log4j.Logger;
import org.deri.infrawebs.sfs.adapter.XMLTranslator;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

import ie.deri.wsmx.adapter.Adapter;
import ie.deri.wsmx.invoker.Invoker;
import ie.deri.wsmx.scheduler.Environment;

public abstract class TP_Showcase_Adapter extends Adapter{
	
    static Logger logger = Logger.getLogger(TP_Showcase_Adapter.class);
	String xslFileName;
	
	public IRI loadIRI(String value)
	{
		WsmoFactory factory = Factory.createWsmoFactory(null);
		IRI result = factory.createIRI(value);
		return result;
	}
	
    public SOAPElement getHeader(List<Entity> instances, String id){
    	return null;
    }
    
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint) {
		
		
		WSMLDocument wsmlDocument = new WSMLDocument("");
		XMLTranslator translator = new XMLTranslator();
		InputStream xsltFile = null;
		try {
			String filePath = TP_Showcase_Constants.xlstLocation+xslFileName;
			if (Environment.isCore()){
				filePath = Environment.getKernelLocation().getAbsolutePath()+File.separator+filePath;
			}
			
			xsltFile = new FileInputStream(filePath);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wsmlDocument.setContent(translator.doTransformation(document, xsltFile));
		return wsmlDocument;
	}
    
}
