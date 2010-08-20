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
package alma.scheduling.array.sbQueue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import alma.scheduling.SchedBlockQueueCallback;
import alma.scheduling.array.util.LoggerFactory;

/**
 * @author rhiriart
 *
 */
public class SchedBlockQueueCallbackNotifier implements Observer {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private Map<String, SchedBlockQueueCallback> registeredCallbacks = 
        new HashMap<String, SchedBlockQueueCallback>();
    
    public SchedBlockQueueCallbackNotifier() { }
    
    public void registerMonitor(String monitorName, SchedBlockQueueCallback callback) {
        registeredCallbacks.put(monitorName, callback);
    }
    
    public void unregisterMonitor(String monitorName) {
        registeredCallbacks.remove(monitorName);
    }

    @Override
    public void update(Observable o, Object arg) {
        logger.info("received queue change notification");
        ObservableReorderingBlockingQueue q = (ObservableReorderingBlockingQueue) o;
        QueueNotification qn = (QueueNotification) arg;
        String[] uids = new String[1];
        uids[0] = qn.getItem().getUid();
        for (Iterator<String> iter = registeredCallbacks.keySet().iterator(); iter.hasNext(); ) {
            SchedBlockQueueCallback callback = registeredCallbacks.get(iter.next());
            callback.report(qn.getItem().getTimestamp(), qn.getOperation(), uids, "");
        }
    }
    
    
}
