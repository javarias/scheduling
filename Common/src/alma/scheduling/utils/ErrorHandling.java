/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package alma.scheduling.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dclarke
 * $Id: ErrorHandling.java,v 1.1 2010/08/18 16:31:10 dclarke Exp $
 */
public final class ErrorHandling {
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private Logger logger;
	/* End Fields
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/**
	 * Create a helper for the given AcsLogger.
	 */
	public ErrorHandling(Logger logger) {
		this.logger = logger;
	}
	/* End Construction
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Formatting
	 * ================================================================
	 */
    /**
     * Print the stack trace of the given Throwable onto a String and
     * return that String.
     * 
     * @param t - a Throwable, the details of which we're interested  
     * @return a String with the details.
     */
    public static String printedStackTrace(Throwable t) {
    	final StringWriter sw = new StringWriter();
    	t.printStackTrace(new PrintWriter(sw));
    	return sw.toString();
    }
	/* End Formatting
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Static helper methods
	 * ================================================================
	 */
    /**
     * Put out a message at one level to the supplied Logger and then
     * put out the stack trace of the given Throwable at another level.
     * Typically the message would be at a coarser level then the stack
     * trace.
     * 
     * @param message
     * @param t
     */
    public static void log(Logger logger,
    		               Level messageLevel, String message,
    		               Level stackLevel,   Throwable t) {
    	logger.log(messageLevel, message);
    	logger.log(stackLevel, printedStackTrace(t));
    }
    
    /**
     * Put out an info message to the supplied Logger and then
     * quietly put out the stack trace of the given Throwable.
     * 
     * @param message
     * @param t
     */
    public static void info(Logger logger,
                            String message,
                            Throwable t) {
    	log(logger, Level.INFO, message, Level.FINEST, t);
    }
    
    /**
     * Put out a warning to the supplied Logger and then quietly
     * put out the stack trace of the given Throwable.
     * 
     * @param message
     * @param t
     */
    public static void warning(Logger logger,
                               String message,
                               Throwable t) {
    	log(logger, Level.WARNING, message, Level.FINEST, t);
    }
    
    /**
     * Put out a 'severe' to the supplied Logger and then quietly
     * put out the stack trace of the given Throwable.
     * 
     * @param message
     * @param t
     */
    public static void severe(Logger logger,
                              String message,
                              Throwable t) {
    	log(logger, Level.SEVERE, message, Level.FINEST, t);
    }
	/* End Static helper methods
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Helper methods
	 * ================================================================
	 */
    /**
     * Put out a message at one level to the Logger we're helping and
     * then put out the stack trace of the given Throwable at another
     * level. Typically the message would be at a coarser level then
     * the stack trace.
     * 
     * @param messageLevel
     * @param message
     * @param stackLevel
     * @param t
     */
    public void log(Level messageLevel, String message,
    		        Level stackLevel, Throwable t) {
    	logger.log(messageLevel, message);
    	logger.log(stackLevel, printedStackTrace(t));
    }
    
    /**
     * Put out an info message to the Logger we're helping and then
     * quietly put out the stack trace of the given Throwable.
     * 
     * @param message
     * @param t
     */
    public void info(String message, Throwable t) {
    	log(Level.INFO, message, Level.FINEST, t);
    }
    
    /**
     * Put out a warning to the Logger we're helping and then quietly
     * put out the stack trace of the given Throwable.
     * 
     * @param message
     * @param t
     */
    public void warning(String message, Throwable t) {
    	log(Level.WARNING, message, Level.FINEST, t);
    }
    
    /**
     * Put out a 'severe' to the Logger we're helping and then quietly
     * put out the stack trace of the given Throwable.
     * 
     * @param message
     * @param t
     */
    public void severe(String message, Throwable t) {
    	log(Level.SEVERE, message, Level.FINEST, t);
    }
	/* End Helper methods
	 * ============================================================= */
}
