package ie.deri.wsmx.asm;

import java.util.List;

public interface ContextInterface {

	/**
	 * @param inID The id of the in instance.
	 * @return all the out terms if the in term exists in this context
	 */
	public abstract List<String> getOutTerms(String inID);

}