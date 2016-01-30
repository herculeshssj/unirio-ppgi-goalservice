/*
 Copyright (c) 2006, University of Innsbruck, Austria

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License along
 with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ie.deri.wsmx.discovery.util;

import ie.deri.wsmx.discovery.util.Discovery;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.omwg.ontology.Ontology;
import org.wsmo.common.TopEntity;
import org.wsmo.factory.Factory;

/**
 * Interface or class description
 *
 * <pre>
 * Created on 01.12.2006
 * Committed by $Author: holgerlausen $
 * $Source: /cvsroot/wsmx/components/discovery/src/main/ie/deri/wsmx/discovery/ontology/Discovery.java,v $,
 * </pre>
 *
 * @author Holger Lausen
 *
 * @version $Revision: 1.2 $ $Date: 2007/02/05 10:09:21 $
 */
public class Discovery {
	 public final static String DISCOVERY_ONTOLOGY_NS="http://wiki.wsmx.org/index.php?title=DiscoveryOntology#";
	 public final static String EXTENDED_PLUGIN_NS="http://heavyweightDiscovery/extendedPluginMatch#";
	    
	 public final static String TYPE_OF_MATCH = DISCOVERY_ONTOLOGY_NS+"typeOfMatch";
	 public final static String EXACT = DISCOVERY_ONTOLOGY_NS+"ExactMatch";
	 public final static String PLUGIN = DISCOVERY_ONTOLOGY_NS+"PluginMatch";
	 public final static String EXTENDED_PLUGIN = DISCOVERY_ONTOLOGY_NS+"ExtendedPluginMatch";
	 public final static String SUBSUMES = DISCOVERY_ONTOLOGY_NS+"SubsumptionMatch";
	 public final static String INTERSECT = DISCOVERY_ONTOLOGY_NS+"IntersectionMatch";
	    
	 public final static String STRATEGY = DISCOVERY_ONTOLOGY_NS+"discoveryStrategy";
	 public final static String LEIGHTWEIGHT = DISCOVERY_ONTOLOGY_NS+"LightweightDiscovery";
	 public final static String HEAVYWEIGHT = DISCOVERY_ONTOLOGY_NS+"HeavyweightDiscovery";
	    
	 public final static String INTENTION = DISCOVERY_ONTOLOGY_NS+"intention";
	 public final static String UNIVERSAL = DISCOVERY_ONTOLOGY_NS+"Universal";
	 public final static String EXISTENTIAL = DISCOVERY_ONTOLOGY_NS+"Existential";
	    
	 public final static String ORDER = EXTENDED_PLUGIN_NS + "order";
	 public final static String SHARED = EXTENDED_PLUGIN_NS + "shared";
	 public final static String DIF = EXTENDED_PLUGIN_NS + "dif";
	    
	 public final static Ontology DISCOVERY_ONTOLOGY;
	    
	 static {
		 Ontology ont = null;
		 try{
			 InputStream in = Discovery.class.getClassLoader().getResourceAsStream(
			 "ie/deri/wsmx/discovery/ontology/discoveryOntology.wsml");
			 
			 TopEntity[] tes = Factory.createParser(null).parse(new InputStreamReader(in));
			 ont = (Ontology)tes[0];
		 }catch (Exception e) {}
		 DISCOVERY_ONTOLOGY = ont;
	 }
		
}
