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
 * File FlowControl.java
 */
 
package alma.scheduling.define;

/**
 * Flow control expressions are used to constrain the order in which members of 
 * an SUnitSet are executed.  If there are no flow control expressions, the members 
 * are executed in any order.  
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class FlowControl {
	
	private int unitName;
	private Expression condition;
	private int[] dependentUnits;
	private TimeInterval waitTime;

	/**
	 * Create a flow control expression.
	 */
	public FlowControl() {
	}

	/**
	 * @return
	 */
	public Expression getCondition() {
		return condition;
	}

	/**
	 * @return
	 */
	public int[] getDependentUnits() {
		return dependentUnits;
	}

	/**
	 * @return
	 */
	public int getUnitName() {
		return unitName;
	}

	/**
	 * @return
	 */
	public TimeInterval getWaitTime() {
		return waitTime;
	}

	/**
	 * @param expression
	 */
	public void setCondition(Expression expression) {
		condition = expression;
	}

	/**
	 * @param is
	 */
	public void setDependentUnits(int[] is) {
		dependentUnits = is;
	}

	/**
	 * @param i
	 */
	public void setUnitName(int i) {
		unitName = i;
	}

	/**
	 * @param interval
	 */
	public void setWaitTime(TimeInterval interval) {
		waitTime = interval;
	}

}
