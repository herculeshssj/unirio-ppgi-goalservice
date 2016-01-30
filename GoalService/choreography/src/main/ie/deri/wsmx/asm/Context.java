package ie.deri.wsmx.asm;

import java.util.ArrayList;
import java.util.List;

import org.omwg.logicalexpression.terms.Term;

/**
 * @author Raluca Zaharia
 *
 * Keeps a list of in instances and the list of out instances that were used by a rule.
 */
public class Context implements ContextInterface {
	private List<String> inTerms;
	private List<String> outTerms;
	private State state;
	/**
	 * Constructor.
	 * @param state The current state of the asm.
	 */
	public Context(State state){
		this.state = state;
		inTerms = new ArrayList<String>();
		outTerms = new ArrayList<String>();
	}
	
	/**
	 * The term is stored if the concept is in or out.
	 * @param term
	 * @param concept
	 * @param isOutput 
	 */
	public void addTerm(Term term, Term concept, boolean isOutput) {
		if(state.isInConcept(concept)) {
			inTerms.add(term.toString());
		} else if(isOutput && state.isOutConcept(concept)) {
			outTerms.add(term.toString());
		}
	}
	/* (non-Javadoc)
	 * @see ie.deri.wsmx.asm.ContextInterface#getOutTerms(java.lang.String)
	 */
	public List<String> getOutTerms(String inID) {
		List<String> toReturn = null;
		if(inTerms.contains(inID)) {
			toReturn = outTerms;
		}
		return toReturn;
	}
}
