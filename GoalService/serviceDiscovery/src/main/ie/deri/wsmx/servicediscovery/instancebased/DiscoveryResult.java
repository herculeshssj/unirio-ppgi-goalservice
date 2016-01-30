package ie.deri.wsmx.servicediscovery.instancebased;

import java.util.ArrayList;
import java.util.List;

import org.wsmo.service.WebService;

public class DiscoveryResult {
	List<Pair<String, Float>> variableValues = new ArrayList<Pair<String, Float>>();
	String rankingCriteria;
	WebService ws;
	boolean entails;
	
	public DiscoveryResult(){
		super();
	}
	
	public DiscoveryResult(WebService ws, boolean entails, String rankingCriteria){
		super();
		this.ws = ws;
		this.entails = entails;
		this.rankingCriteria = rankingCriteria;
	}
	
	public Float getVariableValue(String var) {
		for (Pair<String, Float> pair : variableValues)
		{
			if (pair.getFirst().equals(var))
				return pair.getSecond();
		}
		return null;
	}

	public void setVariableValues(String variable, Float value) {
		variableValues.add(new Pair<String,Float>(variable, value));
	}	
	
    private class Pair <E, F> {
        private E e;
        private F f;
        
        public Pair (E theFirst, F theSecond){
            this.e = theFirst;
            this.f = theSecond;
        }
        
        public E getFirst(){
            return e;
        }
        
        public F getSecond(){
            return f;
        }
    }
}
