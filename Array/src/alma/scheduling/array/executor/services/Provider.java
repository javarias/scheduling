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

import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;

/**
 * @author rhiriart
 *
 */
public interface Provider {
    ControlArray getControlArray();
    Pipeline getPipeline();
    EventPublisher getEventPublisher();
    EventReceiver getEventReceiver();
    EventReceiver getControlEventReceiver();
    ModelAccessor getModel();
    void cleanUp();
}
