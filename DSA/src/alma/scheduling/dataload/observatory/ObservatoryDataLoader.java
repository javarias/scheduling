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
 * "@(#) $Id: ObservatoryDataLoader.java,v 1.2 2010/03/13 02:56:15 rhiriart Exp $"
 */
package alma.scheduling.dataload.observatory;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.observatory.AntennaInstallation;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.TelescopeEquipment;
import alma.scheduling.datamodel.observatory.dao.ObservatoryDao;
import alma.scheduling.datamodel.observatory.dao.XmlObservatoryDao;

@Transactional
public class ObservatoryDataLoader implements DataLoader {
    
    XmlObservatoryDao xmlDao;
    public void setXmlDao(XmlObservatoryDao xmlDao) {
        this.xmlDao = xmlDao;
    }

    ObservatoryDao dao;
    public void setDao(ObservatoryDao dao) {
        this.dao = dao;
    }

    @Override
    public void load() {
        List<TelescopeEquipment> equipments = xmlDao.getAllEquipments();
        dao.saveOrUpdate(equipments);
        List<ArrayConfiguration> arrConfigs = xmlDao.getAllArrayConfigurations();
        dao.saveOrUpdate(arrConfigs);
    }

    @Override
    public void clear() {
        // not very efficient, replace later for a delete SQL command in the DAO
        dao.deleteAll(dao.findAll(AntennaInstallation.class));
        dao.deleteAll(dao.findAll(ArrayConfiguration.class));
        dao.deleteAll(dao.findAll(TelescopeEquipment.class));
    }

}
