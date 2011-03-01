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

import alma.Control.ManualArrayCommand;
import alma.Control.ManualArrayCommandHelper;
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

	private final ManualArrayCommand array;
	private String lastSB;

	public ControlManualArrayImpl(ContainerServices container,
			                      String arrayName)
				throws AcsJContainerServicesEx, TranslationException {
		this.container = container;
		this.arrayName = arrayName;
		array = ManualArrayCommandHelper.narrow(
				container.getComponentNonSticky(NameTranslator.arrayToControlComponentName(arrayName)));
		this.lastSB = "";
	}

	@Override
	public void configure(IDLEntityRef schedBlockRef) 
												throws Exception {
		if (!schedBlockRef.entityId.equals(lastSB)) {
			array.configure(schedBlockRef);
			lastSB = schedBlockRef.entityId;
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
		try {
			container.releaseComponent(NameTranslator
					.arrayToControlComponentName(arrayName));
		} catch (TranslationException e) {
			e.printStackTrace();
		}
	}

}
