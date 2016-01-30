package ie.deri.wsmx.choreography;

import java.util.Set;

import org.omwg.logicalexpression.Molecule;
import org.wsmo.execution.common.nonwsmo.ResponseModifierInterface;
import org.wsmo.service.signature.Grounding;

public class ResponseModifier implements ResponseModifierInterface {
	Grounding grounding;
	Set<Molecule> molecules;
	/**
	 * @return the grounding
	 */
	public Grounding getGrounding() {
		return grounding;
	}

	/**
	 * @return the molecules
	 */
	public Set<Molecule> getMolecules() {
		return molecules;
	}

	public ResponseModifier(Grounding grounding){
		new ResponseModifier(grounding, null);
	}
	
	public ResponseModifier(Grounding grounding,Set<Molecule> molecules){
		this.grounding = grounding;
		this.molecules = molecules;
	}
}
