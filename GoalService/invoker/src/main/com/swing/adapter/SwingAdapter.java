/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  */

package com.swing.adapter;

import ie.deri.wsmx.adapter.Adapter;
import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.invoker.Invoker;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.omwg.ontology.Attribute;
import org.omwg.ontology.Concept;
import org.omwg.ontology.DataValue;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.omwg.ontology.WsmlDataType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.Serializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ontotext.wsmo4j.common.IRIImpl;
import com.ontotext.wsmo4j.common.NamespaceImpl;
import com.ontotext.wsmo4j.ontology.ConceptImpl;
import com.ontotext.wsmo4j.ontology.InstanceImpl;

public class SwingAdapter extends Adapter {

	static final String VIRTUAL = "http://www.swing-project.org/virtual";

	static final String NSPREFIX = "http://www.swing-project.org/adapter#prefix";
	static final String NAMESPACE = "http://www.swing-project.org/adapter#namespace";
	static final String MAPPING = "http://www.swing-project.org/adapter#name";
	static final String ATTRIBUTE = "http://www.swing-project.org/adapter#attribute";
	static final String NONPRINTABLE = "http://www.swing-project.org/adapter#nonprintable";
	static final String TYPE = "http://www.swing-project.org/adapter#type";
	static final String ELEMENT = "element";
	static final String COMPLEX_TYPE = "complexType";
	static final String WSMLTRUE = "http://www.wsmo.org/wsml/wsml-syntax#true";
	static final String WSMLFALSE = "http://www.wsmo.org/wsml/wsml-syntax#false";

	static final String ADAPTER_ONT = "http://www.swing-project.org/adapter";
	static final String ADAPTER_ONT_FROM = "http://www.swing-project.org/adapter#from";
	static final String ADAPTER_ONT_TO = "http://www.swing-project.org/adapter#to";

	static final String GET_FEATURE = "http://swing.brgm.fr/repository/ontologies/WFS/current#getFeatureRequest";

	WsmoFactory wsmoFactory;
	DataFactory dataFactory;

	public SwingAdapter(String id) {
		super(id);
		wsmoFactory = Factory.createWsmoFactory(null);
		dataFactory = Factory.createDataFactory(null);

	}

	public SwingAdapter() {
		super();
		wsmoFactory = Factory.createWsmoFactory(null);
		dataFactory = Factory.createDataFactory(null);
	}

	static Logger logger = Logger.getLogger(Invoker.class);

	static {
		logger.setLevel(Level.ALL);
	}

	private void MyPrint(String s) {
		logger.info(s);
	}

