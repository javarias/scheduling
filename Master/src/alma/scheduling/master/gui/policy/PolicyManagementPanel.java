package alma.scheduling.master.gui.policy;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;

import alma.scheduling.Master;
import alma.scheduling.master.gui.PolicyChangeListener;
import alma.scheduling.master.gui.policy.PoliciesTreeModel.PoliciesFileTreeNode;

public class PolicyManagementPanel extends JPanel implements PolicyChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4690777794339534919L;
	
	private final Master master;
	private JTree policiesTree;
	private JButton selectPolicyButton;
	private JButton refreshPoliciesButton;
	private String beanSelectedName = "";
	private PolicySelectionListener listener;
	
	public PolicyManagementPanel(Master masterSchedulierRef, PolicySelectionListener listener) {
		master = masterSchedulierRef;
		this.listener = listener;
		initialize();
	}
	
	private void initialize() {
		selectPolicyButton = initializeSelectionButton();
		policiesTree = initializePoliciesTree();
		this.add(policiesTree, BorderLayout.CENTER);
	}
	
	private JTree initializePoliciesTree() {
		JTree tree = new JTree();
		tree.setModel(new PoliciesTreeModel(master.getSchedulingPolicies()));
		tree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.getSelectionModel().addTreeSelectionListener( new TreeSelectionListener() {
			
					@Override
					public void valueChanged(TreeSelectionEvent e) {
						if (e.getPath() != null) {
							if (e.getPath().getPathCount() != 3) {
								beanSelectedName = "";
								selectPolicyButton.setEnabled(false);
							}
							else {
								beanSelectedName = "uuid"
										+ ((PoliciesFileTreeNode) e.getPath()
												.getPath()[1]).getFile().uuid + "-";
								beanSelectedName += (String)e.getPath().getPath()[2];
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
				listener.policySelected(beanSelectedName);
				selectPolicyButton.setEnabled(true);
			}
		});
		return button;
	}

	@Override
	public synchronized void refreshPolicyList() {
		try {
			refreshPoliciesButton.setEnabled(false);
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
	
}
