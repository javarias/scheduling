/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration)
 *    and Cosylab 2002, All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 */
package alma.scheduling.testutils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import alma.acs.component.client.AdvancedComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.logging.ClientLogManager;
import alma.acs.logging.AcsLogLevel;

import alma.exec.extension.subsystemplugin.SubsystemPlugin;

/**
 * Start a plugin for exec without starting EXEC.
 * Generic class written hacking Maurizio and Markus code
 * 
 * @author acaproni
 *
 */
public class PluginStarter extends JFrame {
    
    // lots of code just to trap a WindowClosing event...
    class MyWindowAdapter extends WindowAdapter {
        
        private SubsystemPlugin plugin;
        
        public MyWindowAdapter( JFrame frame, SubsystemPlugin plugin) {
        	if (frame==null || plugin==null) {
        		throw new IllegalArgumentException("Invalid null param in MyWindowAdapter");
        	}
            this.plugin = plugin;
            frame.setDefaultCloseOperation( javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        }

        /**
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         */
        @Override
		public void windowClosing( WindowEvent e ) {
//        	controlDlg.setVisible(false);
//        	controlDlg.dispose();
        	logger.log(Level.INFO,"Shutting down the plugin");
            try {
            	plugin.stop();
            }  catch (Exception ex) {
                ex.printStackTrace();
            } finally {
            	setVisible(false);
                dispose();
                plugin=null;
            }
        }
    }
    
    /** 
     * The instance of PluginContainerServices we pass to
     * our plugin via the setServices() method.
     */
    //private MockPluginContainerServices pcservices;

    /**
     *  Our plugin 
     */
    private SubsystemPlugin plugin;
    
    /**
     * The properties for the plugin
     */
    private Properties properties;
    
    /**
     * ACS client
     */
    private AdvancedComponentClient client;
    
    /**
     * The logger
     */
    private Logger logger;
    
    // The dialog to control the life cycle of the plugin
//    private PluginControlDlg controlDlg;
    
    /**
     * ACS container services
     */
    private ContainerServices containerServices;
    
    /**
     * Constructor 
     * 
     * @param title The title of the window
     * @param plugin The plugin to show/activate
     * @param props Custom properties
     * 
     * @throws Exception
     */
    public PluginStarter(String title,SubsystemPlugin plugin, Properties props) throws Exception {
    	super();
    	if (plugin==null || props==null) {
    		throw new IllegalArgumentException("Invalid null param in PluginStarter constructor ");
    	}
    	setName("PluginStarter");
    	setTitle("Plugin tester - "+title);
    	this.plugin=plugin;
    	this.properties=props;
    	setSize(640,480);
    	
    	// Move the window to the center of the screen 
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(
        		Math.max(0,(screenSize.width -windowSize.width)/2), 
        		Math.max(0,(screenSize.height-windowSize.height)/2));
        
		
		// Connect to ACS
        try {
        	connectACSClient();
        } catch (Exception e) {
        	System.err.println("Error connecting ACS client");
        	if (logger!=null) {
        		logger.log(Level.SEVERE,"Error connecting ACS client");
        		System.exit(-1);
        	}
        }
        
        containerServices=client.getContainerServices();
        if (containerServices==null) {
        	logger.log(AcsLogLevel.ERROR, "ContainerServices is null!");
        	System.err.println("Error: client ContainerServices is null!");
        	System.exit(-1);
        }
        logger.log(Level.INFO, "Got the ContainerServices");
        // Set the container services
        PluginContainerServices pluginSvc = new PluginContainerServices(containerServices,props);
        plugin.setServices(pluginSvc);
        
        // Add the plugin to the GUI
        logger.log(Level.INFO, "Attaching the plugin");
        attachPlugin(plugin);
        
        pack();
		setVisible(true);
        
        MyWindowAdapter windowListener = new MyWindowAdapter(this,plugin);
        this.addWindowListener(windowListener);
        
        // Start the plugin
        logger.log(Level.INFO,"Starting the plugin");
		launchPlugin(plugin);
		
		// Show the control dialog
//		Rectangle p = getBounds();
//		controlDlg=new PluginControlDlg(plugin,logger,p);
    }
    
    /**
     * Connect the ACS client
     * 
     * @return A reference to the connected client
     */
    private void connectACSClient() throws Exception {
    	String clientName=getClass().getName();
		logger = ClientLogManager.getAcsLogManager().getLoggerForApplication(clientName, true);
		logger.log(Level.INFO,"Logger initialized");
        String managerLoc = System.getProperty("ACS.manager");
        if (managerLoc==null) {
        	throw new Exception("Error getting ACS.manager property");
        } else {
        	managerLoc=managerLoc.trim();
        }
        
       	client = new AdvancedComponentClient(logger, managerLoc, clientName);
       	logger.log(Level.INFO, "AdvancedComponentClient initialized");
    }
    
