package org.deri.wsmx.mediation.ooMediator.functoids;

public interface ServiceResolver {
	
	public Object invokeService(String indentifier, Object[] parameters);

}
