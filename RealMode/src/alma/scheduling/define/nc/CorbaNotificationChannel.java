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
 * File CorbaNotificationChannel.java
 */
 
package ALMA.scheduling.define.nc;

import java.util.ArrayList;
import java.util.ListIterator;

import org.omg.CORBA.portable.IDLEntity;
import org.omg.CosNotification.*;

/**
 * The CorbaNotificationChannel class implements the notification
 * channel concepts using a CORBA-based approach that employs the CORBA
 * notification services.
 * 
 * @version 1.00 Apr 10, 2003
 * @author Allen Farris
 */
public class CorbaNotificationChannel extends AbstractNotificationChannel {

	/**
	 * Get the Receiver interface to a currently created CORBA channel.
	 * <p>
	 * This method uses the "obtain_offered_types" method that is part of the
	 * the ProxySupplier interface in the CORBA consumer.  The full 
	 * description of this method may be found in the OMG CORBA 
	 * Notification Services specification.  It is described as follows:
	 * <p>
	 * 		EventType[] obtain_offered_Types(ObtainInfoMode mode)
	 * <p>
	 * where, the ObtainInfoMode is an enumeration of:
	 * <ul>
	 * 	<li>	ALL_NOW_UPDATES_OFF,
	 * 	<li>	ALL_NOW_UPDATES_ON,
	 * 	<li>	NONE_NOW_UPDATES_OFF,
	 * 	<li>	NONE_NOW_UPDATES_ON.
	 * </ul>
	 * The mode setting in this method that we employ is
	 * ALL_NOW_UPDATES_OFF.
	 * 
	 * @param channelName	The name of the requested channel.
	 * @param subsystemName	The subsystem of the requested channel.
	 * @return A Receiver interface to the specified channel or 
	 * 			null if the channel does not exist.
	 */
	static public Receiver getCorbaReceiver(String channelName, String subsystemName) {
		CorbaNotificationChannel x = new CorbaNotificationChannel (channelName,subsystemName);
		CorbaReceiver r = new CorbaReceiver(channelName,subsystemName);
		x.setReceiver(r);
		r.setChannel(x);
		EventType[] e = r.getProxySupplier().obtain_offered_types
			(org.omg.CosNotifyChannelAdmin.ObtainInfoMode.ALL_NOW_UPDATES_OFF);
		if (e.length == 0)
			throw new IllegalArgumentException ("There are no events published on channel " +
				channelName + " of subsystem " + subsystemName);
		String[] typeName = new String [e.length];
		for (int i = 0; i < e.length; ++i)
			typeName[i] = e[i].type_name;
		x.setEventType(typeName);
		return x;
	}
	
	/**
	 * The mapToCorbaChannel method maps a channel name and subsystem name to a 
	 * properly specified CORBA notification channel name.
	 * @param channelName The name of the channel.
	 * @param subsystemName The subsystem of the channel.
	 * @return The CORBA notification channel name as a string.
	 */
	static public String mapToCorbaChannel(String channelName, String subsystemName) {
		return subsystemName + "_" + channelName;
	}
	
	/**
	 * The ALMA domain name, which is not explicitly used and is hidden
	 * from the application.
	 */
	static public final String ALMA_DOMAIN = "ALMA";

	/**
	 * The CORBA publisher object that is used to create, access, and publish 
	 * events on the CORBA channel.
	 */
	private CorbaPublisher corbaPublisher;
	
	/**
	 * The CORBA receiver object that is used to attach and detach receivers 
	 * of events.
	 */
	private CorbaReceiver corbaReceiver;


	/**
	 * The list of receivers that are registered to recieve events
	 * on this channel.  The items in this list are all objects
	 * of type EventReceiver.
	 */
	ArrayList receivers;

	/**
	 * Create a CORBA Notification Channel.
	 * @param channelName	The name of this channel.
	 * @param subsystemName	The subsystem to which this channel belongs.
	 * @param eventType	The names of the events that are published on 
	 * 						this channel.
	 */
	public CorbaNotificationChannel (String channelName, String subsystemName, 
			String[] eventType) {
		super(channelName,subsystemName,eventType);
		corbaPublisher = new CorbaPublisher(channelName, subsystemName, eventType);
		corbaReceiver = null;
		receivers = new ArrayList ();
	}
	
	/**
	 * Create a CORBA Notification Channel and specify the CorbaPublisher 
	 * being used.
	 * @param corbaPublisher	The CORBA publisher object.
	 */
	public CorbaNotificationChannel (CorbaPublisher corbaPublisher) {
		super(corbaPublisher.getChannelName(),
			  corbaPublisher.getSubsystemName(),
			  corbaPublisher.getEventType());
		this.corbaPublisher = corbaPublisher;
		this.corbaReceiver = null;
		receivers = new ArrayList ();
	}
	
