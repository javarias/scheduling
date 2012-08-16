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
package alma.scheduling.projectmanager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.projectstatus.StatusBaseT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.StateEngine;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;

public class StateArchiveDAO {

	public  Logger       logger;
	private StateArchive stateArchive;
	private StateEngine  stateEngine;
	private boolean      buggered = false;

	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/**
	 * @param logger
	 * @param managerLoc
	 * @param clientName
	 * @throws Exception
	 */
	protected StateArchiveDAO(Logger                logger,
			                  StateArchive          stateArchive,
			                  StateEngine           stateEngine)
											throws Exception {
		this.logger = logger;
        this.stateArchive = stateArchive;
        this.stateEngine  = stateEngine;
		refresh();
		getTransitionInfo();
	}
	/* End Construction
	 * ============================================================= */



	/*
	 * ================================================================
	 * Identity Building
	 * ================================================================
	 */
	private ProjectStatusEntityT projectStatusEntity(String id) {
		final ProjectStatusEntityT result = new ProjectStatusEntityT();
		result.setEntityId(id);
		return result;
	}
	
	private OUSStatusEntityT ousStatusEntity(String id) {
		final OUSStatusEntityT result = new OUSStatusEntityT();
		result.setEntityId(id);
		return result;
	}
	
	private SBStatusEntityT sbStatusEntity(String id) {
		final SBStatusEntityT result = new SBStatusEntityT();
		result.setEntityId(id);
		return result;
	}
	
	private StatusTStateType state(String label) {
		return StatusTStateType.valueOf(label);
	}
	/* End Identity Building
	 * ============================================================= */



	/*
	 * ================================================================
	 * Status fetching
	 * ================================================================
	 */
	private SortedSet<ProjectStatus> projectStatuses = null;
	private SortedSet<OUSStatus>     ousStatuses = null;
	private SortedSet<SBStatus>      sbStatuses = null;
	
	private ProjectStatus fetchProjectStatus(String id) {
		ProjectStatus result = null;
		try {
			result = stateArchive.getProjectStatus(projectStatusEntity(id));
		} catch (Exception e) {
			logger.warning(String.format(
					"Can not pull ProjectStatus %s from State System - %s",
					id,
					e.getMessage()));
		}
		return result;
	}
	
	private OUSStatus fetchOUSStatus(String id) {
		OUSStatus result = null;
		try {
			result = stateArchive.getOUSStatus(ousStatusEntity(id));
		} catch (Exception e) {
			logger.warning(String.format(
					"Can not pull OUSStatus %s from State System - %s",
					id,
					e.getMessage()));
		}
		return result;
	}
	
	private SBStatus fetchSBStatus(String id) {
		SBStatus result = null;
		try {
			result = stateArchive.getSBStatus(sbStatusEntity(id));
		} catch (Exception e) {
			logger.warning(String.format(
					"Can not pull SBStatus %s from State System - %s",
					id,
					e.getMessage()));
		}
		return result;
	}
	
	private SortedSet<ProjectStatus> newProjectStatusSet() {
		final Comparator<ProjectStatus> comp = new Comparator<ProjectStatus>() {
			@Override
			public int compare(ProjectStatus o1, ProjectStatus o2) {
				return o1.getProjectStatusEntity().getEntityId().compareTo(
						o2.getProjectStatusEntity().getEntityId());
			}
		};
		return new TreeSet<ProjectStatus>(comp);
	}
	
	private SortedSet<OUSStatus> newOUSStatusSet() {
		final Comparator<OUSStatus> comp = new Comparator<OUSStatus>() {
			@Override
			public int compare(OUSStatus o1, OUSStatus o2) {
				return o1.getOUSStatusEntity().getEntityId().compareTo(
						o2.getOUSStatusEntity().getEntityId());
			}
		};
		return new TreeSet<OUSStatus>(comp);
	}
	
