package org.deri.wsmx.mediation.ooMediator.mapper.mappings;

import org.omwg.mediation.language.objectmodel.api.Id;
import org.omwg.ontology.DataValue;

public class DataValueIdPlaceholder extends Id {

	private DataValue dataValue = null; 
	
	
	public DataValueIdPlaceholder(DataValue dataValue){
		this.dataValue = dataValue;
	}
	
	@Override
	public String plainText() {
		return dataValue.getValue().toString();
	}

	public DataValue getDataValue() {
		return dataValue;
	}

	public String toString(){
		return dataValue.getValue().toString();
	}
}
