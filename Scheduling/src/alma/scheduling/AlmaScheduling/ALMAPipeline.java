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
 * File ALMAPipeline.java
 */

package alma.scheduling.AlmaScheduling;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.logging.AcsLogger;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.pipelineql.QlDisplayManager;
import alma.pipelinescience.SciPipeManager;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.SciPipeline;
import alma.scheduling.Define.SciPipelineRequest;
import alma.scheduling.Define.Status;

/**
 * This class communicates with the Science Pipeline Subsystem
 * @author Sohaila Lucero
 * @version $Id: ALMAPipeline.java,v 1.20 2008/09/03 22:01:07 wlin Exp $
 */
public class ALMAPipeline implements SciPipeline {
    //container services
    private ContainerServices containerServices;
    //logger
    private final AcsLogger logger;
    //science pipeline component
    private SciPipeManager sci_pipelineComp;
    //quicklook display component
    private QlDisplayManager quicklookComp;
    //entity serializer
    private EntitySerializer entitySerializer;

    private boolean sci_pipelineAvailable=false;
    private boolean ql_startOk=false;
    
    /**
      *
      */
    public ALMAPipeline(ContainerServices cs) {
        this.containerServices = cs;
        this.logger = cs.getLogger();
        getPipelineComponents();
        entitySerializer = EntitySerializer.getEntitySerializer(cs.getLogger());
    }

    /**
     * Connect to the pipeline components
     */
    private void getPipelineComponents() {
        logger.finest("About to connect to Science Pipeline Component");
        try {
            sci_pipelineComp = alma.pipelinescience.SciPipeManagerHelper.narrow(
                containerServices.getDefaultComponent("IDL:alma/pipelinescience/SciPipeManager:1.0"));
            
            sci_pipelineAvailable = true;
        } catch(AcsJContainerServicesEx e) {
            logger.severe("SCHEDULING: Science Pipeline Component is not available.");
            sci_pipelineAvailable = false;
            sci_pipelineComp = null;
        }
        logger.finest("About to connect to QuickLook Pipeline Component");
        try {
            quicklookComp = alma.pipelineql.QlDisplayManagerHelper.narrow(
                containerServices.getDefaultComponent("IDL:alma/pipelineql/QlDisplayManager:1.0"));
        } catch(AcsJContainerServicesEx e){
            logger.severe("SCHEDULING: QuickLook Pipeline Component is not available.");
            ql_startOk = false;
            quicklookComp = null;
        }
    }

    /**
      * Release pipeline comp
      */
    public void releasePipelineComp() {
        if(sci_pipelineAvailable) {
            try {
                containerServices.releaseComponent(sci_pipelineComp.name());
            }catch(Exception e) {
                logger.severe("SCHEDULING: error releasing science pipeline comp.");
                e.printStackTrace(System.out);
            }
        }
        try {
            containerServices.releaseComponent(quicklookComp.name());
        } catch(Exception e) {
            logger.severe("SCHEDULING: error releasing quicklook pipeline comp.");
            e.printStackTrace(System.out);
        }
    }


    /**
     * Send the pipeline processing request to the pipeline subsystem.
     * @param ppr The xml string which represents the Pipeline Processing Request in the project status
     * @return String The result from the pipeline subsystem
     */
    public String processRequest(String ppr) {
        String requestResult = null;
        try { 
            requestResult = sci_pipelineComp.processRequest(ppr);
            logger.finest("SCHEDULING: result returned from Pipeline = "+ requestResult);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Error sending request to pipeline");
            e.printStackTrace(System.out);
        }
        return  requestResult;
    }

    /**
      * @param SciPipelineRequest
      * @throws SchedulingException
      */
    public void start(SciPipelineRequest ppr) throws SchedulingException {
    }
    /**
      * @param String
      * @throws SchedulingException
      */
    public void start(String pprString) throws SchedulingException {
        
        logger.finest("SCHEDULING: Starting the science pipeline");
    }
    /**
      * @param SciPipelineRequest
      * @return Status
      */
    public Status getStatus(SciPipelineRequest ppr) 
        throws SchedulingException {

        return null;
    }

    public boolean isPipelineAvailable() {
        return sci_pipelineAvailable;
    }

    //////////////////////////////////////////////
    // Quicklook operations
    //////////////////////////////////////////////
    // this method will be replace by the underneath.
    
    public void startQuickLookSession(IDLEntityRef sessionR,
                                      IDLEntityRef sbR,
                                      String title){
        try {
            //quicklookComp.startQlSession(sessionR, sbR, title);
        	quicklookComp.startQlSession(sessionR, sbR, "arrayname",title);
            logger.fine("SCHEDULING: Told QL session is about to start");
            ql_startOk = true;
        }catch(alma.QlDisplayExceptions.InvalidStateErrorEx e) {
            logger.warning("SCHEDULING: Caught quicklook error when session starts, should keep going with sb session");
            ql_startOk = false;
        }
    }
    
    public void startQuickLookSession(IDLEntityRef sessionR,
            IDLEntityRef sbR, String arrayname,
            String title){
    	try {
    		quicklookComp.startQlSession(sessionR, sbR, arrayname,title);
    		logger.fine("SCHEDULING: Told QL session is about to start");
    		ql_startOk = true;
    	}catch(alma.QlDisplayExceptions.InvalidStateErrorEx e) {
    		logger.warning("SCHEDULING: Caught quicklook error when session starts, should keep going with sb session");
    		ql_startOk = false;
    	}
    }
    
    public void endQuickLookSession(IDLEntityRef sessionR, 
                                    IDLEntityRef sbR) {
        if(ql_startOk){
            try {
                quicklookComp.endQlSession(sessionR, sbR);
                logger.fine("SCHEDULING: Told QL session is about to end");
            }catch(alma.QlDisplayExceptions.InvalidStateErrorEx e) {
                logger.warning("SCHEDULING: Caught quicklook error when session ends");
            }
        }
    }
}


