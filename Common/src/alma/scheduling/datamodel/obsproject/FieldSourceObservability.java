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
 * "@(#) $Id: FieldSourceObservability.java,v 1.3 2010/03/09 16:38:16 rhiriart Exp $"
 */
package alma.scheduling.datamodel.obsproject;

import java.util.Date;

import alma.scheduling.datamodel.Updateable;

public class FieldSourceObservability implements Updateable {

    /** The source could be circunpolar */
    private Boolean alwaysVisible;
    
    /** The source could be hidden */
    private Boolean alwaysHidden;
    
    /** Direction of source appareance (degrees) */
    private Double azimuthRising;
    
    /** Direction of source disappareance (degrees) */
    private Double azimuthSetting;
    
    /** Rising time (UTC decimal hours) */
    private Double risingTime;
    
    /** Setting time (UTC decimal hours) */
    private Double settingTime;
    
    private Date lastUpdate;
    
    private Date validUntil;

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Double getAzimuthRising() {
        return azimuthRising;
    }

    public void setAzimuthRising(Double azimuthRising) {
        this.azimuthRising = azimuthRising;
    }

    public Double getAzimuthSetting() {
        return azimuthSetting;
    }

    public void setAzimuthSetting(Double azimuthSetting) {
        this.azimuthSetting = azimuthSetting;
    }

    public Double getRisingTime() {
        return risingTime;
    }

    public void setRisingTime(Double risingTime) {
        this.risingTime = risingTime;
    }

    public Double getSettingTime() {
        return settingTime;
    }

    public void setSettingTime(Double settingTime) {
        this.settingTime = settingTime;
    }
    
}
