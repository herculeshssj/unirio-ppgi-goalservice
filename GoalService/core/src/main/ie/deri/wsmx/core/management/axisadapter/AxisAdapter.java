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

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.commons.Pair;
import ie.deri.wsmx.core.WSMXKernel;
import ie.deri.wsmx.core.management.AdapterServerSocketFactory;
import ie.deri.wsmx.core.management.PlainAdapterServerSocketFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.ws.Endpoint;

import org.apache.log4j.Logger;
import org.ipsuper.nexcom.services.CEOApprovalWebService;
import org.ipsuper.nexcom.services.LegalDepartmentWebService;
import org.ipsuper.nexcom.services.VoIPSEEEntryPoint;
import org.ipsuper.nexcom.services.WholesaleSupplierWebService;
import org.ipsuper.prereview.PackagerWebService;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.BoundedThreadPool;
import org.wsmx.jaxws.WSMXEntryPoints;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;


/**
 * AxisAdapter is an SOAP adapter. It runs a Jetty application server that in turn hosts Axis. This approach allows one to expose WSMX 
 * entrypoints as an operations in Web Services. Axis automatically generates a WSDL files for appropriete classes. MBeanServer object is registered
 * in Axis web application context, therefore it can be referenced from deployed Web Services giving the access to the WSMX infrustructure and 
 * all WSMX components (MBeans) hosted within one machine. 
 *
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2007-10-11 14:35:01 $
 */ 
public class AxisAdapter implements AxisAdapterMBean, MBeanRegistration {
    static Logger logger = Logger.getLogger(AxisAdapter.class);
    
	private Server jettyWebServer;
		
	/** HTTP Server for publishing JAX-WS endpoints */
	private HttpServer httpServer; 
    
    /**
     * Port to listen for connections
     */
    int port = 8050;

    /**
     * Host where to set the server socket
     */
    private String host = "localhost";

    /**
     * Target server
     */
    private MBeanServer server;
    
    private String strRMI;

    /**
     * Server socket
     */
    ServerSocket serverSocket;

    /**
     * Indicates whether the server is running
     */
    boolean alive;
    
    String authenticationMethod = "none";

    String realm = "WSMX";

    private Map<String, String> authorizations = new HashMap<String, String>();

    private AdapterServerSocketFactory socketFactory = null;

    private ObjectName factoryName;

    Date startDate;

    long requestsCount;

    public AxisAdapter(int port, String host, String strRMI) {
		super();
		this.port = port;
		this.host = host;
		this.strRMI = strRMI;
	}

	/**
     * Overloaded constructor to allow the port to be set. The reason this was
     * added was to allow the loading of this adaptor by the dynamic loading
     * service of the MBean server and have the port set from a param in the
     * mlet file. 
     * <p>
     * This constructor uses the default host or the host must be set later.
     * 
     * @param port
     *            The port on which the HttpAdapter should listen
     */
    public AxisAdapter(int port) {
        this.port = port;
    }

    /**
     * Overloaded constructor to allow the host to be set. The reason this was
     * added was to allow the loading of this adaptor by the dynamic loading
     * service of the MBean server and have the host set from a param in the
     * mlet file. 
     * <p>
     * This constructor uses the default port or the port must be set later.
     * 
     * @param host
     *            The host on which the HttpAdapter should listen
     */
    public AxisAdapter(String host) {
        this.host = host;
    }

    /**
     * Overloaded constructor to allow the port to be set. The reason this was
     * added was to allow the loading of this adaptor by the dynamic loading
     * service of the MBean server and have the port set from a param in the
     * mlet file.
     * 
     * @param port
     *            The port on which the HttpAdapter should listen
     * @param host
     *            The host on which the HttpAdapter should listen
     */
    public AxisAdapter(int port, String host) {
        this.port = port;
        this.host = host;
    }

    /**
     * Sets the value of the server's port
     * 
     * @param port
     *            the new port's value
     */
    public void setPort(int port) {
        if (alive) {
            throw new IllegalArgumentException(
                    "Not possible to change port with the server running");
        }
        this.port = port;
    }

    /**
     * Returns the port where the server is running on. Default is 8080
     * 
     * @return HTTPServer's port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the host name where the server will be listening
     * 
     * @param host
     *            Server's host
     */
    public void setHost(String host) {
        if (alive) {
            throw new IllegalArgumentException(
                    "Not possible to change port with the server running");
        }
        this.host = host;
    }

    /**
     * Return the host name the server will be listening to. If null the server
     * listen at the localhost
     * 
     * @return the current hostname
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the Authentication Method.
     * 
     * @param method
     *            none/basic/digest
     */
    public void setAuthenticationMethod(String method) {
        if (alive) {
            throw new IllegalArgumentException(
                    "Not possible to change authentication method with the server running");
        }
        if (method == null
                || !(method.equals("none") || method.equals("basic") || method
                        .equals("digest"))) {
            throw new IllegalArgumentException(
                    "Only accept methods none/basic/digest");
        }
        this.authenticationMethod = method;
    }

    /**
     * Authentication Method
     * 
     * @return authentication method
     */
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    /**
     * Sets the object which create the server sockets
     * 
     * @param factory
     *            the socket factory
     */
    public void setSocketFactory(AdapterServerSocketFactory factory) {
        this.factoryName = null;
        this.socketFactory = factory;
    }

    /**
     * Sets the factory's object name which will create the server sockets
     * 
     * @param factoryName
     *            the socket factory
     */
    public void setSocketFactoryName(ObjectName factoryName) {
        this.socketFactory = null;
        this.factoryName = factoryName;
    }

