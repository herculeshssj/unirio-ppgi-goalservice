package ie.deri.wsmx.discovery.keyword;

/**
 * Copyright (c) 2004 DERI www.deri.org
 * Created on Nov 29, 2004
 * 
 * @author Ioan Toma
 * 
 **/

import ie.deri.wsmx.core.configuration.annotation.*;
import ie.deri.wsmx.discovery.*;

import java.util.*;
import java.util.Map.*;

import org.apache.log4j.*;
import org.omwg.ontology.*;
import org.wsmo.common.*;
import org.wsmo.execution.common.component.*;
import org.wsmo.execution.common.exception.*;
import org.wsmo.factory.*;
import org.wsmo.service.*;

@WSMXComponent(name = 	"KeywordDiscovery",
			   events =	"DISCOVERY")
public class KeywordDiscovery extends  AbstractWSMODiscoveryImpl{
    
    private WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);

    static Logger log = Logger.getLogger(KeywordDiscovery.class);
    
    private Set<WebService> webServices;

    public KeywordDiscovery() {
    	webServices = new HashSet<WebService>();
    }
    
    public List<WebService> discover(Goal goal, Set<WebService> searchSpace) throws ComponentException {
		if (searchSpace == null) {
            return null;
		}
		log.info("Keyword discovery - operating on "+searchSpace.size()+" services.");

		//TODO add options to configure what to match
		return matchByGlobalNFP(goal, searchSpace);
		//matchByAxiomNFP(goal, searchSpace);
//        return null;
	}

	/**
	 * Performs a keyword-based match over the NFP of the goal description and
	 * web services description.
	 * 
	 * @param goal -
	 *            the user goal
	 * @param knownWebServices -
	 *            the set of known Web Services
	 * @return - a set of matching Wen Services
	 * @throws KeywordDiscoveryException
	 */
	public ArrayList<WebService> matchByGlobalNFP(
			Goal goal, Set<WebService> knownWebServices)
	throws KeywordDiscoveryException {
		ArrayList<WebService> matchedServices = new ArrayList<WebService>();

		//TODO add option to restrict to one nfp key
		
        //get goal NFP Attribute Values
        StringBuffer goalString = new StringBuffer();
        for (Object v : goal.listNFPValues().values()){
            goalString.append(" "+ v);
        }
		
        // perform lexical transformation and get the goal NFP atomic keywords
		HashSet<String> goalKeywords = lexicalTransformation(goalString.toString());
        log.debug("Words in GOAL - " +goal.getIdentifier().toString()+" - " +goalKeywords);
        
        SortedMap<Integer, List<WebService>> sortedResultMap = new TreeMap<Integer, List<WebService>>();

		// iterate throughout all the knownWebServices and perform the match
		for (WebService webService : knownWebServices) {
            StringBuffer wsString = new StringBuffer();

            //FIXME: possible problems
            Map<IRI, Set<Object>> map = webService.listNFPValues();
            
            for (Entry ent : map.entrySet()){
            	if (! ( ent.getKey().toString().toLowerCase().equals("http://owner")))
            		wsString.append(" "+ ent.getValue());
            }

			// perform lexical transformation and get the web service NFP atomic keywords
			HashSet<String> webServiceKeywords = lexicalTransformation(wsString.toString());
                        
			int score = partialMatch(goalKeywords, webServiceKeywords);
            if (score>0){
            	Integer key = Integer.valueOf(score*-1);
            	
            	if (sortedResultMap.containsKey(key))
            	{
            		//append existing list
            		List<WebService> l = sortedResultMap.get(key);
            		l.add(webService);
            	} else{
            		//create new list
            		List<WebService> l = new ArrayList<WebService>();
            		l.add(webService);
                    sortedResultMap.put(key,l);
            	}
			}
		}
		
        //logout result
        for (Entry<Integer, List<WebService>> entry : sortedResultMap.entrySet()){
        	List<WebService> wsResult = entry.getValue();
            for (WebService ws : wsResult){
                log.debug("WS - "+ ws.getIdentifier().toString() +" - score: " + Math.abs(entry.getKey()));            	
            }
        }

		
        int i = 2;
        int THRESHOLD = 6;
        //add services
        for (Entry<Integer, List<WebService>> entry : sortedResultMap.entrySet()){
        	List<WebService> wsResult = entry.getValue();
            
        	for (WebService ws : wsResult){
        		matchedServices.add(ws);
        		if (matchedServices.size() >= THRESHOLD)
        			break;
        	}
            if ( (matchedServices.size() >= 4) || (i-- == 0) )break;
        }

		return matchedServices;
	}

	/**
	 * Performs a keyword-based match over the NFP of the axioms that describe
	 * goal postconditions and NFP of the axioms that describe service
	 * capabilities and postconditions.
	 * 
	 * @param goal -
	 *            the user goal
	 * @param knownWebServices -
	 *            the set of known Web Services
	 * @return - a set of matching Web Services
	 * @throws KeywordDiscoveryException
	 */
	public ArrayList<WebService> matchByAxiomNFP(Goal goal,
			ArrayList<WebService> knownWebServices,
			KeywordDiscoveryPreference preference)
			throws KeywordDiscoveryException {

		ArrayList<WebService> matchedServices = new ArrayList<WebService>();

		// check the MATCH type FULL or PARTIAL
		boolean partial = (preference.getMatchType() == KeywordDiscoveryPreference.PARTIAL_MATCH);

		ArrayList<Axiom> goalPostConditions = new ArrayList<Axiom>(goal.getCapability().listPostConditions());
		for (Axiom axiomGoalPostCondition : goalPostConditions) {
			// get goal postConditions NFP Attribute Value (i.e. value of
			// dc:Subject)
			String goalAxiomNFPAttributeValue = axiomGoalPostCondition
					.listNFPValues().get(preference.getNFPAttribute())
					.toString();

			// perform lexical transformation and get the goal postConditions
			// NFP atomic keywords
			HashSet<String> goalAxiomKeywords = lexicalTransformation(goalAxiomNFPAttributeValue);

			// ------------------------------
			// used only for PARTIAL match
			// how many keywords from goal postcondition NFP match with the
			// keywords from web
			// service postcondition NFP
			int highestScore = 0;
			// structure with web services and the associated scores
			//HashMap<WebService, Integer> scoredWebServices = new HashMap<WebService, Integer>();
			SortedMap<Integer, Set<WebService>> scoredWebServices = new TreeMap<Integer, Set<WebService>>();
			// ------------------------------

			// iterate throughout all the knownWebServices and
			// throughout each of their capabilities and perform the match
			for (WebService webService : knownWebServices) {
				ArrayList<Axiom> webServicePostConditions = new ArrayList<Axiom>(
						webService.getCapability().listPostConditions());
				for (Axiom axiomWebServicePostCondition : webServicePostConditions) {

					String webServiceAxiomNFPAttributeValue = axiomWebServicePostCondition
							.listNFPValues().get(preference.getNFPAttribute())
							.toString();

					// different approaches are taken based on the specified match type 
					if (partial) { 
						// PARTIAL MATCH: perform lexical transformation 
						// and get the web service NFP atomic keywords
						HashSet<String> webServiceAxiomKeywords = lexicalTransformation(webServiceAxiomNFPAttributeValue);
						
						int score = partialMatch(goalAxiomKeywords, webServiceAxiomKeywords);
						Set<WebService> wsSet = scoredWebServices.get(Integer.valueOf(score));
						if (wsSet == null)
							wsSet = new HashSet<WebService>();
						wsSet.add(webService);
						scoredWebServices.put(Integer.valueOf(score), wsSet);
						
						if (score > highestScore)
							highestScore = score;
					} else { 
						// FULL MATCH
						if (webServiceAxiomNFPAttributeValue
								.equalsIgnoreCase(goalAxiomNFPAttributeValue)) {
							matchedServices.add(webService);
						}
					}
				}
			}

			// Perform the selection if PARTIAL match
			// based on the threshold from the preference
			if (partial) {
				partialMatchSelection(scoredWebServices, matchedServices, 
						highestScore, preference.getThreshold());
			}
		}

		return matchedServices;
	}

	/**
	 * Performs lexical transformation on the NFP string given as input
	 * 
	 * @param source -
	 *            the NFP string source
	 * @return keywords - the atomic keywords
	 */
	private HashSet<String> lexicalTransformation(String source) {
		// clear the string from characters like [ , ]
		String processedNFP = source.replace('[', ' ');
		processedNFP = processedNFP.replace(',', ' ');
		processedNFP = processedNFP.replace(']', ' ');

		// some common stop words
		List<String> stopWords = Arrays.asList(
				new String[] {"a", "of", "and", "in", "or", "to", "the"});
		
		// break the NFP Attribute Value in atomic keywords
		StringTokenizer strTokenizer = 
			new StringTokenizer(processedNFP);

		// Set is used to assure that same keyword appears only once
		HashSet<String> keywords = new HashSet<String>();
		while (strTokenizer.hasMoreTokens()) {
			String token = strTokenizer.nextToken();
			if (!stopWords.contains(token))
				keywords.add(token);
		}

		return keywords;
	}

	/**
	 * implementation of Keyword-based Partial Match Algorithm
	 * 
	 * @param goalKeywords -
	 *            Goal NFP keywords
	 * @param webServiceKeywords -
	 *            Web Service NFP keywords
	 * @return
	 */
	private int partialMatch(HashSet<String> goalKeywords,
			HashSet<String> webServiceKeywords) {
		// how many keywords from goal match with the keywords from web service
		int score = 0;

		// compare the keywords from goal with the keywords from web service
		for (String goalKeyword : goalKeywords) {
			for (String webServiceKeyword : webServiceKeywords) {
				if (goalKeyword.equalsIgnoreCase(webServiceKeyword))
					score++;
			}
		}
		return score;
	}

	/**
	 * Perform the selection if PARTIAL match based on the threshold from the
	 * preference
	 * 
	 * @param scoredWebServices -
	 *            Web services and the associated scores
	 * @param matchedServices -
	 *            the set of matched Web Services
	 * @param highestScore -
	 *            the highest score of web services
	 * @param threshold -
	 *            the selection threshold
	 */
	private void partialMatchSelection(SortedMap<Integer, Set<WebService>> scoredWebServices,
			ArrayList<WebService> matchedServices, int highestScore,
			double threshold) {
		
		for (Integer score : scoredWebServices.keySet()) {
			if (((double) score.intValue() / highestScore) > threshold) {
				matchedServices.addAll(0, scoredWebServices.get(score));
			}
		}
	}

    public List<WebService> discover(Goal goal) throws ComponentException,
            UnsupportedOperationException {
        log.debug("discover(Goal) invoked with " + goal);
        return discover(goal, webServices);
    }


    public Map<Map<WebService, Interface>, Identifier> discover(
    		Goal goal, Ontology rankingOntology) throws org.wsmo.execution.common.exception.ComponentException, UnsupportedOperationException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

	public void addWebService(WebService service) throws DiscoveryException {
		webServices.add(service);
	}

	public void removeWebService(WebService service) {
		if (!webServices.contains(service))
			throw new IllegalArgumentException("Web service not found");
		webServices.remove(service);
	}
}