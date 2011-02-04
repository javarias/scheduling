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
package alma.scheduling.array.guis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import alma.scheduling.ArrayGUICallback;
import alma.scheduling.utils.LoggerFactory;

/**
 * @author rhiriart
 *
 */
public class ArrayGUICallbackNotifier implements Observer {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private Map<String, ArrayGUICallback> registeredCallbacks = 
        new HashMap<String, ArrayGUICallback>();
    
    public ArrayGUICallbackNotifier() { }
    
    public void registerMonitor(String monitorName, ArrayGUICallback callback) {
        registeredCallbacks.put(monitorName, callback);
    }
    
    public void unregisterMonitor(String monitorName) {
        registeredCallbacks.remove(monitorName);
    }

    @Override
    public void update(Observable o, Object arg) {
    	try {
    		ArrayGUINotification notification = (ArrayGUINotification) arg;
    		logger.info("received GUI change notification");

    		for (Iterator<String> iter = registeredCallbacks.keySet().iterator(); iter.hasNext(); ) {
    			String key = iter.next();
    			ArrayGUICallback callback = registeredCallbacks.get(key);
                try {
                	callback.report(notification.getOperation(),
    					        	notification.getName(),
    					        	notification.getRole());
                }
                catch (org.omg.CORBA.TRANSIENT ex){
                	logger.info("Forcing Unregister of GUI Callback with key: " + key);
                	unregisterMonitor(key);
                }
    		}
    	} catch (ClassCastException e) {
    	}
    }
    
    
}
