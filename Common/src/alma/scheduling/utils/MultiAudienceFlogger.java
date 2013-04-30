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

import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;

import alma.acs.logging.AcsLogger;
import alma.acs.logging.domainspecific.AudienceLogger;
import alma.acs.logging.domainspecific.AudienceLogger.Audience;

/**
 *
 * @author dclarke
 */
public class MultiAudienceFlogger {
    
	private Collection<AudienceLogger> loggers;
	
    public MultiAudienceFlogger(AcsLogger logger, Audience... audiences) {
    	loggers = new Vector<AudienceLogger>();
    	for (final Audience a : audiences) {
    		loggers.add(new AudienceLogger(logger, a){});
    	}
    }

	public void severe(String format, Object... args) {
		final String message = String.format(format, args);
		for (final AudienceLogger logger : loggers) {
			logger.severe(message);
		}
	}

	public void warning(String format, Object... args) {
		final String message = String.format(format, args);
		for (final AudienceLogger logger : loggers) {
			logger.warning(message);
		}
	}

	public void info(String format, Object... args) {
		final String message = String.format(format, args);
		for (final AudienceLogger logger : loggers) {
			logger.info(message);
		}
	}

	public void config(String format, Object... args) {
		final String message = String.format(format, args);
		for (final AudienceLogger logger : loggers) {
			logger.config(message);
		}
	}

	public void fine(String format, Object... args) {
		final String message = String.format(format, args);
		for (final AudienceLogger logger : loggers) {
			logger.fine(message);
		}
	}

	public void finer(String format, Object... args) {
		final String message = String.format(format, args);
		for (final AudienceLogger logger : loggers) {
			logger.finer(message);
		}
	}

	public void finest(String format, Object... args) {
		final String message = String.format(format, args);
		for (final AudienceLogger logger : loggers) {
			logger.finest(message);
		}
	}
    
    public void log(Level level, Throwable thr, String format, Object... args) {
		final String message = String.format(format, args);
		for (final AudienceLogger logger : loggers) {
			logger.log(level, message, thr);
		}
    }
    
}