	private SortedSet<SBStatus> newSBStatusSet() {
		final Comparator<SBStatus> comp = new Comparator<SBStatus>() {
			@Override
			public int compare(SBStatus o1, SBStatus o2) {
				return o1.getSBStatusEntity().getEntityId().compareTo(
						o2.getSBStatusEntity().getEntityId());
			}
		};
		return new TreeSet<SBStatus>(comp);
	}
	
	private SortedSet<ProjectStatus> fetchProjectStatuses() {
		final SortedSet<ProjectStatus> result = newProjectStatusSet();
		final String[] states = getObsProjectStates().toArray(new String[0]);

		try {
			ProjectStatus[] statuses = stateArchive.findProjectStatusByState(states);
			result.addAll(Arrays.asList(statuses));
		} catch (Exception e) {
			logger.warning(String.format(
					"Can not pull ProjectStatuses from State System - %s",
					e.getMessage()));
		}

		return result;
	}

	private void fetchOtherStatuses(Set<ProjectStatus> projectStatuses,
			                        Set<OUSStatus>     ousStatuses,
			                        Set<SBStatus>      sbStatuses) {
		for (final ProjectStatus ps : projectStatuses) {
			try {
				final StatusBaseT[] xml
					= stateArchive.getProjectStatusList(
						projectStatusEntity(ps.getProjectStatusEntity().getEntityId()));
				for (StatusBaseT xes : xml) {
						if (xes instanceof OUSStatus) {
							ousStatuses.add((OUSStatus)xes);
						} else if (xes instanceof SBStatus) {
							sbStatuses.add((SBStatus) xes);
						} else if (!(xes instanceof ProjectStatus)) {
							logger.warning(String.format(
									"Unrecognised entity type for entity from State System - type is %s",
									xes.getClass().getName()));
						}
				}
			} catch (Exception e) {
				logger.warning(String.format(
						"Can not pull child statuses for ProjectStatus %s from State System - %s",
						ps.getProjectStatusEntity().getEntityId(),
						e.getMessage()));
			}
		}
	}

	private void fetchAllStatuses() {
		projectStatuses = fetchProjectStatuses();
		ousStatuses = newOUSStatusSet();
		sbStatuses  = newSBStatusSet();
		fetchOtherStatuses(projectStatuses, ousStatuses, sbStatuses);
	}
		/* End Status fetching
	 * ============================================================= */



	/*
	 * ================================================================
	 * Public interface
	 * ================================================================
	 */
	public void refresh() {
		projectStatuses = null;
		ousStatuses     = null;
		sbStatuses      = null;
		buggered        = false;
	}
	
	public Set<ProjectStatus> getAllProjectStatuses() {
		if (projectStatuses == null) {
			fetchAllStatuses();
		}
		return projectStatuses;
	}
	
	public Set<OUSStatus> getAllOUSStatuses() {
		if (ousStatuses == null) {
			fetchAllStatuses();
		}
		return ousStatuses;
	}
	
	public Set<SBStatus> getAllSBStatuses() {
		if (sbStatuses == null) {
			fetchAllStatuses();
		}
		return sbStatuses;
	}
	
	private void changeProjectStatus(String id, String to, String subsystem) throws Exception {
		for (final String role : Role.ALL_ROLES) {
			try {
				stateEngine.changeState(
						projectStatusEntity(id),
						state(to),
						subsystem,
						role);
				return;
			} catch (Exception e) {
				if (role.equals(Role.ALL_ROLES[Role.ALL_ROLES.length-1])) {
					throw e;
				}
			}
		}
	}
	
	private void changeOUSStatus(String id, String to, String subsystem) throws Exception {
		for (final String role : Role.ALL_ROLES) {
			try {
				stateEngine.changeState(
						ousStatusEntity(id),
						state(to),
						subsystem,
						role);
				return;
			} catch (Exception e) {
				if (role.equals(Role.ALL_ROLES[Role.ALL_ROLES.length-1])) {
					throw e;
				}
			}
		}
	}
	
