/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File GenProject.java
 */
package alma.scheduling.PlanningModeSim;

import alma.scheduling.Define.FrequencyBand;

import java.io.*;
import java.util.Properties;
import java.text.NumberFormat;

public class GenProject extends Properties {
	
	private PrintStream out;
	private String[] priority;
	private String[] weather;
	private FrequencyBand[] band;
	private NumberFormat dform;
	private int projectNumber = 0;
	
	public GenProject(String inFilename, String outFilename) {
		dform = NumberFormat.getInstance();
		dform.setMaximumFractionDigits(2);
		try {
			out = new PrintStream (new FileOutputStream(outFilename,false));
		} catch (FileNotFoundException e) {
			System.out.println("Could not create file " + outFilename);
			e.printStackTrace();
			System.exit(0);
		}

		// load properties file
		try {
			FileInputStream file = new FileInputStream(inFilename);
			super.load(file);
		} catch (IOException err) {
			System.out.println("Could not open file " + inFilename);
			err.printStackTrace();
			System.exit(0);
		}
		
	}
	
	public String getString(String name) {
		String s = getProperty(name);
		if (s == null) {
			System.out.println("Property " + name + " was not found.");
			System.exit(0);
		}
		return s;
	}

	public int getInt(String name) {
		String s = getProperty(name);
		if (s == null) {
			System.out.println("Property " + name + " was not found.");
			System.exit(0);
		}
		int n = 0;
		try {
			n = Integer.parseInt(s);
		} catch (NumberFormatException err) {
			System.out.println("Integer expected as value for " + name + " (" + s + " was found).");
			System.exit(0);
		}
		return n;
	}

	public double getDouble(String name) {
		String s = getProperty(name);
		if (s == null) {
			System.out.println("Property " + name + " was not found.");
			System.exit(0);
		}
		double d = 0.0;
		try {
			d = Double.parseDouble(s);
		} catch (NumberFormatException err) {
			System.out.println("Double expected as value for " + name + " (" + s + " was found).");
			System.exit(0);
		}
		return d;
	}
	
	public void generate() {
		String value = null;
		String name = null;
		
		// Get the priority words.
		value = getString("priority");
		priority = value.split(";",-1);
		for (int i = 0; i < priority.length; ++i) 
			priority[i] = priority[i].trim();
		
		// Get the weather words.
		value = getString("weather");
		weather = value.split(";",-1);
		for (int i = 0; i < weather.length; ++i) 
			weather[i] = weather[i].trim();
		
		// Get the frequency bands from the input.
		// Syntax:
		// 		FrequencyBand.numberOfBands = N
		//		FrequencyBand.<i> = nameOfBand; minimumFrequency; maximumFrequency
		// where 0 <= i < N, and N is the number of frequency bands. Frequencies are in GHz.
		band = new FrequencyBand [getInt(Tag.numberOfBands)];
		double min = 0.0;
		double max = 0.0;
		String[] x = null;
		for (int i = 0; i < band.length; ++i) {
			value = getString(Tag.band + "." + i);
			x = value.split(";",-1);
			if (x.length < 3) {
				System.out.println("Invalid number of frequency band parameters in " + value);
				System.exit(0);
			}
			try {
				name = x[0].trim();
				min = Double.parseDouble(x[1]);
				max = Double.parseDouble(x[2]);
			} catch (NumberFormatException err) {
				System.out.println("Invalid number format in " + value);
				System.exit(0);
			}
			band[i] = new FrequencyBand(name,min,max);
		}
		
		// Get the number of sequences to generate.
		int nGen = getInt("generate");
		// Write out the total number
		int total = 0;
		for (int i = 0; i < nGen; ++i) {
			total += getInt("genNumberProjects." + i);
		}
		out.println("numberProjects = " + total);
		out.println();
		// Generate the sequences.
		int nProjects = 0;
		String project = "";
		String set = "";
		String target = "";
		for (int i = 0; i < nGen; ++i) {
			nProjects = getInt("genNumberProjects." + i);
			project = getString("genProject." + i);
			set = getString("genSet." + i);
			target = getString("genTarget." + i);
			gen(out,nProjects,project,set,target);
		}
	}
	
