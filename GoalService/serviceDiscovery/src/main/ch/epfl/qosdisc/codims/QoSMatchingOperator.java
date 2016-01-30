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

import java.util.*;

import org.apache.log4j.Logger;
import org.wsmo.common.TopEntity;
import org.wsmo.service.*;

import ch.epfl.codimsd.qeef.*;
import ch.epfl.codimsd.qeef.relational.*;
import ch.epfl.codimsd.qeef.types.StringType;
import ch.epfl.codimsd.qep.OpNode;

import ch.epfl.qosdisc.operators.*;
import ch.epfl.qosdisc.database.*;


/**
 * Performs QoS matching.
 * 
 * @author Sebastian Gerlach
 */
public class QoSMatchingOperator extends ch.epfl.codimsd.qeef.Operator {
	    
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(QoSMatchingOperator.class);
        
    /**
     * The matching processor.
     */
    private QoSMatchingLite qm;
    
    /**
     * The objects waiting to be sent on.
     */
    private Stack<Tuple> pending = new Stack<Tuple>();
    
	/**
	 * Constructor.
	 * 
	 * @param id Operator id.
	 * @param opNode Operatore node.
	 */
	public QoSMatchingOperator(int id, OpNode opNode) {
		
		// Call superconstructor.
		super(id);
	}
	
	/**
     * Operator cleanup code.
     */
    public void close() throws Exception {

    	log.debug("Closing operator.");
    	super.close(); 
    }
    	
    /**
     * Create a goal description from a WSMO goal.
     * 
     * @param goal The WSMO goal.
     * @return The goal description.
     */
    private static GoalInterfaceExt expandGoal(Goal goal) {
        
        // Create goal descriptions.
        assert(goal.listInterfaces().size() == 1);
        return new GoalInterfaceExt(goal, (Interface) goal.listInterfaces().iterator().next());
    }

    /**
     * Operator startup code.
     * 
     * @throws Exception
     */
    public void open() throws  Exception {
 
    	log.debug("Opening operator.");
    	super.open();

    	// Clear pending objects.
		pending.clear();
		
		// Load a goal.
		BlackBoard bb = BlackBoard.getBlackBoard();
        TopEntity[] ent = WSMLStore.importWSMLFromString(new StringBuffer((String)bb.get("goalstring")),true);
        Goal goal = null;
        for(TopEntity e : ent) {
        	if(Goal.class.isAssignableFrom(e.getClass()))
        		goal=(Goal)e;
        }
		
		// Recover all properties from the blackboard.
		log.debug("Reading properties from the BlackBoard.");
		Properties props = new Properties();
		for(Map.Entry<String, Object> o : bb.getHashtable().entrySet()) {
			
			if(o.getValue().getClass() == String.class) {
				log.debug("Key : " + o.getKey()+ ", value : " + (String)o.getValue());
				props.setProperty(o.getKey(), (String)o.getValue());
			}
		}
		PropertySet.setup(".");
		PropertySet.setProperties(props);
		log.debug("Properties ok in the QoSMatchingOperator.");
		
		// Connection.open(PropertySet.props);
		
		// Construct matching processor.
		qm = new QoSMatchingLite(expandGoal(goal));
		log.debug("End opening operator QoSMatchingOperator.");
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.codimsd.qeef.Operator#getNext(int)
     */
    public DataUnit getNext(int consumerID) throws  Exception {

    	// If we have something waiting, get rid of that first.
    	if(pending.size()>0) {
    		return pending.pop();
    	}

    	// Otherwise move on to the next service.
    	while(pending.size() == 0) {

    		// Get next input tuple.
    		Tuple current = (Tuple)super.getNext(consumerID);
    		if(current == null)
    			return null;
    		
    		// Recover IRI from input tuple.
    		String iri = current.getData(0).toString();
    		log.debug("Fetching "+iri);
    		System.out.println("Fetching "+iri);
    		
        	Collection<TopEntity> e = WSMLStore.getEntities(iri,WSMLStore.SERVICE);
        	WebService service = (WebService)e.iterator().next();
        	
        	// Get all interfaces.
        	for(Object o : service.listInterfaces()) {
        		
        		InterfaceExt ie = new InterfaceExt(service,(Interface)o,-1);
        		if(qm.matchService(ie)) {
        			
        			// This is completely crap for now. I just hope I will not be condemned
        			// to write a custom serializer for InterfaceExt. That would suck big time.
        			Tuple t = new Tuple();
        			t.addData(new StringType(ie.serializeToString()));
        			pending.push(t);
        		}
        	}
    	}
    	
    	// Return an item from the pending list.
    	return pending.pop();
    }
    
	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.Operator#setMetadata(ch.epfl.codimsd.qeef.Metadata[])
	 */
	@Override
	public void setMetadata(Metadata[] metadata) {

		// This just seems to be required by CoDIMS?
		log.debug("Setting metadata.");
		this.metadata = metadata;
	}
	
//	/* (non-Javadoc)
//	 * @see ch.epfl.codimsd.qeef.Operator#getMetadata(int)
//	 */
//	@Override
//	public Metadata getMetadata(int idConsumer) {
//
//		// Return something.
//		return new TupleMetadata();
//	}

}
