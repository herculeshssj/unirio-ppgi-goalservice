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

package ie.deri.wsmx.core.management.axisadapter;

import ie.deri.wsmx.core.management.AdapterServerSocketFactory;
import ie.deri.wsmx.core.management.webfrontend.ProcessorMBean;

import java.io.IOException;
import java.util.Date;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


/**
 * Management interface for the HttpAdapter MBean.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: maciejzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/axisadapter/AxisAdapterMBean.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2006-01-25 16:24:08 $
 */ 
public interface AxisAdapterMBean
{
   /**
    * Sets the value of the server's port
    *
    * @param port the new port's value
    */
   public void setPort(int port);

   /**
    * Returns the port where the server is running on. Default is 8080
    *
    * @return HTTPServer's port
    */
   public int getPort();

   /**
    * Sets the host name where the server will be listening
    *
    * @param host Server's host
    */
   public void setHost(java.lang.String host);

   /**
    * Return the host name the server will be listening to. If null the server listen at the localhost
    *
    * @return the current hostname
    */
   java.lang.String getHost();

   /**
    * Sets the Authentication Method.
    *
    * @param method none/basic/digest
    */
//   public void setAuthenticationMethod(String method);

   /**
    * Authentication Method
    *
    * @return authentication method
    */
//   public String getAuthenticationMethod();

   /**
    * Sets the object which will post process the XML results.
    * The last value set between the setPostProcessor and setPostProcessorName will be the valid one
    *
    * @param processor a Post processor object
    */
//   public void setProcessor(ProcessorMBean processor);

   /**
    * Sets the classname of the object which will post process the XML results.
    * The adaptor will try to build the object and use the processor name ObjectName to register it.
    * The class name has to implement ProcessorMBean and be MBean compliant
    *
    * @param processorClass a Post processor object
    */
//   public void setProcessorClass(String processorClass);

   /**
    * Sets the object name of the PostProcessor MBean. If ProcessorClass is set the processor will be created
    *
    * @param processorName a Post processor object
    */
//   public void setProcessorNameString(String processorName) throws MalformedObjectNameException;

   /**
    * Sets the object name which will post process the XML result. The last value set between the setPostProcessor and setPostProcessorName will be the valid one. The MBean will be verified to be of instance HttpPostProcessor
    *
    * @param processorName The new processorName value
    */
//   public void setProcessorName(ObjectName processorName);

   /**
    * Returns the ObjectName of the processor set by {@link #setProcessorName}
    */
//   public ObjectName getProcessorName();

   /**
    * Sets the object which create the server sockets
    *
    * @param factory the socket factory
    */
//   public void setSocketFactory(AdapterServerSocketFactory factory);

   /**
    * Sets the factory's object name which will create the server sockets
    *
    * @param factoryName the socket factory
    */
//   public void setSocketFactoryName(ObjectName factoryName);

   /**
    * Sets the factory's object name which will create the server sockets
    *
    * @param factoryName the socket factory
    */
//   public void setSocketFactoryNameString(String factoryName) throws MalformedObjectNameException;

   /**
    * Indicates whether the server's running
    *
    * @return The active value
    */
   public boolean isActive();

   /**
    * Starting date
    *
    * @return The date when the server was started
    */
   public Date getStartDate();

   /**
    * Requests count
    *
    * @return The total of requests served so far
    */
   public long getRequestsCount();

   /**
    * Adds a command processor object
    */
//   public void addCommandProcessor(String path, HttpCommandProcessor processor);

   /**
    * Adds a command processor object by class
    */
//   public void addCommandProcessor(String path, String processorClass);

   /**
    * Removes a command processor object by class
    */
//   public void removeCommandProcessor(String path);

   /**
    * Starts the server
    */
   public void start() throws IOException;

   /**
    * Stops the HTTP daemon
    */
   public void stop();

   /**
    * Adds an authorization pair as username/password
    */
//   public void addAuthorization(String username, String password);
}
