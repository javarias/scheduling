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
package alma.scheduling.array.executor;

import java.util.Date;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;
import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

import alma.Control.Completion;
import alma.Control.ExecBlockEndedEvent;
import alma.Control.ExecBlockStartedEvent;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.entity.xmlbinding.obsproject.ObsProjectRefT;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.StateEngine;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.offline.ASDMArchivedEvent;
import alma.offline.DataCapturerId;
import alma.offline.SubScanProcessedEvent;
import alma.offline.SubScanSequenceEndedEvent;
import alma.scheduling.SchedBlockExecutionCallback;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.array.executor.services.ControlArray;
import alma.scheduling.array.executor.services.Services;
import alma.scheduling.array.sbQueue.SchedBlockItem;
import alma.scheduling.array.sessions.SessionManager;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockControl;
import alma.scheduling.datamodel.obsproject.SchedBlockMode;
import alma.scheduling.datamodel.obsproject.SchedulingConstraints;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;
import alma.scheduling.weather.OpacityInterpolator;
import alma.scheduling.weather.OpacityInterpolatorImpl;

public class ExecutorUnitTests extends MockObjectTestCase {
	{
		setImposteriser(ClassImposteriser.INSTANCE);
	}
	private BlockingQueue<SchedBlockItem> queue;
	private Executor executor;
	private Services services;
	private SessionManager sessions;
	private ModelAccessor model = mock(ModelAccessor.class);
	private StateArchive stateArchive = mock(StateArchive.class);
	private StateEngine stateEngine = mock(StateEngine.class);
	private ControlArray array = mock(ControlArray.class);
	private ObsProjectDao prjDao = mock(ObsProjectDao.class);
	private SchedBlock sb = new SchedBlock();
	private ObsProject prj = new ObsProject();
	private ExecutorCallbackNotifier execNotifier;
	private OUSStatus ousStatus = null;
	private ProjectStatus prjStatus = null;
	private StatusT status = null;
	private WeatherHistoryDAO weatherDao = mock(WeatherHistoryDAO.class);
	private OpacityInterpolator opacityInterpolator = mock(OpacityInterpolator.class);
	private double[] opacityResult = {0.3078, 75.3473}; 
	
	
	@Override
	protected void setUp() throws Exception {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
		super.setUp();
		queue = mock(BlockingQueue.class);
		services = mock(Services.class);
		sessions = mock(SessionManager.class);
		executor = new Executor("Array001", queue);
		executor.setAntennaDiameter(12D);
		executor.setNumOfAvailableAntennas(18);
		sb.setUid("uid://A000/X000/X01");
		sb.setSchedBlockControl(new SchedBlockControl());
		sb.getSchedBlockControl().setAccumulatedExecutionTime(0.0);
		sb.getSchedBlockControl().setExecutionCount(0);
		sb.setProjectUid("uid://A000/X000/X04");
		sb.getSchedBlockControl().setIndefiniteRepeat(true);
		SchedulingConstraints sbConstraints = new SchedulingConstraints();
		sbConstraints.setRepresentativeFrequency(40D);
		Target t = new Target();
		t.setSource(new FieldSource("Alpha Centauri", new SkyCoordinates(14.660137528, -60.833974444), 0D, 0D));
		sbConstraints.setRepresentativeTarget(t);
		sbConstraints.setSchedBlockMode(SchedBlockMode.INTERFEROMETRY);
		sb.setSchedulingConstraints(sbConstraints);
		SBStatusEntityT sbStatusRef = new SBStatusEntityT();
		sbStatusRef.setEntityId("uid://A000/X000/X03");
		sbStatusRef.setEntityTypeName("SCHEDBLOCK");
		sb.setStatusEntity(sbStatusRef);
		prj.setTotalExecutionTime(0.0);
		
		ousStatus = new OUSStatus();
		ousStatus.setHasExecutionCount(true);
		ousStatus.setExecutionsRemaining(10);
		ousStatus.setHasTimeLimit(true);
		ousStatus.setSecondsRemaining(3600);
		ousStatus.setOUSStatusEntity(new OUSStatusEntityT());
		ousStatus.setObsUnitSetRef(new ObsProjectRefT());
		ousStatus.setContainingObsUnitSetRef(null);
		ousStatus.setProjectStatusRef(new ProjectStatusRefT());
		prjStatus = new ProjectStatus();
		prjStatus.setHasExecutionCount(true);
		prjStatus.setExecutionsRemaining(10);
		prjStatus.setHasTimeLimit(true);
		prjStatus.setSecondsRemaining(3600);
		
		status = new StatusT();
		status.setState(StatusTStateType.READY);
		
		checking(new Expectations() {{ 
			allowing(model).getStateArchive(); will(returnValue(stateArchive));
			allowing(model).getStateEngine(); will(returnValue(stateEngine));
			allowing(model).getWeatherDao(); will(returnValue(weatherDao));
			allowing(model).getOpacityInterpolator(); will(returnValue(opacityInterpolator));
			allowing(weatherDao).getTemperatureForTime(with(any(Date.class))); will(returnValue(new TemperatureHistRecord(0D, 1D, 0D, 0D)));
			allowing(weatherDao).getHumidityForTime(with(any(Date.class))); will(returnValue(new HumidityHistRecord(0D, 1D, 0D, 0D)));
			allowing(opacityInterpolator).estimatePWV(with(any(Double.class)), with(any(Double.class))); will(returnValue(0.5));
			allowing(opacityInterpolator).interpolateOpacityAndTemperature(with(any(Double.class)), with(any(Double.class))); will(returnValue(opacityResult));
		}});
		
		execNotifier = new ExecutorCallbackNotifier();
		executor.setServices(services);
		executor.setSessions(sessions);
		executor.addObserver(execNotifier);
		execNotifier.registerMonitor("callback",new TestExecCallback());
	}

