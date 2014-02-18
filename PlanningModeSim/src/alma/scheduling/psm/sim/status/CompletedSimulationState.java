package alma.scheduling.psm.sim.status;

import alma.scheduling.psm.sim.SimulationActionsEnum;
import alma.scheduling.psm.sim.SimulationStateEnum;

public class CompletedSimulationState extends SimulationAbstractState {

	public CompletedSimulationState(SimulationStateContext context) {
		super(context);
	}

	@Override
	public SimulationStateEnum getCurrentState() {
		return SimulationStateEnum.SIMULATION_COMPLETED;
	}

	@Override
	public SimulationActionsEnum[] getAvailableActions() {
		SimulationActionsEnum[] retVal = {SimulationActionsEnum.CLEAN};
		return retVal;
	}

	@Override
	public void clean() {
		context.setState(new StaticDataLoadedState(context));
	}
	
	

}
