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

package org.deri.wsmx.mediation.ooMediator.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.deri.wsmx.mediation.ooMediator.Mediator;
import org.deri.wsmx.mediation.ooMediator.MediatorFactory;
import org.deri.wsmx.mediation.ooMediator.mapper.MappingDocument2Mappings;
import org.deri.wsmx.mediation.ooMediator.mapper.Mappings;
import org.deri.wsmx.mediation.ooMediator.storage.Loader;
import org.deri.wsmx.mediation.ooMediator.util.CONSTANTS;
import org.deri.wsmx.mediation.ooMediator.util.WSMXProperties;
import org.omwg.mediation.language.objectmodel.api.MappingDocument;
import org.omwg.mediation.parser.alignment.XpathParser;
import org.omwg.ontology.Ontology;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.factory.Factory;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.ParserException;
import org.xml.sax.SAXException;

import com.ontotext.wsmo4j.common.IRIImpl;
import com.ontotext.wsmo4j.ontology.OntologyImpl;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 08-Jun-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/gui/TestRunTimeDataMediator.java,v $, 
 * @version $Revision: 1.1 $ $Date: 2007-09-27 06:49:03 $
 */

public class TestRunTimeDataMediator extends JFrame{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Loader dbLoader = new DefaultLoader(new OntologyImpl(new IRIImpl("http://deri.org/iswc2005tutorial/ontologies/travel1#travel1")), 
			new OntologyImpl(new IRIImpl("http://deri.org/iswc2005tutorial/ontologies/travel1#travel1")), new Mappings());
	private JComboBox sourceCombo = new JComboBox();
	private JComboBox targetCombo = new JComboBox();
	
	private JTextField sourceField = new JTextField();
	private JTextField targetField = new JTextField();
	private JTextField mappingsField = new JTextField();
	
	private final String WORDNET_PROPERTY_FILE_NAME = "jwnl_properties.xml";
	private int dbConnectivityStatusOntologyRepository;
	private int dbConnectivityStatusMappingRepository;
	private JButton mediate = new JButton("Mediate ->>");
	
	private File defaultLocation = new File("c:/Software/eclipse3.1.2/myhappyworkspace/travel");
	/**
	 * 
	 */
	
