/*
 * Gets a list of projects for the sbs ids that are passed in.
 * This function is for the start queue scheduling method./*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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
 * $Id: Profiler.java,v 1.1 2010/04/15 23:53:17 rhiriart Exp $
 * 
 */
package alma.scheduling.util;

import java.util.logging.Logger;

public class Profiler {

    private static String PREFIX = "SCHEDULING_PROF: ";
    private Logger logger;
    private String description;
    private long start;
    
    public Profiler(Logger logger) {
        this.logger = logger;
    }
    
    public void start(String description) {
        this.description = description;
        this.start = System.currentTimeMillis();        
    }
    
    public void end() {
        logger.finest(PREFIX + description + ": " + ( System.currentTimeMillis() - start ) + " (ms)");
    }
}
