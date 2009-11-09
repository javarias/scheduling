/*
 * ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2005 
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
 *
 * $Id: StatusProxyTest.java,v 1.2 2009/11/09 23:13:27 rhiriart Exp $
 */

package alma.scheduling.inttest;

import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import alma.ACS.MasterComponent;
import alma.ACS.MasterComponentHelper;
import alma.ACS.ROstringSeq;
import alma.ACS.MasterComponentPackage.SubsystemStateEvent;
import alma.ACSErr.Completion;
import alma.ACSErr.CompletionHolder;
import alma.ACSSim.Simulator;
import alma.ACSSim.SimulatorHelper;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.projectstatus.StatusBaseT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.projectlifecycle.StateSystem;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
import alma.scheduling.AlmaScheduling.statusIF.StatusBaseI;
import alma.scheduling.AlmaScheduling.statusImpl.CachedStatusFactory;
import alma.scheduling.AlmaScheduling.statusImpl.RemoteStatusFactory;
import alma.scheduling.Define.SchedulingException;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;

/**
 * Template for Scheduling tests.
 * 
 */
public class StatusProxyTest extends ComponentClientTestCase {

	public abstract class ProxyFactory<Proxy extends StatusBaseI> {
		public abstract Proxy makeProxy(final String uid);
	}

	public abstract class ProxyGet<Proxy extends StatusBaseI> {
		public abstract String getField(Proxy proxy);
		public abstract void   setField(Proxy proxy, String value);
	}

	public abstract class StatusGet<Status extends StatusBaseT> {
		public abstract String getField(Status status);
		public abstract void   setField(Status status, String value);
	}

	final private StatusGet<ProjectStatus> projectStatusGet = new StatusGet<ProjectStatus>(){
		@Override
		public String getField(ProjectStatus status) {
			return status.getPI();
		}
		@Override
		public void setField(ProjectStatus status, String value) {
			status.setPI(value);
		}};
	final private ProxyGet<ProjectStatusI> projectProxyGet = new ProxyGet<ProjectStatusI>(){
		@Override
		public String getField(ProjectStatusI proxy) {
			return proxy.getPI();
		}
		@Override
		public void setField(ProjectStatusI proxy, String value) {
			proxy.setPI(value);
		}};
   	final StatusGet<OUSStatus> ousStatusGet = new StatusGet<OUSStatus>(){
   		@Override
   		public String getField(OUSStatus status) {
   			return status.getTimeOfUpdate();
		}
		@Override
		public void setField(OUSStatus status, String value) {
			status.setTimeOfUpdate(value);
   		}};
	final ProxyGet<OUSStatusI> ousProxyGet = new ProxyGet<OUSStatusI>(){
		@Override
		public String getField(OUSStatusI proxy) {
			return proxy.getTimeOfUpdate();
		}
		@Override
		public void setField(OUSStatusI proxy, String value) {
			proxy.setTimeOfUpdate(value);
		}};
   	final StatusGet<SBStatus> sbStatusGet = new StatusGet<SBStatus>(){
   		@Override
   		public String getField(SBStatus status) {
   			return status.getTimeOfUpdate();
		}
		@Override
		public void setField(SBStatus status, String value) {
			status.setTimeOfUpdate(value);
   		}};
	final ProxyGet<SBStatusI> sbProxyGet = new ProxyGet<SBStatusI>(){
		@Override
		public String getField(SBStatusI proxy) {
			return proxy.getTimeOfUpdate();
		}
		@Override
		public void setField(SBStatusI proxy, String value) {
			proxy.setTimeOfUpdate(value);
		}};

	private ContainerServices container;
    private Logger logger;
    private Simulator simulator;
//    private Utils utils;

    private EntityDeserializer entityDeserializer;
    private EntitySerializer entitySerializer;

    private ArchiveConnection archConnectionComp;
    private Operational archOperational;
    private Identifier archIdentifierComp;

    private StateSystem stateSystemComp;
    private AbstractStatusFactory remoteFactory;
    private AbstractStatusFactory cachedFactory;

    private MasterComponent schedulingMC;
//    private MasterSchedulerIF masterScheduler;

    public StatusProxyTest() throws Exception {
        super(StatusProxyTest.class.getName());
    }

