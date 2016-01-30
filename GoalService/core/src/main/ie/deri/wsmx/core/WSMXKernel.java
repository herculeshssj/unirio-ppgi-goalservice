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

package ie.deri.wsmx.core;

import ie.deri.wsmx.core.codebase.Codebase;
import ie.deri.wsmx.core.codebase.UnifyingClassLoader;
import ie.deri.wsmx.core.configuration.ComponentConfiguration;
import ie.deri.wsmx.core.configuration.KernelConfiguration;
import ie.deri.wsmx.core.configuration.descriptor.KernelXMLConfigurationFile;
import ie.deri.wsmx.core.configuration.properties.KernelPropertiesConfigurationFile;
import ie.deri.wsmx.core.logging.CleanPatternLayout;
import ie.deri.wsmx.core.management.axisadapter.AxisAdapter;
import ie.deri.wsmx.core.management.httpadapter.HttpAdapter;
import ie.deri.wsmx.core.management.kerneldata.KernelData;
import ie.deri.wsmx.core.management.sshadapter.KeyNotAvailableException;
import ie.deri.wsmx.core.management.sshadapter.SSHAdapter;
import ie.deri.wsmx.core.management.webfrontend.XSLTProcessor;
import ie.deri.wsmx.exceptions.WSMXConfigurationException;
import ie.deri.wsmx.exceptions.WSMXException;
import ie.deri.wsmx.scheduler.DistributedScheduler;
import ie.deri.wsmx.scheduler.Environment;
import ie.deri.wsmx.wrapper.ManagabilityWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.BindException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.management.Descriptor;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.loading.ClassLoaderRepository;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.jmx.LoggerDynamicMBean;

/**
 * The WSMX kernel. Based on a microkernel architecture it is host
 * to a set of components responsible for the execution of Semantic
 * Web Services.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: maciejzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/WSMXKernel.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.71 $ $Date: 2007-10-11 14:35:32 $
 */
public class WSMXKernel implements NotificationListener, Runnable {
	
	public static final ObjectName OBJECT_NAME;
    public static final File KERNEL_LOCATION;
	public static final File HOST_KEY_LOCATION;
    public static final File XML_CONFIG_LOCATION;
    public static final File PROPERTIES_CONFIG_LOCATION;
    public static final File INFORMATIVE_PROPERTIES_LOCATION;
    public static final InetAddress INET_ADDRESS;
    public static final String HOSTNAME;
    public static final String IP_ADDRESS;    

    public static final String VERSION = "v0.05b84";
    public static final String OBJECT_NAME_STRING = "core:name=WSMXKernel";
	public static final String KERNELCONFIGURATION_SCHEMA = "/META-INF/kernelconfiguration.xsd";
    public static final String XML_CONFIGURATION_NAME = "config.xml";
    public static final String PROPERTIES_CONFIGURATION_NAME = "config.properties";
    public static final String INFORMATIVE_PROPERTIES_NAME = "info.properties";
    public static final String DSA_HOST_KEY_NAME = "dsa_host_key";
    public static final String DEFAULT_SPACEADDRESS = "localhost";

    public static final int MAX_BIND_ATTEMPTS = 10;
    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_AXIS_PORT = 8050;
    public static final int DEFAULT_SSH_PORT = 22;
    //TODO include these ports in config
    public static final int DEFAULT_RMI_JRMP_PORT = 7010;
    public static final int DEFAULT_RMI_IIOP_PORT = 7020;
    
    //there's only one kernel per systemclassloader, so certain fields are static
    private static Codebase codebase;
	static ClassLoaderRepository repository;
	static Logger logger;
	
	private final MBeanServer mBeanServer;
	private Set<JMXConnectorServer> connectors = new HashSet<JMXConnectorServer>();
	private long startupTimestamp;
	private int bindAttempts = 0;
    private Thread kernelThread;
	private volatile boolean alive = true;
	
	private Map<ObjectName, DistributedScheduler> schedulerCache = new HashMap<ObjectName, DistributedScheduler>();
	private Map<ComponentConfiguration, Object> componentconfigCache = new HashMap<ComponentConfiguration, Object>();
	private Map<ObjectName, ObjectName> loggerCache = new HashMap<ObjectName, ObjectName>();

	private ObjectName httpAdapterName, xsltProcessorName, sshDaemonName, axisAdapterName;
	private SSHAdapter sshDaemon;
        
	private KernelConfiguration kernelConfiguration;	
	private RequiredModelMBean kernelMBean;
	
	private String strRMI; 
		
	static {
		System.setProperty("java.system.class.loader", UnifyingClassLoader.class.getCanonicalName());
		logger = Logger.getLogger(WSMXKernel.class);		
    	ObjectName name;
    	try {
    		name = new ObjectName(OBJECT_NAME_STRING);
    	} catch (MalformedObjectNameException e) {
    		throw new Error("ObjectName initialization failed:" + e.getMessage());
    	}
    	OBJECT_NAME = name;
    
        URI schema = null;
		try {
			schema = WSMXKernel.class.getResource(KERNELCONFIGURATION_SCHEMA).toURI();
		} catch (URISyntaxException e) {
			//we don't have a logger yet
			System.err.println("Failed to determine location of kernel. Try moving to a different path. Exodus.");
			System.exit(1);
		}        
        String protocolLess = schema.getSchemeSpecificPart();
	    int pos = protocolLess.lastIndexOf('!');
	    protocolLess = protocolLess.substring(5, pos); // remove "file:"	   
        KERNEL_LOCATION = new File(protocolLess).getParentFile();
        
        HOST_KEY_LOCATION = new File(WSMXKernel.KERNEL_LOCATION + "/" + DSA_HOST_KEY_NAME);
        XML_CONFIG_LOCATION = new File(KERNEL_LOCATION + "/" + XML_CONFIGURATION_NAME);
        PROPERTIES_CONFIG_LOCATION = new File(KERNEL_LOCATION + "/" + PROPERTIES_CONFIGURATION_NAME);
        INFORMATIVE_PROPERTIES_LOCATION = new File(KERNEL_LOCATION + "/" + INFORMATIVE_PROPERTIES_NAME);
        
        boolean defaultLogging = false;
        URL propertiesURL = null;
        try {
			if (!PROPERTIES_CONFIG_LOCATION.canRead())
				throw new FileNotFoundException();
        	propertiesURL =  PROPERTIES_CONFIG_LOCATION.toURI().toURL();
        } catch (Throwable t) {
        	defaultLogging = true;
		}
        if (!defaultLogging) {
        	PropertyConfigurator.configure(propertiesURL);
        	
           	//make properties available to the components
	    	Properties p;
			try {
				p = new Properties();
				InputStream stream = WSMXKernel.PROPERTIES_CONFIG_LOCATION.toURI().toURL().openStream();
				p.load(stream);
				stream.close();
				
				//shallow copy properties
				Environment.setConfiguration((Properties)p.clone());
			} catch (Exception e) {
				e.printStackTrace();
			}
        } else {
        	PatternLayout layout = new CleanPatternLayout("%-5p %-25c{1}: %m%n");
        	BasicConfigurator.configure(new ConsoleAppender(layout));
	    	Logger.getRootLogger().setLevel(Level.FATAL);
	    	Logger.getLogger("org.deri").setLevel(Level.DEBUG);
	    	Logger.getLogger("ie.deri").setLevel(Level.DEBUG);
	    	Logger.getLogger("at.deri").setLevel(Level.INFO);
	    	Logger.getLogger("com").setLevel(Level.FATAL);
	    	Logger.getLogger("org").setLevel(Level.FATAL);
	    	Logger.getLogger("net").setLevel(Level.FATAL); 	
        	logger.warn("Property configuration of log4j failed, falling back to default configuration.");
        }

        InetAddress address = null;
        try {
        	address = InetAddress.getLocalHost();
        } catch (UnknownHostException uhe) {
        	logger.warn("Failed to determine IP address because the host is unknown: " + uhe.getMessage(), uhe);   
        } catch (Throwable t) {
        	logger.warn("Failed to determine IP address.", t);
        } finally {
        	INET_ADDRESS = address;
        }
        
        String host;
        if (WSMXKernel.INET_ADDRESS == null || (host = WSMXKernel.INET_ADDRESS.getHostName()) == null)
        	host = "unknown";
        HOSTNAME = host;
        
        String ip = null;
        if (WSMXKernel.INET_ADDRESS == null || (ip = WSMXKernel.INET_ADDRESS.getHostAddress()) == null)
        	ip = "unknown";
        IP_ADDRESS = ip;
        
        logger.info(VERSION + " @ " + HOSTNAME + " :: " + IP_ADDRESS);
    }
	
