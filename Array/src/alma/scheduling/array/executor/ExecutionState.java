/* 
 * ALMA - Atacama Large Millimiter Array
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
package alma.scheduling.array.executor;

import java.util.logging.Logger;

import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.LoggerFactory;

public abstract class ExecutionState {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    protected ExecutionContext context;
    
    public ExecutionState(ExecutionContext context) {
        this.context = context;
    }
    
    public void startObservation() {
        logger.info("startObservation called when state is " + this);
    }
    
    public void stopObservation() {
        logger.info("stopObservation called when state is " + this);        
    }
    
    public void abortObservation() {
        logger.info("abortObservation called when state is " + this);        
    }
    
    public void waitArchival() {
        logger.info("waitArchival called when state is " + this);
    }
    
    /**
     * 
     * @return the execution time in seconds
     */
    public long observe(){
    	logger.info("observe called when state is " + this);
    	return 0;
    }
    
    public void setup(){
    	logger.info("setup called when state is " + this);
    }
    
    public String toString() {
    	return getClass().getSimpleName();
    }
}