	/**
	 * example
	 * 
	 * project:	projectName; Principal Investigator; random(priority); random(1,5)
	 * set:		setName; random(frequencyBand); random(frequency); random(weatherCondition); random(1,5)
	 * target:	targetName; random(0.0, 23.9999); random(-89.9999, 89.9999); random(frequency); random(10,50); random(weatherCondition); random(0,2)
	 */
	private void gen(PrintStream out, int nProject, String project, String set, String target) {
		String[] p = project.split(";",-1);
		String projectName = p[0].trim();
		String PI = p[1].trim();
		String pri = null;
		int nSet = 0;
		String[] s = set.split(";",-1);
		String setName = s[0].trim();
		String frequencyBand = null;
		double frequency = 0.0;
		String weather = null;
		int nTarget = 0;
		String[] t = target.split(";",-1);
		String targetName = t[0].trim();
		double ra = 0.0;
		double dec = 0.0;
		double frequency2 = 0.0;
		int time = 0;
		String weather2 = null;
		int repeat = 0;
		
		for (int i = 0; i < nProject; ++i) {
			pri = parseRandomString(p[2].trim());
			nSet = parseRandomInt(p[3].trim());
			out.println("project." + projectNumber + " = " + 
					projectName + "_" + projectNumber + "; " + PI + "; " + pri + "; " + nSet);
			for (int j = 0; j < nSet; ++j) {
				frequencyBand = parseRandomString(s[1].trim());
				frequency = parseRandomFrequency(frequencyBand,s[2].trim());
				weather = parseRandomString(s[3].trim());
				nTarget = parseRandomInt(s[4].trim());
				out.println("  " +
						"set." + projectNumber + "." + j + " = " +
						setName + "_" + projectNumber + "_" + j + "; " + frequencyBand + "; " +
						dform.format(frequency) + "; " + weather + "; " + nTarget);
				for (int k = 0; k < nTarget; ++k) {
					ra = parseRandomDouble(t[1].trim());
					dec = parseRandomDouble(t[2].trim());
					out.print("    " +
							"target." + projectNumber + "." + j + "." + k + " = " +
							targetName + "_" + projectNumber + "_" + j + "_" + k + "; " +
							dform.format(ra) + "; " + dform.format(dec) + "; ");
					if (t[3].trim().length() == 0)
						out.print("  ; ");
					else {
						frequency2 = parseRandomFrequency(frequencyBand,t[3].trim());
						out.print(dform.format(frequency2) + "; ");
					}
					time = parseRandomInt(t[4].trim());
					out.print(time + "; ");
					if (t[5].trim().length() == 0)
						out.print("  ; ");
					else {
						weather2 = parseRandomString(t[5].trim());
						out.print(weather2 + "; ");
					}
					if (t.length == 7 && t[6].trim().length() != 0) {
						repeat = parseRandomInt(t[6].trim());
						out.print(repeat);
					} else
						out.print("0");
					out.println();
				}
			}
			++projectNumber;
			out.println();
		}
		
	}
	
