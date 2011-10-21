package alma.scheduling.policy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeSelectionModel;

import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.scheduling.Master;
import alma.scheduling.SchedulingPolicyFile;
import alma.scheduling.policy.gui.PoliciesTreeModel.PoliciesFileTreeNode;

public class PolicyManagementPanel extends JPanel implements PolicyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4690777794339534919L;
	
	private final Master master;
	private JTree policiesTree;
	private JButton selectPolicyButton;
	private JButton refreshPoliciesButton;
	private JButton loadFile;
	private JMenuItem selectPolicyMenuItem;
	private JMenuItem deletePolicyMenuItem;
	private JMenuItem refreshPolicyMenuItem;
	private JPopupMenu treePopupMenu;
	private String selectedBeanName = "";
	private List<PolicySelectionListener> listeners;
	private SchedulingPolicyFile selectedPolicyFile = null;
	
	public PolicyManagementPanel(Master masterSchedulierRef) {
		this.master = masterSchedulierRef;
		this.listeners = new ArrayList<PolicySelectionListener>();
		initialize();
	}
	
	private void initialize() {
		selectPolicyButton = initializeSelectionButton();
		policiesTree = initializePoliciesTree();
		loadFile    = initialiseLoadFileControl();
		final JPanel centre = createCentrePanel();
		final JPanel south  = createSouthPanel();
		this.setLayout(new BorderLayout());
		this.add(centre, BorderLayout.CENTER);
		this.add(south,  BorderLayout.SOUTH);
	}

	private JTree initializePoliciesTree() {
		JTree tree = new JTree();
		tree.setModel(new PoliciesTreeModel(master.getSchedulingPolicies()));
		tree.getSelectionModel().setSelectionMode(
				DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.getSelectionModel().addTreeSelectionListener(
				new TreeSelectionListener() {

					@Override
					public void valueChanged(TreeSelectionEvent e) {
						if (e.getPath() != null) {
							System.out.println(e.getPath().getPathCount());
							if (e.getPath().getPathCount() == 3) {
								selectedBeanName = ((PoliciesFileTreeNode) e
										.getPath().getPath()[1])
										.getBeanName((String) e.getPath()
												.getPath()[2]);
								selectPolicyButton.setEnabled(true);
								selectPolicyMenuItem.setEnabled(true);
								selectedPolicyFile = null;
								refreshPolicyMenuItem.setEnabled(false);
							} else if (e.getPath().getPathCount() == 2
									&& !((PoliciesFileTreeNode) e.getPath()
											.getPath()[1]).isSystemFile()) {
								selectedBeanName = "";
								selectPolicyButton.setEnabled(false);
								selectPolicyMenuItem.setEnabled(false);
								selectedPolicyFile = ((PoliciesFileTreeNode) e
										.getPath().getPath()[1]).getFile();
								try {
									if (InetAddress
											.getLocalHost()
											.getHostName()
											.compareToIgnoreCase(
													selectedPolicyFile.hostname) == 0) {
										refreshPolicyMenuItem.setEnabled(true);
									} else {
										selectedPolicyFile = null;
										refreshPolicyMenuItem.setEnabled(false);
									}

								} catch (UnknownHostException e1) {
									e1.printStackTrace();
									refreshPolicyMenuItem.setEnabled(false);
								}
							} else {
								selectedBeanName = "";
								selectPolicyButton.setEnabled(false);
								selectPolicyMenuItem.setEnabled(false);
								selectedPolicyFile = null;
								refreshPolicyMenuItem.setEnabled(false);
							}
						}
					}
				});
		treePopupMenu = initializeTreePopupMenu();
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				openPopupMenu(e);
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 &&
						selectedBeanName.compareTo("") != 0) {
					System.out.println("Policy: " + selectedBeanName + " selected");
					notifyPolicySelected(selectedBeanName);
					closeFrame();
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				openPopupMenu(e);
			}
			
			private void openPopupMenu(MouseEvent e) {
				if (e.isPopupTrigger()) {
					treePopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		return tree;
	}
	
	private JButton initializeSelectionButton() {
		JButton button = new JButton("Select Policy");
		button.setEnabled(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPolicyButton.setEnabled(false);
				notifyPolicySelected(selectedBeanName);
				selectPolicyButton.setEnabled(true);
			}
		});
		return button;
	}
	
	private JPopupMenu initializeTreePopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		selectPolicyMenuItem = new JMenuItem("Select policy");
		selectPolicyMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPolicyButton.setEnabled(false);
				notifyPolicySelected(selectedBeanName);
				selectPolicyButton.setEnabled(true);
			}
		});
		menu.add(selectPolicyMenuItem);
		refreshPolicyMenuItem =  new JMenuItem("Refresh policy");
		refreshPolicyMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				StringBuilder b = new StringBuilder();
				FileReader reader = null;
				try {
					reader = new FileReader(selectedPolicyFile.path);
				int c;
				while ((c = reader.read()) != -1) {
					b.append((char)c);
				}
				} catch (FileNotFoundException ex) {
					// TODO Add dialog showing the error and details
					ex.printStackTrace();
				} catch (IOException ex) {
					// TODO Add dialog showing the error and details
					ex.printStackTrace();
				} finally {
					if (reader != null)
						try {
							reader.close();
						} catch (IOException ex) {
							// TODO Add dialog showing the error and details
							ex.printStackTrace();
						}
				}
				try {
					master.refreshSchedulingPolicies(
							selectedPolicyFile.uuid, 
							selectedPolicyFile.hostname, 
							selectedPolicyFile.path, b.toString());
				} catch (SchedulingInternalExceptionEx ex) {
					// TODO Dialog here
					ex.printStackTrace();
				}
			}
		});
		menu.add(refreshPolicyMenuItem	);
		
		deletePolicyMenuItem =  new JMenuItem("Delete policies file");
		deletePolicyMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					master.removeSchedulingPolicies(selectedPolicyFile.uuid);
					refreshPolicyList();
				} catch (SchedulingInternalExceptionEx e1) {
					// TODO Dialog here
					e1.printStackTrace();
				}
			}
		});
		menu.add(deletePolicyMenuItem);
		return menu;
	}
	
	public synchronized void refreshPolicyList() {
		try {
			refreshPoliciesButton.setEnabled(false);
			refreshPoliciesButton.setForeground(Color.RED);
			refreshPoliciesButton.setBackground(Color.PINK);
		} catch (Exception ex) {}
		try {
			selectPolicyButton.setEnabled(false);
		} catch (Exception ex) {}
		try {
			policiesTree.setEnabled(false);
		} catch (Exception ex) {}
		new RefreshThread().start();
	}
	
	private class RefreshThread extends Thread {

		@Override
		public void run() {
			try {
				policiesTree.setModel(new PoliciesTreeModel(master.getSchedulingPolicies()));
			} catch (org.omg.CORBA.SystemException ex) {
				//Add dialog showing the error and details 
			} catch (Exception ex) {}
			finally {
				try {
					refreshPoliciesButton.setEnabled(true);
				} catch (Exception ex) {}
				try {
					selectPolicyButton.setEnabled(true);
				} catch (Exception ex) {}
				try {
					policiesTree.setEnabled(true);
				} catch (Exception ex) {}
			}
		}
	}
	
	/*
	 * ================================================================
	 * Listener management
	 * ================================================================
	 */
	public synchronized void addListener(PolicySelectionListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(PolicySelectionListener listener) {
		listeners.remove(listener);
	}
	
	public synchronized void notifyPolicySelected(String beanName) {
		for (final PolicySelectionListener listener : listeners) {
			listener.policySelected(beanName);
		}
	}
	/* End Listener management
	 * ============================================================= */
//	
//	
//	
//	/*
//	 * ================================================================
//	 * dynamicMode control
//	 * ================================================================
//	 */
//	public synchronized void addDynamicModeActionListener(ActionListener listener) {
//		dynamicMode.addActionListener(listener);
//	}
//	
//	public synchronized void removeDynamicModeActionListener(ActionListener listener) {
//		dynamicMode.removeActionListener(listener);
//	}
//	
//	private JComboBox initialiseDynamicModeControl() {
//		final String[] choices = { "Active", "Passive" };
//		final JComboBox result = new JComboBox(choices);
//		result.setToolTipText("Set whether to run the dynamic scheduler in Active or Passive mode. See documentation for more information");
//		result.setSelectedIndex(1);
//		result.setName("Dynamic Scheduling Mode");
//		
//		return result;
//	}
//	/* End dynamicMode control
//	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * loadFile control
	 * ================================================================
	 */
	private JButton initialiseLoadFileControl() {
		final ActionListener listener = new ActionListener(){

			JFileChooser chooser = null;
			
			@Override
			public void actionPerformed(ActionEvent event) {
				if (chooser == null) {
					chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					chooser.setMultiSelectionEnabled(false);
					chooser.setFileFilter(new FileFilter(){

						@Override
						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							}

							final String name = f.getName().toLowerCase();
							return name.endsWith(".xml");
						}

						@Override
						public String getDescription() {
							return "XML files (*.xml)";
						}});
				}
				
				int val = chooser.showDialog(PolicyManagementPanel.this,
						"Load Policy File");
				if (val == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					try {
						ingestPolicyFile(file);
					} catch (IOException e) {
						System.out.println(".. and here we had an Exception (" +
								e.getMessage() +
								") trying to ingest " +
								file.getPath());
						e.printStackTrace();
					} catch (SchedulingInternalExceptionEx e) {
						System.out.println(".. and here we had an InternalException (" +
								e.getMessage() +
								") trying to ingest " +
								file.getPath());
						e.printStackTrace();
					}
				}
				
			}

			private void ingestPolicyFile(File file)
					throws IOException, SchedulingInternalExceptionEx {
				System.out.println("Opening file " + file.getCanonicalPath());
				FileReader r = null;
				StringBuilder  b  = null;
		        try {
					r = new FileReader(file);
					b = new StringBuilder();
		            int c;

		            while ((c = r.read()) != -1) {
		                b.append((char) c);
		            }

		        } finally {
		            if (r != null) {
		            	r.close();
		            }
		        }
		        
		        System.out.format("master.addSchedulingPolicies(%s, %s, %s...)%n",
		        		 InetAddress.getLocalHost().getHostName(),
						 file.getCanonicalPath(),
						 b.toString().substring(0, 100));
		        master.addSchedulingPolicies(InetAddress.getLocalHost().getHostName(),
						 file.getCanonicalPath(),
						 b.toString());
			}};
		
		final JButton result = new JButton("Load File");
		result.setToolTipText("Load a new Policy File");
		result.setName("Load File");
		result.addActionListener(listener);
		
		return result;
	}
	/* End loadFile control
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Subsidiary Panel Creation
	 * ================================================================
	 */
	private JPanel createCentrePanel() {
		final JPanel result = new JPanel();
		result.setLayout(new BorderLayout(5, 5));
		policiesTree.setSize(320, 400);
		policiesTree.setMinimumSize(new Dimension(320, 400));
		final JScrollPane scroll = new JScrollPane(policiesTree);
		result.add(scroll, BorderLayout.CENTER);
		return result;
	}
	
	private JPanel createSouthPanel() {
		final JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
		result.add(selectPolicyButton);
		result.add(Box.createHorizontalGlue());
		result.add(loadFile);
		return result;
	}
	/* End Subsidiary Panel Creation
	 * ============================================================= */
	
	private void closeFrame() {
		for (Frame f : Frame.getFrames()) {
			if (f.isActive()) {
				WindowEvent windowClosing = new WindowEvent(f,
						WindowEvent.WINDOW_CLOSING);
				f.dispatchEvent(windowClosing);
			}
		}
	}
}
