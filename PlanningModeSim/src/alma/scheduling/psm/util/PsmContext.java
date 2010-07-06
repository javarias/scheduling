package alma.scheduling.psm.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.input.config.generated.Configuration;

public class PsmContext {
	
	protected String workDir = null;
	protected String outputDir = null;
	protected String reportDir = null;
	protected String contextFile = null;
    protected VerboseLevel verboseLvl = null;

	
	public PsmContext(String workDir){
		this.workDir = workDir;
		this.loadAprcConfig();
	}

	protected void loadAprcConfig(){
		alma.scheduling.input.config.generated.Configuration config = 
            new alma.scheduling.input.config.generated.Configuration();
        File configFile = new File(workDir + "/aprc-config.xml");
        try {
            config = Configuration.unmarshalConfiguration(new FileReader(configFile));
        } catch (MarshalException e) {
            e.printStackTrace();
        } catch (ValidationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        contextFile = "file://" + workDir + "/" + config.getContextFilePath();
        reportDir = workDir + "/" + config.getReportDirectory();
        outputDir = workDir + "/" + config.getOutputDirectory();
	}
	
	public String getContextFile(){
		return contextFile;		
	}
	
	public String getOutputDirectory(){
		return outputDir;		
	}
	
	public String getReportDirectory(){
		return reportDir;		
	}

	public VerboseLevel getVerboseLvl() {
		return verboseLvl;
	}

	public void setVerboseLvl(VerboseLevel verboseLvl) {
		this.verboseLvl = verboseLvl;
	}
}
