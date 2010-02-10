package alma.scheduling.planning_mode_sim.dao;

import java.util.ArrayList;

public class weatherSourcesDAO {
	
	private static ArrayList<String> s = null;
	
	//TODO: Implement a search of sources, over program data, project data, and online data.
	public static ArrayList<String> getSourcesNames(){
		if( s == null ){
			s = new ArrayList<String>();
			s.add("Good");
			s.add("Average");
			s.add("Bad");
		}		
		return s;
	}

}
