package alma.scheduling.output;

import java.util.List;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public class MasterReporter implements Reporter {

    private List<Reporter> reporters;
    
    @Override
    public void generateXMLOutput() {
        for(Reporter r: reporters){
            r.generateXMLOutput();
        }
    }

    @Override
    public void report(SchedBlock schedBlock) {
        for(Reporter r: reporters){
            r.report(schedBlock);
        }
    }

    public List<Reporter> getReporters() {
        return reporters;
    }

    public void setReporters(List<Reporter> reporters) {
        this.reporters = reporters;
    }

    
    
}
