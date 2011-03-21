/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.array.executor.services;

import java.util.logging.Logger;

import alma.acs.container.ContainerServices;
import alma.acs.exceptions.AcsJException;
import alma.scheduling.array.util.NameTranslator.TranslationException;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;

/**
 * @author rhiriart
 *
 */
public class AcsProvider implements Provider {

    private ContainerServices container;
    
    private Logger logger;
    
    private String arrayName;
    
    private boolean isManual;
    
    private ControlArray controlArray;
    
    private Pipeline pipeline;
    
    private AcsNotificationChannel schedulingNotificationChannel;

    private AcsNotificationChannel controlNotificationChannel;

    private ModelAccessor model;
        
    public AcsProvider(ContainerServices container, String arrayName, boolean isManual)
        throws TranslationException, AcsJException {
        this.container = container;
        this.logger = container.getLogger();
        this.arrayName = arrayName;
        this.isManual = isManual;
        
        if (isManual) {
            this.controlArray = new ControlManualArrayImpl(container, arrayName);
        } else {
            this.controlArray = new ControlAutomaticArrayImpl(container, arrayName);
        }

        try {
            this.model = new ModelAccessor();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.schedulingNotificationChannel =
            new AcsNotificationChannel(container, alma.scheduling.CHANNELNAME_SCHEDULING.value);
        this.controlNotificationChannel =
            new AcsNotificationChannel(container, "CONTROL_SYSTEM");
    }

    @Override
    public ControlArray getControlArray() {
        return controlArray;
    }

    @Override
    public Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public EventPublisher getEventPublisher() {
        return schedulingNotificationChannel;
    }
    
    @Override
    public ModelAccessor getModel() {
        return model;
    }

    @Override
    public void cleanUp() {
        controlArray.cleanUp();
        try {
            schedulingNotificationChannel.cleanUp();
        } catch (AcsJException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public EventReceiver getEventReceiver() {
        return schedulingNotificationChannel;
    }

    @Override
    public EventReceiver getControlEventReceiver() {
        return controlNotificationChannel;
    }
}
