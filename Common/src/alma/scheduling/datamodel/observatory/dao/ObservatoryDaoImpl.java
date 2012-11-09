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
package alma.scheduling.datamodel.observatory.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.observatory.AntennaInstallation;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;

@Transactional
public class ObservatoryDaoImpl extends GenericDaoImpl implements ObservatoryDao {

    @Override
    public List<ArrayConfiguration> findArrayConfigurations() {
        // hydrate the antenna installations
        List<ArrayConfiguration> arrCnfs = findAll(ArrayConfiguration.class);
        for (Iterator<ArrayConfiguration> iter = arrCnfs.iterator(); iter.hasNext();) {
            ArrayConfiguration ac = iter.next();
            Set<AntennaInstallation> ais = ac.getAntennaInstallations();
            for (Iterator<AntennaInstallation> iter2 = ais.iterator(); iter2.hasNext();) {
                AntennaInstallation ai = iter2.next();
                ai.getAntenna().getDiameter();
                ai.getPad();
            }
        }
        return arrCnfs;
    }
    
}
