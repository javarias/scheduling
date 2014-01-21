/* @(#)Astro.java   $Revision: 1.1 $  $Date: 2009/02/17 09:19:46 $
 *
 * Copyright (C) 2002 P.Grosbol, European Southern Observatory
 * License:  GNU General Public License version 2 or later
 */
package alma.scheduling.utils;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

/** Astro provides a set of usefull static method for astronomy such
 *  as convert sexagesimal coordinates, Greenwich Mean Siderical Time,
 *  Modified Julian date, Local Siderical Time, and clock methods.
 *
 *  @version $Id: AstroTime.java,v 1.1 2009/02/17 09:19:46 mschilli Exp $
 *  @author P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class AstroTime {

    private final static long   J2000;
    private final static double SIXTY = 60.0;
    private final static double MJD2000 = 51544.5;
    private final static double DAYMILLISEC = 86400000.0;
    private final static double JCENT = 36525.0;

    static {
	TimeZone utc = TimeZone.getTimeZone("UTC");
	GregorianCalendar gc2000 = new GregorianCalendar(utc);
	gc2000.set(2000,0,1,12,0,0);
	J2000 = gc2000.getTime().getTime();
    }

    /** Converts a number written in sexagesimal format to double
     *
     *  @param  coor  string with sexagesimal number
     */
    public static double parseSexagesimal(String coor) {
	double value = 0.0;
	double fac = 1.0;
	boolean neg = 0 <= coor.indexOf("-");
	StringTokenizer stok = new StringTokenizer(coor, "-+ :");
	String tok;
	try {
	    while (stok.hasMoreTokens()) {
		tok = stok.nextToken();
		value += fac*(Double.valueOf(tok).doubleValue());
		fac /= SIXTY;
	    }
	} catch (NumberFormatException e) {
	    System.err.println("Error: bad number format >" + coor + "<");
	}

	return (neg) ? -value : value;
    }

    /** Converts a real number to sexagesimal format  dd:mm:ss
     *
     *  @param  value  real value to be converted to sexagesimal format
     */
    public static String toSexagesimal(double value) {
	return toSexagesimal(value, 4);
    }

    /**Converts a real number to sexagesimal format with specified
     *  precision
     *
     *  @param  value  real value to be converted to sexagesimal foramt
     *  @param  ndigit no. of digits in sexagesimal format
     */
    public static String toSexagesimal(double value, int ndigit) {
	DecimalFormat fmtDeg = new DecimalFormat("00");
	DecimalFormat fmtMin = new DecimalFormat(":00");
	DecimalFormat fmtSec = new DecimalFormat(":00.0");
	FieldPosition fp = new FieldPosition(0);
	StringBuffer sbuf = new StringBuffer();
	if (value<0.0) {
	    value = -value;
	    sbuf.append("-");
	} else {
	    sbuf.append(" ");
	}
	double val = Math.floor(value);
	fmtDeg.format(val, sbuf, fp);
	if (ndigit<2) {
	    return sbuf.toString();
	}
	value = SIXTY*(value-val);
	val = Math.floor(value);
	fmtMin.format(val, sbuf, fp);
	if (ndigit<4) {
	    return sbuf.toString();
	}
	value = SIXTY*(value-val);
	if (ndigit<5) {
	    val = Math.floor(value);
	    fmtMin.format(val, sbuf, fp);
	} else {
	    value -= 0.05;        /* to avoid rounding up to 60.0s  */
	    fmtSec.format(value, sbuf, fp);
	}
	return sbuf.toString();
    }

    /** Computes the Mean Siderical Time at Greenwich
     *
     *  @param  date time for which GMST should be computed
     */
    public static double getGMST(Date date) {
	double d  = getMJD(date);
	double df = Math.floor(d);
	double h  = 24.0 * (d - df);
	double tu = (df - MJD2000)/JCENT;
	double gmst = 24110.54841 + tu*(8640184.812866
					+ tu*(0.093104 - tu*6.2e-6));
	gmst = gmst/3600.0 + 1.0027379094*24.0*(d - df);
	gmst -= Math.floor(gmst/24.0)*24.0;
	return gmst;	
    }

    /** Computes Modified Julian Date
     *
     *  @param  date time for which MDJ should be computed
     */
    public static double getMJD(Date date) {
	double mjd = (double) (date.getTime() - J2000);
	return MJD2000 + mjd/DAYMILLISEC;
    }

    /** Gets Local Siderical Time for site at given date
     *
     *  @param  date  time for which to claculate LST
     *  @param  lon   longitude of site in degress
     *  @return LST in hours for longitude specified
     */
    public static double getLST(Date date, double lon) {
        double lst = AstroTime.getGMST(date) - lon/15.0 + 240.0;
        return lst - Math.floor(lst/24.0)*24.0;
    }

    /** Gets current UTC.
     *
     *  @return utc time in hours 
     */
    public static double getUTC(Date date) {
	double utc = date.getTime()/DAYMILLISEC;
	return (utc - Math.floor(utc))*24.0;
    }

    /** 
     * Sets the time this utility class should
     * use.
     * 
     *  msc: had to change this because this method
     *  wasn't prepared to be called more than once.
     *  i'm not sure, this still delivers correct
     *  values, however.
     *
     *  @return current date in UTC time zone
     */
    public static Date setDate (Date date) {
   	 return date;
    }

// ============= testing ==================
    /**
     * Method for testing the class. It takes upto two parameters namely:
     *    1)  local time in ISO format yyyy-mm-ddThh:mm:ss (default now)
     *    2)  longitude of site (default Chajnantor 67.759167)
     */
    public static void main (String args[]) {

	SimpleDateFormat isolong = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	double lon = 67.759167;

	Date date = new Date();

	if (0 < args.length) {
	    date = isolong.parse(args[0], new ParsePosition(0));
	    if (date == null) {
		System.err.println("Error in date format - " +
				   "use: yyyy-MM-dd'T'HH:mm:ss");
		System.exit(1);
	    }
	    if (1 < args.length) {
		try {
		    lon = Double.parseDouble(args[1]);
		} catch (NumberFormatException e) {
		    System.err.println("Error in longitude format");
		    System.err.println("  " + e);
		    System.exit(2);
		}
	    }
	}

	System.out.println("Date: " + date + ", Longitude: "
			   + AstroTime.toSexagesimal(lon));
	double mjd = AstroTime.getMJD(date);

	System.out.println("MJD : " + mjd);

	double utc = AstroTime.getUTC(date);
	System.out.println("UTC : " + utc + ", " + AstroTime.toSexagesimal(utc));

	double gmst = AstroTime.getGMST(date);
	System.out.println("GMST: " + gmst + ", " + AstroTime.toSexagesimal(gmst));

	double lst = AstroTime.getLST(date, lon);
	System.out.println("LST : " + lst + ", " + AstroTime.toSexagesimal(lst));
		
	System.exit(0);
    }
    

}