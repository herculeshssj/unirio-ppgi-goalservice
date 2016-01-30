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

package ie.deri.wsmx.core.configuration;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A configuration for an entire microkernel, that is
 * a single instance of a WSMX node.
 * 
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/configuration/KernelConfiguration.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.11 $ $Date: 2006-02-17 11:59:40 $
 */ 
public class KernelConfiguration implements Serializable {

	private static final long serialVersionUID = -2108200621164799681L;
	
 	private File systemCodebase;
    private String spaceAddress;
	private int httpPort;
	private int axisPort;
	private int sshPort;
	private String rmiJrmp;
	private String rmiIiop;

	private List<DomainConfiguration> domains = new ArrayList<DomainConfiguration>();
	
	public KernelConfiguration(File systemCodebase, String spaceAddress, int httpPort, int axisPort, int sshPort) {
		super();
		this.systemCodebase = systemCodebase;
        this.spaceAddress = spaceAddress;
		this.httpPort = httpPort;
		this.axisPort = axisPort;
		this.sshPort = sshPort;
	}	

	/**
	 * Returns the systemcodebase, which is the location where the kernel will look for components.
	 * 
	 * @return Returns the systemCodebase.
	 */
	public File getSystemCodebase() {
		return systemCodebase;
	}
	
	/**
	 * Sets the systemcodebase, which is the location where the kernel will look for components.
	 * 
	 * @param systemCodebase The systemCodebase to set.
	 */
	public void setSystemCodebase(File systemCodebase) {
		this.systemCodebase = systemCodebase;
	}
	
	/**
	 * Appends a <code>DomainConfiguration</code> to this
	 * <code>KernelConfiguration</code>. This method
	 * ensures encapsulation of the internal datastructure
	 * implementation that holds the domains and takes care
	 * of maintaining the bidirectional links between
	 * <code>KernelConfiguration</code> and
	 * <code>DomainConfiguration</code>.
	 * 
	 * @param dc the <code>DomainConfiguration</code> to append
	 */
	public void appendDomain(DomainConfiguration dc) {
		domains.add(dc);
		dc.setKernelConfiguration(this);
	}
	
	/**
 	 * Removes a <code>DomainConfiguration</code> from this
	 * <code>KernelConfiguration</code>. This method
	 * ensures encapsulation of the internal datastructure
	 * implementation that holds the domains.
	 * Note that this method will remove the first 
	 * <code>DomainConfiguration</code> it will encounter,
	 * considered to <code>equals()</code> the passed in
	 * <code>DomainConfiguration</code>
	 * 
	 * @param dc the <code>DomainConfiguration</code> to remove
	 */
	public void removeDomain(DomainConfiguration dc) {
		domains.remove(dc);
		dc.setKernelConfiguration(null);
	}
	
	/**
	 * Returns the <code>List</code> of <code>DomainConfiguration</code>
	 * objects of the currently loaded configuration. Note that
	 * the returned <code>List</code> is unmodifyable in order to
	 * properly encapsulate the internal implementation.
	 * 
	 * @return a list of domains
	 */
	public List<DomainConfiguration> getDomains() {
		return Collections.unmodifiableList(domains);
	}
	   
    /**
     * Returns the spaceaddress of the space that is currently
     * used to relay communication between components.
     * 
     * @return Returns the spaceAddress.
     */
    public String getSpaceAddress() {
        return spaceAddress;
    }
    /**
     * Sets the spaceaddress of the space that is currently used
     * to relay communication between components.
     *      
     * @param spaceAddress The spaceAddress to set.
     */
    public void setSpaceAddress(String spaceAddress) {
        this.spaceAddress = spaceAddress;
    }

    /**
     * Return the port number this WSMX instance will listen at for HTTP requests.
     * 
     * @return current integer port number
     */
    public int getAxisPort() {
        return axisPort;
    }

    /**
     * Sets the port number this WSMX instance will listen at for HTTP requests.
     * 
     * @param httpPort the integer port number to set
     */
    public void setAxisPort(int axisPort) {
        this.axisPort = axisPort;
    }    
    
    /**
     * Return the port number this WSMX instance will listen at for HTTP requests.
     * 
     * @return current integer port number
     */
    public int getHTTPPort() {
        return httpPort;
    }

    /**
     * Sets the port number this WSMX instance will listen at for HTTP requests.
     * 
     * @param httpPort the integer port number to set
     */
    public void setHTTPPort(int httpPort) {
        this.httpPort = httpPort;
    }

	/**
	 * Return the port number this WSMX instance will listen at for SSH requests.
	 * 
	 * @return the integer port number
	 */
	public int getSSHPort() {
		return sshPort;
	}

	/**
	 * Sets the port number this WSMX instance will listen at for SSH requests.
     * 
	 * @param sshPort the integer port number to set
	 */
	public void setSSHPort(int sshPort) {
		this.sshPort = sshPort;
	}

	@Override
	public String toString() {
		return systemCodebase + "::HTTP:" + httpPort
		       + "::SSH:" + sshPort;
	}

	public String getRmiIiop() {
		return rmiIiop;
	}

	public void setRmiIiop(String rmiIiop) {
		this.rmiIiop = rmiIiop;
	}

	public String getRmiJrmp() {
		return rmiJrmp;
	}

	public void setRmiJrmp(String rmiJrmp) {
		this.rmiJrmp = rmiJrmp;
	}
}

