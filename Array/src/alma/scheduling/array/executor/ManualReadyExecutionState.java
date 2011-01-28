package alma.scheduling.array.executor;

public class ManualReadyExecutionState extends ExecutionState {

	public ManualReadyExecutionState(ExecutionContext context) {
		super(context);
	}

	@Override
	public void startObservation() {
		super.startObservation();
		context.setState(new ManualRunningExecutionState(context));
	}
}
