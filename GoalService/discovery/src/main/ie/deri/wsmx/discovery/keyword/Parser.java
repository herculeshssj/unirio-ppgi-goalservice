/*
 * Created on 30-Sep-2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ie.deri.wsmx.discovery.keyword;

import java.io.*;
import java.util.*;

import org.omwg.ontology.*;
import org.wsmo.common.*;
import org.wsmo.factory.*;

/**
 * @author Edward Kilgarriff
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Parser {
	
	public static void main(String[] args) {
		Set <Entity> entities = Parser.parse("c:\\myOntology.wsml");

		for (Entity e : entities){
			if (e instanceof Ontology){
				System.out.println("Ontology : " + e);
				System.out.println("Concepts : ");
				for (Concept concept : (Set <Concept>) ((Ontology) e).listConcepts()){
					System.out.println(concept.getIdentifier());
				}
				System.out.println("Instances : " + ((Ontology) e).listInstances());
				System.out.println("Relations : " + ((Ontology) e).listRelations());
				System.out.println("Relation Instances : " + ((Ontology) e).listRelationInstances());
				System.out.println("Axioms : " + ((Ontology) e).listAxioms());
			}
		}
	}

	public static Set<Entity> parse(String theFilename) {
		WsmoFactory wsmoFactory = createWSMOFactory();
		LogicalExpressionFactory leFactory = createLEFactory();

		HashMap<String, Object> props = new HashMap<String, Object>();
		org.wsmo.wsml.Parser parser = Factory.createParser(props);

		Entity[] parsed = null;
		try {
			parsed = parser.parse(new FileReader(theFilename));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Set<Entity> set = new HashSet<Entity>();

		for (int i = 0; i < parsed.length; i++) {
			set.add(parsed[i]);
		}

		return set;
	}

	private static WsmoFactory createWSMOFactory() {
//		Map<String, String> factoryProps = null;
		WsmoFactory wsmoFactory = null;
//		if (extImplData.containsKey(WSMOImplManager.ATTR_WSMO_FACTORY)) {
//			factoryProps = new HashMap<String, String>();
//			factoryProps.put(Factory.PROVIDER_CLASS, extImplData.get(WSMOImplManager.ATTR_WSMO_FACTORY));
//		}
//		try {
//			wsmoFactory = Factory.createWsmoFactory(factoryProps);
//		} catch (RuntimeException re) {
//			re.printStackTrace();
			wsmoFactory = Factory.createWsmoFactory(null);
//		}
		return wsmoFactory;
	}

	private static LogicalExpressionFactory createLEFactory() {
//		Map<String, String> leFactoryProps = null;
		LogicalExpressionFactory leFactory = null;
//		if (extImplData.containsKey(WSMOImplManager.ATTR_LE_FACTORY)) {
//			leFactoryProps = new HashMap<String, String>();
//			leFactoryProps.put(Factory.PROVIDER_CLASS, extImplData.get(WSMOImplManager.ATTR_LE_FACTORY));
//		}
//		try {
//			leFactory = Factory.createLogicalExpressionFactory(leFactoryProps);
//		} catch (RuntimeException re) {
//			re.printStackTrace();
			leFactory = Factory.createLogicalExpressionFactory(null);
//		}
		return leFactory;

	}
}