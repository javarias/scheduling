package alma.scheduling.Scheduler;

public class SpecialSBScheduler extends Scheduler implements Runnable {

    public SpecialSBScheduler(SchedulerConfiguration config) {

        super(config);
    }   

    /**
     * Form a name of this scheduler, which includes its thread name and the id
     * of the array on which it operates.  This name has the folloeing form:
     *          Scheduler [task1] (array 1)
     * @return A string identifying this scheduler.
     */
    protected String name() {
        return "Scheduler [" + Thread.currentThread().getName() +
                        "] (subarray " + arrayName + ")";
                        //"] (array " + arrayName + ")";
    }



    public void run() {
        logger.info("SCHEDULING: Running scheduler for SpecialSBs (fixed time SBs)");
        config.setTask(Thread.currentThread());
    }

        
}

