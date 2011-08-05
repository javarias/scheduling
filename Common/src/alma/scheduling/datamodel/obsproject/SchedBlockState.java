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
 * "@(#) $Id: SchedBlockState.java,v 1.4 2011/08/05 21:41:23 dclarke Exp $"
 */
package alma.scheduling.datamodel.obsproject;

import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.scheduling.datamodel.helpers.ConversionException;

public enum SchedBlockState {
    READY,
    RUNNING,
    FULLY_OBSERVED,
    CANCELED;
    
    public static SchedBlockState getFrom(StatusTStateType apdmStatus)
    	throws ConversionException {
    	switch (apdmStatus.getType()) {
    	case StatusTStateType.READY_TYPE:
    	case StatusTStateType.PHASE1SUBMITTED_TYPE:
    	case StatusTStateType.PHASE2SUBMITTED_TYPE:
    	case StatusTStateType.CSVREADY_TYPE:
    		return SchedBlockState.READY;
    	case StatusTStateType.RUNNING_TYPE:
    	case StatusTStateType.CSVRUNNING_TYPE:
    		return SchedBlockState.RUNNING;
    	case StatusTStateType.FULLYOBSERVED_TYPE:
    		return SchedBlockState.FULLY_OBSERVED;
    	default:
    		throw new ConversionException(String.format(
    				"Unexpected APDM state (%s) - don't know how to interpret this for Scheduling",
    				apdmStatus.toString()));
    	}
    }
}
