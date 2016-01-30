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
package ie.deri.wsmx.visualisation.standalone;

import ie.deri.wsmx.core.configuration.annotation.Exposed;
import ie.deri.wsmx.core.configuration.annotation.Start;
import ie.deri.wsmx.core.configuration.annotation.WSMXComponent;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputAdapter;

import org.apache.log4j.Logger;

/**
 * A visualisation component that shows
 * when components are deployed, undeployed and
 * invoked by the execution semantics.
 * It supports local and remote visualisation.
 *
 * <pre>
 * Created on 01.07.2005
 * Committed by $Author: maciejzaremba $
 * * </pre>
 * 
 * @author Thomas Haselwanter
 *
 */ 
@WSMXComponent(name = "Visualisation",
			   notifications = {"COMPONENT_DEPLOYED",
		                        "COMPONENT_UNDEPLOYED", 
		                        "TASK_SCHEDULED"})
public class Visualisation extends MouseInputAdapter implements NotificationListener {
	static Logger logger = Logger.getLogger(Visualisation.class);
	
    public JWindow window = new JWindow();  
    public JPanel panel;  
    public Point origin = new Point();

    private Map<String, JLabel> components = new HashMap<String, JLabel>();
    private boolean remoteVisualisationEnabled = false;
    //default address
    //private String remoteVisualisationAddress = "http://62.116.8.109:8080/axis/services/VisualisationWS";
    
//    public String invokeWS(String status) {    
//    	logger.debug("Updating remote visualisation with: " + status);
//        VisualisationWSServiceLocator vwss = new VisualisationWSServiceLocator();
//        vwss.setVisualisationWSEndpointAddress(remoteVisualisationAddress);
//        VisualisationWS vws = null;
//        try {
//        	vws = vwss.getVisualisationWS();
//        } catch (ServiceException se) {
//        	logger.warn("Failed to connect to visualisation WS.", se);
//        }
//        String visualisationReturn = null;
//        try {
//			visualisationReturn = vws.showStatus(status);
//		} catch (RemoteException re) {
//			logger.warn("Failed to invoke visualisation WS.", re);
//		}
//		logger.debug("Return value of visualisation WS invokation: " + visualisationReturn);
//		return visualisationReturn;
//    }
    
    @Override
	public void mousePressed(MouseEvent e) {
        origin.x = e.getX();
        origin.y = e.getY();
      }

      @Override
	public void mouseDragged(MouseEvent e) {
        Point p = window.getLocation();
        window.setLocation(
          p.x + e.getX() - origin.x, 
          p.y + e.getY() - origin.y);
      }

