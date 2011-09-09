package alma.scheduling.master.gui.policy;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import alma.scheduling.SchedulingPolicyFile;

public class PoliciesTreeModel implements TreeModel{

	private final PoliciesTreeRoot files;
	public PoliciesTreeModel(SchedulingPolicyFile[] files) {
		PoliciesFileTreeNode[] nodes = new PoliciesFileTreeNode[files.length];
		int i = 0;
		for (SchedulingPolicyFile file: files) {
			PoliciesFileTreeNode node = new PoliciesFileTreeNode(file);
			nodes[i++] = node;
		}
		this.files = new PoliciesTreeRoot(nodes);
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
	public void addTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub

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
	}
}
