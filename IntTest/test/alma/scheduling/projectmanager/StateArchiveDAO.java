package alma.scheduling.projectmanager;

import java.util.Collection;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import alma.acs.entityutil.EntityDeserializer;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.projectlifecycle.StateSystemOperations;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.OperationalOperations;

public class StateArchiveDAO {

	public  Logger                logger;
	private OperationalOperations xmlStore;
	private StateSystemOperations stateSystem;
	private EntityDeserializer    entityDeserializer;

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
			                  OperationalOperations xmlStore,
			                  StateSystemOperations stateSystem)
											throws Exception {
		this.logger = logger;
		this.xmlStore = xmlStore;
		this.stateSystem = stateSystem;
		this.entityDeserializer = EntityDeserializer.
									getEntityDeserializer(logger);
		refresh();
		getTransitionInfo();
	}
	/* End Construction
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
			XmlEntityStruct xml = stateSystem.getProjectStatus(id);
			result = (ProjectStatus) entityDeserializer.
						deserializeEntity(xml, ProjectStatus.class);
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
			XmlEntityStruct xml = stateSystem.getOUSStatus(id);
			result = (OUSStatus) entityDeserializer.
						deserializeEntity(xml, OUSStatus.class);
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
			XmlEntityStruct xml = stateSystem.getSBStatus(id);
			result = (SBStatus) entityDeserializer.
						deserializeEntity(xml, SBStatus.class);
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

		XmlEntityStruct xml[] = null;
		try {
			xml = stateSystem.findProjectStatusByState(states);
		} catch (Exception e) {
			logger.warning(String.format(
					"Can not pull ProjectStatuses from State System - %s",
					e.getMessage()));
		}

		if (xml != null) {
			for (final XmlEntityStruct xes : xml) {
				try {
					final ProjectStatus ps = (ProjectStatus) entityDeserializer.
					deserializeEntity(xes, ProjectStatus.class);
					result.add(ps);
				} catch (Exception e) {
					logger.warning(String.format(
							"Can not deserialise %s %s from State System - %s",
							xes.entityTypeName,
							xes.entityId,
							e.getMessage()));
				}
			}
		}

		return result;
	}

	private void fetchOtherStatuses(Set<ProjectStatus> projectStatuses,
			                        Set<OUSStatus>     ousStatuses,
			                        Set<SBStatus>      sbStatuses) {
		for (final ProjectStatus ps : projectStatuses) {
			try {
				final XmlEntityStruct[] xml
					= stateSystem.getProjectStatusList(
						ps.getProjectStatusEntity().getEntityId());
				for (XmlEntityStruct xes : xml) {
					try {
						if (xes.entityTypeName.equals("OUSStatus")) {
							final OUSStatus ouss = (OUSStatus) entityDeserializer.
							deserializeEntity(xes, OUSStatus.class);
							ousStatuses.add(ouss);
						} else if (xes.entityTypeName.equals("SBStatus")) {
							final SBStatus sbs = (SBStatus) entityDeserializer.
							deserializeEntity(xes, SBStatus.class);
							sbStatuses.add(sbs);
						} else if (!xes.entityTypeName.equals("ProjectStatus")) {
							logger.warning(String.format(
									"Unrecognised entity type for entity %s from State System - type is %s",
									xes.entityId,
									xes.entityTypeName));
						}
					} catch (Exception e) {
						logger.warning(String.format(
								"Can not deserialise %s %s from State System - %s",
								xes.entityTypeName,
								xes.entityId,
								e.getMessage()));
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
				stateSystem.changeProjectStatus(
						id,
						to,
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
				stateSystem.changeOUSStatus(
						id,
						to,
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
				stateSystem.changeSBStatus(
						id,
						to,
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
				final String states = stateSystem.getObsProjectStates(subsystem);
				parseAndStore(psTransitions, obsProjectStates, states, subsystem);
			}
		}
		logger.fine("Getting SCHED_BLOCK transitions");
		for (final String subsystem : Subsystem.ALL_SUBSYSTEMS) {
			if (!subsystem.equals(Subsystem.STATE_ENGINE)) {
				final String states = stateSystem.getSchedBlockStates(subsystem);
				parseAndStore(sbsTransitions, schedBlockStates, states, subsystem);
			}
		}
		logger.fine("Getting OBS_UNIT_SET transitions");
		for (final String subsystem : Subsystem.ALL_SUBSYSTEMS) {
			if (!subsystem.equals(Subsystem.STATE_ENGINE)) {
				final String states = stateSystem.getObsUnitSetStates(subsystem);
				parseAndStore(oussTransitions, obsUnitSetStates, states, subsystem);
			}
		}
		
		logTransitions("ProjectStatuses", psTransitions, obsProjectStates);
		logTransitions("SBStatuses", sbsTransitions, schedBlockStates);
		logTransitions("OUSStatuses", oussTransitions, obsUnitSetStates);
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
			// from now hold the From state
			final String[] tos  = split[1].split(",");
			// tos is now a list of the To states

			for (final String to : tos) {
				storeTransition(transitions, knownStates, from, to, subsystem);
			}
		}
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