    /**
     * Test case fixture setup.
     */
    protected void setUp() throws Exception {
        super.setUp();

        container = getContainerServices();
        logger = container.getLogger();
//        utils = new Utils(container, logger);

        entitySerializer = EntitySerializer.getEntitySerializer(
        		container.getLogger());
        entityDeserializer = EntityDeserializer.getEntityDeserializer(
        		container.getLogger());

        archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(
                container.getComponent("ARCHIVE_CONNECTION"));
        
        archIdentifierComp = alma.xmlstore.IdentifierHelper.narrow(
                container.getComponent("ARCHIVE_IDENTIFIER"));

        archOperational = archConnectionComp
                .getOperational("ObservationTest");
        assertNotNull(archOperational);
        
        final Object comp = container.getDefaultComponent("IDL:alma/ACSSim/Simulator:1.0");
        simulator = SimulatorHelper.narrow(comp);

        logger.info("SCHEDULING: Getting state system component");
        stateSystemComp = alma.projectlifecycle.StateSystemHelper.narrow(
                container.getComponent("OBOPS_LIFECYCLE_STATE_SYSTEM"));

        logger.info("Initializing SCHEDULING...");
        schedulingMC = MasterComponentHelper.narrow(container.getComponent("SCHEDULING_MASTER_COMP"));
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS1);
        if (!waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OFFLINE.PREINITIALIZED", 300)) fail();
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS2);
        if (!waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.ONLINE", 300)) fail();
        
        remoteFactory = RemoteStatusFactory.getInstance();
        cachedFactory = CachedStatusFactory.getInstance();

        remoteFactory.setStatusSystem(
        		stateSystemComp,
        		entitySerializer,
        		entityDeserializer,
        		new ALMAClock(),
        		logger);
       
    }

