package alma.scheduling.planning_mode_sim.controller;

import java.io.File;

import alma.scheduling.planning_mode_sim.gui.MainWindow;

public class Controler {
	
	private MainWindow parent;
	private static Controler instance = null;
	private boolean changes;
	private Configuration conf;
	
	private Controler(){
		this.notifySavedChanges();
	}
	
	public static Controler getControler(){
		if( instance == null) instance = new Controler();
		return instance;
	}
	
	public void setParentWindow(MainWindow parent){
		this.parent = parent;
	}
	
	public MainWindow getParentWindow(){
		return this.parent;
	}
	
	public Configuration createNew(){
		this.setConf(new Configuration());
		this.notifyUnsavedChanges();
		return this.getConf();
	}
	
	public Configuration load(File file){
		this.notifySavedChanges();
		this.setConf(new Configuration(file));
		return this.getConf();
	}
	
	public void save(){
		this.notifySavedChanges();		
		this.getConf().saveTo(this.getFilename());		
	}
	
	public void saveAs(String filename){
		this.notifySavedChanges();
		this.setFilename(filename);
		this.getConf().saveTo(filename);
	}
	
	public void notifyUnsavedChanges(){
		this.changes = true;
	}
	
	public void notifySavedChanges(){
		this.changes = false;
	}
	
	public boolean hasChanged(){
		return changes;
	}
	
	public void exit(){
		if(parent == null){
			System.out.println("No parent has been set");
			return;
		}
		//TODO: Check for unsaved changes.
		System.out.println("Exiting...");
		System.exit(0);
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return conf;
	}

	public void setFilename(String filename) {
		this.getConf().setFilename(filename);
	}

	public String getFilename() {
		return this.getConf().getFilename();
	}

}
