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
package alma.scheduling.array.executor.services;

import java.util.logging.Logger;

import org.omg.CORBA.portable.IDLEntity;

import alma.acs.container.ContainerServices;
import alma.acs.exceptions.AcsJException;
import alma.acs.nc.CorbaNotificationChannel;
import alma.scheduling.utils.LoggerFactory;

/**
 * A wrapper to send event to the Scheduling CORBA notification channel.
 * 
 * @author Rafael Hiriart
 *
 */
public class AcsNotificationChannel implements EventPublisher, EventReceiver {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private ContainerServices container;
    
    /*
     * CorbaNotificationChannel is a wrapper that Allen Farris wrote that
     * includes both the publisher and the receiver. May be we should use
     * the original ACS classes.
     */
    private CorbaNotificationChannel nc;
    
    public AcsNotificationChannel(ContainerServices container, String channelName) throws AcsJException {
        this.container = container;
        this.nc = new CorbaNotificationChannel(channelName, container);
    }

    @Override
    public void publish(IDLEntity event) throws AcsJException {
        nc.publish(event);
    }
    
    @Override
    public void cleanUp() throws AcsJException {
        // Don't deactivate the notification channel!!!
        // If it is deactivated, then if the array component is deactivated
        // and activated again, the notification channel won't be usable.
        //     sched_nc.deactivate();
    }

    @Override
    public void attach(String eventName, Object receiver) {
        nc.attach(eventName, receiver);
    }

    @Override
    public void detach(String eventName, Object receiver) {
        nc.detach(eventName, receiver);
    }

    @Override
    public void begin() {
        nc.begin();
    }

    @Override
    public void end() {
        nc.end();
    }

}
