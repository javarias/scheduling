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

import java.util.logging.Level;

import alma.acs.logging.AcsLogger;
import alma.acs.logging.domainspecific.AudienceLogger;

/**
 *
 * @author dclarke
 * $Id: AudienceFlogger.java,v 1.1 2011/07/13 21:47:45 dclarke Exp $
 */
public class AudienceFlogger extends AudienceLogger {
    
    public AudienceFlogger(AcsLogger logger, Audience audience) {
        super(logger, audience);
    }

	public void severe(String format, Object... args) {
		super.severe(String.format(format, args));
	}

	public void warning(String format, Object... args) {
		super.warning(String.format(format, args));
	}

	public void info(String format, Object... args) {
		super.info(String.format(format, args));
	}

	public void config(String format, Object... args) {
		super.config(String.format(format, args));
	}

	public void fine(String format, Object... args) {
		super.fine(String.format(format, args));
	}

	public void finer(String format, Object... args) {
		super.finer(String.format(format, args));
	}

	public void finest(String format, Object... args) {
		super.finest(String.format(format, args));
	}
    
    public void log(Level level, Throwable thr, String format, Object... args) {
    	super.log(level, String.format(format, args), thr);
    }
    
}