	/**
	 * This method is used by the static method that creates a CORBA
	 * notification channel for a CORBA receiver. 
	 */
	private CorbaNotificationChannel (String channelName, String subsystemName) {
		super(channelName,subsystemName);
		corbaPublisher = null;
		receivers = new ArrayList ();
	}
	/**
	 * This method is used by the static method that creates a CORBA
	 * notification channel for a CORBA receiver. 
	 */
	private void setReceiver(CorbaReceiver corbaReceiver) {
		this.corbaReceiver = corbaReceiver;
	}
	
	/**
	 * Get the Publisher interface to a currently created CORBA channel.
	 * Only the creator of the channel can provide a Publisher interface.
	 * @return A Publisher interface to thisd channel.
	 */
	public Publisher getPublisher() {
		return this;
	}

	/**
	 * Attach a Receiver, that receives one type of event, to this notification 
	 * channel.  The receiver is required to have a public method called
	 * "receive(EventType)", that receives and processes the event.  The 
	 * EventType parameter in the method signature is the name of an IDL 
	 * structure that defines the event.
	 * @param eventTypeName 	The name of the event type that this receiver 
	 * 							wishes to receive.
	 * @param receiver			An object that receives and processes this event.
	 * 							It must have a public method of the form 
	 * 							"receive(EventType)", where the EventType 
	 * 							parameter in the method signature is the name 
	 * 							of an IDL structure that defines the event.
	 */
	public void attach (String eventTypeName, Object receiver) {
		// Make sure this event name is legal.
		if (!checkEventName(eventTypeName))
			throw new IllegalArgumentException(
			"Invalid receiver!  Method receive(" + eventTypeName + ")" + 
			" in Class " + receiver.getClass().getName() + " is not accessible.");

		// Make sure the receiver object has the proper method.
		String err = checkReceiver(eventTypeName,receiver);
		if (err != null)
			throw new IllegalArgumentException(err);

		// Add this eventTypeName/receiver to the list of receivers.
		synchronized (this) {
			// Make sure the eventTypeName/receiver is not already in the list.
			ListIterator iter = receivers.listIterator();
			EventReceiver item = null;
			while (iter.hasNext()) {
				item = (EventReceiver)iter.next();
				if (item.eventTypeName.equals(eventTypeName) &&
					item.receiver == receiver)
					return;
			}
			// OK, then add it.
			EventReceiver x = new EventReceiver(eventTypeName,receiver);
			receivers.add(x);
		}
	}
	
	/**
	 * Detach an eventType/Receiver from this notification channel.  Only the 
	 * specified event type is detached for the specified receiver.
	 * @param eventTypeName 	The name of the event type that this receiver 
	 * 							receives.
	 * @param receiver			The object that receives and processes this event.
	 */
	public void detach (String eventTypeName, Object receiver) {
		// Find the eventTypeName/receiver in the list and remove it.
		int n = 0;
		ListIterator iter = receivers.listIterator();
		EventReceiver item = null;
		while (iter.hasNext()) {
			item = (EventReceiver)iter.next();
			if (item.eventTypeName.equals(eventTypeName) &&
				item.receiver == receiver) {
				receivers.remove(n);
				break;
			}
			++n;
		}
		return;
	}

	/**
	 * Connect this corbaReceiver to the notification channel. 
	 * At this point the objects that have been attached begin receiving events.
	 * This method must be called or no events will be recieved.
	 */
	public void begin() {
		if (corbaReceiver == null)
			return;
		corbaReceiver.connect();
	}
	
	/**
	 * Disconnect this reciever from the notification channel.
	 * All objects that have been recieving events are removed and no further
	 * events are received.
	 */
	public void end() {
		if (corbaReceiver == null)
			return;
		corbaReceiver.disconnect();
		receivers.clear();
	}

	/**
	 * Publish an event on this notification channel.
	 * @param event The event must be an IDL structure.
	 */
	public void publish(IDLEntity event) {
		// Make sure the event is legal.
		if (!checkEvent(event))
			throw new IllegalArgumentException(
			"Invalid event type!  No such event as " + event.getClass().getName());

		// Publish the event.		
		corbaPublisher.publish(event);
	}
	
	/**
	 * Deactivate this notification channel.
	 */
	public void deactivate() {
		corbaPublisher.disconnect();
	}
	
}

