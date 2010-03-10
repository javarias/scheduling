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
 * "@(#) $Id: Receiver.java,v 1.1 2010/03/10 22:53:00 rhiriart Exp $"
 */
package alma.scheduling.datamodel.observatory;

import java.util.Date;

public class Receiver extends TelescopeEquipment {
    
    private ReceiverBand band;
    
    public Receiver() {
        super();
    }
    
    public Receiver(String name, Date commissionDate, ReceiverBand band) {
        super(name, AssemblyGroupType.RECEIVER, commissionDate);
        this.band = band;
    }

    public ReceiverBand getBand() {
        return band;
    }

    public void setBand(ReceiverBand band) {
        this.band = band;
    }

    public double[] getFrequencyRange() {
        if (band == null)
            throw new NullPointerException("band is null");
        double[] retVal = null;
        if (band == ReceiverBand.BAND_1) {
            retVal = new double[] {31.3, 45.0};
        } else if (band == ReceiverBand.BAND_2) {
            retVal = new double[] {67.0, 90.0};
        } else if (band == ReceiverBand.BAND_3) {
            retVal = new double[] {84.0, 116.0};
        } else if (band == ReceiverBand.BAND_4) {
            retVal = new double[] {125.0, 163.0};
        } else if (band == ReceiverBand.BAND_5) {
            retVal = new double[] {163.0, 211.0};
        } else if (band == ReceiverBand.BAND_6) {
            retVal = new double[] {211.0, 275.0};
        } else if (band == ReceiverBand.BAND_7) {
            retVal = new double[] {275.0, 373.0};
        } else if (band == ReceiverBand.BAND_8) {
            retVal = new double[] {385.0, 500.0};
        } else if (band == ReceiverBand.BAND_9) {
            retVal = new double[] {602.0, 720.0};
        } else if (band == ReceiverBand.BAND_10) {
            retVal = new double[] {787.0, 950.0};
        }
        return retVal;
    }
}
