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
 * "@(#) $Id: FieldSourceObservabilityUpdater.java,v 1.2 2010/04/16 20:59:49 javarias Exp $"
 */
package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.AlgorithmPart;
import alma.scheduling.algorithm.astro.CoordinatesUtil;
import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.FieldSourceObservability;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.FieldSourceDao;

public class FieldSourceObservabilityUpdater implements ModelUpdater, AlgorithmPart {

    private static Logger logger = LoggerFactory.getLogger(FieldSourceObservabilityUpdater.class);
    
    private boolean hasUpdated = false;
    
    // --- Spring set properties and accessors ---
    
    private ConfigurationDao configDao;
    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }

    private FieldSourceDao sourceDao;
    public void setSourceDao(FieldSourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }
    
    private List<AlgorithmPart> dependencies;
    public void setAlgorithmPart(List<AlgorithmPart> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Zero-args constructor.
     */
    public FieldSourceObservabilityUpdater() {}
    
    // --- AlgorithmPart interface implementation ---
    
    @Override
    public List<AlgorithmPart> getAlgorithmDependencies() {
        return dependencies;
    }

    @Override
    public void execute(Date ut) {
        if (dependencies != null) {
            for (Iterator<AlgorithmPart> iter = dependencies.iterator(); iter.hasNext();) {
                iter.next().execute(ut);
            }
        }
        update(ut);
    }    
    
    // --- ModelUpdater interface implementation ---
    
    @Override
    public boolean needsToUpdate(Date date) {
        // As the rising and setting times in the FieldSourceObservability are kept
        // in LST, this updater only needs to be run once, in the beginning.
        return !hasUpdated;
    }

    @Override
    public void update(Date date) {
        logger.trace("updating for time " + date);
        
        double latitude = configDao.getConfiguration().getArrayCenterLatitude();
        double longitude = configDao.getConfiguration().getArrayCenterLongitude();
        
        List<FieldSource> sources = sourceDao.findAll(FieldSource.class);
        for (Iterator<FieldSource> iter = sources.iterator(); iter.hasNext();) {
            FieldSource src = iter.next();
            logger.debug("src name: " + src.getName());
            FieldSourceObservability srcObs =
                CoordinatesUtil.getRisingAndSettingParameters(src.getCoordinates(), latitude, longitude);
            src.setObservability(srcObs);
            srcObs.setLastUpdate(date);
            srcObs.setValidUntil(null);
            sourceDao.saveOrUpdate(src);
        }
        hasUpdated = true;
    }

    @Override
    public void update(Date date, Collection<SchedBlock> sbs) {
        update(date);
        
    }

    // --- Internal functions ---
    
}
