/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2010
 * (c) Associated Universities Inc., 2010
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File LogBuffer.java
 */
 package alma.scheduling.utils;

import java.util.List;
import java.util.Vector;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * Utility class to allow us to store up detailed logging information
 * about an operation, and then to determine whether or not to actually
 * put the messages out after the event. Typically the log messages are
 * printed in the event of some form of failure, and quietly dropped if
 * everything went well.
 * 
 * To use this class, at the start of your operation do something akin
 * to:
 * 
 * <pre>
 * 		final LogBuffer lb = new LogBuffer(logger);
 * </pre>
 * 
 * Then during the operation, log away using <code>logger</code> as
 * normal. Once the operation is complete and you know whether it
 * worked or not, use either one of the <code>success<i>*</i>()</code>
 * or the <code>failure<i>*</i>()</code> methods to control the
 * flushing or not of the buffer, and the message which is output
 * afterwards.
 * 
 * <pre>
 * 		lb.successInfo(String.format(
 * 					"ObsProject %s converted successfully",
 * 					projectUID));
 * </pre>
 * 
 * This model should allow <code>LogBuffer</code>s to behave
 * reasonably when stacked. Of course, your definition of reasonable
 * behaviour and mine might differ. In particular, an outer success
 * might, as in life, hide an inner failure.
 * 
 * A more complete outline of one possible method of use is (and this
 * is the one I use):
 * 
 * <pre>
 * private Logger logger; <i>// Assumed to be initialised elsewhere</i>
 * 
 * private void convert(Collection<ObsProject> obsProjects) {
 *    for (ObsProject apdmProject : obsProjects) {
 *       final LogBuffer lb = new LogBuffer(logger);
 *
 *       try {
 *          convert(apdmProject); <i>// Does the real work</i>
 *          lb.successInfo(String.format(
 *                "Converted APDM ObsProject %s",
 *                apdmProject.getObsProjectEntity().getEntityId()));
 *       } catch (ConversionException e) {
 *          lb.failureWarning(String.format(
 *                "cannot convert APDM ObsProject %s - %s",
 *                apdmProject.getObsProjectEntity().getEntityId(),
 *                e.getMessage()));
 *          }
 *       }
 *    }
 * }
 * </pre>
 * 
 * Within the <code>convert()</code> method, just use logger as per
 * normal.
 * <p>
 * Note that:
 * <ol>
 * <li>at the moment, I make no claims about the time-stamps which will
 * appear on log messages which are buffered and then output
 * later;</li>
 * <li>filter settings (most notably the levels, but others too) will
 * apply as at the time the message is first generated <em>and</em> the
 * time the buffer is flushed;</li>
 * <li>a <code>LogBuffer</code> is a one shot object - once you've
 * used a <code>success<i>*</i>()</code> or a
 * <code>failure<i>*</i>()</code> method, that's kind of it - create a
 * new one to go again (it ain't heavy, it's a buffer).</li>
 * </ol>
 * 
 * @author dclarke
 * <br>
 * $Id: LogBuffer.java,v 1.1 2010/06/28 22:49:03 dclarke Exp $
 */
public class LogBuffer extends Handler {

	/*
	 * This class is a subclass of the Handler class, and an instance
	 * of it works by:
	 * 	. replacing the supplied Logger's Handlers with itself, thus
	 *    intercepting all messages published via the Logger;
	 * 	. handling log messages which are published via the Logger by
	 * 	  remembering them in a buffer of LogRecords;
	 *  . replacing the original Handlers once success or failure is
	 *    declared by the client code.
	 */
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	/** A stash for the original Handlers of the underlying Logger */
	private Handler[]       handlers;
	
	/** Was the underlying Logger using its parent's handlers too? */
	private boolean         useParents;
	
	/** The buffer of LogRecords published whilst we're operational */
	private List<LogRecord> buffer;
	
	/** The underlying logger for which we are buffering */
	private Logger          logger;
	
	/** A flag which indicates if we're operational or not */
	private boolean         intercepting = false;
	/* End Fields
	 * ============================================================= */



	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/**
	 * Create a LogBuffer for the given Logger.
	 * 
	 * @param logger - the underlying logger to put a buffer around.
	 */
	public LogBuffer(Logger logger) {
		this.logger = logger;
		replaceHandlers();
		clearBuffer();
	}
	/* End Construction
	 * ============================================================= */