	public void testGoodSemiAutoArray() throws Exception {
		final ExecBlockStartedEvent startEvent = new ExecBlockStartedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", System.currentTimeMillis());
		final ExecBlockEndedEvent endEvent = new ExecBlockEndedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", "DC001", Completion.SUCCESS, null, System.currentTimeMillis() + 2000);
		final ASDMArchivedEvent archEvent = new ASDMArchivedEvent(new DataCapturerId(), "complete", new IDLEntityRef(), 0);
		checking(new Expectations() {{ 
			atLeast(1).of(queue).take(); will(returnValue(new SchedBlockItem("uid://A000/X000/X01", System.currentTimeMillis())));
			allowing(services).getModel(); will(returnValue(model));
			atLeast(1).of(model).getSchedBlockFromEntityId("uid://A000/X000/X01"); will(returnValue(sb));
			SBStatus sbStatus = new SBStatus();
			sbStatus.setStatus(status);
			sbStatus.setSBStatusEntity(new SBStatusEntityT());
			sbStatus.setSchedBlockRef(new SchedBlockRefT());
			OUSStatusRefT ousStatusR = new OUSStatusRefT();
			sbStatus.setContainingObsUnitSetRef(ousStatusR);
			allowing(stateArchive).getSBStatus(sb.getStatusEntity()); will(returnValue(sbStatus));
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.RUNNING, Subsystem.SCHEDULING, Role.AOD);
			atMost(2).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.SUSPENDED, Subsystem.SCHEDULING, Role.AOD);
			IDLEntityRef curSession = new IDLEntityRef();
			atLeast(1).of(sessions).observeSB(sb); will(returnValue(curSession));
			atLeast(1).of(sessions).getCurrentSession(); will(returnValue(curSession));
			IDLEntityRef curSB = new IDLEntityRef();
			curSB.entityId = sb.getUid();
			atLeast(1).of(sessions).getCurrentSB(); will(returnValue(curSB));
			atLeast(1).of(services).getControlArray(); will(returnValue(array));
			atLeast(1).of(array).configure(curSB);
			atLeast(1).of(array).observe(with(any(IDLEntityRef.class)), with(any(IDLEntityRef.class)));
			oneOf(sessions).addExecution(startEvent.execId.entityId);
			allowing(model).getObsProjectDao(); will(returnValue(prjDao));
			atMost(2).of(stateArchive).insertOrUpdate(sbStatus, Subsystem.SCHEDULING);
			atMost(2).of(stateArchive).getOUSStatus(with(any(OUSStatusEntityT.class))); will(returnValue(ousStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(OUSStatus.class)), with(Subsystem.SCHEDULING));
			atMost(1).of(stateArchive).getProjectStatus(with(any(ProjectStatusEntityT.class))); will(returnValue(prjStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(ProjectStatus.class)), with(Subsystem.SCHEDULING));
			allowing(prjDao).findByEntityId("uid://A000/X000/X04"); will(returnValue(prj));
		}});
		executor.start("UnitTest", "MasterOfTheUniverse");
		Thread.sleep(1000);
		ExecutionContext execContext = executor.getCurrentExecution();
		execContext.processExecBlockStartedEvent(startEvent);
		Thread.sleep(1000);
		executor.stop("UnitTest", "MasterOfTheUniverse");
		execContext.processExecBlockEndedEvent(endEvent);
		Thread.sleep(1000);
		execContext.processASDMArchivedEvent(archEvent);
		Thread.sleep(1000);
		executor.stopCurrentExecution("UnitTest", "MasterOfTheUniverse");
	}
	
	public void testGoodAutoArray() throws Exception {
		final ExecBlockStartedEvent startEvent = new ExecBlockStartedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", System.currentTimeMillis());
		final ExecBlockEndedEvent endEvent = new ExecBlockEndedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", "DC001", Completion.SUCCESS, null, System.currentTimeMillis() + 2000);
		final ASDMArchivedEvent archEvent = new ASDMArchivedEvent(new DataCapturerId(), "complete", new IDLEntityRef(), 0);
		checking(new Expectations() {{ 
			atLeast(1).of(queue).take(); will(returnValue(new SchedBlockItem("uid://A000/X000/X01", System.currentTimeMillis())));
			allowing(services).getModel(); will(returnValue(model));
			atLeast(1).of(model).getSchedBlockFromEntityId("uid://A000/X000/X01"); will(returnValue(sb));
			SBStatus sbStatus = new SBStatus();
			sbStatus.setStatus(status);
			sbStatus.setSBStatusEntity(new SBStatusEntityT());
			sbStatus.setSchedBlockRef(new SchedBlockRefT());
			sbStatus.setContainingObsUnitSetRef(new OUSStatusRefT());
			allowing(stateArchive).getSBStatus(sb.getStatusEntity()); will(returnValue(sbStatus));
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.RUNNING, Subsystem.SCHEDULING, Role.AOD);
			oneOf(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.READY, Subsystem.SCHEDULING, Role.AOD);
			atMost(2).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.SUSPENDED, Subsystem.SCHEDULING, Role.AOD);
			IDLEntityRef curSession = new IDLEntityRef();
			atLeast(1).of(sessions).observeSB(sb); will(returnValue(curSession));
			atLeast(1).of(sessions).getCurrentSession(); will(returnValue(curSession));
			IDLEntityRef curSB = new IDLEntityRef();
			curSB.entityId = sb.getUid();
			atLeast(1).of(sessions).getCurrentSB(); will(returnValue(curSB));
			atLeast(1).of(services).getControlArray(); will(returnValue(array));
			atLeast(1).of(array).configure(curSB);
			atLeast(1).of(array).observe(with(any(IDLEntityRef.class)), with(any(IDLEntityRef.class)));
			oneOf(sessions).addExecution(startEvent.execId.entityId);
			allowing(model).getObsProjectDao(); will(returnValue(prjDao));
			atMost(2).of(stateArchive).insertOrUpdate(sbStatus, Subsystem.SCHEDULING);
			atMost(2).of(stateArchive).getOUSStatus(with(any(OUSStatusEntityT.class))); will(returnValue(ousStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(OUSStatus.class)), with(Subsystem.SCHEDULING));
			atMost(1).of(stateArchive).getProjectStatus(with(any(ProjectStatusEntityT.class))); will(returnValue(prjStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(ProjectStatus.class)), with(Subsystem.SCHEDULING));
			allowing(prjDao).findByEntityId("uid://A000/X000/X04"); will(returnValue(prj));
		}});
		executor.setFullAuto(true, "UnitTest", "MasterOfTheUniverse");
		executor.start("UnitTest", "MasterOfTheUniverse");
		Thread.sleep(1000);
		ExecutionContext execContext = executor.getCurrentExecution();
		execContext.processExecBlockStartedEvent(startEvent);
		Thread.sleep(1000);
		execContext.processExecBlockEndedEvent(endEvent);
		Thread.sleep(1000);
		execContext.processASDMArchivedEvent(archEvent);
		Thread.sleep(1000);
		executor.abortCurrentExecution("UnitTest", "MasterOfTheUniverse");
		executor.stop("UnitTest", "MasterOfTheUniverse");
	}
	
	public void testGoodManualArray() throws Exception {
		final ExecBlockStartedEvent startEvent = new ExecBlockStartedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", System.currentTimeMillis());
		final ExecBlockEndedEvent endEvent = new ExecBlockEndedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", "DC001", Completion.SUCCESS, null, System.currentTimeMillis() + 2000);
		final ASDMArchivedEvent archEvent = new ASDMArchivedEvent(new DataCapturerId(), "complete", new IDLEntityRef(), 0);
		checking(new Expectations() {{ 
			SchedBlockItem item = new SchedBlockItem("uid://A000/X000/X01", System.currentTimeMillis());
			atLeast(1).of(queue).take(); will(returnValue(item));
			atLeast(1).of(queue).offer(with(any(SchedBlockItem.class)));
			allowing(services).getModel(); will(returnValue(model));
			atLeast(1).of(model).getSchedBlockFromEntityId("uid://A000/X000/X01"); will(returnValue(sb));
			SBStatus sbStatus = new SBStatus();
			sbStatus.setStatus(status);
			sbStatus.setSBStatusEntity(new SBStatusEntityT());
			sbStatus.setSchedBlockRef(new SchedBlockRefT());
			sbStatus.setContainingObsUnitSetRef(new OUSStatusRefT());
			allowing(stateArchive).getSBStatus(sb.getStatusEntity()); will(returnValue(sbStatus));
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.RUNNING, Subsystem.SCHEDULING, Role.AOD);
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.READY, Subsystem.SCHEDULING, Role.AOD);
			IDLEntityRef curSession = new IDLEntityRef();
			atLeast(1).of(sessions).observeSB(sb); will(returnValue(curSession));
			atLeast(1).of(sessions).getCurrentSession(); will(returnValue(curSession));
			IDLEntityRef curSB = new IDLEntityRef();
			curSB.entityId = sb.getUid();
			atLeast(1).of(sessions).getCurrentSB(); will(returnValue(curSB));
			atLeast(1).of(services).getControlArray(); will(returnValue(array));
			atLeast(1).of(array).configure(curSB);
			atLeast(1).of(array).observe(with(any(IDLEntityRef.class)), with(any(IDLEntityRef.class)));
			oneOf(sessions).addExecution(startEvent.execId.entityId);
		}});
		executor.configureManual(true);
		executor.start("UnitTest", "MasterOfTheUniverse");
		Thread.sleep(1000);
		ExecutionContext execContext = executor.getCurrentExecution();
		execContext.processExecBlockStartedEvent(startEvent);
		Thread.sleep(1000);
		execContext.processExecBlockEndedEvent(endEvent);
		Thread.sleep(1000);
		execContext.processASDMArchivedEvent(archEvent);
		Thread.sleep(1000);
		executor.stop("UnitTest", "MasterOfTheUniverse");
		executor.stopCurrentExecution("UnitTest", "MasterOfTheUniverse");
	}
	
	public void testFailedObservationSemiAutoArray() throws Exception {
		final ExecBlockStartedEvent startEvent = new ExecBlockStartedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", System.currentTimeMillis());
		final ExecBlockEndedEvent endEvent = new ExecBlockEndedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", "DC001", Completion.FAIL, null, System.currentTimeMillis() + 2000);
		checking(new Expectations() {{ 
			atLeast(1).of(queue).take(); will(returnValue(new SchedBlockItem("uid://A000/X000/X01", System.currentTimeMillis())));
			allowing(services).getModel(); will(returnValue(model));
			atLeast(1).of(model).getSchedBlockFromEntityId("uid://A000/X000/X01"); will(returnValue(sb));
			SBStatus sbStatus = new SBStatus();
			sbStatus.setStatus(status);
			sbStatus.setSBStatusEntity(new SBStatusEntityT());
			sbStatus.setSchedBlockRef(new SchedBlockRefT());
			sbStatus.setContainingObsUnitSetRef(new OUSStatusRefT());
			allowing(stateArchive).getSBStatus(sb.getStatusEntity()); will(returnValue(sbStatus));
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.RUNNING, Subsystem.SCHEDULING, Role.AOD);
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.SUSPENDED, Subsystem.SCHEDULING, Role.AOD);
			IDLEntityRef curSession = new IDLEntityRef();
			atLeast(1).of(sessions).observeSB(sb); will(returnValue(curSession));
			atLeast(1).of(sessions).getCurrentSession(); will(returnValue(curSession));
			IDLEntityRef curSB = new IDLEntityRef();
			curSB.entityId = sb.getUid();
			atLeast(1).of(sessions).getCurrentSB(); will(returnValue(curSB));
			atLeast(1).of(services).getControlArray(); will(returnValue(array));
			atLeast(1).of(array).configure(curSB);
			atLeast(1).of(array).observe(with(any(IDLEntityRef.class)), with(any(IDLEntityRef.class)));
			oneOf(sessions).addExecution(startEvent.execId.entityId);
			allowing(model).getObsProjectDao(); will(returnValue(prjDao));
			atMost(2).of(stateArchive).insertOrUpdate(sbStatus, Subsystem.SCHEDULING);
			atMost(2).of(stateArchive).getOUSStatus(with(any(OUSStatusEntityT.class))); will(returnValue(ousStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(OUSStatus.class)), with(Subsystem.SCHEDULING));
			atMost(1).of(stateArchive).getProjectStatus(with(any(ProjectStatusEntityT.class))); will(returnValue(prjStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(ProjectStatus.class)), with(Subsystem.SCHEDULING));
			allowing(prjDao).findByEntityId("uid://A000/X000/X04"); will(returnValue(prj));
		}});
		executor.start("UnitTest", "MasterOfTheUniverse");
		Thread.sleep(1000);
		ExecutionContext execContext = executor.getCurrentExecution();
		execContext.processExecBlockStartedEvent(startEvent);
		Thread.sleep(1000);
		executor.stop("UnitTest", "MasterOfTheUniverse");
		execContext.processExecBlockEndedEvent(endEvent);
		Thread.sleep(1000);
		execContext.processASDMArchivedEvent(null);
		executor.stopCurrentExecution("UnitTest", "MasterOfTheUniverse");
	}
	
	public void testFailedObservationAutoArray() throws Exception {
		final ExecBlockStartedEvent startEvent = new ExecBlockStartedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", System.currentTimeMillis());
		final ExecBlockEndedEvent endEvent = new ExecBlockEndedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", "DC001", Completion.FAIL, null, System.currentTimeMillis() + 2000);
		checking(new Expectations() {{ 
			atLeast(1).of(queue).take(); will(returnValue(new SchedBlockItem("uid://A000/X000/X01", System.currentTimeMillis())));
			allowing(services).getModel(); will(returnValue(model));
			atLeast(1).of(model).getSchedBlockFromEntityId("uid://A000/X000/X01"); will(returnValue(sb));
			SBStatus sbStatus = new SBStatus();
			sbStatus.setStatus(status);
			sbStatus.setSBStatusEntity(new SBStatusEntityT());
			sbStatus.setSchedBlockRef(new SchedBlockRefT());
			sbStatus.setContainingObsUnitSetRef(new OUSStatusRefT());
			allowing(stateArchive).getSBStatus(sb.getStatusEntity()); will(returnValue(sbStatus));
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.RUNNING, Subsystem.SCHEDULING, Role.AOD);
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.SUSPENDED, Subsystem.SCHEDULING, Role.AOD);
			IDLEntityRef curSession = new IDLEntityRef();
			atLeast(1).of(sessions).observeSB(sb); will(returnValue(curSession));
			atLeast(1).of(sessions).getCurrentSession(); will(returnValue(curSession));
			IDLEntityRef curSB = new IDLEntityRef();
			curSB.entityId = sb.getUid();
			atLeast(1).of(sessions).getCurrentSB(); will(returnValue(curSB));
			atLeast(1).of(services).getControlArray(); will(returnValue(array));
			atLeast(1).of(array).configure(curSB);
			atLeast(1).of(array).observe(with(any(IDLEntityRef.class)), with(any(IDLEntityRef.class)));
			oneOf(sessions).addExecution(startEvent.execId.entityId);
			allowing(model).getObsProjectDao(); will(returnValue(prjDao));
			atMost(2).of(stateArchive).insertOrUpdate(sbStatus, Subsystem.SCHEDULING);
			atMost(2).of(stateArchive).getOUSStatus(with(any(OUSStatusEntityT.class))); will(returnValue(ousStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(OUSStatus.class)), with(Subsystem.SCHEDULING));
			atMost(1).of(stateArchive).getProjectStatus(with(any(ProjectStatusEntityT.class))); will(returnValue(prjStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(ProjectStatus.class)), with(Subsystem.SCHEDULING));
			allowing(prjDao).findByEntityId("uid://A000/X000/X04"); will(returnValue(prj));
		}});
		executor.setFullAuto(true, "UnitTest", "MasterOfTheUniverse");
		executor.start("UnitTest", "MasterOfTheUniverse");
		Thread.sleep(1000);
		ExecutionContext execContext = executor.getCurrentExecution();
		execContext.processExecBlockStartedEvent(startEvent);
		Thread.sleep(1000);
		executor.stop("UnitTest", "MasterOfTheUniverse");
		execContext.processExecBlockEndedEvent(endEvent);
		Thread.sleep(1000);
		execContext.processASDMArchivedEvent(null);
		executor.abortCurrentExecution("UnitTest", "MasterOfTheUniverse");
	}
	
	public void testFailArchivingSemiAutoArray() throws Exception {
		final ExecBlockStartedEvent startEvent = new ExecBlockStartedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", System.currentTimeMillis());
		final ExecBlockEndedEvent endEvent = new ExecBlockEndedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", "DC001", Completion.SUCCESS, null, System.currentTimeMillis() + 2000);
		final ASDMArchivedEvent archEvent = null;
		checking(new Expectations() {{ 
			atLeast(1).of(queue).take(); will(returnValue(new SchedBlockItem("uid://A000/X000/X01", System.currentTimeMillis())));
			allowing(services).getModel(); will(returnValue(model));
			atLeast(1).of(model).getSchedBlockFromEntityId("uid://A000/X000/X01"); will(returnValue(sb));
			SBStatus sbStatus = new SBStatus();
			sbStatus.setStatus(status);
			sbStatus.setSBStatusEntity(new SBStatusEntityT());
			sbStatus.setSchedBlockRef(new SchedBlockRefT());
			sbStatus.setContainingObsUnitSetRef(new OUSStatusRefT());
			allowing(stateArchive).getSBStatus(sb.getStatusEntity()); will(returnValue(sbStatus));
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.RUNNING, Subsystem.SCHEDULING, Role.AOD);
			atMost(2).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.SUSPENDED, Subsystem.SCHEDULING, Role.AOD);
			IDLEntityRef curSession = new IDLEntityRef();
			atLeast(1).of(sessions).observeSB(sb); will(returnValue(curSession));
			atLeast(1).of(sessions).getCurrentSession(); will(returnValue(curSession));
			IDLEntityRef curSB = new IDLEntityRef();
			curSB.entityId = sb.getUid();
			atLeast(1).of(sessions).getCurrentSB(); will(returnValue(curSB));
			atLeast(1).of(services).getControlArray(); will(returnValue(array));
			atLeast(1).of(array).configure(curSB);
			atLeast(1).of(array).observe(with(any(IDLEntityRef.class)), with(any(IDLEntityRef.class)));
			oneOf(sessions).addExecution(startEvent.execId.entityId);
			allowing(model).getObsProjectDao(); will(returnValue(prjDao));
			atMost(2).of(stateArchive).insertOrUpdate(sbStatus, Subsystem.SCHEDULING);
			atMost(2).of(stateArchive).getOUSStatus(with(any(OUSStatusEntityT.class))); will(returnValue(ousStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(OUSStatus.class)), with(Subsystem.SCHEDULING));
			atMost(1).of(stateArchive).getProjectStatus(with(any(ProjectStatusEntityT.class))); will(returnValue(prjStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(ProjectStatus.class)), with(Subsystem.SCHEDULING));
			allowing(prjDao).findByEntityId("uid://A000/X000/X04"); will(returnValue(prj));
		}});
		executor.start("UnitTest", "MasterOfTheUniverse");
		Thread.sleep(1000);
		ExecutionContext execContext = executor.getCurrentExecution();
		execContext.processExecBlockStartedEvent(startEvent);
		Thread.sleep(1000);
		executor.stop("UnitTest", "MasterOfTheUniverse");
		execContext.processExecBlockEndedEvent(endEvent);
		Thread.sleep(1000);
		execContext.processASDMArchivedEvent(archEvent);
		Thread.sleep(1000);
		executor.abortCurrentExecution("UnitTest", "MasterOfTheUniverse");
	}
	
	public void testFailManualArray() throws Exception {
		final ExecBlockStartedEvent startEvent = new ExecBlockStartedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", System.currentTimeMillis());
		final ExecBlockEndedEvent endEvent = new ExecBlockEndedEvent(
				new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0"), 
				new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0"), 
				"Array001", "DC001", Completion.FAIL, null, System.currentTimeMillis() + 2000);
		final ASDMArchivedEvent archEvent = new ASDMArchivedEvent(new DataCapturerId(), "complete", new IDLEntityRef(), 0);
		checking(new Expectations() {{ 
			SchedBlockItem item = new SchedBlockItem("uid://A000/X000/X01", System.currentTimeMillis());
			atLeast(1).of(queue).take(); will(returnValue(item));
			atLeast(1).of(queue).offer(with(any(SchedBlockItem.class)));
			allowing(services).getModel(); will(returnValue(model));
			atLeast(1).of(model).getSchedBlockFromEntityId("uid://A000/X000/X01"); will(returnValue(sb));
			SBStatus sbStatus = new SBStatus();
			sbStatus.setStatus(status);
			sbStatus.setSBStatusEntity(new SBStatusEntityT());
			sbStatus.setSchedBlockRef(new SchedBlockRefT());
			sbStatus.setContainingObsUnitSetRef(new OUSStatusRefT());
			allowing(stateArchive).getSBStatus(sb.getStatusEntity()); will(returnValue(sbStatus));
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.RUNNING, Subsystem.SCHEDULING, Role.AOD);
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.READY, Subsystem.SCHEDULING, Role.AOD);
			IDLEntityRef curSession = new IDLEntityRef();
			atLeast(1).of(sessions).observeSB(sb); will(returnValue(curSession));
			atLeast(1).of(sessions).getCurrentSession(); will(returnValue(curSession));
			IDLEntityRef curSB = new IDLEntityRef();
			curSB.entityId = sb.getUid();
			atLeast(1).of(sessions).getCurrentSB(); will(returnValue(curSB));
			atLeast(1).of(services).getControlArray(); will(returnValue(array));
			atLeast(1).of(array).configure(curSB);
			atLeast(1).of(array).observe(with(any(IDLEntityRef.class)), with(any(IDLEntityRef.class)));
			oneOf(sessions).addExecution(startEvent.execId.entityId);
		}});
		executor.configureManual(true);
		executor.start("UnitTest", "MasterOfTheUniverse");
		Thread.sleep(1000);
		ExecutionContext execContext = executor.getCurrentExecution();
		execContext.processExecBlockStartedEvent(startEvent);
		Thread.sleep(1000);
		execContext.processExecBlockEndedEvent(endEvent);
		Thread.sleep(1000);
		execContext.processASDMArchivedEvent(archEvent);
		Thread.sleep(1000);
		execContext.processASDMArchivedEvent(null);
		executor.stop("UnitTest", "MasterOfTheUniverse");
		executor.abortCurrentExecution("UnitTest", "MasterOfTheUniverse");
	}
	
	public void testDataCaptureEventsHandling() throws Exception {
		final IDLEntityRef execBlockRef = new IDLEntityRef("uid://A000/X000/X00","uid://A000/X000/X00", "EXEC", "1.0");
		final IDLEntityRef wrongExecBlockRef = new IDLEntityRef("uid://A000/X001/X00","uid://A000/X000/X00", "EXEC", "1.0");
		final IDLEntityRef sbRef = new IDLEntityRef("uid://A000/X000/X01","uid://A000/X000/X01", "SCHEDBLOCK", "1.0");
		final IDLEntityRef sessionRef = new IDLEntityRef("uid://A000/X000/X02","uid://A000/X000/X02", "SESSION", "1.0");
		final DataCapturerId dcId = new DataCapturerId("DC001", "Array001", sessionRef, sbRef, null, null);
		final ExecBlockStartedEvent startEvent = new ExecBlockStartedEvent(
				execBlockRef, sbRef, sessionRef, 
				"Array001", System.currentTimeMillis());
		final ExecBlockEndedEvent endEvent = new ExecBlockEndedEvent(
				execBlockRef, sbRef, sessionRef, 
				"Array001", "DC001", Completion.SUCCESS, null, System.currentTimeMillis() + 2000);
		final ASDMArchivedEvent archEvent = new ASDMArchivedEvent(new DataCapturerId(), "complete", new IDLEntityRef(), 0);
		final SubScanProcessedEvent ssp1_1 = new SubScanProcessedEvent(
				dcId, "finished", execBlockRef, 1, 1, 
				System.currentTimeMillis() * 10000, 
				(System.currentTimeMillis() - 5000) * 10000, 
				System.currentTimeMillis() * 10000, true);
		final SubScanProcessedEvent ssp1_2 = new SubScanProcessedEvent(
				dcId, "finished", execBlockRef, 1, 2, 
				System.currentTimeMillis() * 10000, 
				(System.currentTimeMillis() - 5000) * 10000, 
				System.currentTimeMillis() * 10000, true);
		final int success1[] = {1, 2};
		final SubScanSequenceEndedEvent ssse1 = new SubScanSequenceEndedEvent(dcId, "finished", execBlockRef, 1, System.currentTimeMillis(), success1);
		final SubScanProcessedEvent ssp2_1 = new SubScanProcessedEvent(
				dcId, "finished", execBlockRef, 2, 1, 
				System.currentTimeMillis() * 10000, 
				(System.currentTimeMillis() - 5000) * 10000, 
				System.currentTimeMillis() * 10000, true);
		final SubScanProcessedEvent ssp2_2 = new SubScanProcessedEvent(
				dcId, "finished", execBlockRef, 2, 2, 
				System.currentTimeMillis() * 10000, 
				(System.currentTimeMillis() - 5000) * 10000, 
				System.currentTimeMillis() * 10000, true);
		final SubScanSequenceEndedEvent ssse2 = new SubScanSequenceEndedEvent(dcId, "finished", execBlockRef, 2, System.currentTimeMillis(), success1);
		
		final SubScanProcessedEvent sspBad1 = new SubScanProcessedEvent(
				dcId, "finished", wrongExecBlockRef, 1, 1, 
				System.currentTimeMillis() * 10000, 
				(System.currentTimeMillis() - 5000) * 10000, 
				System.currentTimeMillis() * 10000, true);
		final SubScanSequenceEndedEvent ssseBad = new SubScanSequenceEndedEvent(dcId, "finished", wrongExecBlockRef, 2, System.currentTimeMillis(), success1);
		
		checking(new Expectations() {{ 
			SchedBlockItem item = new SchedBlockItem("uid://A000/X000/X01", System.currentTimeMillis());
			atLeast(1).of(queue).take(); will(returnValue(item));
//			atLeast(1).of(queue).offer(with(any(SchedBlockItem.class)));
			allowing(services).getModel(); will(returnValue(model));
			atLeast(1).of(model).getSchedBlockFromEntityId("uid://A000/X000/X01"); will(returnValue(sb));
			SBStatus sbStatus = new SBStatus();
			sbStatus.setStatus(status);
			sbStatus.setSBStatusEntity(new SBStatusEntityT());
			sbStatus.setSchedBlockRef(new SchedBlockRefT());
			sbStatus.setContainingObsUnitSetRef(new OUSStatusRefT());
			sbStatus.setHasSensitivityGoal(true);
			sbStatus.setHasExecutionCount(true);
			sbStatus.setExecutionsRemaining(100);
			allowing(stateArchive).getSBStatus(sb.getStatusEntity()); will(returnValue(sbStatus));
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.RUNNING, Subsystem.SCHEDULING, Role.AOD);
			atLeast(1).of(stateEngine).changeState(sb.getStatusEntity(), StatusTStateType.SUSPENDED, Subsystem.SCHEDULING, Role.AOD);
			IDLEntityRef curSession = new IDLEntityRef();
			atLeast(1).of(sessions).observeSB(sb); will(returnValue(curSession));
			atLeast(1).of(sessions).getCurrentSession(); will(returnValue(curSession));
			IDLEntityRef curSB = new IDLEntityRef();
			curSB.entityId = sb.getUid();
			atLeast(1).of(sessions).getCurrentSB(); will(returnValue(curSB));
			atLeast(1).of(services).getControlArray(); will(returnValue(array));
			atLeast(1).of(array).configure(curSB);
			atLeast(1).of(array).observe(with(any(IDLEntityRef.class)), with(any(IDLEntityRef.class)));
			oneOf(sessions).addExecution(startEvent.execId.entityId);
			allowing(model).getObsProjectDao(); will(returnValue(prjDao));
			atMost(2).of(stateArchive).insertOrUpdate(sbStatus, Subsystem.SCHEDULING);
			atMost(2).of(stateArchive).getOUSStatus(with(any(OUSStatusEntityT.class))); will(returnValue(ousStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(OUSStatus.class)), with(Subsystem.SCHEDULING));
			atMost(1).of(stateArchive).getProjectStatus(with(any(ProjectStatusEntityT.class))); will(returnValue(prjStatus));
			atMost(2).of(stateArchive).insertOrUpdate(with(any(ProjectStatus.class)), with(Subsystem.SCHEDULING));
			allowing(prjDao).findByEntityId("uid://A000/X000/X04"); will(returnValue(prj));
		}});
		
		executor.configureManual(false);
		executor.start("UnitTest", "MasterOfTheUniverse");
		Thread.sleep(1000);
		ExecutionContext execContext = executor.getCurrentExecution();
		execContext.processExecBlockStartedEvent(startEvent);
		Thread.sleep(1000);
		execContext.processSubScanProcessedEvent(ssp1_1);
		Thread.sleep(1000);
		execContext.processSubScanProcessedEvent(ssp1_2);
		Thread.sleep(1000);
		execContext.processSubScanSequenceEndedEvent(ssse1);
		Thread.sleep(1000);
		execContext.processSubScanProcessedEvent(ssp2_1);
		Thread.sleep(1000);
		execContext.processSubScanProcessedEvent(sspBad1);
		Thread.sleep(1000);
		execContext.processSubScanSequenceEndedEvent(ssseBad);
		Thread.sleep(1000);
		execContext.processSubScanProcessedEvent(ssp2_2);
		Thread.sleep(1000);
		execContext.processSubScanSequenceEndedEvent(ssse2);
		Thread.sleep(1000);
		execContext.processExecBlockEndedEvent(endEvent);
		Thread.sleep(1000);
		execContext.processASDMArchivedEvent(archEvent);
		Thread.sleep(2000);
		executor.stop("UnitTest", "MasterOfTheUniverse");
		
		//Check if the tree set are sorted
		TreeSet<SubScanSequenceEndedEvent> ssseSet = execContext.getSSSSet();
		TreeSet<SubScanProcessedEvent> sspSet = execContext.getSSPSet();
		int c = 0;
		for (SubScanSequenceEndedEvent ev1: ssseSet) {
			int subscan = ev1.scanNumber;
			int k = 0;
			int[] successSubScans = ev1.successfulSubscans;
			for (int i = c; i < successSubScans.length; i++) {
				SubScanProcessedEvent ev2 = sspSet.pollFirst();
				assertEquals(subscan, ev2.processedScanNum);
				assertEquals(successSubScans[k++], ev2.processedSubScanNum);
			}
		}
	}
	
	private class TestExecCallback implements SchedBlockExecutionCallback {

		@Override
		public void report(SchedBlockQueueItem arg0, String arg1,
				alma.ACSErr.Completion arg2) {
			
		}

		@Override
		public boolean _is_a(String repositoryIdentifier) {
			return false;
		}

		@Override
		public boolean _is_equivalent(Object other) {
			return false;
		}

		@Override
		public boolean _non_existent() {
			return false;
		}

		@Override
		public int _hash(int maximum) {
			return 0;
		}

		@Override
		public Object _duplicate() {
			return null;
		}

		@Override
		public void _release() {
		}

		
		public InterfaceDef _get_interface() {
			return null;
		}

		@Override
		public Object _get_interface_def() {
			return null;
		}

		@Override
		public Request _request(String operation) {
			return null;
		}

		@Override
		public Request _create_request(Context ctx, String operation,
				NVList arg_list, NamedValue result) {
			return null;
		}

		@Override
		public Request _create_request(Context ctx, String operation,
				NVList arg_list, NamedValue result, ExceptionList exclist,
				ContextList ctxlist) {
			return null;
		}

		@Override
		public Policy _get_policy(int policy_type) {
			return null;
		}

		@Override
		public DomainManager[] _get_domain_managers() {
			return null;
		}

		@Override
		public Object _set_policy_override(Policy[] policies,
				SetOverrideType set_add) {
			return null;
		}
		
	}
}
