package alma.scheduling.psm.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.springframework.context.ApplicationContext;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.input.config.generated.Configuration;

public class PsmContext {
	
	protected String workDir = null;
	protected String outputDir = null;
	protected String reportDir = null;
	protected String contextFile = null;
    protected VerboseLevel verboseLvl = null;
    static private ApplicationContext ctx = null;
    
    protected ConfigurationDao configDao = null;
    
	public PsmContext(String workDir){
		this.workDir = workDir;
		this.loadAprcConfig();
	}
	
	protected void loadAprcConfig(){
		Configuration config = new Configuration();
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
	
	public Configuration getAprcConfig(){
		Configuration config = new Configuration();
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
        return config;
	}
	
	public void saveAprcConfig(Configuration config){
		System.out.println("Saving configuration XML to: " + workDir + "/aprc-config.xml");
        File configFile = new File(workDir + "/aprc-config.xml");
        try {
            config.marshal(new FileWriter(configFile));
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		} catch (MarshalException e){
			e.printStackTrace();
		} catch (ValidationException e){
			e.printStackTrace();
		}
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
	
	public static void setApplicationContext( ApplicationContext context ){
		if( ctx == null )
			ctx = context;
		else{
			System.out.println("Error, ApplicationContext already has been set");
		}
	}
	
	public static ApplicationContext getApplicationContext(){
		if( ctx == null ){
			System.out.println("Error, ApplicationContext has not been set");
		}
		return ctx;
	}
	
}
