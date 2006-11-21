/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File InteractiveSession.java
 */
package alma.scheduling.Define;

/**
 * An interactive session is an interface that is implemented by an
 * Interactive Scheduler.  It defines the basic methods that a PI
 * can perform during an interactive observing session.  It includes:
 * <ul>
 * <li>login -- begin an interactive session
 * <li>logout -- end an interactive session
 * <li>getAllSB -- get all SBs currently defined for this session
 * <li>add -- add an SB
 * <li>modify -- modify an SB
 * <li>delete -- delete an SB
 * <li>execute -- execute an SB
 * <li>stop -- stop the currently executing SB
 * <li>startSciPipeline -- start the science pipeline
 * </ul> 
 * 
 * @version $Id: InteractiveSession.java,v 1.4 2006/11/21 23:38:06 sslucero Exp $
 * @author Allen Farris
 */
public interface InteractiveSession {
	
	void login(String PI, String projId, SB interactiveSB) throws SchedulingException;
	
	void logout() throws SchedulingException;
	
	SB[] getAllSB() throws SchedulingException;
	
	void add(SB sb) throws SchedulingException;
	
	void update(SB sb) throws SchedulingException;
	
	void delete(String sbId) throws SchedulingException;
	
	//void execute(String sbId) throws SchedulingException;
	void execute(SB sb) throws SchedulingException;
	
	void stop(String sbId) throws SchedulingException;
	
	void startSciPipeline(SciPipelineRequest req) throws SchedulingException;
	

}
