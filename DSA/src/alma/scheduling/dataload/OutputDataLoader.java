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
package alma.scheduling.dataload;

import alma.scheduling.datamodel.output.dao.OutputDao;

public class OutputDataLoader implements DataLoader {

    private OutputDao outDao;
    
    public OutputDao getOutDao() {
        return outDao;
    }

    public void setOutDao(OutputDao outDao) {
        this.outDao = outDao;
    }

    @Override
    public void clear() {
        outDao.deleteAll();
    }

    @Override
    public void load() throws Exception {
        // Do Nothing.
    }

}
