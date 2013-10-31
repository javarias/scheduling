package alma.scheduling.psm.sim.status;

import alma.scheduling.psm.sim.SimulationActionsEnum;
import alma.scheduling.psm.sim.SimulationStateEnum;

public class StartState extends SimulationAbstractState {

	public StartState(SimulationStateContext context) {
		super(context);
	}

	@Override
	public SimulationStateEnum getCurrentState() {
		return SimulationStateEnum.START;
	}

	@Override
	public SimulationActionsEnum[] getAvailableActions() {
		SimulationActionsEnum[] retVal = {SimulationActionsEnum.FULLLOAD};
		return retVal;
	}

	@Override
	public void fullload() {
		context.setState(new DynamicDataLoadedState(context));
	}
	
	

}
