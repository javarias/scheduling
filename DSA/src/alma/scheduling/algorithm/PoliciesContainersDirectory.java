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
package alma.scheduling.algorithm;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import alma.scheduling.SchedulingPolicyFile;
import alma.scheduling.utils.DynamicSchedulingPolicyFactory;

/**
 * 
 * Directory to manage available Scheduling Policies. The management consider a simple
 * hierarchy of only one level: Files(Containers) contain Scheduling Policies.
 * 
 * This class is a singleton. To get the instance use {@code PoliciesContainersDirectory#getInstance()}
 * 
 * @since ALMA-9.0.0
 * @author Jorge Avarias
 *
 */
public class PoliciesContainersDirectory extends ConcurrentHashMap<UUID, PoliciesContainer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8570479907666564035L;
	private static final PoliciesContainersDirectory instance = new PoliciesContainersDirectory();
	
	private PoliciesContainersDirectory() {
	}
	
	/**
	 * Converts the Scheduling Policies container into CORBA struct data type.
	 * 
	 * @param container 
	 * @return the converted container into CORBA struct.
	 * @throws IllegalArgumentException if the container is null or if the container doesn't
	 * contain scheduling policies
	 */
	static SchedulingPolicyFile convertTo(PoliciesContainer container){
		if (container == null)
			throw new IllegalArgumentException("Policies container cannot be null");
		else if (container.getPolicies().size() == 0)
			throw new IllegalArgumentException("Policies container must contain at least one Scheduling Policy");
		SchedulingPolicyFile corbaStruct = new SchedulingPolicyFile();
		corbaStruct.uuid = container.getUuid().toString();
		corbaStruct.hostname = container.getHostname();
		corbaStruct.path = container.getPath();
		corbaStruct.locked = container.isLocked();
		corbaStruct.schedulingPolicies = container.getPoliciesAsArray();
		return corbaStruct;
	}
	
	/**
	 * @return The instance of this class
	 */
	public static PoliciesContainersDirectory getInstance() {
		return instance;
	}

	/**
	 * @throws PoliciesContainerLockedException if the Policies container to be removed is locked
	 * @throws UnexpectedException if there is a problem with the deletion of the spring beans
	 */
	@Override
	public PoliciesContainer remove(Object key) {
		synchronized (this) {
			if (this.get(key).isLocked()) {
				throw new PoliciesContainerLockedException(this.get(key)
						.getUuid());
			}
			try {
				DynamicSchedulingPolicyFactory.getInstance().removePolicies(this.get(key));
			} catch (NoSuchBeanDefinitionException ex) {
				throw new UnexpectedException(this.get(key).getUuid(), ex);
			} catch (Exception ex) {
				throw new UnexpectedException(this.get(key).getUuid(), ex);
			}
			return super.remove(key);
		}
	}
	
	/**
	 * @throws IllegalArgumentException if the Policy container doesn't exist
	 * @param key
	 */
	public void lockPolicyContainer(UUID key) {
		synchronized (this) {
			if (this.get(key) == null)
				throw new IllegalArgumentException("Policy container UUID:" + key.toString() + " doesn't exist.");
			this.get(key).lock();
		}
	}
	
	/**
	 * @throws IllegalArgumentException if the Policy container doesn't exist
	 * @param key
	 */
	public void unlockPolicyContainer(UUID key) {
		synchronized (this) {
			if (this.get(key) == null)
				throw new IllegalArgumentException("Policy container UUID:" + key.toString() + " doesn't exist.");
			this.get(key).unlock();
		}
	}
	
	public SchedulingPolicyFile[] getAllPoliciesFiles() {
		SchedulingPolicyFile[] retVal = new SchedulingPolicyFile[this.size()];
		int i = 0;
		for (UUID uuid: this.keySet()) {
			SchedulingPolicyFile file = convertTo(this.get(uuid));
			retVal[i++] = file;
		}
		return retVal;
	}

	public class PoliciesContainerLockedException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6506424755783855552L;
		
		
		public PoliciesContainerLockedException(UUID containerUuid) {
			super("Policies container UUID: " + containerUuid.toString() + " is locked and it cannot be deleted");
		}
	}
	
	public class UnexpectedException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6506424755783855552L;
		
		public UnexpectedException(UUID containerUuid) {
			super("Policies in container UUID: " + containerUuid.toString() + 
					" cannot be deleted is not in the directory");
		}
		
		public UnexpectedException(UUID containerUuid, Throwable cause) {
			super("Policies in container UUID: " + containerUuid.toString() + 
					" cannot be deleted" , cause);
		}
	}
}
