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
 * File CorbaPublisher.java
 */
 
package alma.scheduling.define.nc;

import java.lang.reflect.*;

import org.omg.CORBA.portable.IDLEntity;
import org.omg.CORBA.Any;
import org.omg.CosNotification.*;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosEventComm.Disconnected;

/**
 * The CorbaPublisher class implements those methods needed to craft a publisher
 * that publishes events to a CORBA notification channel.
 * 
 * @version 1.00 Apr 10, 2003
 * @author Allen Farris
 */
public class CorbaPublisher extends alma.acs.nc.Supplier {
	
	private StructuredEvent corbaEvent;
	
	/**
	 * The parameters are:
	 * 		channelName	the name of the channel -- e.g., Progress
	 * 		eventType		an array that indicates the types of events
	 * 						this channel is publishing.  These should be 
	 * 						the names of the IDL structures defining those 
	 * 						types.
	 */
	public CorbaPublisher (String channelName) {
		super (channelName);
		corbaEvent = new StructuredEvent ();
	}
	
	/**
	 * This is the main method for publishing an event.  The IDLEntity must be
	 * the IDL structure that defines the event data.  It must match the names of
	 * the events in the list when the channel was created.
	 */
	public final void publish(IDLEntity event) {
		// Get the event's class name.
		String className = event.getClass().getName();
		
		// Populate the CORBA event object.
        String eventName = new String("");
        EventType corbaEventType = new EventType("ALMA",className);
        FixedEventHeader fixedHeader = new FixedEventHeader(corbaEventType, eventName);         
        Property[] variableHeader = new Property[0];
        corbaEvent.header = new EventHeader(fixedHeader, variableHeader);        
        corbaEvent.remainder_of_body = m_helper.m_orb.create_any();        
        // Next, we put the event data in the CORBA event object.
        // Place the eventData in the filterable data.
        corbaEvent.filterable_data = new Property[1];
        Any data = m_helper.m_orb.create_any();
        // To populate the any object, we must use reflection:
        // 		Get the class className + "Helper".
        // 		Execute the classNameHelper.insert(data,event);
        Class helper = null;
		try {
       		helper = Class.forName(className + "Helper");
       		Class[] parm = new Class [2];
      		parm[0] = Class.forName("org.omg.CORBA.Any");
       		parm[1] = Class.forName(className);
       		Method insert = helper.getMethod("insert", parm);
       		Object[] arg = new Object [2];
       		arg[0] = data;
       		arg[1] = event;
			insert.invoke(null,arg);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("No such class as " + helper);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("No such method as insert(org.omg.CORBA.Any," +
				className + ") in class " + helper);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Cannot access method insert(org.omg.CORBA.Any," +
				className + ") in class " + helper);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Cannot invoke method insert(org.omg.CORBA.Any," +
				className + ") in class " + helper);
		}
        corbaEvent.filterable_data[0] = new Property(className,data);
        
        // Publish the event.
		try {
	        	m_proxyConsumer.push_structured_event(corbaEvent);
		} catch (Disconnected err) {
			err.printStackTrace(System.err);
			throw new IllegalStateException(err.toString());
		}
	}
	

	/**
	 * The SetData method is not used in this class.
	 */
    public final void setData(StructuredEvent event)
    {
    }

	/**
	 * the publishEvent method is not used in this class.
	 */
    public final boolean publishEvent()
    {
        return false;
    }

	
	/**
	 * @return The channel name.
	 */
	public String getChannelName() {
		return m_channelName;
	}

}
