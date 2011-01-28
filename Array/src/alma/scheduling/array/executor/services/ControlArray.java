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

import alma.asdmIDLTypes.IDLEntityRef;

/**
 * @author rhiriart
 *
 */
public interface ControlArray {
    /**
     * Set up the array for the given SchedBlock. This method will cope with being
     * invoked multiple times consecutively for the same SchedBlock, the array will
     * only be configured the once.
     */
    void configure(IDLEntityRef schedBlockRef) throws Exception;

    /**
     * Observe the given SchedBlock as part of the given Session. Multiple calls
     * with the same SchedBlock and Session will result in multiple observations.
     */
    void observe(IDLEntityRef schedBlockRef, IDLEntityRef sessionRef) throws Exception;

    /**
     * Stop the execution/observation of the current SchedBlock.
     */
    void stopSB() throws Exception;

    void cleanUp();
}
