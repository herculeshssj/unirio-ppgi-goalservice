/**
 * defines methods for all matchmaking tasks that occur in SDC 
 * 
 * @author Michael Stollberg
 * @version $Revision: 1.3 $ $Date: 2007-04-25 17:12:29 $
 */

package org.deri.wsmx.discovery.caching.matchmaking;

import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;

public interface Matchmaking {
	
	/**
	 * test similarity degree exact 
	 * @param source
	 * @param target
	 * @return boolean (matchmaking result) 
	 */
	public boolean similarityExact(Goal source, Goal target, Ontology sdcGraph); 

	/**
	 * test similarity degree plugin
	 * @param source: the 1. GT 
	 * @param target: the 2. GT 
	 * @param sdcGraph: the SDC Graph wherein both GTs are stored 
	 * @return boolean (matchmaking result) 
	 */
	public boolean similarityPlugin(Goal source, Goal target, Ontology sdcGraph); 
	
	/**
	 * test similarity degree subsume 
	 * @param source: the 1. GT 
	 * @param target: the 2. GT 
	 * @param sdcGraph: the SDC Graph wherein both GTs are stored 
	 * @return boolean (matchmaking result) 
	 */
	public boolean similaritySubsume(Goal source, Goal target, Ontology sdcGraph); 

	
	/**
	 * test similarity degree intersect 
	 * @param source: the 1. GT 
	 * @param target: the 2. GT 
	 * @param sdcGraph: the SDC Graph wherein both GTs are stored 
	 * @return boolean (matchmaking result) 
	 */
	public boolean similarityIntersect(Goal source, Goal target, Ontology sdcGraph); 


	/**
	 * test usability degree plugin
	 * @param source: the goal template 
	 * @param target: the Web service 
	 * @param sdcGraph: the SDC Graph wherein the source GT is stored 
	 * @return boolean (matchmaking result) 
	 */
	public boolean usabilityPlugin(Goal goaltemplate, WebService webservice, Ontology sdcGraph); 
	
	/**
	 * test usability degree subsume 
	 * @param source: the goal template 
	 * @param target: the Web service 
	 * @param sdcGraph: the SDC Graph wherein the source GT is stored 
	 * @return boolean (matchmaking result) 
	 */
	public boolean usabilitySubsume(Goal goaltemplate, WebService webservice, Ontology sdcGraph); 

	
	/**
	 * test usability degree intersect 
	 * @param source: the goal template 
	 * @param target: the Web service 
	 * @param sdcGraph: the SDC Graph wherein the source GT is stored 
	 * @return boolean (matchmaking result) 
	 */
	public boolean usabilityIntersect(Goal goaltemplate, WebService webservice, Ontology sdcGraph); 

	
	/**
	 * test whether a goal instances is a proper instantiation of a goal template 
	 * @param goalinstance
	 * @param goaltemplate
	 * @param sdcGraph: the SDC Graph wherein the corresponding GT is stored 
	 * @return boolean (matchmaking result)
	 */
	public boolean instantiatonCheck(Instance goalInstance, Goal goaltemplate, Ontology sdcGraph); 

	
	/**
	 * test usability of a WS under the subsume degree 
	 * @param goalinstance
	 * @param ws
	 * @return boolean (matchmaking result)
	 */
	public boolean giLevelUsabilitySubsume(Instance goalinstance, WebService ws);
	
	/**
	 * test usability of a WS under the intersect degree
	 * @param goalinstance: the goal instance
	 * @param goaltemplate: the goal template 
	 * @param ws: the Web service 
	 * @param sdcGraph: the SDC Graph wherein the corresponding GT is stored 
	 * @return boolean (matchmaking result)
	 */
	public boolean giLevelUsabilityIntersect(Instance goalinstance, Goal goaltemplate, WebService ws, Ontology sdcGraph);

}
