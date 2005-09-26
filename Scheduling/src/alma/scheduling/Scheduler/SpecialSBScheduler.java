package alma.scheduling.Scheduler;

import alma.scheduling.Define.DateTime;

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
    }

    public void run() {
        DateTime start = clock.getDateTime();
    	DateTime end = config.getCommandedEndTime();
    	config.start(start,end);
    	logger.info("SCHEDULING: "+name() + ": Started " + start);
        
        config.setTask(Thread.currentThread());
        logger.info("SCHEDULING: Running scheduler for SpecialSBs (fixed time SBs)");
        try {
            String[] antennas = config.getControl().getArrayAntennas(arrayName);
            for(int i=0; i < antennas.length; i++){
                config.getControl().setAntennaOfflineNow(antennas[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        config.normalEnd(clock.getDateTime());
        logger.info(name() + " has ended!");
    	logger.info(name() + " started " + config.getActualStartTime());
    	logger.info(name() + " ended " + config.getActualEndTime());

    }

        
}

