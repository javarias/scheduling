package alma.scheduling.psm.sim;

public class SimulationStateContext {

	private SimulationAbstractState currentState;
	
	public SimulationStateContext() {
		this(SimulationStateEnum.START);
	}
	
	public SimulationStateContext(SimulationStateEnum startSate) {
		switch(startSate) {
		case START:
			currentState = new StartState(this);
			break;
		case DYNAMIC_DATA_LOADED:
			currentState = new DynamicDataLoadedState(this);
			break;
		case SIMULATION_COMPLETED:
			currentState = new CompletedSimulationState(this);
			break;
		case STATIC_DATA_LOADED:
			currentState = new StaticDataLoadedState(this);
			break;
		}
	}
	
	void setState(SimulationAbstractState newState) {
		currentState = newState;
	}
	
	public SimulationAbstractState getCurrentState() {
		return currentState;
	}
}