      @Override
	public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
        	window.setVisible(false);
        }
      }   
    
    @Exposed
    public void showVisualisationWindow() {
    	window.setVisible(true);
    }
    
    @Start
    public void showVisualisation() {
    	window.setSize(220, 350);
        window.setAlwaysOnTop(true);
        window.setVisible(true);
        window.setLayout(new GridLayout(12,1));
        Visualisation visualisation = new Visualisation();
        visualisation.setWindow(window);
        window.addMouseListener(visualisation);
        window.addMouseMotionListener(visualisation);
        window.requestFocus();
        	
        JLabel header = new JLabel(
        		"WSMX Components",
        		new ImageIcon(getBytesFromStream("ie/deri/wsmx/core/management/webfrontend/xsl/threecubes_32.png")),
        		JLabel.CENTER);
        ToolTipManager.sharedInstance().registerComponent(header); 
		//our ToolTips can be quite heavy beasts so we timeout late
		ToolTipManager.sharedInstance().setDismissDelay(30000);
		ToolTipManager.sharedInstance().setInitialDelay(100);
		ToolTipManager.sharedInstance().setEnabled(true);
		header.setSize(150, 64);
        
        header.setToolTipText("<html><b>WSMX Visualisation</b><br>" +
				 "Click and hold the mouse to drage this window, double-click" +
				 "to make this window invisible, use the webconsole to make " +
				 "this window visible again.<br>" +
				 "</html>.");

		window.getContentPane().add(header);
		window.validate();
//        panel = new JPanel() {
//        	
//        	int x = 30, y = 30;
//        	
//			public void setCoordinates(int x, int y) {
//				this.x = x;
//				this.y = y;
//			}
//
//			@Override
//			public void paint(Graphics g) {
//				g.setColor(Color.RED);
//				g.fillOval(x, y, 10, 10);
//				g.dispose();
//			}
//        };
//        panel.setSize(300,300);
//        panel.setBounds(window.getBounds());
//        panel.setBackground(new Color(0,0,0,0));        
    }

	private byte[] getBytesFromStream(String resource) {
		byte[] bytes = null;
		InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
        ArrayList<Integer> buffer = new ArrayList<Integer>();
        int readByte;
        try {
        while((readByte = stream.read()) != -1)
        	buffer.add(new Integer(readByte));
        }catch (IOException e) {
        	logger.warn("Failed to read icon.", e);
        }
        bytes = new byte[buffer.size()];
        for (int i = 0; i < buffer.size(); i++)
        	bytes[i] = (byte)buffer.get(i).intValue();
		return bytes;
	}
	
	public void handleNotification(Notification notification, Object handback) {		
        String type = notification.getType();
		if (type.equals("COMPONENT_DEPLOYED")) {
			logger.debug("COMPONENT_DEPLOYED received.");
			if (notification.getUserData() instanceof List)
				updateComponents((List<String>)notification.getUserData());
			else	
				logger.warn("Userdata not of expected type List.");
	    	window.validate();			
		} else if (type.equals("COMPONENT_UNDEPLOYED")) {			
			logger.debug("COMPONENT_UNPLOYED received for " + notification.getMessage());
			removeComponent(notification.getMessage());
	    	window.validate();			
		} else if (type.equals("TASK_SCHEDULED")) {
			logger.debug("TASK_SCHEDULED received for " + notification.getMessage());
			setActive(notification.getMessage());
		}		
	}

	private void updateComponents(List<String> currentSet) {
		for(String name : currentSet) {
			if (!components.containsKey(name) && !name.contains("Visualisation")) {
				JLabel label = new JLabel(name,
						new ImageIcon(getBytesFromStream("ie/deri/wsmx/core/management/webfrontend/xsl/blackcube_32.png")),
						JLabel.LEFT);	
//				label.setVerticalAlignment(JLabel.CENTER);
//				label.setHorizontalAlignment(SwingConstants.LEFT);
				components.put(name, label);
				ToolTipManager.sharedInstance().registerComponent(label); 
				// our ToolTips can be quite heavy beasts so we timeout late
				ToolTipManager.sharedInstance().setDismissDelay(30000);
				label.setFocusable(true);
				label.setToolTipText("<html><b>" + name + "</b><br>" +
						 "This compnent is being invoked by the execution semantic.<br>" +
						 "</html>.");
				label.setSize(150, 50);
				label.setForeground(Color.BLACK);
				window.getContentPane().add(label);
			}
		}
	}

	private void removeComponent(String name) {
		JLabel label = components.get(name);
		window.getContentPane().remove(label);
	}

	private void setActive(String name) {
		logger.debug("Setting component " + name + " active.");
		//local
		for (JLabel label : components.values())
			label.setForeground(Color.BLACK);
		JLabel active = components.get(name);
		if (active != null) {
			active.setForeground(Color.RED);
		}
		//remote
//		if(remoteVisualisationEnabled)
//			invokeWS(name);
	}

	public JWindow getWindow() {
		return window;
	}

	public void setWindow(JWindow window) {
		this.window = window;
	}
	
//	@Exposed
//	public String getRemoteVisualisationAddress() {
//		return remoteVisualisationAddress;
//	}
//
//	@Exposed
//	public void setRemoteVisualisationAddress(String remoteVisualisationAddress) {
//		this.remoteVisualisationAddress = remoteVisualisationAddress;
//	}
//
//	@Exposed
//	public boolean isRemoteVisualisationEnabled() {
//		return remoteVisualisationEnabled;
//	}

//	@Exposed
//	public void setRemoteVisualisationEnabled(boolean remoteVisualisationEnabled) {
//		this.remoteVisualisationEnabled = remoteVisualisationEnabled;
//	}
	
//	@Exposed
//	public String updateRemoteVisualisation(String status) {
//		return "Visualisation Web Service returned " + invokeWS(status);
//	}

    @Exposed
    public void testURLloading() {        
        URL url = getClass().getResource("ie/deri/wsmx/core/management/webfrontend/xsl/threecubes_32.png");
        JLabel label = new JLabel(
                "ResourceLoading",
                new ImageIcon(url),
                JLabel.LEFT);
        window.getContentPane().add(label);
        window.validate();
    }
}
