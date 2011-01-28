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

import alma.Control.ManualArray;
import alma.Control.ManualArrayHelper;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.scheduling.array.util.NameTranslator;
import alma.scheduling.array.util.NameTranslator.TranslationException;

/**
 * @author rhiriart
 *
 */
public class ControlManualArrayImpl implements ControlArray {

    private final ContainerServices container;
    
    private final String arrayName;
    
    private final ManualArray array;
    private String lastSB;
    
    public ControlManualArrayImpl(ContainerServices container, String arrayName)
        throws AcsJContainerServicesEx, TranslationException {
        this.container = container;
        this.arrayName = arrayName;
        this.array = ManualArrayHelper
            .narrow(container.getComponent(NameTranslator.arrayToControlComponentName(arrayName)));
	this.lastSB = "";
    }

    private String hashes(int n) {
	final StringBuilder s = new StringBuilder();
	while (n-- > 0) {
	    s.append('#');
	}
	return s.toString();
    }

    private void shout(String s) {
	final String h = hashes(s.length() + 6);
	System.out.format("%n   %s%n   ## %s ##%n   %s%n%n",
			  h, s, h);

    }

    @Override
    public void configure(IDLEntityRef schedBlockRef)
        throws Exception {
	if (!schedBlockRef.entityId.equals(lastSB)) {
	    shout(String.format("%s.configure(%s) - new SB (versus %s)",
				this.getClass().getSimpleName(),
				schedBlockRef.entityId,
				lastSB));
	    array.configure(schedBlockRef);
	    lastSB = schedBlockRef.entityId;
	} else {
	    shout(String.format("%s.configure(%s) - repeat SB (versus %s)",
				this.getClass().getSimpleName(),
				schedBlockRef.entityId,
				lastSB));
	}
    }
    
    @Override
    public void observe(IDLEntityRef schedBlockRef, IDLEntityRef sessionRef)
        throws Exception {
	// Do nothing, manual observing is not controlled from here
    }
   
    @Override    
    public void stopSB() throws Exception {
    }

    @Override
    public void cleanUp() {
	shout(String.format("%s.cleanUp()", this.getClass().getSimpleName()));
        container.releaseComponent(array.getName());
    }
    
}
