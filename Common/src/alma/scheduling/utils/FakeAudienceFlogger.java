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

import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import alma.acs.logging.domainspecific.AudienceLogger.Audience;

/**
 *
 * @author dclarke
 * $Id: FakeAudienceFlogger.java,v 1.1 2011/07/13 21:47:44 dclarke Exp $
 */
public class FakeAudienceFlogger {
    
	private Logger logger;
	
    public FakeAudienceFlogger(String name, Audience audience) {
    	this.logger   = Logger.getLogger(
    			String.format("%s to %s",
    					name,
    					audience.getIdlName()));
    }

    
    private String format(String format, Object... args) {
    	final StringBuilder s = new StringBuilder();
    	final Formatter     f = new Formatter(s);
    	
    	s.append('\t');
    	f.format(format, args);
    	return s.toString();
    }
    
	public void severe(String format, Object... args) {
		logger.severe(format(format, args));
	}

	public void warning(String format, Object... args) {
		logger.warning(format(format, args));
	}

	public void info(String format, Object... args) {
		logger.info(format(format, args));
	}

	public void config(String format, Object... args) {
		logger.config(format(format, args));
	}

	public void fine(String format, Object... args) {
		logger.fine(format(format, args));
	}

	public void finer(String format, Object... args) {
		logger.finer(format(format, args));
	}

	public void finest(String format, Object... args) {
		logger.finest(format(format, args));
	}
    
    public void log(Level level, Throwable thr, String format, Object... args) {
    	logger.log(level, format(format, args), thr);
    }
    
}
