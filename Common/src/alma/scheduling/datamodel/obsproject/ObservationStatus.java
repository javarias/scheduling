package alma.scheduling.datamodel.obsproject;

public enum ObservationStatus {
	/**
	 * Valid only for Projects and ObsunitSets
	 */
	NOT_STARTED,
	/**
	 * Valid only for SchedBlocks
	 */
	READY,
	/**
	 * Valid only for Projects and ObsunitSets
	 */
	IN_PROGRESS,
	/**
	 * Valid only for SchedBlocks
	 */
	RUNNING,
	COMPLETE
}
