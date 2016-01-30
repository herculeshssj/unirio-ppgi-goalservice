/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ch.epfl.qosdisc.codims;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.*;
import ch.epfl.codimsd.qeef.types.*;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qeef.relational.*;
import ch.epfl.codimsd.qep.OpNode;

/**
 * This operator outputs all the services we want to be working on.
 * 
 * @author Sebastian Gerlach
 */
public class ListServicesOperator extends Acesso {
	
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(ListServicesOperator.class);

	/**
	 * Constructor.
	 * 
	 * @param id Operator id.
	 * @param opNode Operatore node.
	 */
	public ListServicesOperator(int id, OpNode opNode) {
		
		// Call superconstructor.
		super(id);
		
		DataSourceManager dsManager = DataSourceManager.getDataSourceManager();
		dataSource = (DataSource) dsManager.getDataSource(opNode.getOpTimeStamp());
		
		metadata = null;
	}
	
	/**
     * Operator cleanup code.
     */
    public void close() throws Exception {

    	log.debug("Closing operator.");
    	super.close();
    }
    
    /**
     * Operator startup code.
     * 
     * @throws Exception
     */
    public void open() throws  Exception {

    	log.debug("Opening operator.");
    	super.open();
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.codimsd.qeef.Operator#getNext(int)
     */
    public DataUnit getNext(int consumerID) throws  Exception {
    	
    	// Read next element from datasource.
    	instance = (dataSource).read();    	
    	if (instance == null)
    		return null;
    	
    	// Copy the string over into our output tuple.
    	Tuple t = (Tuple) instance;
    	OracleType oracleObject = (OracleType) t.getData(0);
    	StringType stringType = new StringType((String)oracleObject.getObject());
    	
    	Tuple newTuple = new Tuple();
    	newTuple.addData((Type)stringType);
    	
		return newTuple;
    }
    
	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.Operator#setMetadata(ch.epfl.codimsd.qeef.Metadata[])
	 */
//	@Override
	public void setMetadata(Metadata[] prMetadata) {

		// This just seems to be required by CoDIMS?
		log.debug("Setting metadata.");
		metadata = new Metadata[1];
		metadata[0] = new TupleMetadata();
		
		try {
			Column column = new Column("StringType", 
				Config.getDataType(Constants.STRING), 1, 0, false, 0);
			metadata[0].addData(column);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.Operator#getMetadata(int)
	 */
//	@Override
//	public Metadata getMetadata(int idConsumer) {
//
//		// Return something.
//		return new TupleMetadata();
//	}

}