	private void changeSBStatus(String id, String to, String subsystem) throws Exception {
		for (final String role : Role.ALL_ROLES) {
			try {
				stateEngine.changeState(
						sbStatusEntity(id),
						state(to),
						subsystem,
						role);
				return;
			} catch (Exception e) {
				if (role.equals(Role.ALL_ROLES[Role.ALL_ROLES.length-1])) {
					throw e;
				}
			}
		}
	}
	
	public void setStatus(ProjectStatus status, String to) {
		final String id = status.getProjectStatusEntity().getEntityId();
		final String from = status.getStatus().getState().toString();
		final String subsystem = subsystemForProject(from, to);
		try {
			changeProjectStatus(id, to, subsystem);
			updatePS(id);
		} catch (Exception e) {
			logger.warning(String.format(
					"%s while trying to set %s %s to %s - %s",
					e.getClass().getSimpleName(),
					status.getClass().getSimpleName(),
					id,
					to,
					e.getMessage()));
			e.printStackTrace();
		}
	}

	public void setStatus(OUSStatus status, String to) {
		final String id = status.getOUSStatusEntity().getEntityId();
		final String from = status.getStatus().getState().toString();
		final String subsystem = subsystemForObsUnitSet(from, to);
		try {
			changeOUSStatus(id, to, subsystem);
			updateOUS(id);
		} catch (Exception e) {
			logger.warning(String.format(
					"%s while trying to set %s %s to %s - %s",
					e.getClass().getSimpleName(),
					status.getClass().getSimpleName(),
					id,
					to,
					e.getMessage()));
			e.printStackTrace();
		}
	}
	
	public void setStatus(SBStatus status, String to) {
		final String id = status.getSBStatusEntity().getEntityId();
		final String from = status.getStatus().getState().toString();
		final String subsystem = subsystemForSchedBlock(from, to);
		try {
			logger.info(String.format(
					"Setting %s %s to %s",
					status.getClass().getSimpleName(),
					id,
					to));
			changeSBStatus(id, to, subsystem);
			updateSB(id);
		} catch (Exception e) {
			logger.warning(String.format(
					"%s while trying to set %s %s to %s - %s",
					e.getClass().getSimpleName(),
					status.getClass().getSimpleName(),
					id,
					to,
					e.getMessage()));
			e.printStackTrace();
		}
	}
	
	private void updatePS(String id) {
		final ProjectStatus oldStatus = findPS(id);
		final ProjectStatus newStatus = fetchProjectStatus(id);
		projectStatuses.remove(oldStatus);
		projectStatuses.add(newStatus);
	}

	private void updateOUS(String id) {
		final OUSStatus oldStatus = findOUSS(id);
		final OUSStatus newStatus = fetchOUSStatus(id);
		ousStatuses.remove(oldStatus);
		ousStatuses.add(newStatus);
	}
	
	private void updateSB(String id) {
		final SBStatus oldStatus = findSBS(id);
		final SBStatus newStatus = fetchSBStatus(id);
		sbStatuses.remove(oldStatus);
		sbStatuses.add(newStatus);
	}
	
	private ProjectStatus findPS(String id) {
		for (final ProjectStatus ps : projectStatuses) {
			if (ps.getProjectStatusEntity().getEntityId().equals(id)) {
				return ps;
			}
		}
		return null;
	}
	
	private OUSStatus findOUSS(String id) {
		for (final OUSStatus ouss : ousStatuses) {
			if (ouss.getOUSStatusEntity().getEntityId().equals(id)) {
				return ouss;
			}
		}
		return null;
	}
	
	private SBStatus findSBS(String id) {
		for (final SBStatus sbs : sbStatuses) {
			if (sbs.getSBStatusEntity().getEntityId().equals(id)) {
				return sbs;
			}
		}
		return null;
	}
	
	public boolean isBuggered() {
		return buggered;
	}
	/* End Public interface
	 * ============================================================= */



