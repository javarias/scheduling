package alma.scheduling.psm.sim.status;

import alma.scheduling.psm.sim.SimulationActionsEnum;
import alma.scheduling.psm.sim.SimulationStateEnum;

public class DynamicDataLoadedState extends SimulationAbstractState {

	public DynamicDataLoadedState(SimulationStateContext context) {
		super(context);
	}

	@Override
	public SimulationStateEnum getCurrentState() {
		return SimulationStateEnum.DYNAMIC_DATA_LOADED;
	}

	@Override
	public SimulationActionsEnum[] getAvailableActions() {
		SimulationActionsEnum[] retVal = {SimulationActionsEnum.RUN_SIMULATION};
		return retVal;
	}

	@Override
	public void runSimulation() {
		context.setState(new CompletedSimulationState(context));
	}
	
	
}
