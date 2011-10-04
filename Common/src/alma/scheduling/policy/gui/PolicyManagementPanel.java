package alma.scheduling.policy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeSelectionModel;

import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.scheduling.Master;
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
	private String beanSelectedName = "";
	private List<PolicySelectionListener> listeners;
	
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
							if (e.getPath().getPathCount() != 3) {
								beanSelectedName = "";
								selectPolicyButton.setEnabled(false);
							} else {
								beanSelectedName = ((PoliciesFileTreeNode) e
										.getPath().getPath()[1])
										.getBeanName((String) e.getPath()
												.getPath()[2]);
								selectPolicyButton.setEnabled(true);
							}
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
				notifyPolicySelected(beanSelectedName);
				selectPolicyButton.setEnabled(true);
			}
		});
		return button;
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
				//Show dialog, connectivity problem perhaps
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
}
