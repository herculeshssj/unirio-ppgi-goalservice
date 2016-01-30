package org.wsmo.execution.common.nonwsmo;

import java.util.Set;

import org.omwg.logicalexpression.Molecule;
import org.wsmo.service.signature.Grounding;

public interface ResponseModifierInterface {

	Set<Molecule> getMolecules();

	Grounding getGrounding();

}