    /**
     * Test case fixture clean up.
     */
    protected void tearDown() throws Exception {
        logger.info("Shutting down SCHEDULING...");
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_SHUTDOWNPASS1);
        if (waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OFFLINE.PRESHUTDOWN", 300)) {
            schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_SHUTDOWNPASS2);
            waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OFFLINE.SHUTDOWN", 300);
        }
        container.releaseComponent(archConnectionComp.name());
        container.releaseComponent(archIdentifierComp.name());
        container.releaseComponent(simulator.name());
        super.tearDown();
    }
    
    public void testProjectStatusGettersAndSetters() throws Exception {
    	final String newValue = String.format( "new PI: %h", this.hashCode());
    	
    	// Get all the ProjectStatuses from the StateSystem
   		final SortedMap<String, ProjectStatus> statuses = getAllProjectStatuses();

    	// Create proxies of each type for them all
   		final SortedMap<String, ProjectStatusI> remotes = 
   			convertProjectStatuses(statuses.keySet(),
   				                   remoteFactory);
   		final SortedMap<String, ProjectStatusI> cacheds = 
   			convertProjectStatuses(statuses.keySet(),
   				                   cachedFactory);
   		
		show("Project Statuses - before", null, statuses, projectStatusGet, remotes, cacheds, projectProxyGet);
 		
    	// Do a set on each. Remember the old values so we can restore them
   		final Map<String, String> oldValues = new HashMap<String, String>();
   		for (final String uid : statuses.keySet()) {
   			final ProjectStatus  real   = statuses.get(uid);
   			final ProjectStatusI remote = remotes.get(uid);
   			final ProjectStatusI cached = cacheds.get(uid);
   			
   			final String oldValue = real.getPI();
   			
   			assertEquals(String.format(
   					"ProjectStatus %s: PI and remote proxy PI should be equal", uid),
   					oldValue, remote.getPI());
   			assertEquals(String.format(
   					"ProjectStatus %s: PI and cached proxy PI should be equal", uid),
   					oldValue, cached.getPI());
   			
   			oldValues.put(uid, oldValue);
   			projectStatusGet.setField(real, newValue);
   			projectProxyGet.setField(remote, newValue);
   			projectProxyGet.setField(cached, newValue);
   		}
   		
   		show("Project Statuses - during", oldValues, statuses, projectStatusGet, remotes, cacheds, projectProxyGet);
 		
    	// Use a getter to check the setter has taken.
   		for (final String uid : statuses.keySet()) {
   			final ProjectStatus  real   = statuses.get(uid);
   			final ProjectStatusI remote = remotes.get(uid);
   			final ProjectStatusI cached = cacheds.get(uid);
   			
   			assertEquals(String.format(
   					"ProjectStatus %s: setPI() didn't take", uid),
   					newValue, real.getPI());
   			assertEquals(String.format(
   					"ProjectStatus %s: remote proxy setPI() didn't take", uid),
   					newValue, remote.getPI());
   			assertEquals(String.format(
   					"ProjectStatus %s: cached proxy setPI() didn't take", uid),
   					newValue, cached.getPI());
   			
   			final String oldValue = oldValues.get(uid);
   			projectStatusGet.setField(real, oldValue);
   			projectProxyGet.setField(remote, oldValue);
   			projectProxyGet.setField(cached, oldValue);
   		}
   		
   		show("Project Statuses - after", oldValues, statuses, projectStatusGet, remotes, cacheds, projectProxyGet);
 		
    }
    
    public void testOUSStatusGettersAndSetters() throws Exception {
    	final String newValue = String.format( "new Time: %h", this.hashCode());
    	
    	// Get all the OUSStatuses from the StateSystem
   		final SortedMap<String, OUSStatus> statuses = getAllOUSStatuses();

    	// Create proxies of each type for them all
   		final SortedMap<String, OUSStatusI> remotes = 
   			convertOUSStatuses(statuses.keySet(),
   				                   remoteFactory);
   		final SortedMap<String, OUSStatusI> cacheds = 
   			convertOUSStatuses(statuses.keySet(),
   				                   cachedFactory);
   		
		show("OUS Statuses - before", null, statuses, ousStatusGet, remotes, cacheds, ousProxyGet);

	   		// Do a set on each. Remember the old values so we can restore them
   		final Map<String, String> oldValues = new HashMap<String, String>();
   		for (final String uid : statuses.keySet()) {
   			final OUSStatus  real   = statuses.get(uid);
   			final OUSStatusI remote = remotes.get(uid);
   			final OUSStatusI cached = cacheds.get(uid);
   			
   			final String oldValue = real.getTimeOfUpdate();
   			
   			assertEquals(String.format(
   					"OUSStatus %s: TimeOfUpdate and remote proxy TimeOfUpdate should be equal", uid),
   					oldValue, remote.getTimeOfUpdate());
   			assertEquals(String.format(
   					"OUSStatus %s: TimeOfUpdate and cached proxy TimeOfUpdate should be equal", uid),
   					oldValue, cached.getTimeOfUpdate());
   			
   			oldValues.put(uid, oldValue);
   			ousStatusGet.setField(real, newValue);
   			ousProxyGet.setField(remote, newValue);
   			ousProxyGet.setField(cached, newValue);
   		}
   		
   		show("OUS Statuses - during", oldValues, statuses, ousStatusGet, remotes, cacheds, ousProxyGet);
 		
     	// Use a getter to check the setter has taken.
   		for (final String uid : statuses.keySet()) {
   			final OUSStatus  real   = statuses.get(uid);
   			final OUSStatusI remote = remotes.get(uid);
   			final OUSStatusI cached = cacheds.get(uid);
   			
   			assertEquals(String.format(
   					"OUSStatus %s: setTimeOfUpdate() didn't take", uid),
   					newValue, real.getTimeOfUpdate());
   			assertEquals(String.format(
   					"OUSStatus %s: remote proxy setTimeOfUpdate() didn't take", uid),
   					newValue, remote.getTimeOfUpdate());
   			assertEquals(String.format(
   					"OUSStatus %s: cached proxy setTimeOfUpdate() didn't take", uid),
   					newValue, cached.getTimeOfUpdate());
   			
   			final String oldValue = oldValues.get(uid);
   			ousStatusGet.setField(real, oldValue);
   			ousProxyGet.setField(remote, oldValue);
   			ousProxyGet.setField(cached, oldValue);
   		}
   		
   		show("OUS Statuses - after", oldValues, statuses, ousStatusGet, remotes, cacheds, ousProxyGet);
 		
    }
    
    public void testSBStatusGettersAndSetters() throws Exception {
    	final String newValue = String.format( "new Time: %h", this.hashCode());
    	
    	// Get all the SBStatuses from the StateSystem
   		final SortedMap<String, SBStatus> statuses = getAllSBStatuses();

    	// Create proxies of each type for them all
   		final SortedMap<String, SBStatusI> remotes = 
   			convertSBStatuses(statuses.keySet(),
   				                   remoteFactory);
   		final SortedMap<String, SBStatusI> cacheds = 
   			convertSBStatuses(statuses.keySet(),
   				                   cachedFactory);
   		
		show("SB Statuses - before", null, statuses, sbStatusGet, remotes, cacheds, sbProxyGet);

    	// Do a set on each. Remember the old values so we can restore them
   		final Map<String, String> oldValues = new HashMap<String, String>();
   		for (final String uid : statuses.keySet()) {
   			final SBStatus  real   = statuses.get(uid);
   			final SBStatusI remote = remotes.get(uid);
   			final SBStatusI cached = cacheds.get(uid);
   			
   			final String oldValue = real.getTimeOfUpdate();
   			
   			assertEquals(String.format(
   					"SBStatus %s: TimeOfUpdate and remote proxy TimeOfUpdate should be equal", uid),
   					oldValue, remote.getTimeOfUpdate());
   			assertEquals(String.format(
   					"SBStatus %s: TimeOfUpdate and cached proxy TimeOfUpdate should be equal", uid),
   					oldValue, cached.getTimeOfUpdate());
   			
   			oldValues.put(uid, oldValue);
   			sbStatusGet.setField(real, newValue);
   			sbProxyGet.setField(remote, newValue);
   			sbProxyGet.setField(cached, newValue);
   		}
   		
   		show("SB Statuses - after", oldValues, statuses, sbStatusGet, remotes, cacheds, sbProxyGet);
 		
    	// Use a getter to check the setter has taken.
   		for (final String uid : statuses.keySet()) {
   			final SBStatus  real   = statuses.get(uid);
   			final SBStatusI remote = remotes.get(uid);
   			final SBStatusI cached = cacheds.get(uid);
   			
   			assertEquals(String.format(
   					"SBStatus %s: setTimeOfUpdate() didn't take", uid),
   					newValue, real.getTimeOfUpdate());
   			assertEquals(String.format(
   					"SBStatus %s: remote proxy setTimeOfUpdate() didn't take", uid),
   					newValue, remote.getTimeOfUpdate());
   			assertEquals(String.format(
   					"SBStatus %s: cached proxy setTimeOfUpdate() didn't take", uid),
   					newValue, cached.getTimeOfUpdate());
   			
   			final String oldValue = oldValues.get(uid);
   			sbStatusGet.setField(real, oldValue);
   			sbProxyGet.setField(remote, oldValue);
   			sbProxyGet.setField(cached, oldValue);
   		}
   		
   		show("SB Statuses - after", oldValues, statuses, sbStatusGet, remotes, cacheds, sbProxyGet);
 		
    }

    
    public void testProjectStatusRemotesChangeArchive() throws Exception {
    	final String newValue = String.format( "new PI: %h", this.hashCode());
    	
    	// Get all the ProjectStatuses from the StateSystem
   		final SortedMap<String, ProjectStatus> statuses = getAllProjectStatuses();

    	// Create proxies for them all
   		final SortedMap<String, ProjectStatusI> proxies = 
   			convertProjectStatuses(statuses.keySet(),
   				                   remoteFactory);

		show("Project Statuses - before", statuses, projectStatusGet, proxies, projectProxyGet);
 		
    	// Do a set on each.
   		for (final String uid : statuses.keySet()) {
   			final ProjectStatusI proxy = proxies.get(uid);
   			projectProxyGet.setField(proxy, newValue);
   		}
   		
   		final SortedMap<String, ProjectStatus> modifiedStatuses = getAllProjectStatuses();
   		show("Project Statuses - during", modifiedStatuses, projectStatusGet, proxies, projectProxyGet);
 		
    	// Compare with the archive
   		for (final String uid : statuses.keySet()) {
   			final ProjectStatus real = modifiedStatuses.get(uid);
   			final String        field = projectStatusGet.getField(real);

   			assertEquals(String.format(
   					"ProjectStatus %s: setPI() didn't take in archive", uid),
   					newValue, field);
   			
   			projectProxyGet.setField(
   					proxies.get(uid),
   					projectStatusGet.getField(statuses.get(uid)));
   		}
   		
   		show("Project Statuses - after", getAllProjectStatuses(), projectStatusGet, proxies, projectProxyGet);
 		
    }

    
    public void testOUSStatusRemotesChangeArchive() throws Exception {
    	final String newValue = String.format( "new PI: %h", this.hashCode());
    	
    	// Get all the OUSStatuses from the StateSystem
   		final SortedMap<String, OUSStatus> statuses = getAllOUSStatuses();

    	// Create proxies for them all
   		final SortedMap<String, OUSStatusI> proxies = 
   			convertOUSStatuses(statuses.keySet(),
   				                   remoteFactory);

		show("OUS Statuses - before", statuses, ousStatusGet, proxies, ousProxyGet);
 		
    	// Do a set on each.
   		for (final String uid : statuses.keySet()) {
   			final OUSStatusI proxy = proxies.get(uid);
   			ousProxyGet.setField(proxy, newValue);
   		}
   		
   		final SortedMap<String, OUSStatus> modifiedStatuses = getAllOUSStatuses();
   		show("OUS Statuses - during", modifiedStatuses, ousStatusGet, proxies, ousProxyGet);
 		
    	// Compare with the archive
   		for (final String uid : statuses.keySet()) {
   			final OUSStatus real = modifiedStatuses.get(uid);
   			final String        field = ousStatusGet.getField(real);

   			assertEquals(String.format(
   					"OUSStatus %s: setPI() didn't take in archive", uid),
   					newValue, field);
   			
   			ousProxyGet.setField(
   					proxies.get(uid),
   					ousStatusGet.getField(statuses.get(uid)));
   		}
   		
   		show("OUS Statuses - after", getAllOUSStatuses(), ousStatusGet, proxies, ousProxyGet);
 		
    }

    
    public void testSBStatusRemotesChangeArchive() throws Exception {
    	final String newValue = String.format( "new PI: %h", this.hashCode());
    	
    	// Get all the SBStatuses from the StateSystem
   		final SortedMap<String, SBStatus> statuses = getAllSBStatuses();

    	// Create proxies for them all
   		final SortedMap<String, SBStatusI> proxies = 
   			convertSBStatuses(statuses.keySet(),
   				                   remoteFactory);

		show("SB Statuses - before", statuses, sbStatusGet, proxies, sbProxyGet);
 		
    	// Do a set on each.
   		for (final String uid : statuses.keySet()) {
   			final SBStatusI proxy = proxies.get(uid);
   			sbProxyGet.setField(proxy, newValue);
   		}
   		
   		final SortedMap<String, SBStatus> modifiedStatuses = getAllSBStatuses();
   		show("SB Statuses - during", modifiedStatuses, sbStatusGet, proxies, sbProxyGet);
 		
    	// Compare with the archive
   		for (final String uid : statuses.keySet()) {
   			final SBStatus real = modifiedStatuses.get(uid);
   			final String        field = sbStatusGet.getField(real);

   			assertEquals(String.format(
   					"SBStatus %s: setPI() didn't take in archive", uid),
   					newValue, field);
   			
   			sbProxyGet.setField(
   					proxies.get(uid),
   					sbStatusGet.getField(statuses.get(uid)));
   		}
   		
   		show("SB Statuses - after", getAllSBStatuses(), sbStatusGet, proxies, sbProxyGet);
 		
    }

    
    public void testProjectStatusCachedDontChangeArchive() throws Exception {
    	final String newValue = String.format( "new PI: %h", this.hashCode());
    	
    	// Get all the ProjectStatuses from the StateSystem
   		final SortedMap<String, ProjectStatus> statuses = getAllProjectStatuses();

    	// Create proxies for them all
   		final SortedMap<String, ProjectStatusI> proxies = 
   			convertProjectStatuses(statuses.keySet(),
   				                   cachedFactory);

		show("Project Statuses - before", statuses, projectStatusGet, proxies, projectProxyGet);
 		
    	// Do a set on each.
   		for (final String uid : statuses.keySet()) {
   			final ProjectStatusI proxy = proxies.get(uid);
   			projectProxyGet.setField(proxy, newValue);
   		}
   		
   		final SortedMap<String, ProjectStatus> unmodifiedStatuses = getAllProjectStatuses();
   		show("Project Statuses - during", unmodifiedStatuses, projectStatusGet, proxies, projectProxyGet);
 		
    	// Compare with the archive
   		for (final String uid : statuses.keySet()) {
   			assertEquals(String.format(
   					"ProjectStatus %s: setPI() took in archive", uid),
   					projectStatusGet.getField(unmodifiedStatuses.get(uid)),
   					projectStatusGet.getField(statuses.get(uid)));
   		}
    }

    
    public void testOUSStatusCachedDontChangeArchive() throws Exception {
    	final String newValue = String.format( "new PI: %h", this.hashCode());
    	
    	// Get all the OUSStatuses from the StateSystem
   		final SortedMap<String, OUSStatus> statuses = getAllOUSStatuses();

    	// Create proxies for them all
   		final SortedMap<String, OUSStatusI> proxies = 
   			convertOUSStatuses(statuses.keySet(),
   				                   cachedFactory);

		show("OUS Statuses - before", statuses, ousStatusGet, proxies, ousProxyGet);
 		
    	// Do a set on each.
   		for (final String uid : statuses.keySet()) {
   			final OUSStatusI proxy = proxies.get(uid);
   			ousProxyGet.setField(proxy, newValue);
   		}
   		
   		final SortedMap<String, OUSStatus> unmodifiedStatuses = getAllOUSStatuses();
   		show("OUS Statuses - during", unmodifiedStatuses, ousStatusGet, proxies, ousProxyGet);
 		
    	// Compare with the archive
   		for (final String uid : statuses.keySet()) {
   			assertEquals(String.format(
   					"OUSStatus %s: setPI() took in archive", uid),
   					ousStatusGet.getField(unmodifiedStatuses.get(uid)),
   					ousStatusGet.getField(statuses.get(uid)));
   		}
    }

    
    public void testSBStatusCachedDontChangeArchive() throws Exception {
    	final String newValue = String.format( "new PI: %h", this.hashCode());
    	
    	// Get all the SBStatuses from the StateSystem
   		final SortedMap<String, SBStatus> statuses = getAllSBStatuses();

    	// Create proxies for them all
   		final SortedMap<String, SBStatusI> proxies = 
   			convertSBStatuses(statuses.keySet(), cachedFactory);

		show("SB Statuses - before", statuses, sbStatusGet, proxies, sbProxyGet);
 		
    	// Do a set on each.
   		for (final String uid : statuses.keySet()) {
   			final SBStatusI proxy = proxies.get(uid);
   			sbProxyGet.setField(proxy, newValue);
   		}
   		
   		final SortedMap<String, SBStatus> unmodifiedStatuses = getAllSBStatuses();
   		show("SB Statuses - during", unmodifiedStatuses, sbStatusGet, proxies, sbProxyGet);
 		
    	// Compare with the archive
   		for (final String uid : statuses.keySet()) {
   			assertEquals(String.format(
   					"SBStatus %s: setPI() took in archive", uid),
   					sbStatusGet.getField(unmodifiedStatuses.get(uid)),
   					sbStatusGet.getField(statuses.get(uid)));
   		}
    }
    
    
    public void subtestNavigation(String label, AbstractStatusFactory factory) throws Exception {
   		final SortedMap<String, SBStatus> statuses = getAllSBStatuses();
   		final SortedMap<String, SBStatusI> proxies = convertSBStatuses(statuses.keySet(),factory);
   		
   		for (final String uid : proxies.keySet()) {
   			final SBStatusI         sb = proxies.get(uid);
   			final ProjectStatusRefT psRef = sb.getProjectStatusRef();
   			
   			assertNotNull(String.format("(%s) No ProjectStatusRef for SBStatus %s", label, uid),
   					      psRef);

   			final ProjectStatusI ps = sb.getProjectStatus();
   			
   			assertNotNull(String.format(
   					"(%s) No ProjectStatus for SBStatus %s (ref is to '%s')", label,
   					uid,
   					psRef.getEntityId()),
   					ps);
   			
   			final OUSStatusRefT ousRef = ps.getObsProgramStatusRef();
   			
   			assertNotNull(String.format("(%s) No ObsProgramStatusRef for ProjectStatus %s (from SBStatus %s)", label,
   					                    ps.getProjectStatusEntity().getEntityId(),
   					                    uid),
   					      ousRef);

   			final OUSStatusI prog = ps.getObsProgramStatus();
   			
   			assertNotNull(String.format(
   					"(%s) No program status for SBStatus %s (ref is to '%s') via ProjectStatus %s", label,
   					uid,
   					ousRef.getEntityId(),
   					psRef.getEntityId()),
   					ps);
   			
   			final SBStatusI sb2 = find("", uid, prog);
   			
   			assertNotNull(String.format(
   					"(%s) Cannot find SBStatus %s in ProjectStatus %s (via OUSStatus %s)", label,
   					uid,
   					psRef.getEntityId(),
   					ousRef.getEntityId()),
   					sb2);
   			
   			logger.fine(String.format("navigation from SBStatus %s passed", uid));
   		}
    }
    
    public void testRemotesNavigation() throws Exception {
    	subtestNavigation("Remotes", remoteFactory);
    }
    
    public void testCachedNavigation() throws Exception {
    	subtestNavigation("Cached", cachedFactory);
    }
    
    
	private SBStatusI find(String prefix, String uid, OUSStatusI ouss)
					throws SchedulingException {
		for (final SBStatusI sbs : ouss.getSBStatus()) {
			if (sbs.getSBStatusEntity().getEntityId().equals(uid)) {
				return sbs;
			}
		}
		for (final OUSStatusI child : ouss.getOUSStatus()) {
			final SBStatusI sbs = find(prefix + "   ", uid, child);
			if (sbs != null) {
				return sbs;
			}
		}
		return null;
	}

	private String chars(char ch, int columns) {
   		final StringBuilder s = new StringBuilder();
   		while (columns-- > 0) {
   			s.append(ch);
   		}
   		return s.toString();
   	}
	
	private String dashes(int i) {
		return chars('-', i);
	}

	private<Status extends StatusBaseT, Proxy extends StatusBaseI> void show(
			String label,
			Map<String, String>       originals,
			SortedMap<String, Status> statuses,
			StatusGet<Status>         statusGet,
			SortedMap<String, Proxy>  remotes,
			SortedMap<String, Proxy>  cacheds,
			ProxyGet<Proxy>           proxyGet) {
		final StringBuilder b = new StringBuilder();
		final Formatter     f = new Formatter(b);
		final String line = dashes(24);
		final String sep  = String.format(
				"\t+-%s-+-%s-+-%s-+-%s-+-%s-+%n",
				line, line, line, line, line);
		
		f.format("%nComparisons %s%n", label);
		b.append(sep);
		f.format("\t| %-24s | %-24s | %-24s | %-24s | %-24s |%n",
				"UID", "Real Field", "Remote Field", "Cached Field", "Old Value");
		b.append(sep);
		
		for (final String uid : statuses.keySet()) {
			final Status real   = statuses.get(uid);
			final Proxy  remote = remotes.get(uid);
			final Proxy  cached = cacheds.get(uid);
			
			f.format("\t| %-24s | %-24s | %-24s | %-24s | %-24s |%n",
					uid,
					(real == null)? "NULL": statusGet.getField(real),
					(remote == null)? "NULL": proxyGet.getField(remote),
					(cached == null)? "NULL": proxyGet.getField(cached),
					(originals == null)? "n/a": originals.get(uid));
		}
		b.append(sep);
		
		logger.info(b.toString());
	}

	private<Status extends StatusBaseT, Proxy extends StatusBaseI> void show(
			String label,
			SortedMap<String, Status> statuses,
			StatusGet<Status>         statusGet,
			SortedMap<String, Proxy>  proxies,
			ProxyGet<Proxy>           proxyGet) {
		final StringBuilder b = new StringBuilder();
		final Formatter     f = new Formatter(b);
		final String line = dashes(24);
		final String sep  = String.format(
				"\t+-%s-+-%s-+-%s-+%n",
				line, line, line);
		
		f.format("%nComparisons %s%n", label);
		b.append(sep);
		f.format("\t| %-24s | %-24s | %-24s |%n",
				"UID", "Real Field", "Proxy Field");
		b.append(sep);
		
		for (final String uid : statuses.keySet()) {
			final Status real   = statuses.get(uid);
			final Proxy  proxy = proxies.get(uid);
			
			f.format("\t| %-24s | %-24s | %-24s |%n",
					uid,
					(real == null)? "NULL": statusGet.getField(real),
					(proxy == null)? "NULL": proxyGet.getField(proxy));
		}
		b.append(sep);
		
		logger.info(b.toString());
	}

