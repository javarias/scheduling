/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * All rights reserved
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File ALMANothingCanBeScheduledSupplier.java
 * 
 */

package alma.scheduling.project_manager;

import alma.scheduling.NothingCanBeScheduledEvent;
/*
import ALMA.acsnc.*;
import org.omg.CosNotification.StructuredEvent;
*/
import alma.acs.nc.SimpleSupplier;
/**
 *  I should probabaly put this as a component!
 */
public class ALMANothingCanBeScheduledSupplier {
    private SimpleSupplier simpleSupplier;

    public ALMANothingCanBeScheduledSupplier() {
        String[] names = new String[3];
        names[SimpleSupplier.CHANNELPOS] = alma.scheduling.CHANNELNAME.value;
        names[SimpleSupplier.TYPEPOS] = ALMA.acsnc.DEFAULTTYPE.value;
        names[SimpleSupplier.HELPERPOS] = new 
                String("alma.scheduling.NothingCanBeScheduledEventHelper");
        simpleSupplier = new SimpleSupplier(names);
    }

    public void sendEvent(String reason) {
        try {
            NothingCanBeScheduledEvent scbs = new
                NothingCanBeScheduledEvent(reason);
            simpleSupplier.publishEvent(scbs);
        } catch(Exception e) {
            //log error!
        }
    }
    
    
    
    public static void main(String[] args) {
    }
}