	public TestRunTimeDataMediator() {

		String userHome = System.getProperty("user.home");
		String propsFile = userHome + "/" + WORDNET_PROPERTY_FILE_NAME;
		      

		try {
			;
			/*if (!JWNL.isInitialized())
			{
				JWNL.initialize(new FileInputStream(propsFile));
			}
			else
				System.out.print("WordNet is already initialized\n");
			*/

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
		System.out.println(System.getProperty("user.home"));
		WSMXProperties.setPath(System.getProperty("user.home"));
				
		testDBConnection();
		//dbLoader = new StorageLoader();
		sourceCombo = new JComboBox(dbLoader.getAvailableOntologies().toArray());
		targetCombo = new JComboBox(dbLoader.getAvailableOntologies().toArray());
		
		sourceCombo.setRenderer(new  DefaultListCellRenderer(){
			public java.awt.Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus){
				Ontology ontology = (Ontology)value;
				if (ontology !=null)
					return super.getListCellRendererComponent(list, ontology.getIdentifier(), index, isSelected, cellHasFocus);
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		});

		targetCombo.setRenderer(new  DefaultListCellRenderer(){
			public java.awt.Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus){
				Ontology ontology = (Ontology)value;
				if (ontology != null)
					return super.getListCellRendererComponent(list, ontology.getIdentifier(), index, isSelected, cellHasFocus);
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		});				
		
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //benchSize = new Dimension(screenSize.width  - inset*2, screenSize.height - inset*2);
        URL url = this.getClass().getResource("runtime.png"); 
        JPanel panel = buildRuntimeEmulator();
        
		JMenuBar menuBar = new JMenuBar();
		
		JMenu storageConnection = new JMenu("Storage Connection");
		menuBar.add(storageConnection);
		JMenuItem connect = new JMenuItem("Connect...");
		storageConnection.add(connect);
		connect.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if (createGeneralConnectionDialog()==0 || createMappingConnectionDialog()==0)
					mediate.setEnabled(false);
				else
					mediate.setEnabled(true);
			}});
	
		this.setJMenuBar(menuBar);       
        
        this.getContentPane().add(panel);
        setIconImage(Toolkit.getDefaultToolkit().createImage(url));
		setVisible(true);
		setTitle("Run-Time Emulator");
		
		this.addWindowListener(new WindowAdapter(){
			
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
			
			public void windowIconified(WindowEvent e){		
			}
		});
	    
		//setSize(600, 400);
		
		//setBounds(inset, inset,
				//screenSize.width  - inset*2, 2 * panel.getHeight());
       //         600, 400);
                //screenSize.height - inset*2);
		this.pack();
		
	}
	
	
	public static void main(String[] args) {
		
		new TestRunTimeDataMediator();
	}
	
	private JPanel buildRuntimeEmulator(){

		sourceField.setColumns(30);
		targetField.setColumns(30);
		mappingsField.setColumns(15);
		
		GridBagLayout gridbag = new GridBagLayout();
		
		JPanel re = new JPanel(gridbag);
		//re.getContentPane().setLayout(gridbag);
		
		GridBagConstraints c1 = new GridBagConstraints();
		
		final JTextArea  sourceArea = new JTextArea();
		sourceArea.setRows(20);
		final JTextArea  targetArea = new JTextArea();
		targetArea.setRows(20);

		mediate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				targetArea.setText("");
				//Ontology source = (Ontology)sourceCombo.getSelectedItem();
				//Ontology target = (Ontology)targetCombo.getSelectedItem();
				StringBuffer payload  = new StringBuffer(sourceArea.getText());
				targetArea.setText(mediateButtonPressed(payload).toString());
			}
		});
		
		JScrollPane sourceSPane = new JScrollPane(sourceArea);
		JScrollPane targetSPane = new JScrollPane(targetArea);
		JLabel sourceComboLabel = new JLabel("Source ontology:");
		JLabel sourceInstanceLabel = new JLabel("Source instance: ");
		JLabel targetComboLabel = new JLabel("Target ontology: ");
		JLabel targetInstance = new JLabel("Target instance: ");
		JPanel mediatePanel = new JPanel(new BorderLayout());
		
		
		JButton mappingButton = new JButton("Browse...");
		mappingButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				if (defaultLocation.exists())
					chooser.setCurrentDirectory(defaultLocation);
				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION){
					mappingsField.setText(chooser.getSelectedFile().getPath());
				}
			}});
		JPanel innerMediatePannel = new JPanel();
		innerMediatePannel.add(new JLabel("Mappings: "));
		innerMediatePannel.add(mappingsField);
		innerMediatePannel.add(mappingButton);
		
		
		
		mediatePanel.add(mediate, BorderLayout.EAST);
		mediatePanel.add(innerMediatePannel, BorderLayout.WEST);
		mediatePanel.setBorder(new LineBorder(Color.GRAY));

		
		JButton sourceBrowse = new JButton("Browse...");
		sourceBrowse.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				if (defaultLocation.exists())
					chooser.setCurrentDirectory(defaultLocation);				
				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION){
					sourceField.setText(chooser.getSelectedFile().getPath());
				}
			}});
		
		JButton targetBrowse = new JButton("Browse...");
		targetBrowse.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				if (defaultLocation.exists())
					chooser.setCurrentDirectory(defaultLocation);				
				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION){
					targetField.setText(chooser.getSelectedFile().getPath());
				}
			}});
		
		
		JPanel sourceComboPanel = new JPanel(new BorderLayout());
		JPanel sourceComboPanelInner = new JPanel();
		sourceComboPanelInner.add(sourceField);
		sourceComboPanelInner.add(sourceBrowse);
		sourceComboPanel.add(sourceComboPanelInner, BorderLayout.WEST);

		JPanel targetComboPanel = new JPanel(new BorderLayout());
		JPanel targetComboPanelInner = new JPanel();
		targetComboPanelInner.add(targetField);
		targetComboPanelInner.add(targetBrowse);
		targetComboPanel.add(targetComboPanelInner, BorderLayout.WEST);
		
		c1.fill = GridBagConstraints.BOTH;
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1.0;
		c1.weighty = 0.0;
		c1.insets = new Insets(2, 5, 2, 5);
		gridbag.setConstraints(sourceComboLabel, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 0.0;
		//c1.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(sourceComboPanel, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1.0;  
		//c1.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(sourceInstanceLabel, c1);		
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1.0;  
		c1.weighty = 1.0;
		gridbag.setConstraints(sourceSPane, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 0.0;
		c1.weighty = 0.0;
		//c1.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(mediatePanel, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1.0;  
		gridbag.setConstraints(targetComboLabel, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 0.0;  
		//c1.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(targetComboPanel, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1.0;  
		//c1.fill = GridBagConstraints.BOTH;		
		gridbag.setConstraints(targetInstance, c1);		
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1.0;
		c1.weighty = 1.0;
		gridbag.setConstraints(targetSPane, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1.0;
		c1.weighty = 0.0;

		re.add(sourceComboLabel);
		re.add(sourceComboPanel);
		re.add(sourceInstanceLabel);
		re.add(sourceSPane);
		re.add(mediatePanel);
		re.add(targetComboLabel);
		re.add(targetComboPanel);
		re.add(targetInstance);
		re.add(targetSPane);
		
		
		
		return re;
		//return null;
	}	
	
	private StringBuffer mediateButtonPressed(StringBuffer payload){

		Ontology srcO = parseOntologyFile(sourceField.getText());
		Ontology tgtO = parseOntologyFile(targetField.getText());
		Mappings mappings = null;
		if (srcO == null && tgtO == null)
			return new StringBuffer("Ontology files ERROR");
		mappings = parseMappingFile(mappingsField.getText(), srcO, tgtO);
		if (mappings==null)
			return new StringBuffer("Mapping file ERROR");
		dbLoader = new DefaultLoader(srcO, tgtO, mappings);		
		Mediator mediator = MediatorFactory.createMediator(CONSTANTS.WSML_DATA_MEDIATOR, dbLoader);
		System.out.println("*** " + srcO + " -> " + tgtO);
		return mediator.mediate(srcO, tgtO, payload);
		//return new StringBuffer("Setting OK");
		
	}
	
	
    public void testDBConnection() {
        /*
    	UtilDB testConnection = new UtilDB();
        if (!testConnection.connect()) {
            if (createGeneralConnectionDialog() == 1)
                dbConnectivityStatusOntologyRepository = 1;
            else
                dbConnectivityStatusOntologyRepository = 0;
        }
        else
            dbConnectivityStatusOntologyRepository = 1;
        UtilDB testConnection1 = new UtilDB(ConstantsDB.dbMappingName);
        if (!testConnection1.connect())
            if (createMappingConnectionDialog() == 1)
                dbConnectivityStatusMappingRepository = 1;
            else
                dbConnectivityStatusMappingRepository = 0;
        else
            dbConnectivityStatusMappingRepository = 1;
        
        if (dbConnectivityStatusOntologyRepository == 0 || dbConnectivityStatusMappingRepository == 0)
        	mediate.setEnabled(false);
        else
        	mediate.setEnabled(true);
        //refreshMenuBar.actionPerformed(null);
         * 
         */
    }

    private int createGeneralConnectionDialog() {
    	/*
        Object[] components = new Object[10];
        JLabel title = new JLabel("Please set the details for the ontology repository connection:");
        components[0] = title;
        JLabel dbURL = new JLabel("URL:");
        JTextField dbURLField = new JTextField(UtilDB.getDbOntologyURL());
        components[1] = dbURL;
        components[2] = dbURLField;
        JLabel dbUser = new JLabel("User name:");
        JTextField dbUserField = new JTextField(UtilDB.getDbOntologyUser());
        components[3] = dbUser;
        components[4] = dbUserField;
        JLabel dbPassw = new JLabel("Password:");
        JPasswordField dbPasswField = new JPasswordField(UtilDB.getDbOntologyPassword());
        dbPasswField.setFont(dbUserField.getFont());
        components[5] = dbPassw;
        components[6] = dbPasswField;
        JLabel dbDriver = new JLabel("Driver:");
        JTextField dbDriverField = new JTextField(UtilDB.getOntologyDriver());
        components[7] = dbDriver;
        components[8] = dbDriverField;
        JCheckBox createTables = new JCheckBox("Create necessary tables?");
        createTables.setSelected(false);
        components[9] = createTables;

        int option = JOptionPane.showConfirmDialog(this, components, "Ontology Repository Connection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            UtilDB.setDbOntologyURL(dbURLField.getText());
            UtilDB.setDbOntologyUser(dbUserField.getText());
            UtilDB.setDbOntologyPassword(new String(dbPasswField.getPassword()));
            UtilDB.setOntologyDriver(dbDriverField.getText());
            UtilDB testConnection = new UtilDB();
            if (!testConnection.connect()) {
                return createGeneralConnectionDialog();
            }
            if (createTables.isSelected())
                testConnection.createOntologyStoreTables();
            WSMXProperties.getInstance().setProperty(ConstantsDB.DB_ONTOLOGY_URL_PROPERTY, dbURLField.getText());
            WSMXProperties.getInstance().setProperty(ConstantsDB.DB_ONTOLOGY_USER_PROPERTY, dbUserField.getText());
            WSMXProperties.getInstance().setProperty(ConstantsDB.DB_ONTOLOGY_PASSWORD_PROPERTY, new String(dbPasswField.getPassword()));
            WSMXProperties.getInstance().setProperty(ConstantsDB.DB_ONTOLOGY_DRIVER_PROPERTY, dbDriverField.getText());
            WSMXProperties.getInstance().saveProperties();
            //refreshMenuBar.actionPerformed(null);
            return 1;
        }
        */
        return 0;
    }

    private int createMappingConnectionDialog() {
        /*
    	Object[] components = new Object[10];
        JLabel title = new JLabel("Please set the details for the mappings repository connection:");
        components[0] = title;
        JLabel dbURL = new JLabel("URL:");
        JTextField dbURLField = new JTextField(UtilDB.getDbMappingURL());
        components[1] = dbURL;
        components[2] = dbURLField;
        JLabel dbUser = new JLabel("User name:");
        JTextField dbUserField = new JTextField(UtilDB.getDbMappingUser());
        components[3] = dbUser;
        components[4] = dbUserField;
        JLabel dbPassw = new JLabel("Password:");
        JPasswordField dbPasswField = new JPasswordField(UtilDB.getDbMappingPassword());
        dbPasswField.setFont(dbUserField.getFont());
        components[5] = dbPassw;
        components[6] = dbPasswField;
        JLabel dbDriver = new JLabel("Driver:");
        JTextField dbDriverField = new JTextField(UtilDB.getMappingriver());
        components[7] = dbDriver;
        components[8] = dbDriverField;
        JCheckBox createTables = new JCheckBox("Create necessary tables?");
        createTables.setSelected(false);
        components[9] = createTables;

        int option = JOptionPane.showConfirmDialog(this, components, "Mapping Repository Connection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            UtilDB.setDbMappingURL(dbURLField.getText());
            UtilDB.setDbMappingUser(dbUserField.getText());
            UtilDB.setDbMappingPassword(new String(dbPasswField.getPassword()));
            UtilDB.setMappingriver(dbDriverField.getText());
            UtilDB testConnection = new UtilDB(ConstantsDB.dbMappingName);
            if (!testConnection.connect()) {
                return createMappingConnectionDialog();
            }
            if (createTables.isSelected())
                testConnection.createMappingStoreTables();
            WSMXProperties.getInstance().setProperty(ConstantsDB.DB_MAPPING_URL_PROPERTY, dbURLField.getText());
            WSMXProperties.getInstance().setProperty(ConstantsDB.DB_MAPPING_USER_PROPERTY, dbUserField.getText());
            WSMXProperties.getInstance().setProperty(ConstantsDB.DB_MAPPING_PASSWORD_PROPERTY, new String(dbPasswField.getPassword()));
            WSMXProperties.getInstance().setProperty(ConstantsDB.DB_MAPPING_DRIVER_PROPERTY, dbDriverField.getText());
            WSMXProperties.getInstance().saveProperties();
            //refreshMenuBar.actionPerformed(null);
            return 1;
        }*/
        return 0;
    }
	
    private Ontology parseOntologyFile(String theFilePath) {
        
    	File theFile = new File(theFilePath);
    	if (!theFile.exists())
    		return null;
 
    	Reader payload;
		try {
			payload = new FileReader(theFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return null;
		}
    	
    	Parser parser = Factory.createParser(null);
 
        TopEntity[] holders = null;
		try {
			holders = parser.parse(payload);
			if (holders.length>0 && holders[0] instanceof Ontology)
				return (Ontology)holders[0];
		} catch (ParserException e) {
			e.printStackTrace();
		} catch (InvalidModelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    private Mappings parseMappingFile(String theFilePath, Ontology src, Ontology tgt){
    	File theFile = new File(theFilePath);
    	if (!theFile.exists())
    		return null;
 
    	MappingDocument md = null;
		try {								
			md = (MappingDocument)XpathParser.parse(theFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return new MappingDocument2Mappings().mappingDocument2Mappings(src, tgt, md);
				
    }
    
}