	private String make_escape_seq(String str) {
		StringBuilder newstr = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
			case '<':
				newstr.append("&lt;");
				break;
			case '>':
				newstr.append("&gt;");
				break;
			case '\"':
				newstr.append("&quot;");
				break;
			case '\'':
				newstr.append("&apos;");
				break;
			default:
				newstr.append(str.charAt(i));
				break;
			}
		}
		return newstr.toString();
	}

	public org.w3c.dom.Document getXML(List<Entity> instances) {
		// for (Entity entity : instances) {
		// Instance instance = (Instance) entity;
		// }
		String sDoc = "";

		for (Entity entity : instances) {
			Instance instance = (Instance) entity;
			Set concepts = instance.listConcepts();
			Map attributes = instance.listAttributeValues();
			Set names = attributes.keySet();
			Iterator attribIterator = names.iterator();

			if (concepts.size() != 1) {
				String msg = "The instance "
						+ instance.getIdentifier().toString()
						+ " has more than one concept:";
				for (Concept c : ((Set<Concept>) concepts)) {
					msg += c.getIdentifier() + ", ";
				}
				MyPrint(msg);
				throw new RuntimeException(msg);
			}
			Iterator conceptIter = concepts.iterator();
			Concept concept = (Concept) conceptIter.next();

			Identifier iri = concept.getIdentifier();
			MyPrint("The instance belongs to: ");
			String iristring = concept.getIdentifier().toString();
			MyPrint(iristring);

			// a flag to check if the iri has been matched
			boolean validInstanceFound = false;

			WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);

			// Handle AggregateRequest
			if (iristring
					.equalsIgnoreCase("http://www.example.org/SwingAggregateOntology#AggregateRequest")) {
				validInstanceFound = true;

				sDoc += "<aggregate xmlns=\"http://aggregation.sintef.org\">\n";
				while (attribIterator.hasNext()) {

					IRI att = (IRI) attribIterator.next();
					Set values = (Set) attributes.get((Object) att);
					String searchStringAttValue = values.iterator().next()
							.toString();
					if (att
							.toString()
							.equalsIgnoreCase(
									"http://www.example.org/SwingAggregateOntology#gml")) {
						sDoc += "     <gml>"
								+ make_escape_seq(searchStringAttValue)
								+ "</gml>\n";
					}
					if (att
							.toString()
							.equalsIgnoreCase(
									"http://www.example.org/SwingAggregateOntology#feature")) {
						sDoc += "     <feature>" + searchStringAttValue
								+ "</feature>\n";
					}
					if (att
							.toString()
							.equalsIgnoreCase(
									"http://www.example.org/SwingAggregateOntology#property")) {
						sDoc += "     <property>" + searchStringAttValue
								+ "</property>\n";
					}
				}
				sDoc += "</aggregate>";
			}

			// http://www.swing.org/Ontology/Aggregate#AggregateRequest
			if (iristring
					.equalsIgnoreCase("http://www.swing.org/Ontology/Aggregate#AggregateRequest")) {
				validInstanceFound = true;

				sDoc += "<aggregate xmlns=\"http://aggregation.sintef.org\">\n";
				while (attribIterator.hasNext()) {

					IRI att = (IRI) attribIterator.next();
					Set values = (Set) attributes.get((Object) att);
					Object value = values.iterator().next();
					String searchStringAttValue = value.toString();
					if (att.toString().equalsIgnoreCase(
							"http://www.swing.org/Ontology/Aggregate#gml")) {
						if (value.getClass() == InstanceImpl.class) {
							String msg = recursive_OGC_XML((Instance) value,
									true, null);
							MyPrint("Aggregate Adapter for "
									+ searchStringAttValue + ":" + msg);
							sDoc += "     <gml>" + make_escape_seq(msg)
									+ "</gml>\n";
						} else {
							// handle incorrect class
							MyPrint("Incorrect class in Aggregate Adapter for "
									+ searchStringAttValue + "class : "
									+ value.getClass());
						}
					}
					if (att.toString().equalsIgnoreCase(
							"http://www.swing.org/Ontology/Aggregate#feature")) {
						sDoc += "     <feature>" + searchStringAttValue
								+ "</feature>\n";
					}
					if (att.toString().equalsIgnoreCase(
							"http://www.swing.org/Ontology/Aggregate#property")) {
						sDoc += "     <property>" + searchStringAttValue
								+ "</property>\n";
					}

				}
				sDoc += "</aggregate>";
			}

			// Handle SocioEconomicConstantsRequest
			if (iristring
					.equalsIgnoreCase("http://swing.brgm.fr/Ontology/BRGM#SocioEconomicConstantsRequest")) {
				validInstanceFound = true;

				sDoc += "<ns2:getValueByKey xmlns:ns2=\"http://DefaultNamespace\">\n";
				while (attribIterator.hasNext()) {

					IRI att = (IRI) attribIterator.next();
					Set values = (Set) attributes.get((Object) att);
					String searchStringAttValue = values.iterator().next()
							.toString();
					if (att.toString().equalsIgnoreCase(
							"http://swing.brgm.fr/Ontology/BRGM#key")) {
						sDoc += "     <ns2:key>" + searchStringAttValue
								+ "</ns2:key>\n";
					}
				}
				sDoc += "</ns2:getValueByKey>";
			}

			// Handle getPopulationFromDepartmentRequest
			if (iristring
					.equalsIgnoreCase("http://swing.brgm.fr/INSEEOntology#INSEEgetPopulationByDepartmentRequest")) {
				validInstanceFound = true;

				sDoc += "<ns2:getPopulationFromDepartment xmlns:ns2=\"http://DefaultNamespace\">\n";
				while (attribIterator.hasNext()) {

					IRI att = (IRI) attribIterator.next();
					Set values = (Set) attributes.get((Object) att);
					String searchStringAttValue = values.iterator().next()
							.toString();
					if (att
							.toString()
							.equalsIgnoreCase(
									"http://swing.brgm.fr/INSEEOntology#codeDepartment")) {
						sDoc += "     <ns2:departmentCodeINSEE>"
								+ searchStringAttValue
								+ "</ns2:departmentCodeINSEE>\n";
					}
				}
				sDoc += "</ns2:getPopulationFromDepartment>";
			}

			// Handle MultiplyRequest
			if (iristring
					.equalsIgnoreCase("http://www.swing.org/ontology/supportService#MultiplyRequest")) {
				validInstanceFound = true;

				sDoc += "<ns3:multiply xmlns:ns3=\"http://support.sintef.org\">";
				while (attribIterator.hasNext()) {
					IRI att = (IRI) attribIterator.next();
					Set values = (Set) attributes.get((Object) att);
					String searchStringAttValue = values.iterator().next()
							.toString();
					if (att
							.toString()
							.equalsIgnoreCase(
									"http://www.swing.org/ontology/supportService#Operand1")) {
						sDoc += "     <ns3:Operand1>" + searchStringAttValue
								+ "</ns3:Operand1>\n";
					}
					if (att
							.toString()
							.equalsIgnoreCase(
									"http://www.swing.org/ontology/supportService#Operand2")) {
						sDoc += "     <ns3:Operand2>" + searchStringAttValue
								+ "</ns3:Operand2>\n";
					}

				}
				sDoc += "</ns3:multiply>";
			}

			// Handle PassThroughRequest
			if (iristring
					.equalsIgnoreCase("http://www.swing.org/ontology/supportService#PassThroughRequest")) {
				validInstanceFound = true;

				sDoc += "<ns3:passthrough xmlns:ns3=\"http://support.sintef.org\">";
				while (attribIterator.hasNext()) {
					IRI att = (IRI) attribIterator.next();
					Set values = (Set) attributes.get((Object) att);
					Instance AttValue = (Instance) values.iterator().next();
					if (att
							.toString()
							.equalsIgnoreCase(
									"http://www.swing.org/ontology/supportService#input")) {
						Ontology onto = wsmoFactory
								.createOntology(wsmoFactory
										.createIRI("http://www.swing.org/ontologies/response"
												+ genID()));
						// Ontology o = instance.getOntology();
						// if(o == null) {
						// o = stateOntology;
						// }
						// Namespace ns = new NamespaceImpl()
						//						
						// Set<Namespace> namespaces = o.listNamespaces();
						// for (Namespace ns: namespaces) {
						// onto.addNamespace(ns);
						// }
						// onto.setWsmlVariant(o.getWsmlVariant());

						onto
								.addNamespace(new NamespaceImpl(
										"swi",
										new IRIImpl(
												"http://swing-project.org/ontologies/usecase1#")));
						onto.addNamespace(new NamespaceImpl("wfs", new IRIImpl(
								"http://swing-project.org/ontologies/wfs#")));
						onto.addNamespace(new NamespaceImpl("gml", new IRIImpl(
								"http://swing.brgm.org/ontologies/gml2/uc1#")));

						recursive_addInstance(AttValue, onto);
						String outputOnto = prettyPrint(onto);
						sDoc += "     <ns3:in>" + outputOnto + "</ns3:in>\n";
						onto.removeOntology(onto);
					}
				}
				sDoc += "</ns3:passthrough>";
			}

			// Handle GetFeature
			if (iristring.equalsIgnoreCase(GET_FEATURE)) {
				validInstanceFound = true;
				// sDoc = Generate_OGC_XML(instance);
				sDoc = recursive_OGC_XML(instance, true, null);
			}

			// Handle WFS request
			Set superConcepts = concept.listSuperConcepts();
			if (superConcepts.size() == 0) {
				MyPrint("The concept " + concept.getIdentifier().toString()
						+ " has no superconcept.");
			} else if (superConcepts.size() > 1) {
				MyPrint("The concept " + concept.getIdentifier().toString()
						+ " has more than one superconcepts.");
			} else {
				Concept superconcept = (Concept) superConcepts.iterator()
						.next();
				iristring = superconcept.getIdentifier().toString().toString();
				if (iristring.equalsIgnoreCase(GET_FEATURE)) {
					validInstanceFound = true;
					sDoc = recursive_OGC_XML(instance, true, null);
				}
			}

			if (iristring.contains(VIRTUAL)) {
				Set supers = concept.listSuperConcepts();
				if (supers.size() == 0) {
					MyPrint("The virtual concept has no superconcept.");
				} else if (supers.size() > 1) {
					MyPrint("The virtual concept has more than one superconcepts.");
				} else {
					Concept superconcept = (Concept) supers.iterator().next();
					iristring = superconcept.getIdentifier().toString()
							.toString();
					if (iristring.equalsIgnoreCase(GET_FEATURE)) {
						validInstanceFound = true;
						sDoc = recursive_OGC_XML(instance, true, null);
					}
				}
			}

			// the iri has not been matched
			if (validInstanceFound == false) {
				throw new RuntimeException("The type of the instance: "
						+ iristring + " was not recognized. ");
			}

		}

		MyPrint(sDoc);

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(sDoc));
			org.w3c.dom.Document doc = builder.parse(is);

			return doc;

		} catch (Exception e) {
			// MyPrint("Error in processing document");
			MyPrint(e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	private void recursive_addInstance(Instance instance, Ontology onto) {
		try {
			onto.addInstance(instance);
		} catch (InvalidModelException e) {
			e.printStackTrace();
		}

		// call recursively for each attribute while has an instances a value
		LinkedHashMap attributesValues = (LinkedHashMap) instance
				.listAttributeValues();
		Set attEntrySet = attributesValues.entrySet();
		Iterator attribIterator = attEntrySet.iterator();
		while (attribIterator.hasNext()) {
			Map.Entry attributeKeyValuePair = (Map.Entry) attribIterator.next();
			IRI attributeIRI = (IRI) attributeKeyValuePair.getKey();
			Set attributeValues = (Set) attributeKeyValuePair.getValue();
			Iterator valueIterator = attributeValues.iterator();
			while (valueIterator.hasNext()) {
				Value val = (Value) valueIterator.next();

				// MyPrint(val.toString() + " has : " + val.getClass());
				// MyPrint(val.toString() + " has : " + getClass(val));

				if (val instanceof Instance) {
					// MyPrint("Making recursive call for " + ((Instance)
					// val).getIdentifier().toString());
					recursive_addInstance((Instance) val, onto);
				}
			}
		}
	}

	private String getLocalName(String iri) {

		int pos = iri.lastIndexOf('#');

		if (pos < 0) {
			pos = iri.lastIndexOf(':');
		}

		if (pos < 0) {
			return iri;
		}
		return iri.substring(pos + 1);
	}

	private String getNameSpace(String iri) {

		int pos = iri.lastIndexOf('#');

		if (pos < 0) {
			pos = iri.lastIndexOf(':');
		}

		if (pos < 0) {
			return iri;
		}
		return iri.substring(0, pos);
	}

	private String getNameSpacePrefix(Set<Namespace> namespaces,
			String namespace) {
		for (Namespace ns : namespaces) {
			if (namespace
					.equalsIgnoreCase(getNameSpace(ns.getIRI().toString()))) {
				return ns.getPrefix();
			}
		}
		return null;
	}

	private String GetNFPString(Entity entity, String NFPIdentifier,
			String defaultvalue) {
		Set cfullnames = entity.listNFPValues(wsmoFactory
				.createIRI(NFPIdentifier));
		if (cfullnames.size() > 0) {
			return cfullnames.iterator().next().toString();
		}
		return defaultvalue;
	}

	private Boolean GetNFPBooleanTest(Entity entity, String NFPIdentifier,
			String Condition, boolean defaultvalue) {
		Set ctypes = entity.listNFPValues(wsmoFactory.createIRI(NFPIdentifier));
		boolean value = defaultvalue;
		if (ctypes.size() > 0) {
			String NFPValue = ctypes.iterator().next().toString();
			if (NFPValue.equalsIgnoreCase(Condition)) {
				value = true;
			} else {
				value = false;
			}
		}
		return value;
	}

	private String recursive_OGC_XML(Instance instance, boolean genNameSpaces,
			String attributename) {
		
		/* find the concept of the instance */
		Set concepts = instance.listConcepts();

		if (concepts.size() != 1) {
			MyPrint("The instance " + instance.getIdentifier().toString()
					+ " has more than one concept!");
			throw new RuntimeException(
					"The instance has more than one concept!");
		}
		Concept concept = (Concept) concepts.iterator().next();
		String iristring = concept.getIdentifier().toString();
		String cnamespace = getNameSpace(iristring);
		MyPrint("The instance belongs to: " + iristring);

		// Handle virtual namespaces by looking up the super concept and using
		// its namespace/localname
		if (cnamespace.equals(VIRTUAL)) {
			Set supers = concept.listSuperConcepts();
			if (supers.size() != 1) {
				MyPrint("The virtual concept has more than one superconcepts.");
			}
			concept = (Concept) supers.iterator().next();
			iristring = concept.getIdentifier().toString().toString();
			MyPrint("The concept is virtual, uses concept " + iristring
					+ " instead");
		}

		// Create the element tag (Concept corresponds to element in GML
		// mapping)
		String cfullname = GetNFPString(concept, MAPPING, null);
		boolean celementType = GetNFPBooleanTest(concept, TYPE, ELEMENT, true);

		String sDoc = "";

		// concept will be printed as an "element"
		if (celementType) {
			if (attributename != null)
				sDoc += "<" + attributename + ">"; //
			sDoc += "<" + cfullname;
		} else {
			if (attributename != null)
				sDoc += "<" + attributename;
			else
				MyPrint("Concept does not correspond to an element and the element name is not available from an attribute.");
		}

		// Handle attributes
		LinkedHashMap attributes = (LinkedHashMap) instance
				.listAttributeValues();
		Set names = attributes.keySet();
		Iterator attribIterator = names.iterator();
		while (attribIterator.hasNext()) {
			IRI attIRI = (IRI) attribIterator.next();
			Set attDefs = instance.findAttributeDefinitions(attIRI);
			if (attDefs.size() > 0) {
//				Set temp = instance.findAttributeDefinitions(attIRI);
//				MyPrint("The instance has attribute " + attIRI.toString()
//						+ " which has " + attDefs.size() + " definitions!");
//				
//			} else {
				//flattened attributes workaround
				Attribute attDef = (Attribute) attDefs.iterator().next();
				for (Object obj: attDefs.toArray()){
					Attribute attr = (Attribute) obj;
					Set attTests = attr.listNFPValues(wsmoFactory.createIRI(ATTRIBUTE));
					if (attTests != null && attTests.size()>0){
						attDef = attr;
						break;
					}
				}
				
//				Attribute attDef = (Attribute) attDefs.iterator().next();
				Set attTests = attDef.listNFPValues(wsmoFactory
						.createIRI(ATTRIBUTE));

				// Check if the attribute corresponds to an XML attribute
				if (GetNFPBooleanTest(attDef, ATTRIBUTE, WSMLTRUE, false)) {
					Set anames = attDef.listNFPValues(wsmoFactory
							.createIRI(MAPPING));
					String afullname = anames.iterator().next().toString();

					Set values = (Set) attributes.get((Object) attIRI);
					String searchStringAttValue = values.iterator().next()
							.toString();

					sDoc += " " + afullname + "=\"" + searchStringAttValue
							+ "\" ";
				}
			}
		}

		// Handle NameSpaces
		if (genNameSpaces) {
			sDoc += CreateNameSpaceString(instance);
		}
		sDoc += ">";

		// Handle Elements
		LinkedHashMap attributes2 = (LinkedHashMap) instance
				.listAttributeValues();
		Set attEntrySet = attributes2.entrySet();
		Iterator attribIterator2 = attEntrySet.iterator();
		while (attribIterator2.hasNext()) {
			Map.Entry attr = (Map.Entry) attribIterator2.next();
			IRI attIRI = (IRI) attr.getKey();

			// Find the attribute
			Set attDefs = instance.findAttributeDefinitions(attIRI);
			if (attDefs.size() > 0) {
//				MyPrint("The instance has attribute" + attIRI.toString()
//						+ " which has more than one definition!");
//			} else {

				Attribute attDef = (Attribute) attDefs.iterator().next();

				// Only handle attribute if it corresponds to an XML element
				if (!GetNFPBooleanTest(attDef, ATTRIBUTE, WSMLTRUE, false)) {
					String efullname = GetNFPString(attDef, MAPPING, null);
					boolean printable = false;
					if (efullname != null)
						printable = true;
					printable = !GetNFPBooleanTest(attDef, NONPRINTABLE,
							WSMLTRUE, !printable);

					Set attTypes = attDef.listTypes();
					if (attTypes.size() != 1) {
						MyPrint("The instance has attribute"
								+ attIRI.toString()
								+ " which has more than one type!");
						Iterator attTypesIterator = attTypes.iterator();
						while (attTypesIterator.hasNext()) {
							Object attType = attTypesIterator.next();
							MyPrint("Attribute" + attIRI.toString() + " has "
									+ attType.toString());
						}
					}
					Object attType = attTypes.iterator().next();

					// handle the values of the attribute
					Set values = (Set) attr.getValue();
					Iterator valueIterator = values.iterator();
					while (valueIterator.hasNext()) {
						Object val = valueIterator.next();

						if (getClass(attType) == ConceptImpl.class) {
							// Bør sjekke om val har type Instance
							if (printable)
								sDoc += recursive_OGC_XML((Instance) val,
										false, efullname);
							else
								sDoc += recursive_OGC_XML((Instance) val,
										false, null);
						} else {
							if (printable) { // print the attribute name as
								// element
								sDoc += "<" + efullname + ">";
							}
							sDoc += val.toString();
							if (printable) { // print the attribute name as
								// element
								sDoc += "</" + efullname + ">";
							}
						}
					}
				}
			}

		}
		if (celementType) {
			sDoc += "</" + cfullname + ">";
		}

		if (attributename != null) {
			sDoc += "</" + attributename + ">"; //
		}

		return sDoc;
	}

	private String CreateNameSpaceString(Instance instance) {
		Map<String, String> NameSpaceMap = new LinkedHashMap<String, String>();
		collectNameSpaces(instance, NameSpaceMap);
		String sDoc = "";
		Set<String> prefixes = NameSpaceMap.keySet();
		for (String prefix : prefixes) {
			sDoc += " xmlns:" + prefix + "=\"" + NameSpaceMap.get(prefix)
					+ "\" ";
		}
		return sDoc;
	}

	private void collectNameSpaces(Instance instance,
			Map<String, String> NameSpaceMap) {

		/* find the concept of the instance */
		Set concepts = instance.listConcepts();

		if (concepts.size() != 1) {
			MyPrint("The instance " + instance.getIdentifier().toString()
					+ " has " + concepts.size() + " concepts!");
			Iterator i = concepts.iterator();
			while (i.hasNext()) {
				MyPrint("The instance " + instance.getIdentifier().toString()
						+ " has concept : " + i.next().toString());
			}
			throw new RuntimeException(
					"The instance has more than one concept!");
		}
		Concept concept = (Concept) concepts.iterator().next();
		String iristring = concept.getIdentifier().toString();
		String cnamespace = getNameSpace(iristring);

		if (cnamespace.equals(VIRTUAL)) {
			Set supers = concept.listSuperConcepts();
			if (supers.size() > 1) {
				MyPrint("The virtual concept has more than one superconcepts.");
				throw new RuntimeException(
						"The virtual concept has more than one superconcepts.");
			} else if (supers.size() == 0) {
				MyPrint("The virtual concept has no superconcepts.");
				throw new RuntimeException(
						"The virtual concept has no superconcepts.");
			} else
				concept = (Concept) supers.iterator().next();
		}

		// TODO
		if (concept.getOntology() != null) {
			String prefix = GetNFPString(concept.getOntology(), NSPREFIX, null);
			String namespace = GetNFPString(concept.getOntology(), NAMESPACE,
					null);

			if (prefix != null && namespace != null) {
				NameSpaceMap.put(prefix, namespace);
			}
		}
		// Handle Elements
		LinkedHashMap attributes2 = (LinkedHashMap) instance
				.listAttributeValues();
		Set attEntrySet = attributes2.entrySet();
		Iterator attribIterator2 = attEntrySet.iterator();
		while (attribIterator2.hasNext()) {
			// handle the values of the attribute
			Map.Entry attr = (Map.Entry) attribIterator2.next();
			Set values = (Set) attr.getValue();
			Iterator valueIterator = values.iterator();
			while (valueIterator.hasNext()) {
				Object val = valueIterator.next();

				if (val instanceof Instance) {
					collectNameSpaces((Instance) val, NameSpaceMap);
				}
			}
		}
	}

	static int idCnt = 0;

	private int genID() {
		return idCnt++;
	}

	private Class getClass(Object o) {
		Class ObjectClass = null;
		try {
			ObjectClass = o.getClass();
			if (o instanceof Proxy) {
				ObjectClass = (Class) Proxy.getInvocationHandler(o).invoke(o,
						ConceptImpl.class.getMethod("getClass"), new Object[0]);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return ObjectClass;
	}

	Ontology ontology;
	Namespace nsDefault;

	public WSMLDocument getWSML(String xmlString, EndpointGrounding endpoint) {

		// Parse xml message
		// MyPrint("Adapter parser : " + xmlString);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		org.w3c.dom.Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlString));
			doc = builder.parse(is);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		// Set up ontologies and factories
		// wsmoFactory = Factory.createWsmoFactory(null);
		// dataFactory = Factory.createDataFactory(null);

		String ontID = "http://www.swing.org/ontologies/response" + genID();
		ontology = wsmoFactory.createOntology(wsmoFactory.createIRI(ontID));
		nsDefault = wsmoFactory.createNamespace("default", wsmoFactory
				.createIRI(ontID + "#"));
		Namespace nsPrefix = wsmoFactory.createNamespace("resp", wsmoFactory
				.createIRI(ontID + "#"));
		ontology.addNamespace(nsDefault);
		ontology.addNamespace(nsPrefix);

		// finds top eleme
		Element element = doc.getDocumentElement();

		// handle namespaces
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			if (attribute.getPrefix().equals("xmlns")) {
				String prefix = attribute.getLocalName();
				String iri = attribute.getNodeValue();
				String internal_iri = findNameSpace(iri);
				if (internal_iri != null) {
					iri = internal_iri + "#";
					// MyPrint("Creating Namespace prefix : " + prefix + " => "
					// + iri);
					Namespace ns = wsmoFactory.createNamespace(prefix,
							wsmoFactory.createIRI(iri));
					ontology.addNamespace(ns);
				} else {
					MyPrint("Warning: Cannot find mapping for namespace: "
							+ iri);
				}
			}
		}

		recursiveGetWSML(element, null);
		// MyPrint("Adapter has generated : " + prettyPrint(ontology));

		WSMLDocument wsmlDocument = new WSMLDocument("");
		wsmlDocument.setContent(prettyPrint(ontology));

		return wsmlDocument;
	}

	private Instance recursiveGetWSML(Element element, Concept SuggestedConcept) {

		String localname = element.getLocalName();
		String namespace = element.getNamespaceURI();
//		 String prefix = element.getPrefix();

		 MyPrint("Element : namespace : " + namespace + " localname: " +
		 localname );

		// Look up concept
		String ontologyName = findNameSpace(namespace);
		String prefix = findPrefix(ontologyName);
		if (prefix == null) {
			MyPrint("Cannot find prefix for " + ontologyName);
		}
		Concept concept = findWSMOConcept(ontologyName, prefix + ":"
				+ localname);
		if (concept == null) {
			// try one level up
			Ontology ont = wsmoFactory.createOntology(wsmoFactory
					.createIRI(ontologyName));
			Set<Ontology> importedOntologies = ont.listOntologies();
			for (Ontology imported : importedOntologies) {
				concept = findWSMOConcept(imported.getIdentifier().toString(),prefix + ":"
						+ localname);
				if(concept != null)
					break;
				
			}
			if(concept == null){
				if (SuggestedConcept == null)
					MyPrint("Unable to find concept for " + prefix + ":"
							+ localname);
				else
					concept = SuggestedConcept;
			}
		}

		// Create instance
		// MyPrint("Creating instance for " + prefix + "#" + localname + " => "
		// + concept.getIdentifier().toString());
		IRI iiri = wsmoFactory.createIRI(nsDefault, localname + genID());

		try {
			Instance instance = wsmoFactory.createInstance(iiri, concept);

			// Special handling if an element has content (for example inherited
			// from string or mixed etc)
			// In this case the value is assigned the attribute that has the
			// same mapping as the concept
			Attribute specialatt = FindWSMOAttribute(concept, prefix + ":"
					+ localname, false);
			if (specialatt != null) {
				Object attType = getWSMOAttibuteType(specialatt);
				if (attType.getClass() != ConceptImpl.class) {
					WsmlDataType wattType = (WsmlDataType) attType;
					String value = element.getTextContent();
					DataValue d = dataFactory.createDataValueFromJavaObject(
							wattType, value);
					instance.addAttributeValue(specialatt.getIdentifier(), d);
				}
			}

			// Handle the attributes
			NamedNodeMap attributes = element.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				String aprefix = attribute.getPrefix();
				if (aprefix == null) // fix when attribute have no namespace
					aprefix = prefix;
				if (!aprefix.equals("xmlns")) {
					String avalue = attribute.getNodeValue();
					String alocalname = attribute.getLocalName();
					if (aprefix.equals("xsi")) {
						MyPrint("Ignoring xsi attribute : " + alocalname
								+ " value : " + avalue);
					} else {
						String anamespace = attribute.getNamespaceURI();
						if (anamespace == null) {
							anamespace = namespace;
						}
						aprefix = findPrefix(findNameSpace(anamespace));

						// Find the WSMO attribute
						Attribute attDef = FindWSMOAttribute(concept, aprefix
								+ ":" + alocalname, true);
						if (attDef == null) {
							MyPrint("The attribute" + aprefix + ":"
									+ alocalname + " not found");
						} else {
							// Find the type
							Object attType = getWSMOAttibuteType(attDef);

							if (getClass(attType) == ConceptImpl.class) {
								MyPrint("The current implementation expects attributes to have a wsmltype that is a datatype");
							} else {
								// Create the value and store the instance
								DataValue d = dataFactory
										.createDataValueFromJavaObject(
												(WsmlDataType) attType, avalue);
								instance.addAttributeValue(attDef
										.getIdentifier(), d);
							}
						}
					}
				}
			}

			// Handle elements
			NodeList elements = element.getChildNodes();
			// NodeList elements = element.getElementsByTagName("*");
			for (int i = 0; i < elements.getLength(); i++) {
				Node n = elements.item(i);
				if (n.getNodeType() == n.ELEMENT_NODE) {
					Element e = (Element) n;
					String eNamespace = e.getNamespaceURI();
					// String ePrefix = e.getPrefix();
					String eLocalname = e.getLocalName();
					String eOntologyName = findNameSpace(eNamespace);
					String ePrefix = findPrefix(eOntologyName);

					// tries to find the WSMOAttribute that corresponds to the
					// element by inspecting the attributes
					// of the concept
					Attribute attDef = FindWSMOAttribute(concept, ePrefix + ":"
							+ eLocalname, false);
					if (attDef != null) {
						// MyPrint("Found element by comparing attributes NFP");
						// Find the type
						Object attType = getWSMOAttibuteType(attDef);
						if (getClass(attType) == ConceptImpl.class) {
							Concept attConcept = (Concept) attType;
							Instance inst = null;
							if (GetNFPBooleanTest(attConcept, TYPE, ELEMENT,
									true)
									&& !GetNFPBooleanTest(attDef, NONPRINTABLE,
											WSMLTRUE, false)) {
								inst = JumpToNextLevel((Element) n, attConcept);
							} else {
								inst = recursiveGetWSML((Element) n, attConcept);
							}
							instance.addAttributeValue(attDef.getIdentifier(),
									inst);
							MyPrint("Creating attribute value for element "
									+ attDef.getIdentifier() + " value : "
									+ inst.getIdentifier().toString());
						} else {
							WsmlDataType wattType = (WsmlDataType) attType;
							if (!HasElementsOrAttributes(e)) { // This is a
								// simple
								// element
								String eValue = e.getTextContent();
								MyPrint("Creating attribute value for element "
										+ ePrefix + "#" + eLocalname
										+ " value : " + eValue);
								DataValue d = dataFactory
										.createDataValueFromJavaObject(
												wattType, eValue);
								instance.addAttributeValue(attDef
										.getIdentifier(), d);
							} else {
								MyPrint("Something is wrong: simple WSMO type, but Complex XML type for "
										+ attDef.getIdentifier());
							}
						}
					} else {
						// tries find to the attribute that corresponds to the
						// element by first
						// finding the concept that match the element and then
						// finding the attribute that
						// match this concept
						Concept TargetConcept = findWSMOConcept(eOntologyName,
								ePrefix + ":" + eLocalname);
						if (TargetConcept != null)
							attDef = FindWSMOAttributeByConcept(concept,
									TargetConcept);
						else {
							attDef = null;
						}

						if (attDef != null) {
							// MyPrint("Found attibute by comparing concept
							// NFP");
							// This attribute must correspond to a concept and
							// not a built-in type
							// no checks are made on the attribute type
							Instance inst = recursiveGetWSML((Element) n,
									TargetConcept);
							instance.addAttributeValue(attDef.getIdentifier(),
									inst);
							MyPrint("Creating attribute value for element "
									+ attDef.getIdentifier() + " value : "
									+ inst.getIdentifier().toString());
						} else {
							MyPrint("The attribute for element " + ePrefix
									+ ":" + eLocalname + " not found");
						}
					}
				}
			} // for element
			ontology.addInstance(instance);
			return instance;
		} catch (SynchronisationException e) {
			MyPrint("Cannot create instance.");
			e.printStackTrace();
			return null;
		} catch (InvalidModelException e) {
			MyPrint("Cannot create instance.");
			e.printStackTrace();
			return null;
		} catch (Throwable e) {
			MyPrint("general cannot create instance.");
			e.printStackTrace();
			return null;
		}
	}

	private Instance JumpToNextLevel(Element element, Concept SuggestedConcept) {
		// special handling for elements that are only mapped to WSMO attributes
		// and not concepts
		// (e.g. GML properties). These elements can only have one subelement
		// and no attributes
		// otherwise they must be mapped to concepts

		// finds the first element and call recursively on this element
		NodeList elements = element.getChildNodes();
		for (int i = 0; i < elements.getLength(); i++) {
			Node n = elements.item(i);
			if (n.getNodeType() == n.ELEMENT_NODE) {
				return (recursiveGetWSML((Element) n, SuggestedConcept));
			}
		}
		return null;
	}

	private Boolean HasElementsOrAttributes(Element element) {
		NodeList elements = element.getChildNodes();
		for (int i = 0; i < elements.getLength(); i++) {
			Node n = elements.item(i);
			if (n.getNodeType() == n.ELEMENT_NODE
					|| n.getNodeType() == n.ATTRIBUTE_NODE)
				return (true);
		}
		return (false);
	}

	private Attribute FindWSMOAttributeByConcept(Concept ConceptWithAttributes,
			Concept TargetConcept) {
		// Looks up attribute that has the concept as superclass and has no
		// mapping
		Set<Attribute> attributes = ConceptWithAttributes.listAttributes();
		for (Attribute a : attributes) {
			Object attType = getWSMOAttibuteType(a);
			if (attType instanceof Concept
					&& isSubConceptOf(TargetConcept, (Concept) attType)) {
				return a;
			}
		}

		// must also check the attibutes of the superconcepts
		// Hence, the function calls itself recursively for all superconcepts
		Set<Concept> superconcepts = ConceptWithAttributes.listSuperConcepts();
		for (Concept s : superconcepts) {
			Attribute a = FindWSMOAttributeByConcept(s, TargetConcept);
			if (a != null)
				return a;
		}

		return null;
	}

	private boolean isSubConceptOf(Concept subconcept, Concept superconcept) {
		if (subconcept.equals(superconcept))
			return true;

		Set<Concept> supers = subconcept.listSuperConcepts();
		for (Concept s : supers) {
			if (isSubConceptOf(s, superconcept))
				return true;
		}

		return false;
	}

	private Attribute FindWSMOAttribute(Concept concept, String xmlname,
			boolean LocalMatch) {
		// Find the WSMO attribute
		Set<Attribute> attributes = concept.listAttributes();
		String xmllocalname = getLocalName(xmlname);

		for (Attribute a : attributes) {
			Set mappings = a.listNFPValues(wsmoFactory.createIRI(MAPPING));
			Iterator i = mappings.iterator();
			while (i.hasNext()) {
				String identifier = i.next().toString();
				String ilocalname = getLocalName(identifier);
				if (identifier.equalsIgnoreCase(xmlname)
						|| (LocalMatch && ilocalname.equalsIgnoreCase(xmllocalname))) {
					return (a);
				}
			}
		}

		// Must also check the attibutes of the superconcepts
		// Hence, the function calls itself recursively for all superconcepts
		Set<Concept> superconcepts = concept.listSuperConcepts();
		for (Concept s : superconcepts) {
			Attribute a = FindWSMOAttribute(s, xmlname, LocalMatch);
			if (a != null)
				return a;
		}

		return null;
	}

	private Object getWSMOAttibuteType(Attribute attribute) {
		// Find the type of attribute
		Set attTypes = attribute.listTypes();
		if (attTypes.size() != 1) {
			MyPrint("The attribute " + attribute.getIdentifier().toString()
					+ " has " + attTypes.size() + " types!");
		}
		Object attType = attTypes.iterator().next();
		// MyPrint("1 AttType object for " +
		// attribute.getIdentifier().toString() + " has class: " +
		// attType.getClass().toString());
		// MyPrint("1 AttType object for " +
		// attribute.getIdentifier().toString() + " has class: " +
		// attType.toString());
		return (attType);
	}

	private Concept findWSMOConcept(String ontologyName, String xmlname) {
		Ontology ont = wsmoFactory.createOntology(wsmoFactory
				.createIRI(ontologyName));
		Set<Concept> concepts = ont.listConcepts();
		for (Concept c : concepts) {
			Set mappings = c.listNFPValues(wsmoFactory.createIRI(MAPPING));
			Iterator i = mappings.iterator();
			while (i.hasNext()) {
				if (i.next().toString().equals(xmlname))
					return (c);
			}
		}
		return null;
	}

	private String findNameSpace(String iri) {
		Ontology ont = wsmoFactory.createOntology(wsmoFactory
				.createIRI(ADAPTER_ONT));
		Set<Instance> instances = ont.listInstances();
		for (Instance i : instances) {
			Set values = i.listAttributeValues(wsmoFactory
					.createIRI(ADAPTER_ONT_FROM));
			if (values.iterator().next().toString().equals(iri)) {
				values = i.listAttributeValues(wsmoFactory
						.createIRI(ADAPTER_ONT_TO));
				return (values.iterator().next().toString());
			}
		}
		return null;
	}

	private String findPrefix(String OntologyName) {
		Ontology ont = wsmoFactory.createOntology(wsmoFactory
				.createIRI(OntologyName));
		return GetNFPString(ont, NSPREFIX, null);
	}

	/*
	 * Pretty-prints the contents of the input ontology
	 */
	private String prettyPrint(Ontology ont) {
		Serializer ontologySerializer = Factory
				.createSerializer(new HashMap<String, Object>());
		String ontContent = "";
		try {
			StringWriter sw = new StringWriter();
			ontologySerializer.serialize(new TopEntity[] { ont }, sw);
			ontContent = sw.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ontContent;
	}

	public String getURL(String urlStr, List<Entity> data) {
		String urlParameters = "";

		for (Entity entity : data) {
			Instance instance = (Instance) entity;
			Set concepts = instance.listConcepts();
			Map<Identifier, Set<Value>> attributes = instance
					.listAttributeValues();
			Set<Identifier> attributeNames = attributes.keySet();

			if (concepts.size() != 1) {
				String msg = "The instance "
						+ instance.getIdentifier().toString()
						+ " has more than one concept:";
				for (Concept c : ((Set<Concept>) concepts)) {
					msg += c.getIdentifier() + ", ";
				}
				MyPrint(msg);
				throw new RuntimeException(msg);
			}
			Iterator conceptIter = concepts.iterator();
			Concept concept = (Concept) conceptIter.next();

			Identifier iri = concept.getIdentifier();
			MyPrint("The instance belongs to: ");
			String iristring = concept.getIdentifier().toString();
			MyPrint(iristring);

			// a flag to check if the iri has been matched
			boolean validInstanceFound = false;

			WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);

			// Handle WFS request
			Set superConcepts = concept.listSuperConcepts();
			if (superConcepts.size() == 0) {
				MyPrint("The concept " + concept.getIdentifier().toString()
						+ " has no superconcept.");
			} else if (superConcepts.size() > 1) {
				MyPrint("The concept " + concept.getIdentifier().toString()
						+ " has more than one superconcepts.");
			} else {
				Concept superconcept = (Concept) superConcepts.iterator()
						.next();
				iristring = superconcept.getIdentifier().toString().toString();
				if (iristring.equalsIgnoreCase(GET_FEATURE)) {
					validInstanceFound = true;
					for (Identifier name : attributeNames) {
						if (name.toString().endsWith("service_idx")) {
							urlParameters += "service_idx="
									+ attributes.get(name).iterator().next()
									+ "&";
						} else if (name.toString().endsWith("map")) {
							urlParameters += "map="
									+ attributes.get(name).iterator().next()
									+ "&";
						} else if (name.toString().endsWith("maxFeatures")) {
							urlParameters += "maxFeatures="
									+ attributes.get(name).iterator().next()
									+ "&";
						} else if (name.toString().endsWith("query")) {
							Instance querry = (Instance) attributes.get(name)
									.iterator().next();

							for (Identifier querryAtrName : querry
									.listAttributeValues().keySet()) {
								if (querryAtrName.toString().endsWith(
										"typeName")) {
									urlParameters += "typeName="
											+ querry.listAttributeValues().get(
													querryAtrName).iterator()
													.next() + "&";
								} else if(querryAtrName.toString().endsWith(
								"filter")) {
									Instance filter = (Instance) querry.listAttributeValues().get(querryAtrName).iterator().next();
									logger.info("filter " + filter.getIdentifier().toString());
									
									for (Identifier filterAttr : filter.listAttributeValues().keySet()) {
										if (filterAttr.toString().endsWith("encodes")) {
											Instance bbox = (Instance) filter.listAttributeValues(filterAttr).iterator().next();
											
											
											for (Identifier bboxAttr : bbox.listAttributeValues().keySet()) {
												if (bboxAttr.toString().endsWith("arguments")) {
													Instance box = (Instance) bbox.listAttributeValues(bboxAttr).iterator().next();
													for (Identifier boxAttr : box.listAttributeValues().keySet()) {
														if (boxAttr.toString().endsWith("coordinates")) {
															Instance coord = (Instance) box.listAttributeValues(boxAttr).iterator().next();
															for (Identifier coordAttr : coord.listAttributeValues().keySet()) {
																if (coordAttr.toString().endsWith("decimal")) {
																	urlParameters += "bbox=" + coord.listAttributeValues().get(coordAttr).iterator().next().toString() + "&";
																}
															}
														}
													}
												}
											}
											
											
											
										}
									}
								}
							}
						}
					}
					urlParameters += "request=getfeature";

				}
			}
			// the iri has not been matched
			if (validInstanceFound == false) {
				throw new RuntimeException("The type of the instance: "
						+ iristring + " was not recognized. ");
			}
		}
		return urlParameters;

	}
}
