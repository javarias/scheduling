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
 * File AbstractNotificationChannel.java
 */
 
package alma.scheduling.define.nc;

import java.lang.reflect.*;

/**
 * The AbstractNotificationChannel class forms the base class
 * from which Local and CORBA Notification Channel classes are
 * extended.
 * 
 * @version 1.00 Apr 17, 2003
 * @author Allen Farris
 */
public abstract class AbstractNotificationChannel implements NotificationChannel {
	
	/**
	 * The designation for a local notification channel.
	 */
	public static final int LOCAL = 0;
	/**
	 * The designation for a CORBA notification channel.
	 */
	public static final int CORBA = 1;
	
	/**
	 * Create a Notification Channel.
	 * @param type Either LOCAL or CORBA.
	 * @param channelName	The name of this channel.
	 * @param eventType	The names of the events that are published on 
	 * 						this channel.
	 */
	static public AbstractNotificationChannel createNotificationChannel(int type,String channelName) {
		switch (type) {
			case LOCAL: return new LocalNotificationChannel(channelName);
			case CORBA: return new CorbaNotificationChannel(channelName);
		}
		throw new IllegalArgumentException("Illeagl type value (" + type + 
			").  Must be LOCAL or CORBA.");
	}
	
	// Alternative code WITH the check of event type is designated with CheckEventType.
	// CheckEventType
	/*
	static public AbstractNotificationChannel createNotificationChannel(int type,
		String channelName, String[] eventType) {
		switch (type) {
			case LOCAL: return new LocalNotificationChannel(channelName,eventType);
			case CORBA: return new CorbaNotificationChannel(channelName,eventType);
		}
		throw new IllegalArgumentException("Illeagl type value (" + type + 
			").  Must be LOCAL or CORBA.");
	}
	*/

	/**
	 * Get the Receiver interface to a currently created channel.
	 * @param type Either LOCAL or CORBA.
	 * @param channelName	The name of the requested channel.
	 */
	static public Receiver getReceiver(int type, String channelName) {
		switch (type) {
			case LOCAL: return LocalNotificationChannel.getLocalReceiver(channelName);
			case CORBA: return CorbaNotificationChannel.getCorbaReceiver(channelName);
		}
		throw new IllegalArgumentException("Illeagl type value (" + type + 
			").  Must be LOCAL or CORBA.");
	}

	/**
	 * The name of this channel.
	 */
	protected String channelName;

	// CheckEventType
	// * The type of events that are published on this channel.
	// protected String[] eventType;
	
	
	/**
	 * Create an AbstractNotification Channel.
	 * @param channelName	The name of this channel.
	 * @param eventType	The names of the events that are published on 
	 * 						this channel.
	 */
	protected AbstractNotificationChannel (String channelName) {
		// Make sure the argument is legal.
		if (channelName == null || channelName.length() == 0)
			throw new IllegalArgumentException("channelName cannot be null");
		// Save the argument.
		this.channelName = channelName;
	}
	// CheckEventType
	/*
	protected AbstractNotificationChannel (String channelName, String[] eventType) {
		// Make sure the arguments are legal.
		if (channelName == null || channelName.length() == 0)
			throw new IllegalArgumentException("channelName cannot be null");
		// Save the arguments.
		this.channelName = channelName;
		setEventType(eventType);
	}
	protected AbstractNotificationChannel (String channelName) {
		// Make sure the arguments are legal.
		if (channelName == null || channelName.length() == 0)
			throw new IllegalArgumentException("channelName cannot be null");
		// Save the arguments.
		this.channelName = channelName;
		eventType = null;
	}	
	// * Set the event types that are published on this channel.
	// * @param eventType The event types that are published on this channel.
	protected void setEventType(String[] eventType) {
		if (eventType == null || eventType.length == 0)
			throw new IllegalArgumentException("eventType cannot be null.");
		Class c = null;
		for (int i = 0; i < eventType.length; ++i) {
			if (eventType[i] == null || eventType[i].length() == 0)
				throw new IllegalArgumentException("eventType " + i + 
				" cannnot be null.");
			// And, make sure the classes defining the event types are defined.
			try {
				c = Class.forName(eventType[i]);
				// These classes must also have a "Helper" class.
				c = Class.forName(eventType[i] + "Helper");
			} catch (ClassNotFoundException err) {
				throw new IllegalArgumentException(
					"Invalid event type!  There must be classes defining " + 
					eventType[i] + " and " + eventType[i] + "Helper");
			}
		}
		this.eventType = new String [eventType.length];
		for (int i = 0; i < eventType.length; ++i)
			this.eventType[i] = eventType[i];
	}
	*/

	
	/**
	 * Get the Publisher interface to a currently created channel.
	 * @return A Publisher interface to the specified channel.
	 */
	public abstract Publisher getPublisher();

	/**
	 * Deactivate this notification channel.
	 */
	public abstract void deactivate();

	/**
	 * Get the channelName.
	 * @return String
	 */
	public String getChannelName() {
		return channelName;
	}
	
	//	CheckEventType
	/*
	 * Get the event types.
	 * @return String[] The names of the event types.
	public String[] getEventType() {
		return eventType;
	}
	 * Return true if the event name is in the list of events that can
	 * be published on this channel; otherwise, return false.
	protected boolean checkEventName(String eventTypeName) {
		for (int i = 0; i < eventType.length; ++i)
			if (eventType[i].equals(eventTypeName))
				return true;;
		return false;	
	}
	 * Return true if this event can be published on this channel; 
	 * otherwise, return false.
	protected boolean checkEvent(IDLEntity event) {
		return checkEventName(event.getClass().getName());
	}
	*/

	// The following are helper methods used by the implementing classes. 
	
	/**
	 * Return an error message if the receiver object does not contain
	 * a method of the type "receive(EventType)"; otherwise return null.
	 * @param eventTypeName 	The name of the event type that this receiver 
	 * 							wishes to receive.
	 * @param receiver			An object that receives and processes this event.
	 * 							It must have a public method of the form 
	 * 							"receive(EventType)", where the EventType 
	 * 							parameter in the method signature is the name 
	 * 							of an IDL structure that defines the event.
	 */
	static String checkReceiver(String eventTypeName, Object receiver) {
		// Make sure the receiver object has the proper method.
		Class receiverClass = receiver.getClass();
		Method receiveMethod = null;
		Class[] parm = new Class [1];
		try {
			parm[0] = Class.forName(eventTypeName);
			receiveMethod = receiverClass.getMethod("receive",parm);
		} catch (ClassNotFoundException err) { 
			return
			"Invalid event type!  There is no class defining " + eventTypeName;
		} catch (NoSuchMethodException err) { 
			return
			"Invalid receiver!  Class " + receiverClass.getName() + 
			" has no such public method as receive(" + eventTypeName + ")";
		} catch (SecurityException err) { 
			return
			"Invalid receiver!  Method receive(" + eventTypeName + ")" + 
			" in Class " + receiverClass.getName() + " is not accessible.";
		}
		return null;
	}
	
}
