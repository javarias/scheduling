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
 * File CorbaReceiver.java
 */
 
package ALMA.scheduling.define.nc;

import java.lang.reflect.*;
import java.util.*;

import org.omg.CORBA.Any;
import org.omg.CosNotification.*;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplier;


/**
 * The CorbaReceiver class implements those methods needed to craft an object
 * that receives and processes events from a CORBA notification channel.  It is intended
 * for use within a CorbaNotificationChannel, in conjunction with the attach and detach
 * methods.
 * 
 * @version 1.00 Apr 10, 2003
 * @author Allen Farris
 */
public class CorbaReceiver extends alma.acs.nc.Consumer {

	private String channelName;
	private String subsystemName;
	private CorbaNotificationChannel channel;
	
	/**
	 * Create a CORBA receiver object.  The CORBA receiver object is only 
	 * created.  Nothing actually happens until the "connect" method is 
	 * executed.  One must use the static method "getCorbaReceiver" in
	 * the CorbaNotificationChannel class to create such an object.
	 * 
	 * @param channelName	The name of the channel to which we are going to listen.
	 * @param subsystemName	The subsystem of the channel to which we are going to listen.
	 * @param eventType	The name of the type of event we are going to process.
	 * @param processor	This is an object that processes the specified event to which 
	 * 						we are going to listen.  It is required to have a method of
	 * 						the name "process(<eventType>)", where the name of the specified
	 * 						eventType is an IDL struct that represents the event data being
	 * 						processed.
	 */
	CorbaReceiver (String channelName, String subsystemName) {
		super (CorbaNotificationChannel.mapToCorbaChannel(channelName,subsystemName));
		this.channelName = channelName;
		this.subsystemName = subsystemName;
		this.channel = null;
	}
	
	void setChannel (CorbaNotificationChannel channel) {
		this.channel = channel;
	}
	
	StructuredProxyPushSupplier getProxySupplier() {
		return m_proxySupplier;
	}
	
	/**
	 * Connect this CORBA receiver to its CORBA channel to begin
	 * receiving events.
	 *
	 */
	public void connect() {
		try {
			String [] types = channel.getEventType();
			for (int i = 0; i < types.length; ++i) {
				addSubscription(types[i]);
			}
			consumerReady();
		} catch (alma.acs.nc.ncExcept err) {
			err.printStackTrace(System.err);
			throw new IllegalStateException(err.toString());
		}
	}
	
	/**
	 * Disconnect this CORBA receiver from its CORBA channel.  No events
	 * are received after this method is executed.
	 *
	 */
	public void disconnect() {
		try {
			String [] types = channel.getEventType();
			for (int i = 0; i < types.length; ++i) {
				removeSubscription(types[i]);
			}
		} catch (alma.acs.nc.ncExcept err) {
			err.printStackTrace(System.err);
			throw new IllegalStateException(err.toString());
		}
		super.disconnect();
	}

	/**
	 * This is the required CORBA method used to actually process the
	 * events requested by this receiver.  It is automatically called
	 * by the notification service.
	 * @param event The event to be received and processed.
	 */
	public void push_structured_event(StructuredEvent event) {
		// Get the idl any object.
		Any any = event.filterable_data[0].value;
		
		// Extract the idl struct EventType.
        Class helper = null;
        Object idlStruct = null;
		String eventType = event.header.fixed_header.event_type.type_name;
		try {
       		helper = Class.forName(eventType + "Helper");
       		Class[] parm = new Class [1];
      		parm[0] = Class.forName("org.omg.CORBA.Any");
       		Method extract = helper.getMethod("extract", parm);
       		Object[] arg = new Object [1];
       		arg[0] = any;
 			idlStruct = extract.invoke(null,arg);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("No such class as " + helper);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("No such method as insert(org.omg.CORBA.Any," +
				eventType + ") in class " + helper);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Cannot access method insert(org.omg.CORBA.Any," +
				eventType + ") in class " + helper);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Cannot invoke method insert(org.omg.CORBA.Any," +
				eventType + ") in class " + helper);
		}

		String className = idlStruct.getClass().getName();
		if (!channel.checkEventName(className))
			throw new IllegalArgumentException(
			"Invalid event type!  No such event as " + className);

		// Execute the "receive(<EventType>)" methods of all receivers
		// registered to receive this event.
		ListIterator iter = channel.receivers.listIterator();
		EventReceiver item = null;
		Class receiverClass = null;
		Method receiveMethod = null;
		Class[] parm = new Class [1];
		Object[] arg = new Object [1];
		while (iter.hasNext()) {
			item = (EventReceiver)iter.next();
			if (item.eventTypeName.equals(className)) {
				try {
					receiverClass = item.receiver.getClass();
					parm[0] = idlStruct.getClass();
					receiveMethod = receiverClass.getMethod("receive",parm);
					arg[0] = idlStruct;
					receiveMethod.invoke(item.receiver,arg);
				// If we've done the type checking properly, we should get
				// no exceptions at this point.
				} catch (NoSuchMethodException ex) {
					throw new IllegalArgumentException (
					"Internal Error! Cannot find method receive(" + 
					item.eventTypeName + ") in class " + receiverClass.getName());
				} catch (InvocationTargetException ex) {
					throw new IllegalArgumentException (
					"Internal Error! Cannot invoke method receive(" + 
					item.eventTypeName + ") in class " + receiverClass.getName());
				} catch (IllegalAccessException ex) {
					throw new IllegalArgumentException (
					"Internal Error! Cannot access method receive(" + 
					item.eventTypeName + ") in class " + receiverClass.getName());
				}
			}
		}

	}

}