	private String parseRandomString(String s) {
		if (s.startsWith("random")) {
			if (s.indexOf("priority") != -1)
				return randomPriority();
			if (s.indexOf("frequencyBand") != -1)
				return randomFrequencyBand();
			if (s.indexOf("weatherCondition") != -1)
				return randomWeather();
			System.out.println("Invalid syntax in " + s);
			System.exit(0);
		}
		return s;
	}
	private double parseRandomFrequency(String band, String s) {
		double x = 0.0;
		if (s.startsWith("random")) {
			if (s.indexOf("frequency") == -1) {
				System.out.println("Invalid syntax in " + s);
				System.exit(0);
			}
			return randomFrequency(band);
		}
		try {
			x = Double.parseDouble(s);
		} catch (NumberFormatException err) {
			System.out.println("Invalid syntax in " + s);
			err.printStackTrace();
			System.exit(0);
		}
		return x;
	}	
	private int parseRandomInt(String s) {
		int x = 0;
		int n1 = 0;
		int n2 = 0;
		int n3 = 0;
		int low = 0;
		int high = 0;
		String t = null;
		if (s.startsWith("random")) {
			// parsing: random(1,5)
			n1 = s.indexOf("(");
			n2 = s.indexOf(",");
			n3 = s.indexOf(")");
			if (n1 == -1 || n2 == -1 || n3 == -1) {
				System.out.println("Invalid syntax in " + s);
				System.exit(0);				
			}
			t = s.substring(n1 + 1,n2).trim();
			if (t.length() == 0) {
				System.out.println("Invalid syntax in " + s);
				System.exit(0);				
			}
			try {
				low = Integer.parseInt(t);
			} catch (NumberFormatException err) {
				System.out.println("Invalid syntax in " + s);
				err.printStackTrace();
				System.exit(0);
			}
			t = s.substring(n2 + 1,n3).trim();
			if (t.length() == 0) {
				System.out.println("Invalid syntax in " + s);
				System.exit(0);				
			}
			try {
				high = Integer.parseInt(t);
			} catch (NumberFormatException err) {
				System.out.println("Invalid syntax in " + s);
				err.printStackTrace();
				System.exit(0);
			}
			return random(low,high);
		}
		try {
			x = Integer.parseInt(s);
		} catch (NumberFormatException err) {
			System.out.println("Invalid syntax in " + s);
			err.printStackTrace();
			System.exit(0);
		}
		return x;
	}
	private double parseRandomDouble(String s) {
		double x = 0.0;
		int n1 = 0;
		int n2 = 0;
		int n3 = 0;
		double low = 0;
		double high = 0;
		String t = null;
		if (s.startsWith("random")) {
			// parsing: random(0.0, 23.9999)
			n1 = s.indexOf("(");
			n2 = s.indexOf(",");
			n3 = s.indexOf(")");
			if (n1 == -1 || n2 == -1 || n3 == -1) {
				System.out.println("Invalid syntax in " + s);
				System.exit(0);				
			}
			t = s.substring(n1 + 1,n2).trim();
			if (t.length() == 0) {
				System.out.println("Invalid syntax in " + s);
				System.exit(0);				
			}
			try {
				low = Double.parseDouble(t);
			} catch (NumberFormatException err) {
				System.out.println("Invalid syntax in " + s);
				err.printStackTrace();
				System.exit(0);
			}
			t = s.substring(n2 + 1,n3).trim();
			if (t.length() == 0) {
				System.out.println("Invalid syntax in " + s);
				System.exit(0);				
			}
			try {
				high = Double.parseDouble(t);
			} catch (NumberFormatException err) {
				System.out.println("Invalid syntax in " + s);
				err.printStackTrace();
				System.exit(0);
			}
			return random(low,high);
		}
		try {
			x = Double.parseDouble(s);
		} catch (NumberFormatException err) {
			System.out.println("Invalid syntax in " + s);
			err.printStackTrace();
			System.exit(0);
		}
		return x;
	}
	
	
	private double random(double min, double max) {
		double x = Math.random();
		return x * (max - min) + min;
	}
	
	private int random(int min, int max) {
		double x = Math.random();
		return (int)(x * (max - min) + 0.5) + min;
	}
	
	private String randomPriority() {
		double x = Math.random();
		int n = (int)(x * priority.length);
		return priority[n];
	}

	private String randomWeather() {
		double x = Math.random();
		int n = (int)(x * weather.length);
		return weather[n];
	}

	private String randomFrequencyBand() {
		double x = Math.random();
		int n = (int)(x * band.length);
		return band[n].getName();
	}
	
	private double randomFrequency(String bandname) {
		int i = 0; 
		for (; i < band.length; ++i) {
			if (band[i].getName().equals(bandname))
				break;
		}
		if (i == band.length) {
			System.out.println("Invalid frequency band name.");
			System.exit(0);
		}
		return random(band[i].getLowFrequency(),band[i].getHighFrequency());
	}
	
	
	
