package alma.scheduling.planning_mode_sim.test;

import alma.scheduling.planning_mode_sim.define.DateTime;
import alma.scheduling.planning_mode_sim.define.Date;
import alma.scheduling.planning_mode_sim.define.Time;

import alma.scheduling.planning_mode_sim.simulator.ClockSimulator;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestA {

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
			File outFile = new File("outA.txt");
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
		double sinDec = Math.sin(dec);
		double cosDec = Math.cos(dec);
		out.println("ra = " + ra + "   Dec = " + toDeg(dec));

		double sinElMax = sinL * sinDec + cosL * cosDec;
		double elMax = Math.asin(sinElMax);
		out.println("maximum elevation = " + toDeg(elMax));

		double minEl = 8.0 * degToRad; // minimum elevatinon of VLA
		double sinMinEl = Math.sin(minEl);
		double cosMinH = (sinMinEl - sinL * sinDec) / (cosL * cosDec);
		double minH = Math.acos(cosMinH);
		out.println("hour angle at VLA's minimum elevation " + toHour(minH));

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

		initDisplay();
		out.println();

		double H = 0.0;
		double cosH = 0.0;
		double el = 0.0;
		for (int i = 0; i < 25; ++i) {
			H = ((double)i  - ra) * hourToRad;
			cosH = Math.cos(H);
			el = Math.asin(sinL * sinDec + cosL * cosDec * cosH);
			store(50,el,i);
		}
		
		out.println("Elevation as a function of LST.");	
		display(out);

		out.println();
		// Now, we will do the beginning time.
		
		// The VLA clock.
		DateTime.setClockCoordinates(107.6177275,34.0787491666667,-6);
		// Form the beginning time.
		DateTime begin = new DateTime(2003,8,25.0);
		// Compute the corresponding LST in hours.
		double lstHrs = begin.getLocalSiderealTime(); 
		out.println("begining time " + begin);
		out.println("beginning LST " + lstHrs);
		//begining time 2003-08-25T00:00:00
		//beginning LST -2.968081903837871
		
		out.println("sin(LST) " + Math.sin(lstHrs * hourToRad));
		out.println("cos(LST) " + Math.cos(lstHrs * hourToRad));
		
		// Form the DateTime for the LST.
		DateTime lst = DateTime.add(begin,(lstHrs / 24.0));		
		// Set the default clock to this time.
		// Now, the clock becomes an LST clock.
		Date d = lst.getDate();
		Time t = lst.getTime();
		out.println("default clock year  " + d.getYear());
		out.println("default clock month " + d.getMonth());
		out.println("default clock day   " + d.getDay());
		out.println("default clock hour  " + t.getHours());
		out.println("default clock min   " + t.getMinutes());
		out.println("default clock day   " + (int)t.getSeconds());
		
		ClockSimulator clock = new ClockSimulator (107.6177275,34.0787491666667,-6);
		clock.setTime(lst);
		// This clock is now an LST clock.
		out.println("Actual starting time as LST: " + clock.getDateTime());
		out.println("Actual starting time as LST: " + clock.getTimeOfDay());
		out.println("sin(current time of day) " + Math.sin(clock.getTimeOfDay() * hourToRad));
		out.println("cos(current time of day) " + Math.cos(clock.getTimeOfDay() * hourToRad));
		
		// Compute elevation of source.
		ra = 4.2;
		dec = 50.0 * degToRad;
		sinL = Math.sin(clock.getLatitude());
		cosL = Math.cos(clock.getLatitude());
		sinDec = Math.sin(dec);
		cosDec = Math.cos(dec);
		double hourAngleSource = clock.getTimeOfDay() - ra; // The time is now the LST.
		out.println("hourAngleSource = " + hourAngleSource);
		double x = sinL * sinDec + cosL * cosDec * Math.cos(hourAngleSource * hourToRad);
		double sourceEl	= Math.asin(x);
		out.println("Elevation of source at beginning time " + toDeg(sourceEl));
		clock.advance(3600 - 60 - 55);
		x = sinL * sinDec + cosL * cosDec * Math.cos((clock.getTimeOfDay() - ra) * hourToRad);
		out.println("Time: " + clock.getDateTime() + " elevation: " + toDeg(Math.asin(x)));
		clock.advance(3600);
		x = sinL * sinDec + cosL * cosDec * Math.cos((clock.getTimeOfDay() - ra) * hourToRad);
		out.println("Time: " + clock.getDateTime() + " elevation: " + toDeg(Math.asin(x)));

		///////////////////////////////////////////////////////////////////
		out.close();
		System.out.println("Look at outA.txt for output");
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

