/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2006 
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
package alma.scheduling.algorithm;

import alma.scheduling.SchedulingPolicyFile;
import alma.scheduling.algorithm.PoliciesContainersDirectory.UnexpectedException;
import junit.framework.TestCase;

public class PoliciesContainerDirectoryTests extends TestCase {

	private PoliciesContainer sysCont = null;
	private PoliciesContainer cont = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sysCont = new PoliciesContainer("localhost", "system", true);
		sysCont.getPolicies().add("Policy0");
		sysCont.getPolicies().add("Policy1");
		cont = new PoliciesContainer("localhost", "/my/policy/dir");
		cont.getPolicies().add("Policy0");
		cont.getPolicies().add("Policy1");
		PoliciesContainersDirectory.getInstance().put(sysCont.getUuid(),
				sysCont);
		PoliciesContainersDirectory.getInstance().put(cont.getUuid(), cont);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		PoliciesContainersDirectory.getInstance().clear();
	}

	public void testConvertToCORBAStruct() {
		SchedulingPolicyFile policyFile = PoliciesContainersDirectory
				.convertTo(sysCont);
		assertEquals("localhost", policyFile.hostname);
		assertEquals("system", policyFile.path);
		assertEquals(true, policyFile.locked);
		assertEquals(2, policyFile.schedulingPolicies.length);
		assertEquals("Policy0", policyFile.schedulingPolicies[0]);
		assertEquals("Policy1", policyFile.schedulingPolicies[1]);

		policyFile = PoliciesContainersDirectory.convertTo(cont);
		assertEquals("localhost", policyFile.hostname);
		assertEquals("/my/policy/dir", policyFile.path);
		assertEquals(false, policyFile.locked);
		assertEquals(2, policyFile.schedulingPolicies.length);
		assertEquals("Policy0", policyFile.schedulingPolicies[0]);
		assertEquals("Policy1", policyFile.schedulingPolicies[1]);
		cont.lock();
		policyFile = PoliciesContainersDirectory.convertTo(cont);
		assertEquals(true, policyFile.locked);
		cont.unlock();
		policyFile = PoliciesContainersDirectory.convertTo(cont);
		cont.lock();
		cont.lock();
		cont.unlock();
		policyFile = PoliciesContainersDirectory.convertTo(cont);
		assertEquals(true, policyFile.locked);
		cont.unlock();

		PoliciesContainersDirectory.getInstance().getAllPoliciesFiles();
	}

	public void testRemovingPolicy() {
		try {
			PoliciesContainersDirectory.getInstance().remove(sysCont.getUuid());
		} catch (PoliciesContainersDirectory.PoliciesContainerLockedException ex) {
			System.out
					.println("Tried to remove system policy, this is invalid");
		}

		try {
			PoliciesContainersDirectory.getInstance().remove(cont.getUuid());
		} catch (UnexpectedException ex) {
			// This is ok. No spring context has been initialized
			PoliciesContainersDirectory.getInstance().clear();
		}
		try {
			PoliciesContainersDirectory.getInstance().remove(cont.getUuid());
		} catch (NullPointerException ex) {
			System.out
					.println("Tried to remove twice a policy, this is invalid");
		}
	}

	public void testConcurrency() {
		Runnable r1 = new Runnable() {
			@Override
			public void run() {
				PoliciesContainersDirectory.getInstance().lockPolicyContainer(
						cont.getUuid());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				PoliciesContainersDirectory.getInstance()
						.unlockPolicyContainer(cont.getUuid());
				try {
					PoliciesContainersDirectory.getInstance().remove(
							cont.getUuid());
				} catch (PoliciesContainersDirectory.PoliciesContainerLockedException ex) {
					System.out
							.println("Tried to remove a locked policy. Another thread locked the policy");
				}

			}
		};

		Runnable r2 = new Runnable() {
			@Override
			public void run() {
				PoliciesContainersDirectory.getInstance().lockPolicyContainer(
						cont.getUuid());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				PoliciesContainersDirectory.getInstance()
						.unlockPolicyContainer(cont.getUuid());
				try {
					PoliciesContainersDirectory.getInstance().remove(
							cont.getUuid());
				} catch (UnexpectedException ex) {

				}
			}
		};
		Thread t1 = new Thread(r1);
		Thread t2 = new Thread(r2);
		t1.start();
		t2.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
		}
		try {
			t2.join();
		} catch (InterruptedException e) {
		}
		PoliciesContainersDirectory.getInstance().clear();
		try {
			PoliciesContainersDirectory.getInstance().lockPolicyContainer(
					cont.getUuid());
		} catch (IllegalArgumentException ex) {
			System.out.println("Try to lock a non existant policy");
		}
		try {
			PoliciesContainersDirectory.getInstance().unlockPolicyContainer(
					cont.getUuid());
		} catch (IllegalArgumentException ex) {
			System.out.println("Try to unlock a non existant policy");
		}
	}
}