    /**
	 * Creates a WSMX instance.
	 * 
	 * @throws WSMXException
	 *             if failed
	 */
	public WSMXKernel() throws WSMXException {
		startupTimestamp = System.currentTimeMillis();
		mBeanServer = MBeanServerFactory.createMBeanServer();
		Environment.setMBeanServer(mBeanServer);		
		repository = mBeanServer.getClassLoaderRepository();
		try {
			kernelMBean =
	            (RequiredModelMBean) mBeanServer.instantiate(
	                "javax.management.modelmbean.RequiredModelMBean");	    
		    
		    ModelMBeanInfo kernelMBeanInfo = getModelMBeanInfo();
			kernelMBean.setModelMBeanInfo(kernelMBeanInfo);
		    
			kernelMBean.setManagedResource(this, "ObjectReference");
		    mBeanServer.registerMBean(kernelMBean, OBJECT_NAME);			
		} catch (Exception e) {
			throw new WSMXException("Registration at the MBeanServer failed.",	e);
		}
		KernelData.setMBeanServer(mBeanServer);
	}
	
	private void startConnectors() {
		JMXConnectorServer jrmp = startJMXConnectorServer(
				"service:jmx:rmi://" + HOSTNAME + ":" + DEFAULT_RMI_JRMP_PORT);
		if (jrmp != null) {
			logger.info("Started RMI over JRMP connector at " + jrmp.getAddress());
			Properties p = new Properties();
            try {
				if (p != null) {
					p.setProperty("wsmx.rmi-jrmp", jrmp.getAddress().toString());
					this.strRMI = jrmp.getAddress().toString();
					OutputStream outputStream = new FileOutputStream(INFORMATIVE_PROPERTIES_LOCATION);
					p.store(outputStream, "the RMI over JRMP address to connect to the kernel");
					outputStream.close();
				}
			} catch (MalformedURLException e1) {
				logger.warn("Tried to store RMIoverJRMP address but URL is malformed.", e1);
			} catch (IOException e1) {
				logger.warn("Tried to store RMIoverJRMP address but I/O exception occured.", e1);
			}
		} else
			logger.warn("Failed to start RMI over JRMP connector.");						

		JMXConnectorServer iiop = startJMXConnectorServer(
				"service:jmx:iiop://" + HOSTNAME + ":" + DEFAULT_RMI_IIOP_PORT);
		if (iiop != null) {
			logger.info("Started RMI over IIOP connector at " + iiop.getAddress());			
		} else
			logger.warn("Failed to start RMI over IIOP connector.");
		}


