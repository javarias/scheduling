/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.master.gui;

import alma.scheduling.ArrayEvent;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArrayStatusCallbackPOA;

public class ArrayStatusCallbackImpl extends ArrayStatusCallbackPOA {

	private ArrayStatusListener listener;
	
	public ArrayStatusCallbackImpl (ArrayStatusListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("ArrayStatusListener cannot be null");
		this.listener = listener;
	}

	@Override
	public void report(ArrayEvent operation, ArrayModeEnum arrayMode,
			String arrayName) {
		if (operation.value() == ArrayEvent._CREATION) 
			listener.notifyArrayCreation(arrayName, arrayMode);
		else if (operation.value() == ArrayEvent._DESTRUCTION)
			listener.notifyArrayDestruction(arrayName);
	}
	
}