    /**
     * Execute the plugin as a separate thread
     * 
     * @param pl The plugin to execute
     */
    private void launchPlugin(SubsystemPlugin pl) {
    	class MyThread extends Thread {
    		SubsystemPlugin plugIn;
    		public MyThread(SubsystemPlugin thePlugin) {
    			super();
    			if (thePlugin==null) {
    				throw new IllegalArgumentException("Null plugin");
    			}
    			plugIn=thePlugin;
    		}
    		@Override
			public void run() {
    			try {
    				plugIn.start();
    			} catch (Exception e) {
    				System.err.println("Exception starting the plugin "+e.getMessage());
    				e.printStackTrace();
    				System.exit(-1);
    			}
    		}
    	};
    	MyThread thread = new MyThread(pl);
    	thread.start();
    }
    
    /** 
     * Attache the component to the GUI
     * 
     * @param pl The component to show in the frame
     */
    private void attachPlugin(SubsystemPlugin pl) {
    	class MyRunnable extends Thread {
    		SubsystemPlugin plugIn;
    		public MyRunnable(SubsystemPlugin thePlugin) {
    			super();
    			if (thePlugin==null) {
    				throw new IllegalArgumentException("Null plugin");
    			}
    			plugIn=thePlugin;
    		}
    		@Override
			public void run() {
    			if (plugIn instanceof JRootPane) {
    	    		setRootPane((JRootPane)plugIn);
    	    	} else {
    	    		add((JComponent)plugIn,BorderLayout.CENTER);
    	    	}
    		}
    	};
    	MyRunnable runnable = new MyRunnable(pl);
    	SwingUtilities.invokeLater(runnable);
    }

    /**
     * The name of the class of the plugin to start must be in the command line
     * It is possible to add the name of a properties file as second parameter
     * 
     * @param args
     * @throws Exception
     */
    public static void main( String[] args ) throws Exception {
    	if (args.length<1) {
    		throw new IllegalArgumentException("The name of the plugin class is expected as first argument");
    	}
    	SubsystemPlugin plugin=null;
    	try {
    		plugin=buildPlugin(args[0]);
    	} catch (ClassNotFoundException e) {
    		System.err.println("Error: class "+args[0]+" not found");
    		System.exit(-1);
    	} catch (Exception e) {
    		System.err.println("Error: "+e.getMessage());
    		System.exit(-1);
    	}
    	Properties props = new Properties();
    	if (args.length==2) {
    		try {
    			loadProperties(args[1],props);
    		} catch (Exception e) {
    			System.err.println("Error reading "+args[1]+": "+e.getMessage());
    			System.exit(-1);
    		}
    	}
    	// Everything is ok: create the window
    	PluginStarter pluginStarter = new PluginStarter(args[0],plugin,props);
    }
    
    /**
     * Build the plugin with its empty constructor
     * 
     * @param className The name of the class of the plugin
     * @return
     */
    private static SubsystemPlugin buildPlugin(String className) throws 
    ClassNotFoundException, 
    NoSuchMethodException, 
    InstantiationException,
    InvocationTargetException,
    IllegalAccessException {
    	Thread t = Thread.currentThread();
		ClassLoader loader = t.getContextClassLoader();
		Class cl =loader.loadClass(className);
		System.out.println(cl.getSuperclass().getName());
		boolean found=false;
		
		Class c = cl;
		while (c!=null) {
			Class[] interfaces = c.getInterfaces();
			for (Class interf: interfaces) {
				if (interf.getName().equals("alma.exec.extension.subsystemplugin.SubsystemPlugin")) {
					found=true;
					break;
				}	
			}
			c=c.getSuperclass();
		}
		
		if (!found) {
			throw new IllegalArgumentException("The class does not implement SubsystemPlugin");
		}
		// Get the empty constructor 
		Class[] classes = {};
		Constructor constructor = cl.getConstructor(classes);
		Object obj = constructor.newInstance();
		return (SubsystemPlugin)obj;
    }
    
    /**
     * Load the properties from a file
     * 
     * @param fileName The name of the file of props
     * @param properties The Properties to load the new ones in
     * @throws IOException
     */
    private static void loadProperties(String fileName, Properties properties) throws IOException {
    	if (properties==null || fileName==null) {
    		throw new IllegalArgumentException("Invalid null parameter");
    	}
    	File file = new File(fileName);
    	if (!file.canRead()) {
    		throw new IOException(fileName+" not found or unreadable");
    	}
    	FileInputStream inF = new FileInputStream(file);
    	properties.load(inF);
    }
}
