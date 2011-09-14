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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * To get an instance of this class, use {@link ErrorHandling#getInstance()}
 *
 * @author dclarke
 * $Id: ErrorHandling.java,v 1.7 2011/09/14 22:03:09 dclarke Exp $
 */
public final class ErrorHandling {
	/*
	 * ================================================================
	 * Constants
	 * ================================================================
	 */
	private static Level DetailsLevel = Level.INFO;
	/* End Fields
	 * ============================================================= */

	
	
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
	private ErrorHandling(Logger logger) {
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
    
    /**
     * Print a stack trace of where we are on to the given stream.
     * 
     * @return a String with the details.
     */
    public static void printStackTrace(PrintStream stream) {
    	Exception e = new Exception();
    	stream.println(printedStackTrace(e));
    }
    
    /**
     * Print a stack trace of where we are onto the System output.
     * 
     * @return a String with the details.
     */
    public static void printStackTrace() {
    	printStackTrace(System.out);
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
    	log(logger, Level.INFO, message, DetailsLevel, t);
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
    	log(logger, Level.WARNING, message, DetailsLevel, t);
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
    	log(logger, Level.SEVERE, message, DetailsLevel, t);
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
     * Put out an debug message to the Logger we're helping
     * 
     * @param message
     */
    public void trace(String message) {
    	logger.log(Level.FINEST, message);
    }
    
    /**
     * Put out an debug message to the Logger we're helping
     * 
     * @param message
     */
    public void debug(String message) {
    	logger.log(Level.FINER, message);
    }
    
    /**
     * Put out an debug message to the Logger we're helping and then
     * quietly put out the stack trace of the given Throwable.
     * 
     * @param message
     * @param t
     */
    public void debug(String message, Throwable t) {
    	log(Level.FINER, message, Level.FINER, t);
    }
    
    /**
     * Put out an info message to the Logger we're helping and then
     * quietly put out the stack trace of the given Throwable.
     * 
     * @param message
     * @param t
     */
    public void info(String message, Throwable t) {
    	log(Level.INFO, message, DetailsLevel, t);
    }
    
    public void info(String message) {
    	logger.log(Level.INFO, message);
    }
    
    /**
     * Put out a warning to the Logger we're helping and then quietly
     * put out the stack trace of the given Throwable.
     * 
     * @param message
     * @param t
     */
    public void warning(String message, Throwable t) {
    	log(Level.WARNING, message, DetailsLevel, t);
    }
    
    /**
     * Put out a 'severe' to the Logger we're helping and then quietly
     * put out the stack trace of the given Throwable.
     * 
     * @param message
     * @param t
     */
    public void severe(String message, Throwable t) {
    	log(Level.SEVERE, message, DetailsLevel, t);
    }
	/* End Helper methods
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Miscellany
	 * ================================================================
	 */
	public static void logArray(Logger   logger,
			                    String   label,
			                    Object[] choices) {
		logger.fine(formatArray(label, true, choices));
	}
	
	public static String formatArray(String   label,
			                         boolean  showCount,
			                         Object[] choices) {
		final StringBuilder sb = new StringBuilder();

		sb.append(label);
		if (showCount) {
			sb.append(String.format("[%d]", choices.length));
		}
		sb.append(" = [");
		
		String sep = "";
		for (Object choice : choices) {
			sb.append(sep);
			sb.append(choice.toString());
			sep = ", ";
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	public static String formatList(Object[] choices) {
		final StringBuilder sb = new StringBuilder();

		String sep = "";
		for (Object choice : choices) {
			sb.append(sep);
			sb.append(choice.toString());
			sep = ", ";
		}
	
		return sb.toString();
	}
	/* End Miscellany
	 * ============================================================= */

	private static ErrorHandling instance;
	
	/**
	 * 
	 * @return true if the singleton is initialized, false otherwise
	 */
	public static boolean isInitialized() {
		if (instance == null)
			return false;
		else
			return true;
	}
	
	/**
	 * Initialize the instance to be used in the singleton.
	 * If the instance is already initialized and created , 
	 * this method create a new instance with given logger.
	 * 
	 * @param logger the instance of the logger to be used in the handler instance
	 */
	public static void initialize(Logger logger) {
		if (logger == null)
			throw new IllegalArgumentException("logger cannot be null");
		instance = new ErrorHandling(logger);
	}
	
	/**
	 * 
	 * It will return the instance of this class already initialized with
	 * the specified logger. If the instance was not properly initialized
	 * the logger to be set will be the default java logger ({@link Logger})
	 *
	 * @see ErrorHandling#initialize(Logger)
	 * 
	 * @return the ErrorHandling singleton
	 */
	public static ErrorHandling getInstance() {
		if (instance == null)
			initialize(Logger.getLogger(Logger.GLOBAL_LOGGER_NAME));
		return instance;
	}
}
