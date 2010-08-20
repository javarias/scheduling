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
package alma.scheduling.array.executor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import alma.ACSErr.Completion;
import alma.ACSErr.ErrorTrace;
import alma.scheduling.SchedBlockExecutionCallback;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.array.util.LoggerFactory;

/**
 * @author rhiriart
 *
 */
public class ExecutorCallbackNotifier implements Observer {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private Map<String, SchedBlockExecutionCallback> registeredCallbacks = 
        new HashMap<String, SchedBlockExecutionCallback>();
    
    public ExecutorCallbackNotifier() { }
    
    public void registerMonitor(String monitorName, SchedBlockExecutionCallback callback) {
        registeredCallbacks.put(monitorName, callback);
    }
    
    public void unregisterMonitor(String monitorName) {
        registeredCallbacks.remove(monitorName);
    }

    @Override
    public void update(Observable o, Object arg) {
        logger.info("received notification of execution state change");
        Executor e = (Executor) o;
        ExecutionStateChange stch = (ExecutionStateChange) arg;
        SchedBlockQueueItem item = new SchedBlockQueueItem(stch.getItem().getTimestamp(),
                stch.getItem().getUid());
        for (Iterator<String> iter = registeredCallbacks.keySet().iterator(); iter.hasNext(); ) {
            SchedBlockExecutionCallback callback = registeredCallbacks.get(iter.next());
            callback.report(item, stch.getNewState(),
                    new Completion(0L, 0, 0, new ErrorTrace[0]));
        }
    }
    
    
}
