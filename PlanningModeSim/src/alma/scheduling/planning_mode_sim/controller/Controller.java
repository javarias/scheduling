package alma.scheduling.planning_mode_sim.controller;

import alma.scheduling.planning_mode_sim.gui.simpreparation.MainWindow;

public class Controller {
	
	private String filename;
	private MainWindow parent;
	
	public Controller(MainWindow parent){
		this.filename = null;
		this.parent = parent;
	}
	
	public Configuration createNew(){
		return new Configuration();
	}
	
	public Configuration load(String filename){
		return new Configuration();
	}
	
	public void save(){
		
	}
	
	public void saveAs(String filename){
	}
	
	public void exit(){
		
	}

}