	/*
	 * ================================================================
	 * Knowledge of stateSystem transitions
	 * ================================================================
	 */
	/*
	 * xxTransitions is keyed by From state, then To state and the
	 * value then returned is a Set of Subsystems which can make that
	 * transition.
	 */
	private Map<String, Map<String, Set<String>>> psTransitions;
	private Map<String, Map<String, Set<String>>> sbsTransitions;
	private Map<String, Map<String, Set<String>>> oussTransitions;

	/*
	 * xxStates contains all the states to which we can change an xx.
	 */
	private Set<String> obsProjectStates;
	private Set<String> schedBlockStates;
	private Set<String> obsUnitSetStates;

	private void getTransitionInfo() {
		psTransitions   = new TreeMap<String, Map<String, Set<String>>>();
		sbsTransitions  = new TreeMap<String, Map<String, Set<String>>>();
		oussTransitions = new TreeMap<String, Map<String, Set<String>>>();
		obsProjectStates = new TreeSet<String>();
		schedBlockStates = new TreeSet<String>();
		obsUnitSetStates = new TreeSet<String>();

		logger.fine("Getting OBS_PROJECT transitions");
		for (final String subsystem : Subsystem.ALL_SUBSYSTEMS) {
			if (!subsystem.equals(Subsystem.STATE_ENGINE)) {
				final String states = stateEngine.getObsProjectStates(subsystem);
				parseAndStore(psTransitions, obsProjectStates, states, subsystem);
			}
		}
		logger.fine("Getting SCHED_BLOCK transitions");
		for (final String subsystem : Subsystem.ALL_SUBSYSTEMS) {
			if (!subsystem.equals(Subsystem.STATE_ENGINE)) {
				final String states = stateEngine.getSchedBlockStates(subsystem);
				parseAndStore(sbsTransitions, schedBlockStates, states, subsystem);
			}
		}
		logger.fine("Getting OBS_UNIT_SET transitions");
		for (final String subsystem : Subsystem.ALL_SUBSYSTEMS) {
			if (!subsystem.equals(Subsystem.STATE_ENGINE)) {
				final String states = stateEngine.getObsUnitSetStates(subsystem);
				parseAndStore(oussTransitions, obsUnitSetStates, states, subsystem);
			}
		}
		
		logAllStates();
		logTransitions("ProjectStatuses", psTransitions, obsProjectStates);
		logTransitions("SBStatuses", sbsTransitions, schedBlockStates);
		logTransitions("OUSStatuses", oussTransitions, obsUnitSetStates);
		
		if (buggered) {
			logger.severe("Serious errors found, this will probably not work (see logs above for clues).");
		}
	}



	/**
	 * Split the given state string up and store it in the given
	 * transitions dictionary.
	 * 
	 * @param transitions
	 * @param states - expected to be in the form: 
	 * 			   			from1:to1,to2;from2:to3,to4
	 * 				   as specified in the StateSystem IDL.
	 * @param subsystem
	 */
	private void parseAndStore(
			Map<String, Map<String, Set<String>>> transitions,
			Set<String>                           knownStates,
			String                                states,
			String                                subsystem) {
		if (states == null || states.equals("")) {
			logger.fine(String.format(
					"no transitions for %s", subsystem));
			return;
		}
		final String[] byFrom = states.split(";");
		// byFrom is now {"from1:to1,to2", "from2:to3,to4"}
		for (final String forFrom : byFrom) {
			final String[] split = forFrom.split(":");
			// split is now {"from1", "to1,to2"}
			final String   from = split[0];
			// from now holds the From state
			final String[] tos  = split[1].split(",");
			// tos is now a list of the To states

			for (final String to : tos) {
				storeTransition(transitions, knownStates, from, to, subsystem);
			}
		}
	}

