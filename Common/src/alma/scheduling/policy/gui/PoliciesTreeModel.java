/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.policy.gui;

import java.util.Collection;
import java.util.HashSet;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import alma.scheduling.SchedulingPolicyFile;

public class PoliciesTreeModel implements TreeModel, PolicyChangeListener, PolicySelectionListener {

	private final PoliciesTreeRoot files;
	private Collection<TreeModelListener> listeners;
	
	public PoliciesTreeModel(SchedulingPolicyFile[] files) {
		PoliciesFileTreeNode[] nodes = new PoliciesFileTreeNode[files.length];
		int i = 0;
		for (SchedulingPolicyFile file: files) {
			PoliciesFileTreeNode node = new PoliciesFileTreeNode(file);
			nodes[i++] = node;
		}
		this.files = new PoliciesTreeRoot(nodes);
		this.listeners = new HashSet<TreeModelListener>();
	}
	
	@Override
	public Object getRoot() {
		return files;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent instanceof PoliciesTreeRoot)
			return ((PoliciesTreeRoot) parent).getFiles()[index];
		else if (parent instanceof PoliciesFileTreeNode)
			return ((PoliciesFileTreeNode) parent).getFile().schedulingPolicies[index];
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent instanceof PoliciesTreeRoot)
			return ((PoliciesTreeRoot) parent).getFiles().length;
		else if (parent instanceof PoliciesFileTreeNode)
			return ((PoliciesFileTreeNode) parent).getFile().schedulingPolicies.length;
		return 0;
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node instanceof PoliciesTreeRoot)
			return false;
		else if (node instanceof PoliciesFileTreeNode)
			return false;
		else if (node instanceof String)
			return true;
		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		int i = 0;
		if (parent instanceof PoliciesTreeRoot) {
			for (PoliciesFileTreeNode file: ((PoliciesTreeRoot) parent).getFiles()) {
				if (file == child)
					return i;
				i++;
			}
		}
		else if (parent instanceof PoliciesFileTreeNode)
			for (String policy: ((PoliciesFileTreeNode) parent).getFile().schedulingPolicies) {
				if (policy.compareTo((String) child) == 0)
					return i;
				i++;
			}
		return 0;
	}

	@Override
	public synchronized void addTreeModelListener(TreeModelListener listener) {
		listeners.add(listener);
	}

	@Override
	public synchronized void removeTreeModelListener(TreeModelListener listener) {
		listeners.remove(listener);
	}
	
	public class PoliciesTreeRoot {
		
		private PoliciesFileTreeNode[] files;
		
		public PoliciesTreeRoot(PoliciesFileTreeNode[] files) {
			this.files = files;
		}
		
		public PoliciesFileTreeNode[] getFiles() {
			return files;
		}

		@Override
		public String toString() {
			return files.length + " Policies file" + ((files.length == 1)? " ": "s ") + "available";
		}
	}
	
	public class PoliciesFileTreeNode {
		private SchedulingPolicyFile file;
		
		public PoliciesFileTreeNode(SchedulingPolicyFile file) {
			this.file = file;
		}
		
		public SchedulingPolicyFile getFile () {
			return file;
		}
		
		@Override
		public String toString() {
			return file.username + "@" + file.hostname + ":" + file.path;
		}
		
		public boolean isSystemFile() {
			if (file.path.compareTo("system") == 0)
				return true;
			return false;
		}
		
		public String getBeanName(String policyName) {
			if (isSystemFile())
				return policyName;
			else
				return "uuid" + file.uuid + "-" + policyName;
		}
	}

	
	
	/*
	 * ================================================================
	 * PolicySelectionListener implementation
	 * ================================================================
	 */
	@Override
	public void policySelected(String beanName) {
	}
	/* End PolicySelectionListener implementation
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * PolicyChangeListener implementation
	 * ================================================================
	 */
	@Override
	public void refreshPolicyList() {
	}
	/* End PolicyChangeListener implementation
	 * ============================================================= */
}