/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */

package ie.deri.wsmx.executionsemantic;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.wsmo.common.Entity;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;

/** * Final Executions Semantics response. Contains both Goal and Web service between which communication has been 
 *    carried out. All messages received from the Web service during that interaction are also preserved. 
 *    
 * * @author Maciej Zaremba
 *
 * Created on 2006-05-05
 * Committed by $Author: maciejzaremba $
 * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/executionsemantic/ExecutionSemanticsFinalResponse.java,v $, * @version $Revision: 1.4 $ $Date: 2006-08-23 16:07:00 $
 */
public class ExecutionSemanticsFinalResponse implements Serializable{

	private static final long serialVersionUID = 5357797594651670972L;
	//discovered web service
	private Goal goal;
	//discovered web service
	private List<WebService> webServices;
	//messages send to Web service
	private Set<Entity> sendMessages;
	//messages received from Web service
	private Set<Entity> receivedMessages;
	//indicates whether overall execution was successful 
	private boolean isSuccessful;
	//final message
	private String msg; 
	
	//execution semantics
	private AbstractExecutionSemantic executionSemantic;
	
	public ExecutionSemanticsFinalResponse(Goal goal, List<WebService> webServices, Set<Entity> sendMessages, Set<Entity> receivedMessages, boolean isSuccessful, AbstractExecutionSemantic executionSemantic, String msg) {
		super();
		this.goal = goal;
		this.webServices = webServices;
		this.sendMessages = sendMessages;
		this.receivedMessages = receivedMessages;
		this.isSuccessful = isSuccessful;
		this.executionSemantic = executionSemantic;
		this.msg = msg;
	}

	public Goal getGoal() {
		return goal;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public String getMsg() {
		return msg;
	}

	public Set<Entity> getReceivedMessages() {
		return receivedMessages;
	}

	public Set<Entity> getSendMessages() {
		return sendMessages;
	}

	public WebService getWebservice() {
		return webServices.get(0);
	}
	
	public List<WebService> getWebservices() {
		return webServices;
	}

	public AbstractExecutionSemantic getExecutionSemantic() {
		return executionSemantic;
	}

}
 