//	private void show(SortedMap<String, ProjectStatus> projectStatuses,
//			          SortedMap<String, ALMAProjectStatus> proxies) {
//		final StringBuilder b = new StringBuilder();
//		final Formatter     f = new Formatter(b);
//		final String line = dashes(20);
//		final String sep  = String.format(
//				"\t+-%s-+-%s-+-%s-+-%s-+%n",
//				line, line, line, line);
//		
//		f.format("%nIn-memory vs. in-archive ProjectStatuses%n");
//		b.append(sep);
//		f.format("\t| %-20s | %-20s | %-20s | %-20s |%n",
//				"In-memory UID", "In-memory PI", "In-archive PI", "In-archive UID");
//		b.append(sep);
//		
//		for (final String uid : projectStatuses.keySet()) {
//			final ProjectStatus inMemory = projectStatuses.get(uid);
//			final ALMAProjectStatus inArchive = proxies.get(uid);
//			
//			f.format("\t| %-20s | %-20s | %-20s | %-20s |%n",
//					uid,
//					(inMemory == null)? "null": inMemory.getPI(),
//					(inArchive == null)? "null": inArchive.getPI(),
//					inArchive.getUID());
//		}
//		b.append(sep);
//		
//		logger.info(b.toString());
//	}

	private SortedMap<String, ProjectStatusI> convertProjectStatuses(Collection<String>    uids,
			                                                         AbstractStatusFactory factory)
			                                  throws SchedulingException {
		final SortedMap<String, ProjectStatusI> result = new TreeMap<String, ProjectStatusI>();
		for (final String uid : uids) {
			final ProjectStatusI proxy = factory.createProjectStatus(uid);
			result.put(uid, proxy);
		}
		return result;
	}

	private SortedMap<String, OUSStatusI> convertOUSStatuses(Collection<String>    uids,
			                                                 AbstractStatusFactory factory)
			                              throws SchedulingException {
		final SortedMap<String, OUSStatusI> result = new TreeMap<String, OUSStatusI>();
		for (final String uid : uids) {
			final OUSStatusI proxy = factory.createOUSStatus(uid);
			result.put(uid, proxy);
		}
		return result;
	}

	private SortedMap<String, SBStatusI> convertSBStatuses(Collection<String>    uids,
			                                               AbstractStatusFactory factory)
			                             throws SchedulingException {
		final SortedMap<String, SBStatusI> result = new TreeMap<String, SBStatusI>();
		for (final String uid : uids) {
			final SBStatusI proxy = factory.createSBStatus(uid);
			result.put(uid, proxy);
		}
		return result;
	}

	/**
     * Waits for the subystems property to reach a given state.
     * @param stateProp Subsystem Master state property
     * @param expected Expected state
     * @param timeout timeout in seconds
     * 
     */
    private boolean waitForSubsystemState(ROstringSeq stateProp, String expected, int timeout)
        throws Exception {
        
        String state = "";
        int sleepInterval = 1000;
        int timeoutCount = (int) 1000.0 * timeout / sleepInterval;
        int count = 0;
        logger.info("Waiting for subsystem to reach state "+expected+". Timeout is " +timeout+" (s).");
        do {
            Thread.sleep(sleepInterval);
            count++;
            Completion c = new Completion(0, 0, 0, new alma.ACSErr.ErrorTrace[] {});
            CompletionHolder ch = new CompletionHolder(c);
            String[] substates = stateProp.get_sync(ch);
            
            state = "";
            for (String s : substates)
                state += s+".";
            state = state.substring(0, state.length()-1);
            // logger.info("Current state is " + state);
            if (state.equals("AVAILABLE.ERROR")) {
                logger.severe("Subsystem went to error state");
                return false;
            }
        } while(!state.equals(expected) && count < timeoutCount);
        if (!state.equals(expected)) {
            logger.severe("Timeout waiting for state "+expected+"; real state is "+state);
            return true;
        }
        logger.info("Subsystem state is now "+state);
        return true;
    }

    /**
     * Get all the project statuses that are in the state archive .
     * 
     * @return a map from EntityId to ProjectStatus containing all the
     *         ProjectStatus entities found
     * 
     * @throws ArchiveInternalError
     */
    private SortedMap<String, ProjectStatus> getAllProjectStatuses()
    throws ArchiveInternalError {
    	final SortedMap<String, ProjectStatus> result =
    		new TreeMap<String, ProjectStatus>();

    	final String[] states = { StatusTStateType.ANYSTATE.toString() };

    	XmlEntityStruct xml[] = null;
    	try {
    		xml = stateSystemComp.findProjectStatusByState(states);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	for (final XmlEntityStruct xes : xml) {
    		try {
    			final ProjectStatus ps = (ProjectStatus) entityDeserializer.
    			deserializeEntity(xes, ProjectStatus.class);
    			result.put(ps.getProjectStatusEntity().getEntityId(), ps);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}

    	logger.info(String.format("%n%n%d ProjectStatus%s found%n%n",
    			result.size(),
    			(result.size() == 1)? "": "es"));
    	return result;
    }

    /**
     * Get all the OUS statuses that are in the state archive .
     * 
     * @return a map from EntityId to OUSStatus containing all the
     *         OUSStatus entities found
     * 
     * @throws ArchiveInternalError
     */
    private SortedMap<String, OUSStatus> getAllOUSStatuses()
    	throws ArchiveInternalError {
    	final SortedMap<String, ProjectStatus> projects =
    		getAllProjectStatuses();
    	
    	final SortedMap<String, OUSStatus> result =
    		new TreeMap<String, OUSStatus>();

    	for (final ProjectStatus ps : projects.values()) {
    		addInOUSStatuses(ps.getObsProgramStatusRef(), result);
    	}

    	logger.info(String.format("%n%n%d OUSStatus%s found%n%n",
    			result.size(),
    			(result.size() == 1)? "": "es"));

    	return result;
    }

    private void addInOUSStatuses(OUSStatusRefT ousStatusRef,
    		                      SortedMap<String, OUSStatus> results) {
    	final String oussId = ousStatusRef.getEntityId();
    	XmlEntityStruct xml = null;
    	try {
        	// 1. Get the OUSStatus indicated by ousStatusRef & add it to the results
    		xml = stateSystemComp.getOUSStatus(oussId);
			final OUSStatus ouss = (OUSStatus) entityDeserializer.
					deserializeEntity(xml, OUSStatus.class);
			
	    	// 2. Add it to the results
			results.put(ouss.getOUSStatusEntity().getEntityId(), ouss);
			
	    	// 3. Recurse down the OUSStatus tree
			for (final OUSStatusRefT subRef : ouss.getOUSStatusChoice().getOUSStatusRef()) {
				addInOUSStatuses(subRef, results);
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}

	/**
     * Get all the SB statuses that are in the state archive .
     * 
     * @return a map from EntityId to SBStatus containing all the
     *         SBStatus entities found
     * 
     * @throws ArchiveInternalError
     */
    private SortedMap<String, SBStatus> getAllSBStatuses()
    throws ArchiveInternalError {
    	final SortedMap<String, SBStatus> result =
    		new TreeMap<String, SBStatus>();

    	final String[] states = { StatusTStateType.ANYSTATE.toString() };

    	XmlEntityStruct xml[] = null;
    	try {
    		xml = stateSystemComp.findSBStatusByState(states);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	for (final XmlEntityStruct xes : xml) {
    		try {
    			final SBStatus sbs = (SBStatus) entityDeserializer.
    			deserializeEntity(xes, SBStatus.class);
    			result.put(sbs.getSBStatusEntity().getEntityId(), sbs);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}

    	logger.info(String.format("%n%n%d SBStatus%s found%n%n",
    			result.size(),
    			(result.size() == 1)? "": "es"));

    	return result;
    }
}

