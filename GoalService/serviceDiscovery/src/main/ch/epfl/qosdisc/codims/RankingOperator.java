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
import org.wsmo.common.TopEntity;
import org.wsmo.service.Goal;
import org.wsmo.service.Interface;

import java.util.*;

import ch.epfl.codimsd.qeef.*;
import ch.epfl.codimsd.qeef.types.StringType;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qeef.relational.*;
import ch.epfl.codimsd.qep.OpNode;

import ch.epfl.qosdisc.database.WSMLStore;
import ch.epfl.qosdisc.operators.*;

/**
 * This operator performs the ranking.
 * 
 * @author Sebastian Gerlach
 */
public class RankingOperator extends ch.epfl.codimsd.qeef.Operator {
	
    
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(RankingOperator.class);
    
    /**
     * Set to true after we are done.
     */
    private boolean done;
    
	/**
	 * Constructor.
	 * 
	 * @param id Operator id.
	 * @param opNode Operatore node.
	 */
	public RankingOperator(int id, OpNode opNode) {
		
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
    	
    	done = false;
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.codimsd.qeef.Operator#getNext(int)
     */
    public DataUnit getNext(int consumerID) throws  Exception {
    	
    	// Check whether we have already come through here.
    	if(done)
    		return null;
    	
    	List<InterfaceExt> ifs = new ArrayList<InterfaceExt>();

    	Tuple t = null;
    	while((t = (Tuple) super.getNext(consumerID)) != null) {
    		
    		StringType sie = (StringType)t.getData(0);
    		InterfaceExt ie = InterfaceExt.deserializeFromString(sie.toString());
    		log.debug("Received "+ie.getInterface().getIdentifier().toString());
    		ifs.add(ie);
    	}
    	
		// Load a goal.
		BlackBoard bb = BlackBoard.getBlackBoard();
        TopEntity[] ent = WSMLStore.importWSMLFromString(new StringBuffer((String)bb.get("goalstring")),true);
        Goal goal = null;
        for(TopEntity e : ent) {
        	if(Goal.class.isAssignableFrom(e.getClass()))
        		goal=(Goal)e;
        }

		RankingInfo ri = new RankingInfo(expandGoal(goal));
    	ri.performRanking(ifs);
    	
    	// Finished, return one tuple with all our stuff inside.
    	StringBuffer sb = new StringBuffer();
    	for(InterfaceExt ie : ifs) {
    		sb.append(ie.getServiceDescription().getIdentifier().toString());
    		sb.append("\\");
    		sb.append(ie.getInterface().getIdentifier().toString());
    		sb.append("\\");
    		sb.append(ie.getRanking());
    		sb.append("\\");
    	}
    	done = true;
    	t = new Tuple();
    	t.addData(new StringType(sb.toString()));
    	return t;
    	
    }
    
	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.Operator#setMetadata(ch.epfl.codimsd.qeef.Metadata[])
	 */
	@Override
	public void setMetadata(Metadata[] prMetadata) {

		// Setting RankingOperator metadata
		log.debug("Setting metadata in Rank.");
		
		try {
			 
			metadata = new Metadata[1];
			metadata[0] = new TupleMetadata();
			Column column = new Column("StringType", Config.getDataType(Constants.STRING), 1, 0, false, 0);
			metadata[0].addData(column);

		} catch (Exception ex) {
			 // This is never the case
		}
	}
}
