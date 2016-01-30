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
package ie.deri.wsmx.commons;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.deri.wsmo4j.io.serializer.wsml.SerializerImpl;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.common.IRI;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.Serializer;

/** * Interface or class description
 * * @author Maciej Zaremba
 *
 * Created on 15 Jan 2007
 * Committed by $Author: maciejzaremba $
 * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/commons/WSMXActiveMQPublisher.java,v $, * @version $Revision: 1.1 $ $Date: 2007-02-05 15:25:30 $
 */
public class WSMXActiveMQPublisher {

	/**
	 * Default IRS Events default topic
	 */
	private static final String WSMX_EVENTS_TOPIC = "super.events.wsmx";
	private ConnectionFactory jmsFactory;
	private Connection connection;
	private Session session;
	private Topic topic;
	private MessageProducer publisher;
	private boolean initialized = false;
	
	private Serializer wsmlSerializer;
	private WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap());
	
	/**
	 * 
	 */
	public WSMXActiveMQPublisher() {
		super();

		jmsFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

		try {
			connection = jmsFactory.createConnection();
	        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			topic = session.createTopic(WSMX_EVENTS_TOPIC);
	        publisher = session.createProducer(topic);
	        publisher.setDeliveryMode(DeliveryMode.PERSISTENT);
	        initialized  = true;
	        wsmlSerializer = new SerializerImpl(new HashMap());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			connection.stop();
			connection.close();
			connection = null;
			initialized = false;
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void publish(Instance instance) {
		if (!initialized)
			return;
		try {
			TextMessage om = session.createTextMessage(serializeEvent(instance));
			publisher.send(om);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param instance
	 * @return
	 */
	private String serializeEvent(Instance instance) {
		String response = "";
	    StringBuffer sb = new StringBuffer();
	    IRI ontoIRI = this.wsmoFactory.createIRI( "http://www.example.org#anonOntology" + Helper.getRandomLong());
	    Ontology ontology = this.wsmoFactory.createOntology(ontoIRI);
	    try {
			ontology.addInstance(instance);
		    this.wsmlSerializer.serialize(new TopEntity[]{(TopEntity)ontology}, sb);
		    response = sb.toString();
		    System.out.println(response);
		    return response;
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

}

 