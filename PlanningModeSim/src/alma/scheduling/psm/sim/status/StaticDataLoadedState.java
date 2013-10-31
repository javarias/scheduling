package alma.scheduling.psm.sim.status;

import alma.scheduling.psm.sim.SimulationActionsEnum;
import alma.scheduling.psm.sim.SimulationStateEnum;

public class StaticDataLoadedState extends SimulationAbstractState {

	public StaticDataLoadedState(SimulationStateContext context) {
		super(context);
	}

	@Override
	public SimulationStateEnum getCurrentState() {
		return SimulationStateEnum.STATIC_DATA_LOADED;
	}

	@Override
	public SimulationActionsEnum[] getAvailableActions() {
		SimulationActionsEnum[] retVal = {SimulationActionsEnum.LOAD};
		return retVal;
	}

	@Override
	public void load() {
		context.setState(new DynamicDataLoadedState(context));
	}
	
}
