package alma.scheduling.planning_mode_sim.test;

import alma.scheduling.planning_mode_sim.define.DateTime;
import alma.scheduling.planning_mode_sim.define.Date;
import alma.scheduling.planning_mode_sim.define.Time;

import alma.scheduling.planning_mode_sim.simulator.ClockSimulator;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestB {

	static private final double radToDeg = 180.0 / Math.PI;
	static private final double radToHour =  12.0 / Math.PI;
	static private final double degToRad = Math.PI / 180.0;
	static private final double hourToRad =  Math.PI / 12.0;
		
	static private String showDouble(double d) {
		int n = 0;
		if (d > 0.0)
			n = (int)(d * 100.0 + 0.5);
		else
			n = (int)(d * 100.0 - 0.5);
		String sn = Integer.toString(n);
		String s = null;
		if (sn.length() > 2)
			s = sn.substring(0,sn.length() - 2) + "." + sn.substring(sn.length() - 2);
		else if (sn.length() == 2) {
			if (n > 0)
				s = "." + sn;
			else
				s = "-.0" + sn.charAt(1);
		} else
			s = ".0" + sn;
		return s;
	}
	static private String toDeg(double rad) {
		return showDouble(rad * radToDeg);
	}
	static private String toHour(double rad) {
		return showDouble(rad * radToHour);
	}

	public static void main(String[] args) {
		System.out.println("Test.");
		// Create the output file.
		PrintStream out = null;
		try {
			// Create the output text file.
			File outFile = new File("outB.txt");
			out = new PrintStream (new FileOutputStream (outFile));
		} catch (IOException ioerr) {
			 ioerr.printStackTrace();
			 System.exit(0);
		}
		///////////////////////////////////////////////////////////////////
		
		out.println("VLA");

		double VLALongitude = 107.6177275 * degToRad;
		double VLALatitude = 34.0787491666667 * degToRad;
		double sinL = Math.sin(VLALatitude);
		double cosL = Math.cos(VLALatitude);
		
		double ra = 0.0;
		double dec = 50.0 * degToRad;
		
		for (int k = 0; k < 24; ++k) {
			ra = k * hourToRad;
		for (int i = -90; i < 91; i += 5) {
			dec = i * degToRad;
	
			double sinDec = Math.sin(dec);
			double cosDec = Math.cos(dec);
			out.print("ra = " + toDeg(ra) + "   Dec = " + toDeg(dec));
	
			double sinElMax = sinL * sinDec + cosL * cosDec;
			double elMax = Math.asin(sinElMax);
			out.print(" max el = " + toDeg(elMax));
	
			double minEl = 8.0 * degToRad; // minimum elevatinon of VLA
			double sinMinEl = Math.sin(minEl);
			double cosMinH = (sinMinEl - sinL * sinDec) / (cosL * cosDec);
			int visible = 0;
			double lstMax = 0.0;
			double lstSet = 0.0;
			double lstRise = 0.0;
			if (cosMinH > 1) {
				out.println(" This source is never visible.");
				visible = -1;
			} else if (cosMinH < -1) {
				out.println(" This source is always above the minimum.");
				lstMax = ra;
				visible = 1;
			} else {
				double minH = Math.acos(cosMinH);
				//out.println(" H at min el " + toHour(minH));
				lstMax = ra;
				lstSet = minH + ra;
				lstRise = -minH + ra;
				out.println(" LST rise " + toHour(lstRise) + " max " + toHour(lstMax) + " set " + toHour(lstSet));
			}
			double lst = 0.0;
			char[] s = new char [100];
			s[96] = ' ';s[97] = ' '; s[98] = ' '; s[99] = ' ';  
			boolean isVisible = false;
			double nextLstRise = 0.0;
			double prevLstSet = 0.0;
			for (int l = 0; l < 96; ++l) {
				lst = l * 0.25 * hourToRad;
				if (visible == -1)
					s[l] = ' ';
				else if (visible == 1)
					s[l] = '-';
				else {
					isVisible = false;
					if (lstRise < lst && lst < lstSet)
						isVisible = true;
					/*nextLstRise = 2.0 * Math.PI + lstRise;
					double nextLstSet = 2.0 * Math.PI + lstSet;
					if (nextLstRise < lst && lst < nextLstSet)
						isVisible = true;
					double prevLstRise = lstRise - 2.0 * Math.PI;
					prevLstSet = lstSet - 2.0 * Math.PI;
					if (prevLstRise < lst && lst < prevLstSet)
						isVisible = true;
					//out.println(" lst = " + lst + " " + lstRise + " " + lstSet + " "
					//+ nextLstRise + " " + nextLstSet + " "
					//+ prevLstRise + " " + prevLstSet + " ");*/
					if (lstRise < 0.0) {
						nextLstRise = 2.0 * Math.PI + lstRise;
						if (nextLstRise < lst)
							isVisible = true;
					}
					if (lstSet > 2 * Math.PI) {
						prevLstSet = lstSet - 2.0 * Math.PI;
						if (lst < prevLstSet)
							isVisible = true;
					}
					s[l] = (isVisible ? '-' : ' ');
				}
			}
			if (visible > -1) {
				int maxPos = (int)(lstMax * radToHour * 4.0 + 0.5);
				out.println(">>" + maxPos);
				s[maxPos] = '+';
			}
			String ls = new String (s);
			out.println("|" + ls + "|");	
		}
		}
				
		/*
		double lstMax = ra;
		double minH_hrs = minH * radToHour;
		double lstSet = minH_hrs + ra;
		if (lstSet < 0.0)
			lstSet = 24.0 - lstSet; 
		double lstRise = 24.0 - minH_hrs + ra;
		if (lstRise < 0.0)
			lstRise = 24.0 - lstRise; 
		out.println("LST at maximum elevation " + lstMax);
		out.println("LST at rising elevation  " + lstRise);
		out.println("LST at setting elevation " + lstSet);

		out.println();
		ra = 4.2;
		dec = 50.0 * degToRad;
		sinDec = Math.sin(dec);
		cosDec = Math.cos(dec);
		out.println("ra = " + ra + "   Dec = " + toDeg(dec));

		sinElMax = sinL * sinDec + cosL * cosDec;
		elMax = Math.asin(sinElMax);
		out.println("maximum elevation = " + toDeg(elMax));

		minEl = 8.0 * degToRad; // minimum elevatinon of VLA
		sinMinEl = Math.sin(minEl);
		cosMinH = (sinMinEl - sinL * sinDec) / (cosL * cosDec);
		minH = Math.acos(cosMinH);
		out.println("hour angle at VLA's minimum elevation " + toHour(minH));

		lstMax = ra;
		minH_hrs = minH * radToHour;
		lstSet = minH_hrs + ra;
		if (lstSet < 0.0)
			lstSet = 24.0 - lstSet; 
		lstRise = 24.0 - minH_hrs + ra;
		if (lstRise < 0.0)
			lstRise = 24.0 - lstRise; 
		out.println("LST at maximum elevation " + lstMax);
		out.println("LST at rising elevation  " + lstRise);
		out.println("LST at setting elevation " + lstSet);

		int totalTimeInSeconds = 1800;
		double totalTime = 1800 / 3600.0;
		
		double optimalStart = lstMax - 0.5 * totalTime;
		double earlyStart = lstRise;
		double lateStart = lstSet - totalTime;

		out.println("Total time of SB       " + totalTime);
		out.println("Optimal starting time  " + optimalStart);
		out.println("Earliest starting time " + earlyStart);
		out.println("Latest starting time   " + lateStart);
		if (earlyStart > lateStart) {
			earlyStart -= 24.0;
		}
		out.println("Optimal starting time  " + optimalStart);
		out.println("Earliest starting time " + earlyStart);
		out.println("Latest starting time   " + lateStart);
		*/
		
		

		///////////////////////////////////////////////////////////////////
		out.close();
		System.out.println("Look at outB.txt for output");
		System.out.println("End test.");
	}

	private static char[][] matrix = new char [181][25];
	private static char[] symbols = { '0','1','2','3','4','5','6','7','8','9' };
	private static void store(int idec, double el, int t) {
		el *= radToDeg;
		el += (el > 0.0) ? 0.5 : -0.5;
		int iEl = (int)el + 90;
		char c = symbols[idec / 10];
		matrix[iEl][t] = c;
	}
	private static void initDisplay() {
		for (int i = 0; i < 181; ++i)
			for (int j = 0; j < 25; ++j)
				matrix[i][j] = ' ';
	}
	private static void display(PrintStream out) {
		char[] line = new char [50];
		String s = null;
		for (int i = 180; i >= 0; --i) {
			for (int j = 0; j < 25; ++j) {
				line[2 * j] = matrix[i][j];
				line[2 * j + 1] = ' ';
			}
			s = new String(line);
			int n = i - 90;
			
			String blank = "     ";
			String sn = Integer.toString(n);
			String psn = blank.substring(sn.length()) + sn + " |";
			if (n == 90) {
				out.println("      |0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4");
				out.println(psn + s);
			} else if (n == 0) {
				out.println(psn + s);
				out.println("      |0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4");				
			} else if (n == -90) {
				out.println(n + "\t|\t" + s);
				out.println("      |0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4");				
			} else {
				out.println(psn + s);
			}
		}
	}

}

