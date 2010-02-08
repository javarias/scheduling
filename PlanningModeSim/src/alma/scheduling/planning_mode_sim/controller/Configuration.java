package alma.scheduling.planning_mode_sim.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class Configuration extends Properties{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1359417865298384766L;
	
	private boolean runned;
	private String filename;
	
	public Configuration(){
		setArchiveSource(archiveSources.XML_FILE);
		setWeatherSource(weatherSources.XML_FILE);
		setOcdbSource(ocdbSources.XML_FILE);
		//TODO: Fill with the correct relative path and filename
		setArchiveXMLFile("executive/executive.xml");
		setWeatherXMLFile("weather/weather.xml");
		setOcdbXMLFile("observatory/observatory.xml");
		this.filename = "";
	}
	
	public Configuration(File file){
		//TODO: Missing loaded strings validation. Enums are useless using java properties
		FileInputStream in;
		try {
			in = new FileInputStream(file);
			this.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.filename = file.toString();
	}
	
	public enum archiveSources {
	    ALMA_ARCHIVE, XML_FILE
	}
	
	public enum ocdbSources {
	    ALMA_OCDB, XML_FILE
	}
	
	public enum weatherSources {
	    ALMA_TELCAL, XML_FILE, SIMULATION
	}

	public void setArchiveSource(archiveSources archiveSource) {
		this.setProperty("ArchiveSource", archiveSource.toString());
	}

	public archiveSources getArchiveSource() {
		return archiveSources.valueOf( getProperty("ArchiveSource") );
	}

	public void setOcdbSource(ocdbSources ocdbSource) {
		this.setProperty("OcdbSource", ocdbSource.toString());
	}

	public ocdbSources getOcdbSource() {
		return ocdbSources.valueOf( getProperty("OcdbSource") );
	}

	public void setWeatherSource(weatherSources weatherSource) {
		this.setProperty("WeatherSource", weatherSource.toString());
	}

	public weatherSources getWeatherSource() {
		return weatherSources.valueOf( getProperty("WeatherSource") );
	}

	public void setSimStartTime(Date simStartTime) {
		this.setProperty("SimStartTime", simStartTime.toString());
	}

	@SuppressWarnings("deprecation")
	public Date getSimStartTime() {
		return new Date( getProperty("SimStartTime") );
	}

	public void setSimStopTime(Date simStopTime) {
		this.setProperty("SimStopTime", simStopTime.toString());
	}

	@SuppressWarnings("deprecation")
	public Date getSimStopTime() {
		return new Date( getProperty("SimStopTime") );
	}

	public void setArchiveXMLFile(String archiveXMLFile) {
		this.setProperty("ArchiveXMLFile", archiveXMLFile);
	}

	public String getArchiveXMLFile() {
		return getProperty("ArchiveXMLFile");
	}

	public void setOcdbXMLFile(String ocdbXMLFile) {
		this.setProperty("OcdbXMLFile", ocdbXMLFile);
	}

	public String getOcdbXMLFile() {
		return getProperty("OcdbXMLFile");
	}

	public void setWeatherXMLFile(String weatherXMLFile) {
		this.setProperty("WeatherXMLFile", weatherXMLFile);
	}

	public String getWeatherXMLFile() {
		return getProperty("WeatherXMLFile");
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}
	
	public void saveTo(String filename){
		FileOutputStream out;
		try {
			out = new FileOutputStream(filename);
			this.store(out, "---No Comment---");
			out.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
