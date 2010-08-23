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
public class Services {
    
    private Services() {};
    
    private static Provider provider;
    
    public static void registerProvider(Provider p) {
        provider = p;
    }
    
    public static ControlArray getControlArray() {
        if (provider == null)
            throw new IllegalStateException("Provider has not been initialized");
        return provider.getControlArray();
    }
    
    public static Pipeline getPipeline() {
        if (provider == null)
            throw new IllegalStateException("Provider has not been initialized");
        return provider.getPipeline();
    }
    
    public static EventPublisher getEventPublisher() {
        if (provider == null)
            throw new IllegalStateException("Provider has not been initialized");
        return provider.getEventPublisher();
    }
    
    public static ModelAccessor getModel() {
        if (provider == null)
            throw new IllegalStateException("Provider has not been initialized");
        return provider.getModel();        
    }
    
    public static void cleanUp() {
        if (provider != null)
            provider.cleanUp();
    }
}