	//TODO throw exceptions
	private JMXConnectorServer startJMXConnectorServer(String serviceURL) {
		JMXConnectorServer server;
		Map<String,Object> env = new HashMap<String, Object>();
		ClassLoader loader = new UnifyingClassLoader(repository);
		//Thread.currentThread().setContextClassLoader(loader);
		env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER, loader);
		env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "com.sun.jmx.remote.protocol");		
		try {
			server = JMXConnectorServerFactory.newJMXConnectorServer(
					new JMXServiceURL(serviceURL),
					null,
					mBeanServer);
			connectors.add(server);
		} catch (MalformedURLException mue) {
			logger.warn("Failed to create RMI connector.", mue);
			return null;
		} catch (IOException ioe) {
			logger.warn("Failed to create RMI connector.", ioe);
			return null;
		}
		if (server == null) {
			logger.info("Failed to create RMI connector.");
			return null;
		}
		try {
			server.start();
		} catch (IOException ioe) {
			logger.warn("Failure during startup phase of connector.", ioe);
			return null;
		}
		return server;		
	}
	
	public static void main(String[] args) {        
		WSMXKernel wsmxServer = null;
		try {
			wsmxServer = new WSMXKernel();
            if (XML_CONFIG_LOCATION.canRead()) {
                logger.info("Using XML file for configuration: " + XML_CONFIG_LOCATION.getAbsolutePath());                
                wsmxServer.configure(XML_CONFIG_LOCATION);
            } else {                
                if (XML_CONFIG_LOCATION.exists())
                    logger.warn("External XML configuration " + XML_CONFIG_LOCATION.getAbsolutePath() + " exist but cannot be read. Fallback to property configuration.");
    			if (PROPERTIES_CONFIG_LOCATION.canRead()) {
                    logger.info("Using properties file for kernel configuration: " + PROPERTIES_CONFIG_LOCATION.getAbsolutePath());
                    Properties p = new Properties();
                    InputStream stream = PROPERTIES_CONFIG_LOCATION.toURI().toURL().openStream();
                    p.load(stream);
                    wsmxServer.configure(p);
                    stream.close();
                } else {
                    if (PROPERTIES_CONFIG_LOCATION.exists())
                        logger.warn("External property configuration " + PROPERTIES_CONFIG_LOCATION.getAbsolutePath() + " exist but cannot be read. Fallback to default configuration.");
                    logger.info("Using default configuration.");
                    wsmxServer.configure();
                }
            }
//            wsmxServer.addShutdownHook();
            wsmxServer.boot();
			wsmxServer.startAxisConsole();
            wsmxServer.startHTTPConsole();
            wsmxServer.startSSHConsole();
            wsmxServer.kernelThread = new Thread(wsmxServer, "Kernel");
			wsmxServer.kernelThread.start();
		} catch (WSMXConfigurationException ce) {
			logger.fatal("Failed to configure WSMX. Exodus.", ce);
			System.exit(1);
		} catch (WSMXException e){
		    logger.fatal("Failed to create WSMX instance. Exodus.", e);
			System.exit(1);
		} catch (ExceptionInInitializerError eiie) {
			if (eiie.getCause() instanceof AccessControlException)
				logger.fatal("Insufficient access control context. Grant sufficient priveleges. Exodus.", eiie);
			else
			    logger.fatal("Unexpected condition. Exodus.", eiie);
			System.exit(1);
		} catch (Throwable t) {
		    logger.fatal("Unexpected condition. Exodus.", t);
			System.exit(1);
		}
	}


	/**
	 * Configure this WSMX instance with a configuration instance from
	 * an <code>URL</code> and the default schema embedded in the jar.
	 * @param instance 
	 */	
	public synchronized void configure(URL instance) throws WSMXConfigurationException {		
		kernelConfiguration = new KernelXMLConfigurationFile(instance).load();	
		Environment.setSpaceAddress(kernelConfiguration.getSpaceAddress());
	}	

	/**
	 * Configure this WSMX instance with a configuration instance from
	 * a <code>File</code> and the default schema embedded in the jar.
	 * @param instance 
	 */	
	public synchronized void configure(File instance) throws WSMXConfigurationException {
		kernelConfiguration = new KernelXMLConfigurationFile(instance).load();		
		Environment.setSpaceAddress(kernelConfiguration.getSpaceAddress());
	}	

    /**
     * Configure this WSMX instance with a properties instance.
     * @param instance 
     */
    public synchronized void configure(Properties instance) throws WSMXConfigurationException {
        kernelConfiguration = new KernelPropertiesConfigurationFile(instance).load();  
		Environment.setSpaceAddress(kernelConfiguration.getSpaceAddress());
    }

    /**
     * Configure this WSMX instance with sane default values.
     * The systemcodebase is assumed to be in the directory
     * where the core executable resides. A space is searched
     * for locally, and web and ssh console will run on 8080
     * and 22 respectively.
     * 
     */
    public synchronized void configure() {
        kernelConfiguration = new KernelConfiguration(
                KERNEL_LOCATION,
                DEFAULT_SPACEADDRESS,
                DEFAULT_HTTP_PORT,
                DEFAULT_AXIS_PORT,
                DEFAULT_SSH_PORT
        );
		Environment.setSpaceAddress(kernelConfiguration.getSpaceAddress());
    }
	
	/**
	 * Initializes the boot sequence of WSMX.
	 */
	public synchronized void boot() {
	    logger.info("Initiating boot sequence.");		        
	    startConnectors();
	    Logger wsmxLogger = Logger.getLogger("ie.deri.wsmx");
        try {        
            mBeanServer.registerMBean(new LoggerDynamicMBean(wsmxLogger),
            		                  createLoggerObjectName(wsmxLogger));
        } catch (Exception e){
            logger.fatal("WSMX Logger cound not be registered.", e);
        }		

        try {        
            mBeanServer.registerMBean(new LoggerDynamicMBean(logger),
            		                  createLoggerObjectName(logger));
        } catch (Exception e){
            logger.fatal("Kernel Logger cound not be registered.", e);
        }		

		codebase = new Codebase(kernelConfiguration.getSystemCodebase());
		injectAndBlacklistComponents(codebase.update());
	}

	private void injectAndBlacklistComponents(List<ComponentConfiguration> componentConfigurations) {
		try {
            injectComponents(componentConfigurations);
        } catch (InjectionFailedException e) {
            logger.warn("Failed to inject component.", e);
            codebase.blacklistArchive(e.getCodebase());
        }
	}

	//FIXME in-memory configuration is not updated
	private void injectComponents(List<ComponentConfiguration> componentConfigurations) throws InjectionFailedException {
		if (componentConfigurations == null)
			return;
		logger.info(extractInjectionInformation(componentConfigurations));
		for (ComponentConfiguration componentConfig : componentConfigurations) {
			try {
				Class<?> componentClass = componentConfig.getComponentClass();
				try {
				UnifyingClassLoader masterLoader = (UnifyingClassLoader)ClassLoader.getSystemClassLoader();
				masterLoader.getComponentClassLoaderRepository().addLoader(componentConfig.getObjectName(),
								                                           componentConfig.getClassloader());
				} catch (Throwable t) {
					//silent
				}				
				
				Object component = componentClass.newInstance();				
				componentconfigCache.put(componentConfig, component);
				
				logger.debug("Component " + componentConfig.getName() + " has " + 
						componentConfig.getExposedMethods().size() + " exposed methods.");
				//classloader
				mBeanServer.registerMBean(componentConfig.getClassloader(),
						                  createClassLoaderObjectName(componentConfig));
				
				//scheduler
                DistributedScheduler scheduler = createScheduler(component);			
				RequiredModelMBean schedulerMBean = createModelMBean();
				ObjectName schedulerName = createSchedulerObjectName(componentConfig);				
			    initializeSchedulerMBean(scheduler, schedulerMBean, schedulerName);	        
				registerScheduler(scheduler, schedulerMBean, schedulerName);

				//managability wrapper
				ManagabilityWrapper wrapper = createComponentWrapper(componentConfig, component);
				RequiredModelMBean wrapperMBean = createModelMBean();	
				ObjectName wrapperName = componentConfig.getObjectName();
				initializeWrapperMBean(component, wrapper, wrapperMBean, wrapperName);
				mBeanServer.registerMBean(wrapperMBean, wrapperName);

				//logger
				Logger componentLogger = Logger.getLogger(componentClass);
				ObjectName loggerName = createLoggerObjectName(componentLogger);
				mBeanServer.registerMBean(new LoggerDynamicMBean(componentLogger),
										  loggerName);
				loggerCache.put(wrapperName, loggerName);

				//at this point the registration process is finished and we can inform the codebase
				codebase.tagAsDeployed(componentConfig);				
				
//				notifications
				startupComponent(componentConfig, component);				
				startupScheduler(componentConfig, scheduler);                				
				//attach core notification listener to all schedulers
				attachMBeanToCore(schedulerMBean.getClass(), schedulerName);	  				
				//attach scheduler notification listeners to the core broadcaster
				//attachCoreToMBean(,, scheduler.getClass(), scheduler);				
				//attach components notification listeners to the core broadcaster for notifications they're subscribed to
				attachCoreToMBean(componentConfig.getNotifications(),
							      componentConfig.getNotificationFilter(),
							      componentClass,
							      component);		
				
				sendDeploymentInformationNotification();
			} catch (InvocationTargetException ite) {
				logger.warn("Abnormalcy during startup phase of component: The start operation could not be invoked.", ite);
				throw new InjectionFailedException(componentConfig.getCodeBase(),
						                  "Injection of " + componentConfig.getName() + " failed.",
						                  ite);
			} catch (IllegalAccessException iae) {
			    logger.warn("Abnormalcy during injection: Illegal Access.", iae);
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  iae);

			} catch (InstantiationException ie) {
			    logger.warn("Abnormalcy during injection: Instantiation failed.", ie);
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  ie);

			} catch (MBeanRegistrationException mre) {
			    logger.warn("Abnormalcy during injection: Registration failed.", mre);
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  mre);

			} catch (InstanceAlreadyExistsException iaee) {
			    logger.warn("Abnormalcy during injection: Instance already exists. There's already a component registered under that name.", iaee);
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  iaee);

			} catch (NotCompliantMBeanException ncme) {
			    logger.warn("Abnormalcy during injection: The MBean is not compliant.", ncme);
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  ncme);

			} catch (MalformedObjectNameException mone) {
			    logger.warn("Abnormalcy during injection: The MBeans ObjectName is not valid.", mone);
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  mone);

			} catch (InstanceNotFoundException infe) {
			    logger.warn("Abnormalcy during injection: A required MBean is not registered.", infe);
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  infe);

			} catch (ReflectionException re) {
				logger.warn("Abnormalcy during injection: Reflection failed.", re);
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  re);
			} catch (MBeanException mbe) {
				logger.warn("Abnormalcy during injection: MBean exception.", mbe);	
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  mbe);

			} catch (RuntimeOperationsException roe) {
				logger.warn("Abnormalcy during injection: Runtime operation failed.", roe);	
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  roe);

			} catch (InvalidTargetObjectTypeException itot) {
				logger.warn("Abnormalcy during injection: Invalid target objecttype.", itot);	
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  itot);

			} catch (Throwable t) {
				logger.warn("Abnormalcy during injection: Unexpected abnormalcy.", t);	
				throw new InjectionFailedException(componentConfig.getCodeBase(),
		                  "Injection of " + componentConfig.getName() + " failed.",
		                  t);
			}
		}
	}


	private void sendDeploymentInformationNotification() throws MBeanException {
		List<String> deployedComponents = new ArrayList<String>();
		for(ObjectName name : schedulerCache.keySet())
			deployedComponents.add(name.getKeyProperty("name"));
		//TODO seq numbers
		Notification n = new Notification("COMPONENT_DEPLOYED",
		        						  OBJECT_NAME,
		        						  1);
		n.setUserData(deployedComponents);
		kernelMBean.sendNotification(n);
	}

	private String extractInjectionInformation(List<ComponentConfiguration> componentConfigurations) {
		String injection = "Injecting " + componentConfigurations.size() + " component";
		if (componentConfigurations.size() != 1)
			injection += "s";
		injection += ":";
		for (ComponentConfiguration component : componentConfigurations)
			injection += " " + component.getName();
		return injection;
	}


	private void startupScheduler(ComponentConfiguration componentConfig, DistributedScheduler scheduler) {
		//TODO we're ignoring multiple eventtypes
		String eventtype = null;
		if (componentConfig.getEvents().size() > 0)
			eventtype = componentConfig.getEvents().get(0);
		scheduler.start(this.kernelConfiguration.getSpaceAddress(), eventtype);
	}


	private void initializeWrapperMBean(Object component, ManagabilityWrapper wrapper, RequiredModelMBean wrapperMBean, ObjectName wrapperName) throws MBeanException, InstanceNotFoundException, InvalidTargetObjectTypeException {
		ModelMBeanInfo wrapperMBeanInfo = wrapper.getModelMBeanInfo(wrapperName);
		wrapperMBean.setModelMBeanInfo(wrapperMBeanInfo);
		wrapperMBean.setManagedResource(component, "ObjectReference");
	}

	private ManagabilityWrapper createComponentWrapper(ComponentConfiguration componentConfig, Object componentObject) {
		ManagabilityWrapper wrapper = new ManagabilityWrapper();
		wrapper.registerComponent(componentObject, componentConfig);
		wrapper.addExposedMethods(componentConfig.getExposedMethods());
		return wrapper;
	}


	private void registerScheduler(DistributedScheduler scheduler, RequiredModelMBean schedulerMBean, ObjectName schedulerName) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		mBeanServer.registerMBean(schedulerMBean, schedulerName);
		schedulerCache.put(schedulerName, scheduler);
	}


	private void initializeSchedulerMBean(DistributedScheduler scheduler, RequiredModelMBean schedulerMBean, ObjectName schedulerName) throws MBeanException, InstanceNotFoundException, InvalidTargetObjectTypeException {
		scheduler.setSchedulerMBean(schedulerMBean);
		ModelMBeanInfo schedulerMBeanInfo = scheduler.getModelMBeanInfo(schedulerName);
		schedulerMBean.setModelMBeanInfo(schedulerMBeanInfo);
		schedulerMBean.setManagedResource(scheduler, "ObjectReference");
	}


	private RequiredModelMBean createModelMBean() throws ReflectionException, MBeanException {
		RequiredModelMBean mBean =
		    (RequiredModelMBean) mBeanServer.instantiate(
		        "javax.management.modelmbean.RequiredModelMBean");		
		return mBean;
	}


	private DistributedScheduler createScheduler(Object componentObject) {
		DistributedScheduler scheduler = new DistributedScheduler();
		scheduler.registerComponent(componentObject);
		return scheduler;
	}


	private ObjectName createSchedulerObjectName(ComponentConfiguration component) throws MalformedObjectNameException {
		String assembledName = "schedulers:name=" + component.getName();
		for(String key : component.getProperties().keySet())
		    assembledName += "," + key + "=" + component.getProperties().get(key);
		ObjectName schedulerName = new ObjectName(assembledName);
		return schedulerName;
	}
	
	private ObjectName createClassLoaderObjectName(ComponentConfiguration component) throws MalformedObjectNameException {
		String assembledName = "classloaders:name=" + component.getName();
		for(String key : component.getProperties().keySet())
		    assembledName += "," + key + "=" + component.getProperties().get(key);
		ObjectName schedulerName = new ObjectName(assembledName);
		return schedulerName;
	}

	private ObjectName createLoggerObjectName(Logger logger) throws MalformedObjectNameException {
		String assembledName = "loggers:name=" + logger.getName();
		ObjectName schedulerName = new ObjectName(assembledName);
		return schedulerName;
	}


	private void startupComponent(ComponentConfiguration component, Object componentObject) throws IllegalAccessException, InvocationTargetException {
		if (component.getStartMethod() != null) {
			logger.debug("Starting up component " + component.getName());
		    Method startMethod = component.getStartMethod();
		    Object[] parameterObjects = new Object[]{};
			startMethod.invoke(componentObject, parameterObjects);
		}
	}

    public String undeploy(ObjectName objectname) throws InstanceNotFoundException, MBeanRegistrationException {
        ObjectName _scheduler = null, _component = null, _classloader = null, _logger = null;
        String domain = objectname.getDomain();
        if (domain.equals("loggers")) {
        	logger.debug("Attempted logger undeployment.");
        	return "Undeploy a component either in the components, classloaders or scheduler domains.";
        }
        if (!(domain.equals("schedulers")||
        		domain.equals("components") ||
                domain.equals("classloaders"))) {
        	return "This component cannot be undeployed";
        }
        String domainlessName = objectname.toString().split(":")[1];
        try {
        	_component = new ObjectName("components:" + domainlessName);
        	_scheduler = new ObjectName("schedulers:" + domainlessName);
        	_classloader = new ObjectName("classloaders:" + domainlessName);
        	_logger = loggerCache.get(_component);
        } catch (MalformedObjectNameException mone) {
        	logger.warn("Failed to create neccesary objectnames of all MBeans of this component." +
        			    " Undeploy operation might not succeed.", mone);
        	
        }	
   		mBeanServer.unregisterMBean(_component);
   		logger.debug("Undeployed " + _component);
   		mBeanServer.unregisterMBean(_classloader);
   		logger.debug("Undeployed " + _classloader);
   		mBeanServer.unregisterMBean(_logger);
   		logger.debug("Undeployed " + _logger);
   		mBeanServer.unregisterMBean(_scheduler);
    	if (schedulerCache.get(_scheduler) == null) 
        	logger.warn("Scheduler that was marked for removal is not known.\n" +
        			    "Requested scheduler for "+ _scheduler + "\n" +
        			    "Known schedulers: " + schedulerCache); 
        else
        	schedulerCache.get(_scheduler).stop();
    	schedulerCache.remove(_scheduler);
        logger.debug("Undeployed " + _scheduler);        
        codebase.unregisterComponent(_component);        

        try {
        	kernelMBean.sendNotification(new Notification("COMPONENT_UNDEPLOYED", OBJECT_NAME, 1, objectname.getKeyProperty("name")));
        } catch (MBeanException mbe) {
			logger.warn("Failed to send undeployment notification.", mbe);	        	
        }
        
        return "Component successfully undeployed. The following MBeans were unregistered: " 
        	   + _component + ", "
        	   + _classloader + ", "
        	   + _scheduler + ", "
        	   + _logger  + ".";
    }

	/**
	 * Add the components notification listeners to the core broadcaster,
	 * using either atomic/parent eventsubcription or custom eventfilter,
	 * depending on the underlying configuration.
	 * 
     * @param config
     * @param mbeanClass
     * @param mbeanObject
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InstanceNotFoundException
     */
    private void attachCoreToMBean(List<String> notifications, Class<?> filterClass, Class<?> mbeanClass, Object mbeanObject) 
    		throws InstantiationException, IllegalAccessException, InstanceNotFoundException {
        Object  filterObject = null;
        if (filterClass != null) {
        	filterObject = filterClass.newInstance();
        }
        if (!Arrays.asList(mbeanClass.getInterfaces()).contains(NotificationListener.class) && 
            !Arrays.asList(mbeanClass.getSuperclass().getInterfaces()).contains(NotificationListener.class)
            )
                
            logger.debug("Component " + mbeanClass.getName() + 
                        " of " + mbeanClass + 
                        " does not implement NotificationListener. " +
            			"It will not be able to receive notification from the kernel.");
        else {
        	// a wrapper listens only to the notifications it has interest in
            if (filterObject == null && !notifications.isEmpty()) {
                // subscription to atomic & parent event
                NotificationFilterSupport filter = new NotificationFilterSupport();
                for (String type : notifications)
                    filter.enableType(type);                
                mBeanServer.addNotificationListener(OBJECT_NAME,
                        (NotificationListener)mbeanObject,
                        filter, null);
				            
            } else if (filterObject == null && notifications.isEmpty()) {
                // no events
                logger.debug("Component " + mbeanClass.getName() + " has neither an notificationfilter " +
                		"nor is it subscribed to any notification. It will receive no notification.");
            } else {
                // event filter & event filter override
                if (filterObject != null && !notifications.isEmpty())
                    logger.warn("Component " + mbeanClass.getName() +
                                " of " + mbeanClass + 
        	                    ": Notificationfilter overrides atomic/parent notification subscription.");				           				        
                mBeanServer.addNotificationListener(OBJECT_NAME,
                                                    (NotificationListener)mbeanObject,
                                                    (NotificationFilter)filterObject, null);				            
            }
        }
    }


    /**
     * Adds the core notification listener to the
     * components broadcasters.
     * 
     * @param mbeanClass
     * @param objectName
     * @throws InstanceNotFoundException
     */
    private void attachMBeanToCore(Class<?> mbeanClass, ObjectName objectName) throws InstanceNotFoundException {
        List interfaces = Arrays.asList(mbeanClass.getInterfaces());
        if (!(interfaces.contains(NotificationBroadcaster.class) || 
              interfaces.contains(NotificationEmitter.class) ||
              mbeanClass.getSuperclass().equals(NotificationBroadcasterSupport.class) ||
              mbeanClass.getSuperclass().getSuperclass().equals(NotificationBroadcasterSupport.class)
             )
           )
            logger.warn("Component " + objectName.getKeyProperty("name") + 
                        " of " + mbeanClass +
                        " does not implement NotificationBroadcaster. " +
            		    "It will not be able to send notifications to the kernel.");
        else {
        	logger.debug("Adding " + objectName + " to core listener.");
            // the core listens to all wrappers notifications
        	mBeanServer.addNotificationListener(objectName, this, null, null);
        }
    }


    /**
	 * Starts up the HTTP Management Console.
	 */
	public synchronized void startHTTPConsole() {
	    logger.debug("Launching HTTP management console.");
		try {			
			httpAdapterName = new ObjectName("core:name=HTTPAdapter");
			HttpAdapter httpAdapter = new HttpAdapter(kernelConfiguration.getHTTPPort(),"0.0.0.0");
			xsltProcessorName = new ObjectName("core:name=XSLTProcessor");
		    XSLTProcessor xsltProcessor = new XSLTProcessor();
			
		    httpAdapter.preRegister(mBeanServer, httpAdapterName);
		    httpAdapter.setProcessor(xsltProcessor);

		    mBeanServer.registerMBean(xsltProcessor, xsltProcessorName);
		    mBeanServer.registerMBean(httpAdapter, httpAdapterName);
		    bindHTTPConsole(httpAdapter);    

		} catch (Exception e) {
			logger.warn("HTTP Adapter boot failed.", e);
		}
	}
	
    /**
	 * Starts up the Axis Management Console.
	 */
	public synchronized void startAxisConsole() {
	    logger.debug("Launching Axis management console.");
		try {			
			axisAdapterName = new ObjectName("core:name=AxisAdapter");
			AxisAdapter axisAdapter = new AxisAdapter(kernelConfiguration.getAxisPort(),"0.0.0.0", strRMI);
			
		    axisAdapter.preRegister(mBeanServer, axisAdapterName);

		    mBeanServer.registerMBean(axisAdapter, axisAdapterName);
		    bindAxisConsole(axisAdapter);    

		} catch (Exception e) {
			logger.warn("Axis Adapter boot failed.", e);
		}
	}	
	
    
    /**
	 * Starts up the SSH Management Console.
	 */
	public synchronized void startSSHConsole() {
		logger.debug("Launching SSH management console.");
		try {
			sshDaemon = new SSHAdapter(
					mBeanServer,
					HOST_KEY_LOCATION,
					WSMXKernel.class.getResourceAsStream("/META-INF/server.xml"),
					WSMXKernel.class.getResourceAsStream("/META-INF/platform.xml"),
					kernelConfiguration.getSSHPort()
			);
			sshDaemon.start();
			sshDaemonName = new ObjectName("core:name=SSHDaemon");
			RequiredModelMBean daemonMBean = (RequiredModelMBean) mBeanServer
					.instantiate("javax.management.modelmbean.RequiredModelMBean");
			ModelMBeanInfo daemonMBeanInfo = sshDaemon.getModelMBeanInfo(sshDaemonName);
			daemonMBean.setModelMBeanInfo(daemonMBeanInfo);
			daemonMBean.setManagedResource(sshDaemon, "ObjectReference");
			mBeanServer.registerMBean(daemonMBean, sshDaemonName);
		} catch (KeyNotAvailableException knae) {
			logger.warn("SSHAdapter startup failed because no host key is available.", knae);
		} catch (Throwable t) {
			logger.warn("SSHAdapter startup failed.", t);
		}

	}

	/**
	 * Tries to bind the Management Console to the port specified in the loaded
	 * configuration. If that fails this method will increment the port number
	 * and attempt to bind the console to the incremented port. This procedure
	 * is repeated for a certain number of times, before this method will
	 * eventually give up.
	 * 
	 * @param httpAdapter
	 *            the HTTPAdapter which is to be bound to a port
	 * @throws IOException
	 */
    private void bindHTTPConsole(HttpAdapter httpAdapter) throws IOException {
        try {
            httpAdapter.start();
        } catch (BindException be) {
			logger.warn("Adapter boot failed. Port " + kernelConfiguration.getHTTPPort() + " can not be bound: " + be.getMessage());
			kernelConfiguration.setHTTPPort(kernelConfiguration.getHTTPPort() + 1);
			httpAdapter.setPort(kernelConfiguration.getHTTPPort());
			if (bindAttempts < MAX_BIND_ATTEMPTS) {
			    bindAttempts += 1;
				logger.info("Attempting to recover and start HTTPAdapter with incremented port number " + kernelConfiguration.getHTTPPort());
				bindHTTPConsole(httpAdapter);
			} else
				logger.warn("Maximum number of binding attempts reached. Giving up to find a free port to bind to.");
        }
    }
    
    private void bindAxisConsole(AxisAdapter axisAdapter) throws IOException {
        try {
            axisAdapter.start();
        } catch (BindException be) {
			logger.warn("Adapter boot failed. Port " + kernelConfiguration.getAxisPort() + " can not be bound: " + be.getMessage());
			kernelConfiguration.setAxisPort(kernelConfiguration.getAxisPort() + 1);
			axisAdapter.setPort(kernelConfiguration.getAxisPort());
			if (bindAttempts < MAX_BIND_ATTEMPTS) {
			    bindAttempts += 1;
				logger.info("Attempting to recover and start AxisAdapter with incremented port number " + kernelConfiguration.getAxisPort());
				bindAxisConsole(axisAdapter);
			} else
				logger.warn("Maximum number of binding attempts reached. Giving up to find a free port to bind to.");
        }
    }

	public synchronized void kill() {
		logger.info("System is shutting down.");

		for (JMXConnectorServer connector : connectors) {
			logger.info("Stopping connector " + connector.getAddress());
			try {
				connector.stop();
			} catch (IOException ioe) {
				logger.warn("Failed to stop connector." + ioe.getMessage(), ioe);
			}
		}
        for (DistributedScheduler wrapper : schedulerCache.values()) {
            logger.info("Stopping " + wrapper);
            wrapper.stop();
        }
        	
        for (Entry<ComponentConfiguration, Object> entry : componentconfigCache.entrySet()){
			try {
				entry.getKey().getStopMethod().invoke(entry.getValue(), (Object[])null);
			} catch (Exception e) {
				//no stop method has been provided
				logger.debug("Failed to stop component " + entry.getKey().getName() + ": " + e.getMessage());
			}
		}
        
        // TODO unregister mbeans
		logger.info("System is shut down and consoles will halt in 5 seconds.");
		//we need to allow the httpserver to return a command confirmation
		//give the http server some time to tell the user that his shutdown
		//command has been successfully invoked before we shut it down too
		try {
			Thread.sleep(5000);
		} catch(InterruptedException ie) {
			//at this point the system already shut down
			//and we're just waiting for the console daemons
			//there's noone to notify() us, but if this
			//somehow happens then we simply continue
		    logger.warn("Warning: console return sleep interrupted.");
		}		

        try {
        	//TODO check for startup and shutdown only in case
        	sshDaemon.stop();
        } catch (Throwable t) {
            logger.warn("SSH daemon shutdown failed.", t);
        }

		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //stop axis adapter
		try {
			mBeanServer.invoke(axisAdapterName, "stop",
					new Object[] {},
					new String [] {});
		} catch (InstanceNotFoundException infe) {
			logger.warn("Shutdown of AxisAdapter failed. AxisAdapter is not registered: " + infe.getMessage());
		} catch (MBeanException mbe) {
		    logger.warn("Shutdown of AxusAdapter failed. Userdefined MBean Exception : " + mbe.getMessage());
		} catch (ReflectionException re) {
		    logger.warn("Shutdown of AxusAdapter failed." + re.getMessage());			
		} catch (Throwable t) {
		    logger.warn("Shutdown of AxisAdapter failed.", t);						
		}
	
		try {
			mBeanServer.invoke(httpAdapterName, "stop",
					new Object[] {},
					new String [] {});
		} catch (InstanceNotFoundException infe) {
			logger.warn("Shutdown of HTTPAdapter failed. HTTPAdapter is not registered: " + infe.getMessage());
		} catch (MBeanException mbe) {
		    logger.warn("Shutdown of HTTPAdapter failed. Userdefined MBean Exception : " + mbe.getMessage());
		} catch (ReflectionException re) {
		    logger.warn("Shutdown of HTTPAdapter failed." + re.getMessage());			
		} catch (Throwable t) {
		    logger.warn("Shutdown of HTTPAdapter failed.", t);						
		}
	}
	
	public synchronized String shutdown() {
		// TODO do we need sanity checks here?
		alive = false;
		return "System is shutting down.";
	}

	public String getVersion() {
		return VERSION;
	} 
    
	/**
	 * @return Returns the kernelConfiguration.
	 */
	public KernelConfiguration getKernelConfiguration() {
		return kernelConfiguration;
	}
    
	/**
	 * @param configuration The kernelConfiguration to set.
	 */
	public synchronized void setKernelConfiguration(KernelConfiguration configuration) {
		kernelConfiguration = configuration;
	}

	/* (non-Javadoc)
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
	 */
	public void handleNotification(Notification notification, Object handback) {
	    logger.debug("Received notification of type: " + notification.getType());        
        if(notification.getType().equals("TASK_SCHEDULED")) {
        	try {
        		kernelMBean.sendNotification(notification);
        	} catch (MBeanException mbe) {
        		logger.warn("Retransmission of TASK_SCHEDULED notification failed.", mbe);
        	}
       }
	}

	/**
	 * @return Returns the mBeanServer.
	 */
	public MBeanServer getMBeanServer() {
		return mBeanServer;
	}
	
	public long getUptime() {
		return System.currentTimeMillis() - startupTimestamp;
	}

	public long getStartupTimestamp() {
		return startupTimestamp;
	}

	public String getFormattedUptime() {
		String uptime = "";
		long milliseconds = getUptime();
		long seconds = milliseconds/1000;
		milliseconds %= 1000;
		long minutes = seconds/60;
		seconds %= 60;
		long hours = minutes/60;
		minutes %= 60;
		long days = hours/24;
		hours %= 24;
		if (days != 0)
			uptime += days + " days ";
		if (hours != 0)
			uptime += hours + " hours ";
		if (minutes != 0)
			uptime += minutes + " minutes ";
		if (seconds != 0)
			uptime += seconds + " seconds ";
		if (milliseconds != 0)
			uptime += milliseconds + " milliseconds";
		return uptime;
	}

	public String getFormattedStartupTimestamp() {
		return new Date(startupTimestamp).toString();
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		addShutdownHook();
		while(alive) {	
			Thread.yield();
            try {
                Thread.sleep(2);
				List<ComponentConfiguration> componentConfigurations  = codebase.update();
				if (componentConfigurations.size() > 0)
                    try {
                        injectAndBlacklistComponents(componentConfigurations);
                    } catch (Throwable t) {
                        logger.warn("Failed to inject component.", t);
                    }
            } catch(InterruptedException ie) {
                //back to loop
            }            
		}
		kill();
		logger.info("Kernel halted.");
	}
	
	private ModelMBeanInfo getModelMBeanInfo() {
		Descriptor cmDescription = new DescriptorSupport(
				new String[] {
						("name=" + OBJECT_NAME),
						"descriptorType=mbean",
						("displayName=" + WSMXKernel.class.getSimpleName()),
						"type=" + WSMXKernel.class.getCanonicalName(),
						"log=T", "logFile=wsmxkernel.log",
						"currencyTimeLimit=0" });

		ModelMBeanAttributeInfo[] attributes = getModelMBeanAttributeInfo();
		ModelMBeanOperationInfo[] operations = getModelMBeanOperationInfo();
        ModelMBeanNotificationInfo[] notifications = getModelMBeanNotificationInfo();

        ModelMBeanInfo modelMBeanInfo = new ModelMBeanInfoSupport(
				WSMXKernel.class.getSimpleName(),
				"A microkernel that hosts components.",
				attributes,
				new ModelMBeanConstructorInfo[0],
				operations,
				notifications);

		try {
			modelMBeanInfo.setMBeanDescriptor(cmDescription);
		} catch (Exception e) {
			logger.warn("CreateMBeanInfo failed with " + e.getMessage());
		}
		return modelMBeanInfo;
	}


	/**
	 * @return
	 */
	private ModelMBeanNotificationInfo[] getModelMBeanNotificationInfo() {
		ModelMBeanNotificationInfo[] notifications = {
			new ModelMBeanNotificationInfo(new String[] {"COMPONENT_DEPLOYED"}, "ComponentDeployment", 
					"Indicates that a component was deployed.")
        };
		return notifications;
	}


	/**
	 * @return
	 */
	private ModelMBeanOperationInfo[] getModelMBeanOperationInfo() {
		ModelMBeanOperationInfo[] operations = new ModelMBeanOperationInfo[8];

		Descriptor getUptime = new DescriptorSupport(
				new String[] {
						"name=getUptime",
						"descriptorType=operation",
						"class=" + WSMXKernel.class.getCanonicalName(),
						"role=operation",
                        "currencyTimeLimit=-1"  });

		operations[0] = new ModelMBeanOperationInfo("getUptime",
				"Returns the time this instance of the kernel has been running in milliseconds.",
				new MBeanParameterInfo[0], 
				"long",
				MBeanOperationInfo.INFO,
				getUptime);
		
		Descriptor getStartupTimestamp = new DescriptorSupport(
				new String[] {
						"name=getStartupTimestamp",
						"descriptorType=operation",
						"class=" + WSMXKernel.class.getCanonicalName(),
						"role=operation" });

		operations[1] = new ModelMBeanOperationInfo("getStartupTimestamp",
				"Returns the time when this instance of the kernel has been booted.",
				new MBeanParameterInfo[0], 
				"long",
				MBeanOperationInfo.INFO,
				getStartupTimestamp);
		
		Descriptor getFormattedUptime = new DescriptorSupport(
				new String[] {
						"name=getFormattedUptime",
						"descriptorType=operation",
						"class=" + WSMXKernel.class.getCanonicalName(),
						"role=operation",
                        "currencyTimeLimit=-1" });

		operations[2] = new ModelMBeanOperationInfo("getFormattedUptime",
				"Returns the time this instance of the kernel has been running as a formatted string.",
				new MBeanParameterInfo[0], 
				"java.lang.String",
				MBeanOperationInfo.INFO,
				getFormattedUptime);
		
		Descriptor getFormattedStartupTimestamp = new DescriptorSupport(
				new String[] {
						"name=getFormattedStartupTimestamp",
						"descriptorType=operation",
						"class=" + WSMXKernel.class.getCanonicalName(),
						"role=operation" });

		operations[3] = new ModelMBeanOperationInfo("getFormattedStartupTimestamp",
				"Returns the time when this instance of WSMX has been booted as a formatted string.",
				new MBeanParameterInfo[0], 
				"java.lang.String",
				MBeanOperationInfo.INFO,
				getFormattedStartupTimestamp);
		
		Descriptor getVersion = new DescriptorSupport(
				new String[] {
						"name=getVersion",
						"descriptorType=operation",
						"class=" + WSMXKernel.class.getCanonicalName(),
						"role=operation" });

		operations[4] = new ModelMBeanOperationInfo("getVersion",
				"Returns the version of the kernel.",
				new MBeanParameterInfo[0], 
				"java.lang.String",
				MBeanOperationInfo.INFO,
				getVersion);
		
		Descriptor shutdown = new DescriptorSupport(
				new String[] {
						"name=shutdown",
						"descriptorType=operation",
						"class=" + WSMXKernel.class.getCanonicalName(),
						"role=operation" });

		operations[5] = new ModelMBeanOperationInfo("shutdown",
				"Stops all components and halts the system.",
				new MBeanParameterInfo[0], 
				"",
				MBeanOperationInfo.ACTION,
				shutdown);

        Descriptor undeploy = new DescriptorSupport(
                new String[] {
                        "name=undeploy",
                        "descriptorType=operation",
                        "class=" + WSMXKernel.class.getCanonicalName(),
                        "role=operation",
                        "currencyTimeLimit=-1" });
        
        operations[6] = new ModelMBeanOperationInfo("undeploy",
                "Removes a given component from this kernel instance.",
                new MBeanParameterInfo[]{
                    new MBeanParameterInfo("objectname",
                                           "javax.management.ObjectName",
                                           "The objectname reference of the component that should be undeployed."
                    )
                },
                "",
                MBeanOperationInfo.ACTION,
                undeploy);
        
		Descriptor getKernelConfiguration = new DescriptorSupport(
				new String[] {
						"name=getKernelConfiguration",
						"descriptorType=operation",
						"class="+ WSMXKernel.class.getCanonicalName(),
						"role=operation",
                        "currencyTimeLimit=-1"  });

		operations[7] = new ModelMBeanOperationInfo("getKernelConfiguration",
				"Returns the kernel configuration for this kernel instance.",
				new MBeanParameterInfo[0], 
				KernelConfiguration.class.getCanonicalName(),
				MBeanOperationInfo.INFO,
				getKernelConfiguration);
		return operations;
	}


	/**
	 * @return
	 */
	private ModelMBeanAttributeInfo[] getModelMBeanAttributeInfo() {
		ModelMBeanAttributeInfo[] attributes = new ModelMBeanAttributeInfo[5];
		
		Descriptor uptime =
			new DescriptorSupport(
				new String[] {
					"name=Uptime",
					"descriptorType=attribute",
					"displayName=Uptime",
					"getMethod=getUptime",
                    "currencyTimeLimit=-1"  });

		attributes[0] =
			new ModelMBeanAttributeInfo(
				"Uptime",
				"long",
				"The time this instance of the kernel has been running in milliseconds.",
				true,
				false,
				false,
				uptime);
		
		Descriptor startupTimestamp =
			new DescriptorSupport(
				new String[] {
					"name=StartupTimestamp",
					"descriptorType=attribute",
					"displayName=StartupTimestamp",
					"getMethod=getStartupTimestamp" });

		attributes[1] =
			new ModelMBeanAttributeInfo(
				"StartupTimestamp",
				"long",
				"The time when this instance of the kernel has been booted.",
				true,
				false,
				false,
				startupTimestamp);

		Descriptor formattedUptime =
			new DescriptorSupport(
				new String[] {
					"name=FormattedUptime",
					"descriptorType=attribute",
					"displayName=FormattedUptime",
					"getMethod=getFormattedUptime",
                    "currencyTimeLimit=-1" });

		attributes[2] =
			new ModelMBeanAttributeInfo(
				"FormattedUptime",
				"java.lang.String",
				"Returns the time this instance of the kernel has been running as a formatted string.",
				true,
				false,
				false,
				formattedUptime);
		
		Descriptor formattedStartupTimestamp =
			new DescriptorSupport(
				new String[] {
					"name=FormattedStartupTimestamp",
					"descriptorType=attribute",
					"displayName=FormattedStartupTimestamp",
					"getMethod=getFormattedStartupTimestamp"});

		attributes[3] =
			new ModelMBeanAttributeInfo(
				"FormattedStartupTimestamp",
				"java.lang.String",
				"Returns the time when this instance of the kernel has been booted as a formatted string.",
				true,
				false,
				false,
				formattedStartupTimestamp);
		
		Descriptor version =
			new DescriptorSupport(
				new String[] {
					"name=Version",
					"descriptorType=attribute",
					"displayName=Version",
					"getMethod=getVersion",
					"currencyTimeLimit=0" });

		attributes[4] =
			new ModelMBeanAttributeInfo(
				"Version",
				"java.lang.String",
				"Returns the version of this kernel instance.",
				true,
				false,
				false,
				version);
		return attributes;
	}

    public static Codebase getCodebase() {
		return codebase;
	}

	private void addShutdownHook() {
    	Runtime.getRuntime().addShutdownHook(new ShutdownInterceptor(this));
    }

	public Thread getKernelThread() {
		return kernelThread;
	}


	public boolean isAlive() {
		return alive;
	}
}

class ShutdownInterceptor extends Thread {
	Logger logger = Logger.getLogger(ShutdownInterceptor.class);
	private WSMXKernel kernel = null;

	public ShutdownInterceptor(WSMXKernel kernel) {
		super();
		this.kernel = kernel;
	}	

	@Override
	public void run() {
		if (!kernel.isAlive())
			return;
		logger.info("Intercepting JVM shutdown and shutting down microkernel.");
		kernel.shutdown();
		try {
			kernel.getKernelThread().join(60000);
		} catch (InterruptedException e) {
			logger.warn("Shutdown interrupted. Terminating.");
		}
	}
}