	/*
	 * ================================================================
	 * Handler handling
	 * ================================================================
	 */
	/**
	 * Replace the underlying Logger's Handlers by <code>this</code>.
	 */
	private void replaceHandlers() {
		useParents = logger.getUseParentHandlers();
		logger.setUseParentHandlers(false);
		handlers = logger.getHandlers();
		for (Handler h : handlers) {
			logger.removeHandler(h);
		}
		logger.addHandler(this);
		intercepting = true;
	}

	/**
	 * Restore the underlying Logger's Handlers.
	 */
	private void restoreHandlers() {
		logger.removeHandler(this);
		for (Handler h : handlers) {
			logger.addHandler(h);
		}
		logger.setUseParentHandlers(useParents);
		intercepting = false;
	}
	/* End Handler handling
	 * ============================================================= */



	/*
	 * ================================================================
	 * Message buffer
	 * ================================================================
	 */
	/**
	 * Reinitialise the message buffer - will create it if necessary.
	 */
	private void clearBuffer() {
		try {
			buffer.clear();
		} catch (NullPointerException e) {
			buffer = new Vector<LogRecord>();
		}
	}
	
	/**
	 * Publish the cached messages to the original Handlers. Clears the
	 * message buffer.
	 */
	private void flushBuffer() {
		if (intercepting) {
			// If we're still operational, then swap in the original
			// Handlers, publish the message to them and then swap them
			// out again.
			restoreHandlers();
			flushBuffer();
			replaceHandlers();
		} else {
			// We're not operational, so the original Handlers are in
			// place.
			for (LogRecord lr : buffer) {
				logger.log(lr);
			}
			clearBuffer();
		}
	}
	/* End Message buffer
	 * ============================================================= */



	/*
	 * ================================================================
	 * Termination of the buffered operation
	 * ================================================================
	 */
	/**
	 * Tacitly admit that things have gone wrong. Publishes the
	 * buffered messages.
	 */
	public void failure() {
		restoreHandlers();
		flushBuffer();
	}
	
	/**
	 * Log that the operation we're logging has failed. Publishes the
	 * buffered messages.
	 * 
	 * @param message - the message to put out saying what's happened.
	 */
	public void failureWarning(String message) {
		restoreHandlers();
		flushBuffer();
		logger.warning(message);
	}
	
	/**
	 * Log that the operation we're logging has failed badly. Publishes
	 * the buffered messages.
	 * 
	 * @param message - the message to put out saying what's happened.
	 */
	public void failureSevere(String message) {
		restoreHandlers();
		flushBuffer();
		logger.severe(message);
	}

	/**
	 * Quietly gloat that things have succeeded. Does not publish the
	 * buffered messages.
	 */
	public void success() {
		restoreHandlers();
		clearBuffer();
	}

	/**
	 * Log that the operation we're logging has succeeded. Does not
	 * publish the buffered messages.
	 * 
	 * @param message - the message to publish saying what's happened.
	 */
	public void successInfo(String message) {
		restoreHandlers();
		clearBuffer();
		logger.info(message);
	}

	/**
	 * Log that the operation we're logging has succeeded. Does not
	 * publish the buffered messages.
	 * 
	 * @param message - the message to publish saying what's happened.
	 */
	public void successFine(String message) {
		restoreHandlers();
		clearBuffer();
		logger.fine(message);
	}

	/**
	 * Log that the operation we're logging has succeeded. Does not
	 * publish the buffered messages.
	 * 
	 * @param message - the message to publish saying what's happened.
	 */
	public void successFiner(String message) {
		restoreHandlers();
		clearBuffer();
		logger.finer(message);
	}

	/**
	 * Log that the operation we're logging has succeeded. Does not
	 * publish the buffered messages.
	 * 
	 * @param message - the message to publish saying what's happened.
	 */
	public void successFinest(String message) {
		restoreHandlers();
		clearBuffer();
		logger.finest(message);
	}
	/* End Termination of the buffered operation
	 * ============================================================= */



	/*
	 * ================================================================
	 * Implementation of the Handler contract
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see java.util.logging.Handler#close()
	 */
	@Override
	public void close() throws SecurityException {
		failure();
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#flush()
	 */
	@Override
	public void flush() {
		flushBuffer();
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	@Override
	public void publish(LogRecord record) {
		// Add the published LogRecord to the buffer.
		buffer.add(record);
	}
	/* End Implementation of the Handler contract
	 * ============================================================= */
}