    /**
     * Sets the factory's object name which will create the server sockets
     * 
     * @param factoryName
     *            the socket factory
     */
    public void setSocketFactoryNameString(String factoryName)
            throws MalformedObjectNameException {
        this.socketFactory = null;
        this.factoryName = new ObjectName(factoryName);
    }

    /**
     * Indicates whether the server's running
     * 
     * @return The active value
     */
    public boolean isActive() {
        return alive;
    }

    /**
     * Starting date
     * 
     * @return The date when the server was started
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Requests count
     * 
     * @return The total of requests served so far
     */
    public long getRequestsCount() {
        return requestsCount;
    }

    /**
     * Starts the server
     */
    public void start() throws IOException {
   
		  jettyWebServer = new Server();
		  Connector connector= new SelectChannelConnector();
		  connector.setPort(port);
		  jettyWebServer.setConnectors( new Connector[]{connector} );
		  BoundedThreadPool threadPool = new BoundedThreadPool();
		  threadPool.setMinThreads( 10 );
		  threadPool.setMaxThreads( 100 );
		  jettyWebServer.setThreadPool( threadPool );
		  Helper.setAttribute("MBeanServer", server);
          Helper.setAttribute("strRMI", strRMI);	
          
		  File f = new File(WSMXKernel.KERNEL_LOCATION + "/axisWSMXEntryPoints.war"); 
		  
		  WebAppContext wac = new WebAppContext();
		  wac.setWar(f.getCanonicalPath());
		  wac.setContextPath("/axis");
		  
		  ContextHandlerCollection contexts = new ContextHandlerCollection();
		  contexts.addHandler(wac);

		  jettyWebServer.addHandler(contexts);
		  jettyWebServer.setStopAtShutdown( true);
		  jettyWebServer.setGracefulShutdown(0);
		  
		  try {
			jettyWebServer.start();
			while (!jettyWebServer.isRunning()){
				Thread.sleep(50);
			}
			logger.info("Axis2 started on port " + port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("There are problems with startting Axis2");
		}
		
		// start server for JAX WS services and publish endpoints
		httpServer = HttpServer.create(new InetSocketAddress(8001), -1);
		httpServer.setExecutor(null);
		httpServer.start();

		Pair[] endpointsAndHttpContexts = {
				new Pair<Endpoint, HttpContext>(Endpoint.create(new WholesaleSupplierWebService()),
						httpServer.createContext("/VoIPProviders")),
				new Pair<Endpoint, HttpContext>(Endpoint.create(new VoIPSEEEntryPoint()),
						httpServer.createContext("/VoIPSEEEntryPoint")),
				new Pair<Endpoint, HttpContext>(Endpoint.create(new CEOApprovalWebService()),
						httpServer.createContext("/CEOApproval")),
				new Pair<Endpoint, HttpContext>(Endpoint.create(new LegalDepartmentWebService()),
						httpServer.createContext("/LegalDepartment")),
				new Pair<Endpoint, HttpContext>(Endpoint.create(new PackagerWebService()),
						httpServer.createContext("/Packager")),						
				new Pair<Endpoint, HttpContext>(Endpoint.create(new WSMXEntryPoints()),
						httpServer.createContext("/jaxws/WSMXEntryPoints")),
		};
		
		for (Pair p : endpointsAndHttpContexts) {
			Pair<Endpoint, HttpContext> endpointAndHttpCtx = (Pair<Endpoint, HttpContext>) p;
			endpointAndHttpCtx.getFirst().publish( endpointAndHttpCtx.getSecond() );
		}
		logger.info("JAX-WS Web services started on port 8001");

		MBeanServer mBeanServer = (MBeanServer) Helper.getAttribute("MBeanServer");
		VoIPSEEEntryPoint.setMBeanServer(mBeanServer);

		startDate = new Date();
		requestsCount = 0;
     }

    /**
     * Restarts the server. Useful when changing the Server parameters
     * @throws Exception 
     * 
     * @deprecated
     */
    @Deprecated
	public void restart() throws Exception {
        stop();
        start();
    }

    /**
     * Stops the Axis daemon
     */
    public void stop() {
        if (alive)
			alive = false;
        
		try {
			if (jettyWebServer != null) {
				logger.info("Trying to stop Axis Adapter...");
				jettyWebServer.stop();				
				while (!jettyWebServer.isStopped())
					Thread.sleep(50);	
			}
			logger.info("Axis Adapter stopped.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (httpServer != null) {
				logger.info("Trying to stop JAX-WS web services...");
				httpServer.stop(1);
				logger.info("JAX-WS web services stopped.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Gathers some basic data
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws java.lang.Exception {
        this.server = server;
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws java.lang.Exception {
        // stop the server
        stop();
    }

    public void postDeregister() {
    }

    private ServerSocket createServerSocket() throws IOException {
        if (socketFactory == null) {
            if (factoryName == null) {
                socketFactory = new PlainAdapterServerSocketFactory();
                return socketFactory.createServerSocket(port, 50, host);
            }
			try {
			    return (ServerSocket) server.invoke(factoryName,
			            "createServerSocket", new Object[] {
			                    new Integer(port), new Integer(50), host },
			            new String[] { "int", "int", "java.lang.String" });
			} catch (Exception x) {
			    logger
			            .error(
			                    "Exception invoking AdapterServerSocketFactory via MBeanServer",
			                    x);
			}
        } else {
            return socketFactory.createServerSocket(port, 50, host);
        }

        return null;
    }

    boolean isUsernameValid(String username, String password) {
        if (authorizations.containsKey(username)) {
            return password.equals(authorizations.get(username));
        }
        return false;
    }

}
