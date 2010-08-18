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

import java.util.logging.Logger;

import alma.acs.logging.AcsLogger;
import alma.acs.logging.config.LogConfig;

/**
 *
 * @author dclarke
 * $Id: SLogger.java,v 1.1 2010/08/18 16:31:10 dclarke Exp $
 */
public class SLogger extends AcsLogger {

	/**
	 * @param name
	 * @param resourceBundleName
	 * @param logConfig
	 */
	public SLogger(String name, String resourceBundleName, LogConfig logConfig) {
		super(name, resourceBundleName, logConfig);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 * @param resourceBundleName
	 * @param logConfig
	 * @param allowNullLogConfig
	 * @param delegate
	 */
	public SLogger(String name, String resourceBundleName, LogConfig logConfig,
			boolean allowNullLogConfig, Logger delegate) {
		super(name, resourceBundleName, logConfig, allowNullLogConfig, delegate);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
