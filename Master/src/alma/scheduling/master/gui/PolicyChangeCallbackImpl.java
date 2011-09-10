package alma.scheduling.master.gui;

import alma.scheduling.PolicyChangeCallbackPOA;
import alma.scheduling.PolicyEvent;

public class PolicyChangeCallbackImpl extends PolicyChangeCallbackPOA{

	private final PolicyChangeListener listener;
	
	public PolicyChangeCallbackImpl(PolicyChangeListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void report(PolicyEvent operation) {
		listener.refreshPolicyList();
	}

}