	private void logAllStates() {
		StringBuilder b = new StringBuilder();
		@SuppressWarnings("unchecked")
		Enumeration<StatusTStateType> e = StatusTStateType.enumerate();
		SortedSet<String> states = new TreeSet<String>();
		
		while (e.hasMoreElements()) {
			StatusTStateType t = e.nextElement();
			states.add(t.toString());
		}
		
		b.append("All known states from StatusTStateType:");
		
		for (String state : states) {
			b.append("\n\t");
			b.append(state);
		}
		
		logger.info(b.toString());
	}
	
	private void logTransitions(
			String                                object,
			Map<String, Map<String, Set<String>>> transitions,
			Set<String>                           knownStates) {
		StringBuilder b = new StringBuilder();
		Formatter     f = new Formatter(b);
		f.format("States for %s%n", object);
		for (final String state : knownStates) {
			f.format("\t%s%n", state);
		}
		f.format("%n");
		f.format("Transitions for %s%n", object);
		
		for (final String fromState : transitions.keySet()) {
			final Map<String, Set<String>> toAndWhom = transitions.get(fromState);
			String from = fromState;
			for (final String to : toAndWhom.keySet()) {
				final Set<String> whom = toAndWhom.get(to);
				f.format("%20s -> %20s: %s%n",
						 from, to, logCollection(whom));
				from = "";
			}
		}
		
		logger.info(b.toString());
	}
	
	private String logCollection(Collection<String> strings) {
		StringBuilder b = new StringBuilder();
		Formatter     f = new Formatter(b);
		String sep = "";
		for (final String s : strings) {
			f.format("%s%s", sep, s);
			sep = ", ";
		}
		return b.toString();
	}
	
	private void storeTransition(
			Map<String, Map<String, Set<String>>> transitions,
			Set<String>                           knownStates,
			String                                from,
			String                                to,
			String                                subsystem) {
		
		Map<String, Set<String>> toPart;
		Set<String> subsystems;

		logger.finer(String.format("Adding transition %s to %s for %s",
				from, to, subsystem));

		try {
			StatusTStateType.valueOf(from);
		} catch (IllegalArgumentException e) {
			logger.warning(String.format("State %s (the 'from' state, given by the StateEngine) is not known to StatusTStateType", from));
			buggered = true;
		}
		try {
			StatusTStateType.valueOf(to);
		} catch (IllegalArgumentException e) {
			logger.warning(String.format("State %s (the 'to' state, given by the StateEngine) is not known to StatusTStateType", to));
			buggered = true;
		}
		
		if (transitions.containsKey(from)) {
			toPart = transitions.get(from);
		} else {
			toPart = new TreeMap<String, Set<String>>();
			transitions.put(from, toPart);
			knownStates.add(from);
		}
		
		if (toPart.containsKey(to)) {
			subsystems = toPart.get(to);
		} else {
			subsystems = new TreeSet<String>();
			toPart.put(to, subsystems);
			knownStates.add(to);
		}
		
		subsystems.add(subsystem);
	}

	public Set<String> getObsProjectStates() {
		return obsProjectStates;
	}

	public Set<String> getSchedBlockStates() {
		return schedBlockStates;
	}

	public Set<String> getObsUnitSetStates() {
		return obsUnitSetStates;
	}
	
	private String subsystemFor(
			Map<String, Map<String, Set<String>>> transitions,
			String                                from,
			String                                to) {
		String result = null;
		if (transitions.containsKey(from)) {
			if (transitions.get(from).containsKey(to)) {
				Iterator<String> i = transitions.get(from).get(to).iterator();
				if (i.hasNext()) {
					result = i.next();
				}
			}
		}
		return result;
	}
	
	public String subsystemForProject(String from, String to) {
		return subsystemFor(psTransitions, from, to);
	}
	
	public String subsystemForObsUnitSet(String from, String to) {
		return subsystemFor(oussTransitions, from, to);
	}
	
	public String subsystemForSchedBlock(String from, String to) {
		return subsystemFor(sbsTransitions, from, to);
	}
	/* End Knowledge of stateSystem transitions
	 * ============================================================= */
}
