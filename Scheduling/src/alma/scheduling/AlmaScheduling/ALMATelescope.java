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
 * File ALMATelescope.java
 */

package alma.scheduling.AlmaScheduling;

import alma.scheduling.Define.Telescope;
import alma.scheduling.Define.SiteCharacteristics;
import alma.scheduling.Define.FrequencyBand;
import alma.scheduling.Define.Antenna;
import alma.scheduling.Define.Subarray;

/**
 * @author Sohaila Lucero
 */
public class ALMATelescope extends Telescope  {
    public ALMATelescope () {
        super();
        setupSiteInfo();
    }

    /**
     * 
     */
    private void setupSiteInfo() {
        FrequencyBand[] freq = new FrequencyBand[1];
        freq[0] = new FrequencyBand("1", 31.3, 45.0);
        SiteCharacteristics site = new SiteCharacteristics(
            107.6177275,34.0787491666667, -6, 2124.0, 8.0, 64, freq);
        super.setSite(site);
        Antenna[] antennas = new Antenna[64];
        for(int i = 0; i < 64; i++) {
            antennas[i] = new Antenna(""+i+"", i, false);
        }
        super.addSubarray(new Subarray((short)0,antennas)); 

    }


}
