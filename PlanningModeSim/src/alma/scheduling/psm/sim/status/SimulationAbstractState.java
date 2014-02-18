package alma.scheduling.psm.sim.status;

import alma.scheduling.psm.sim.SimulationActionsEnum;
import alma.scheduling.psm.sim.SimulationStateEnum;

public abstract class SimulationAbstractState {

	protected SimulationStateContext context;
	
	public SimulationAbstractState(SimulationStateContext context) {
		this.context = context;
	}
	
	abstract public SimulationStateEnum getCurrentState();
	
	abstract public SimulationActionsEnum[] getAvailableActions();
	
	public void fullload() {
		throw new RuntimeException("Action full load not available for the current state");
	}
	
	public void load() {
		throw new RuntimeException("Action load not available for the current state");
	}
	
	public void runSimulation() {
		throw new RuntimeException("Action Run simulation not available for the current state");
	}
	
	public void clean() {
		throw new RuntimeException("Action clean not available for the current state");
	}
}
