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
 * File NothingCanBeScheduled.java
 */
 
package alma.Scheduling.Define;

/**
 * The NothingCanBeScheduled class captures data when a dynamic
 * scheduling algorithm cannot schedule any activity.  This data 
 * includes:
 * <ul>
 * <li> the time when this event occured,
 * <li> an enumerated reason code,
 * <li> and a text comment.
 * </ul>
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class NothingCanBeScheduled {

	// An enumberation of reasons that nothing can be scheduled.
	
	/**
	 * There are no visible targets at this time.
	 */
	public static final int NoVisibleTargets 	= 0;
	/**
	 * The current weather conditions are not favorable.
	 */
	public static final int BadWeather 			= 1;
	/**
	 * There are not sufficient resources to execute anything.
	 */
	public static final int NoResources 		= 2;
	/**
	 * The only visible targets can be improved by waiting until a later time.
	 */
	public static final int BetterToWait		= 3;
	/**
	 * The computed scores are too low.
	 */
	public static final int LowScores			= 4;
	/**
	 * Some other condition not enumerated is present.
	 */
	public static final int Other				= 5;
	/**
	 * A text statement of the reasons why nothing can be scheduled.
	 */
	public static final String[] Message = {
			"There are no visible targets at this time.",
			"The current weather conditions are not favorable.",
			"There are not sufficient resources to execute anything.",
			"The only visible targets can be improved by waiting until a later time.",
			"The computed scores are too low.",
			"Some other condition not enumerated is present."
	};
	
	private DateTime time;
	private int reasonCode;
	private String comment;

	/**
	 * Construct an object containing data on when and why nothing could be scheduled.
	 * @param time	The time when nothing could be scheduled.
	 * @param reasoncode The reason code designating why nothing could be scheduled.
	 * @param code An additional comment about why nothing could be scheduled.
	 */
	public NothingCanBeScheduled(DateTime time, int reasonCode, String comment) {
		this.time = time;
		this.comment = comment;
		if (reasonCode >= 0 && reasonCode <= Other)
			this.reasonCode = reasonCode;
		else
			throw new IllegalArgumentException ("The specified reason (" + reasonCode + ") is not a valid reason code.");
	}

	/**
	 * Get an additional comment about why nothing could be scheduled.
	 * @return An additional comment about why nothing could be scheduled. 
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Get the reason code designating why nothing could be scheduled.
	 * @return The reason code designating why nothing could be scheduled.
	 */
	public int getReason() {
		return reasonCode;
	}

	/**
	 * Get the time when nothing could be scheduled.
	 * @return The time when nothing could be scheduled.
	 */
	public DateTime getTime() {
		return time;
	}
	
	public String toString() {
		return time.toString() + ": " + Message[reasonCode] + " " + comment;
	}

}
