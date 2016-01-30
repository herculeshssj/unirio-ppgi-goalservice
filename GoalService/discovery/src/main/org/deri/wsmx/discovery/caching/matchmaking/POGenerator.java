/**
 * defines methods for generating TPTP proof obligations 
 * for all matchmaking tasks that occur in SDC 
 * 
 * @author Michael Stollberg
 * @version $Revision: 1.3 $ $Date: 2007-04-25 17:12:29 $
 */

package org.deri.wsmx.discovery.caching.matchmaking;

import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;


public interface POGenerator {
	
	/**
	 * PO generation for goal similarity degree 
	 * @param gt1: the source goal temnplate 
	 * @param gt2: the target goal temnplate 
	 * @param degree: the degree to be tested 
	 * @return TPTP proof obligation as a String
	 */
	public String generatePOsimilarity(Goal gt1, Goal gt2, String degree, Ontology sdcGraph);

	
	/**
	 * PO generation for WS usability degree
	 * @param gt: the source goal temnplate
	 * @param ws: the target Web service 
	 * @param degree: the degree to be tested
	 * @return TPTP proof obligation as a String
	 */
	public String generatePOusability(Goal gt, WebService ws, String degree, Ontology sdcGraph);

	/**
	 * PO generation for checking if a goal instance instantiates a goal template
	 * @param goalinstance: the goal instance 
	 * @param goaltemplate: the goal template 
	 * @return TPTP proof obligation as a String
	 */
	public String generatePOInstantiatonCheck(Instance goalInstance, Goal goaltemplate, Ontology sdcGraph) ;

	/**
	 * PO generation for usability of a WS under the subsume degree 
	 * @param goalinstance: the goal instance
	 * @param ws: the Web service 
	 * @return TPTP proof obligation as a String
	 */
	public String generatePOgilevelSubsume(Instance goalinstance, WebService ws);
	
	/**
	 * PO generation for usability of a WS under the intersect degree
	 * @param goalinstance: the goal instance
	 * @param goaltemplate: the goal template 
	 * @param ws: the Web service 
	 * @return TPTP proof obligation as a String
	 */
	public String generatePOgilevelIntersect(Instance goalinstance, Goal goaltemplate, WebService ws, Ontology sdcGraph);

}