	/****** this is old stuff.
	// Strategy
	// N = number of projects
	// U = number of ObsUnitSets per project
	// S = number of SchedBlocks per project
	// Within an ObsUnitSet, keep the frequency and weather condition the same 
	// Typical:
	//		500 projects
	//		  5 ObsUnitSets per project
	//		  5 SchedBlocks per ObsUnitSet
	
	private void doProjects() {
		int p = 0;
		String priority = null;
		for (int i = 0; i < numberProjects; ++i) {
			p = genInt(1,10);
			switch (p) {
				case 10: priority = "highest"; break;
				case  9: priority = "higher"; break;
				case  8: priority = "high"; break;
				case  7: priority = "mediumPlus"; break;
				case  6: priority = "medium"; break;
				case  5: priority = "mediumMinus"; break;
				case  4: priority = "low"; break;
				case  3: priority = "lower"; break;
				case  2: priority = "lowest"; break;
				case  1: priority = "background"; break;
				default: priority = "***"; break;
			}
			out.println("project." + i + " = af" + i + "; Allen Farris; " + 
						priority + "; " + numberTargetsPerProject);	
			doTargets(i, numberTargetsPerProject);
		}
	}

	private void doTargets(int p, int n) {
		double ra = 0.0;
		double dec = 0.0;
		double frequency = 0.0;
		double totalTime = 0;
		int weather = 0;
		String weatherCondition = null;
		// target.<i>.<j> = targetName; ra; dec; frequency; total-time; weather-condition

		for (int i = 0; i < n; ++i) {
			ra = genDouble(0.0,24.0);
			dec = genDouble(-90.0, 90.0);
			
			frequency = genDouble(30.0,900.0);
			
			totalTime = genDouble(15.0,45.0);
			weather = genInt(1,8);
			switch (weather) {
				case  8: weatherCondition = "exceptional"; break;
				case  7: weatherCondition = "excellent"; break;
				case  6: weatherCondition = "good"; break;
				case  5: weatherCondition = "average"; break;
				case  4: weatherCondition = "belowAverage"; break;
				case  3: weatherCondition = "poor"; break;
				case  2: weatherCondition = "dismal"; break;
				case  1: weatherCondition = "whatever"; break;
				default: weatherCondition = "***"; break;
			}
			
			out.println("target." + p + "." + i + " = " +
						"af" + p + i + "; " +
						ra + "; " +
						dec + "; " +
						frequency + "; " +
						totalTime + "; " +
						weatherCondition);
		}
	}
	
	static public void main (String[] arg) {
		System.out.println("Test");
		
		GenProject gen = new GenProject (arg[0], 10, 5);
		gen.gen();
		
		double ra = gen.genDouble(0.0,24.0);
		double dec = gen.genDouble(-90.0, 90.0);
		int band = gen.genInt(1,10);
		gen.println("ra = " + ra + " dec = " + dec + " band = " + band);
		ra = gen.genDouble(0.0,24.0);
		dec = gen.genDouble(-90.0, 90.0);
		band = gen.genInt(1,10);
		gen.println("ra = " + ra + " dec = " + dec + " band = " + band);
		ra = gen.genDouble(0.0,24.0);
		dec = gen.genDouble(-90.0, 90.0);
		band = gen.genInt(1,10);
		gen.println("ra = " + ra + " dec = " + dec + " band = " + band);
		ra = gen.genDouble(0.0,24.0);
		dec = gen.genDouble(-90.0, 90.0);
		band = gen.genInt(1,10);
		gen.println("ra = " + ra + " dec = " + dec + " band = " + band);
		ra = gen.genDouble(0.0,24.0);
		dec = gen.genDouble(-90.0, 90.0);
		band = gen.genInt(1,10);
		gen.println("ra = " + ra + " dec = " + dec + " band = " + band);
		
		System.out.println("End Test");
	}
	*******/

}
