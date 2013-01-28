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

import java.util.HashMap;
import java.util.logging.Logger;

import org.omg.CORBA.portable.IDLEntity;

import alma.ACSErrTypeCommon.wrappers.AcsJCouldntPerformActionEx;
import alma.ACSErrTypeCommon.wrappers.AcsJIllegalStateEventEx;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.SchedulingMasterExceptions.wrappers.AcsJACSInternalExceptionEx;
import alma.acs.container.ContainerServices;
import alma.acs.exceptions.AcsJException;
import alma.acs.nc.AcsEventPublisher;
import alma.acs.nc.AcsEventSubscriber;
import alma.acs.nc.AcsEventSubscriber.Callback;
import alma.acsErrTypeLifeCycle.wrappers.AcsJEventSubscriptionEx;
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
    
    private HashMap<Callback<IDLEntity>, AcsEventSubscriber<IDLEntity>> activeNcSubscribers = new HashMap<Callback<IDLEntity>, AcsEventSubscriber<IDLEntity>>();
    
    /*
     * CorbaNotificationChannel is a wrapper that Allen Farris wrote that
     * includes both the publisher and the receiver. May be we should use
     * the original ACS classes.
     */
    private AcsEventPublisher<IDLEntity> nc;
    
    public AcsNotificationChannel(ContainerServices container, String channelName) throws AcsJException {
        this.container = container;
        this.nc = container.createNotificationChannelPublisher(channelName, IDLEntity.class);
    }

    @Override
    public void publish(IDLEntity event) throws AcsJException {
        nc.publishEvent(event);
    }
    
    @Override
    public void cleanUp() throws AcsJException {
        // Don't deactivate the notification channel!!!
        // If it is deactivated, then if the array component is deactivated
        // and activated again, the notification channel won't be usable.
        //     sched_nc.deactivate();
    }

    @SuppressWarnings("unchecked")
	@Override
    public void attach(String channelName, Callback<? extends IDLEntity> receiver) {
    	AcsEventSubscriber<IDLEntity> s = null;
		try {
			s = (AcsEventSubscriber<IDLEntity>) container.createNotificationChannelSubscriber(channelName, receiver.getEventType());
			s.addSubscription(receiver);
			synchronized (activeNcSubscribers) {
				activeNcSubscribers.put((Callback<IDLEntity>) receiver, s);
			}
		} catch (AcsJContainerServicesEx e) {
			AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
			e.log(container.getLogger());
			ex.printStackTrace();
		} catch (AcsJEventSubscriptionEx e) {
			AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
			e.log(container.getLogger());
			ex.printStackTrace();
		}
        
    }

    @Override
    public void detach(String channelName, Callback<? extends IDLEntity> receiver) {
    	AcsEventSubscriber<IDLEntity> s = null;
    	synchronized (activeNcSubscribers) {
    		s = activeNcSubscribers.remove(receiver);
    	}
    	try {
    		s.disconnect();
		} catch (AcsJIllegalStateEventEx e) {
			AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
			e.log(container.getLogger());
			ex.printStackTrace();
		} catch (AcsJCouldntPerformActionEx e) {
			AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
			e.log(container.getLogger());
			ex.printStackTrace();
		}
    }

	@Override
	public void end() {
		synchronized(activeNcSubscribers) {
			for (Callback<? extends IDLEntity> c: activeNcSubscribers.keySet()) {
				try {
					activeNcSubscribers.get(c).disconnect();
				} catch (AcsJIllegalStateEventEx e) {
					AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
					e.log(container.getLogger());
					ex.printStackTrace();
				} catch (AcsJCouldntPerformActionEx e) {
					AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
					e.log(container.getLogger());
					ex.printStackTrace();
				}
			}
			activeNcSubscribers.clear();
		}
	}

	@Override
	public void begin() {
		synchronized(activeNcSubscribers) {
			for (Callback<? extends IDLEntity> c: activeNcSubscribers.keySet()) {
				try {
					activeNcSubscribers.get(c).startReceivingEvents();
				} catch (AcsJIllegalStateEventEx e) {
					AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
					e.log(container.getLogger());
					ex.printStackTrace();
				} catch (AcsJCouldntPerformActionEx e) {
					AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
					e.log(container.getLogger());
					ex.printStackTrace();
				}
			}
		}
	}

}
