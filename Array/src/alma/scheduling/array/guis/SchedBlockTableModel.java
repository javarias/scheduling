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

package alma.scheduling.array.guis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.formatting.Format;

/**
 * A model for a table showing a collection of
 * alma.scheduling.datamodel.obsproject.SchedBlocks.
 * 
 * @author dclarke
 * $Id: SchedBlockTableModel.java,v 1.20 2012/03/08 21:00:25 dclarke Exp $
 */
@SuppressWarnings("serial") // We are unlikely to need to serialise
public class SchedBlockTableModel extends AbstractTableModel {
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	/** Of course, a Logger */
	private Logger logger;
	
	/** Map the displayed columns to the internal logical columns */
	private int viewToModelColumnMap[];
	
	/** The displayed columns which are to be updated periodically */
	private Collection<Integer> liveDisplayColumns;
	
	/** Track where to find the project id under the current map */
	private int unmappedProjectIdColumn;
	
	/** Track where to find the SB id under the current map */
	private int unmappedSBIdColumn;
	
	/** Value to use in the absence of a score */
	private final static double noScore = -1;
	
	/** Value to use in the absence of a rank */
	private final static int noRank = 999999;
	
	/**
	 * Thread to do live updating of the display (NOT of the projects
	 * and SBs)
	 */
	private PeriodicUpdater updater = null;
	
	/* End Fields
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction and configuration
	 * ================================================================
	 */
	/**
	 * The one and only constructor.
	 */
	public SchedBlockTableModel() {
		super();
		logger = Logger.getLogger("Scheduliing GUI");
		configure(false);
	}
	
	public void configure(boolean dynamic) {
		if (dynamic) {
			viewToModelColumnMap = dynamicMap();
			useScores = true;
		} else {
			viewToModelColumnMap = defaultMap();
			useScores = false;
		}
		unmappedProjectIdColumn = unmap(Column_Project);
		unmappedSBIdColumn = unmap(Column_EntityId);
		liveDisplayColumns = new Vector<Integer>();
		for (int internalCol : Live_Columns) {
			int um = unmap(internalCol);
			if (um >= 0) {
				liveDisplayColumns.add(um);
			}
		}
		fireTableStructureChanged();
		initialiseData();
	}
	
	public void stopLiveUpdates() {
		if (updater != null) {
			updater.stopRunning();
			updater = null;
		}
	}
	/* End Construction and configuration
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Managing models with different sets of columns
	 * ================================================================
	 */
	/**
	 * Create the map from view columns to logical columns for the
	 * default model. At current this is the only mapping, but this
	 * gives us a framework for changing this later (including
	 * supporting different views on the same data for different users,
	 * and even allowing the user to customise their view).
	 * 
	 * @return
	 */
	private int[] defaultMap() {
		final int[] result = {
				Column_PI,
				Column_Name,
				Column_Revision,
				Column_Executive,
				Column_State,
				Column_Band,
				Column_Frequency,
				Column_EntityId,
				Column_CSV,
				Column_Project,
				Column_Note,
				Column_RA,
				Column_Dec,
				Column_HourAngle,
				Column_Elevation,
				Column_Azimuth,
				Column_minHA,
				Column_maxHA,
				Column_Mode
		};
		
		return result;
	}
	
	/**
	 * Create the map from view columns to logical columns for a
	 * dynamic scheduler.
	 * @param liveDisplayColumns2 
	 * 
	 * @return
	 */
	private int[] dynamicMap() {
		final int[] result = {
				Column_Rank,
				Column_Score,
				Column_PrevRank,
				Column_PrevScore,
				Column_PI,
				Column_Name,
				Column_Revision,
				Column_Executive,
				Column_State,
				Column_Band,
				Column_Frequency,
				Column_EntityId,
				Column_CSV,
				Column_Project,
				Column_Note,
				Column_RA,
				Column_Dec,
				Column_HourAngle,
				Column_Elevation,
				Column_Azimuth,
				Column_minHA,
				Column_maxHA,
				Column_Mode
		};
		
		return result;
	}
	
	private  int unmap(int modelColumn) {
		for (int viewColumn = 0; viewColumn < viewToModelColumnMap.length; viewColumn++) {
			if (map(viewColumn) == modelColumn) {
				return viewColumn;
			}
		}
		return -1;
	}
	
	private int map(int viewColumn) {
		return viewToModelColumnMap[viewColumn];
	}
	/* End Managing models with different sets of columns
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Underlying data
	 * ================================================================
	 */
	/** The underlying data for which we are providing a TableModel */
	private List<SchedBlock> data;
	
	/** The scores  of the data, keyed by SchedBlock UID */
	private Map<String, SBRank> scores;
	
	/** The ranks of the data, keyed by SchedBlock UID */
	private Map<String, Integer> ranks;
	
	/** The previous scores  of the data, keyed by SchedBlock UID */
	private Map<String, SBRank> prevScores;
	
	/** The previous ranks of the data, keyed by SchedBlock UID */
	private Map<String, Integer> prevRanks;
	
	/** Do we use the scores? */
	private boolean useScores;
	
	/**
	 * Initialise our internal storage
	 */
	private void initialiseData() {
		this.data = new ArrayList<SchedBlock>();
		this.scores = new HashMap<String, SBRank>();
		this.ranks  = new HashMap<String, Integer>();
		this.prevScores = new HashMap<String, SBRank>();
		this.prevRanks  = new HashMap<String, Integer>();
		this.fireTableDataChanged();
	}
	
	/**
	 * Set the data of the TableModel
	 */
	public void setData(Collection<SchedBlock> schedBlocks) {
		data.clear();
		data.addAll(schedBlocks);
		fireTableDataChanged();
		if (updater == null) {
			updater = new PeriodicUpdater(this, 1000);
			updater.start();
		}
	}
	
	/**
	 * Set the scores for the TableModel
	 */
	public void setScores(Map<String, SBRank>  scores,
						  Map<String, Integer> ranks) {
		if (useScores) {
			this.prevScores = this.scores;
			this.prevRanks  = this.ranks;
			this.scores = scores;
			this.ranks  = ranks;
			fireTableDataChanged();
		}
	}
	
	/**
	 * Get the data of the TableModel
	 */
	private List<SchedBlock> getData() {
		return this.data;
	}
	/* End Underlying data
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * TableModel implementation
	 * ================================================================
	 */
	private static final int  Column_EntityId =  0;
	private static final int        Column_PI =  1;
	private static final int Column_Executive =  2;
	private static final int      Column_Name =  3;
	private static final int     Column_State =  4;
	private static final int       Column_CSV =  5;
	private static final int   Column_Project =  6;
	private static final int      Column_Note =  7;
	private static final int     Column_minHA =  8;
	private static final int     Column_maxHA =  9;
	private static final int        Column_RA = 10;
	private static final int       Column_Dec = 11;
	private static final int      Column_Rank = 12;
	private static final int     Column_Score = 13;
	private static final int  Column_PrevRank = 14;
	private static final int Column_PrevScore = 15;
	private static final int Column_HourAngle = 16;
	private static final int Column_Elevation = 17;
	private static final int   Column_Azimuth = 18;
	private static final int      Column_Mode = 19;
	private static final int Column_Frequency = 20;
	private static final int      Column_Band = 21;
	private static final int  Column_Revision = 22;
	
	/**
	 * All  of the columns which should be updated in (near) real time
	 * (whether we use them or not - that will be figured out when the
	 * mapping from display to internal is set up)
	 */
	private static final int[] Live_Columns =
		{ Column_HourAngle, Column_Azimuth, Column_Elevation };


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return viewToModelColumnMap.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return getData().size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final SchedBlock schedBlock;
		final String     uid;

		try {
			schedBlock = getData().get(rowIndex);
		} catch (IndexOutOfBoundsException e) {
			logger.severe(String.format(
					"row out of bounds in %s.getValueAt(%d, %d)",
					this.getClass().getSimpleName(),
					rowIndex, columnIndex));
			return null;
		}
		
		switch (map(columnIndex)) {
		case Column_EntityId:
			return schedBlock.getUid();
		case Column_PI:
			return schedBlock.getPiName();
		case Column_Executive:
			return schedBlock.getExecutive().getName();
		case Column_Name:
			return schedBlock.getName();
		case Column_State:
			return schedBlock.getSchedBlockControl().getState();
		case Column_CSV:
			return schedBlock.getCsv();
		case Column_Project:
			return schedBlock.getProjectUid();
		case Column_Note:
			return schedBlock.getNote();
		case Column_minHA:
			return schedBlock.getPreConditions().getMinAllowedHourAngle();
		case Column_maxHA:
			return schedBlock.getPreConditions().getMaxAllowedHourAngle();
		case Column_RA:
			try {
				return Format.formatRA(schedBlock.getRepresentativeCoordinates().getRA());
			} catch (NullPointerException npe) {
				return "n/a";
			}
		case Column_Dec:
			try {
				return Format.formatDec(schedBlock.getRepresentativeCoordinates().getDec());
			} catch (NullPointerException npe) {
				return "n/a";
			}
		case Column_Rank:
			uid = schedBlock.getUid();
			if (ranks.containsKey(uid)) {
				return ranks.get(uid);
			}
			return noRank;
		case Column_Score:
			try {
				final SBRank rank = scores.get(schedBlock.getUid());
				return rank.getRank();
			} catch (Exception e) {
			}
			return noScore;
		case Column_PrevRank:
			uid = schedBlock.getUid();
			if (prevRanks.containsKey(uid)) {
				return prevRanks.get(uid);
			}
			return noRank;
		case Column_PrevScore:
			try {
				final SBRank rank = prevScores.get(schedBlock.getUid());
				return rank.getRank();
			} catch (Exception e) {
			}
			return noScore;
		case Column_HourAngle:
			try {
				return Format.formatHA(schedBlock.getRepresentativeCoordinates().getHourAngle());
			} catch (NullPointerException npe) {
				return "n/a";
			}
		case Column_Elevation:
			try {
				return String.format("% 6.2f",
						schedBlock.getRepresentativeCoordinates().getElevation());
			} catch (NullPointerException npe) {
				return "n/a";
			}
		case Column_Azimuth:
			try {
				return String.format("% 6.2f",
						schedBlock.getRepresentativeCoordinates().getAzimuth());
			} catch (NullPointerException npe) {
				return "n/a";
			}
		case Column_Mode:
			return schedBlock.getSchedulingConstraints().getSchedBlockMode().toString();
		case Column_Frequency:
			double f = schedBlock.getRepresentativeFrequency();
			if (f > 0) {
				return String.format("% 7.3f", f);
			} else {
				return "n/a";
			}
		case Column_Band:
			int b = schedBlock.getRepresentativeBand();
			if (b > 0) {
				return String.format("%2d", b);
			} else {
				return "n/a";
			}
		case Column_Revision:
			return schedBlock.getRevision();
		default:
			logger.severe(String.format(
					"column out of bounds in %s.getValueAt(%d, %d)",
					this.getClass().getSimpleName(),
					rowIndex, columnIndex));
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (map(columnIndex)) {
		case Column_EntityId:
			return String.class;
		case Column_PI:
			return String.class;
		case Column_Executive:
			return Executive.class;
		case Column_Name:
			return String.class;
		case Column_State:
			return SchedBlockState.class;
		case Column_CSV:
			return Boolean.class;
		case Column_Project:
			return String.class;
		case Column_Note:
			return String.class;
		case Column_minHA:
			return Double.class;
		case Column_maxHA:
			return Double.class;
		case Column_RA:
			return String.class;
		case Column_Dec:
			return String.class;
		case Column_Rank:
			return Integer.class;
		case Column_Score:
			return Double.class;
		case Column_PrevRank:
			return Integer.class;
		case Column_PrevScore:
			return Double.class;
		case Column_HourAngle:
			return String.class;
		case Column_Elevation:
			return String.class;
		case Column_Azimuth:
			return String.class;
		case Column_Mode:
			return String.class;
		case Column_Frequency:
			return String.class;
		case Column_Band:
			return String.class;
		case Column_Revision:
			return String.class;
		default:
			logger.severe(String.format(
					"column out of bounds in %s.getColumnClass(%d)",
					this.getClass().getSimpleName(),
					columnIndex));
			return Object.class;
		}
	}

	/**
	 * Return the text of the column's name. Leave it up to
	 * getColumnName() to apply any HTML formatting.
	 * 
	 * @param columnIndex
	 * @return
	 */
	private String getColumnInnerName(int columnIndex) {
		switch (map(columnIndex)) {
		case Column_EntityId:
			return "Entity ID";
		case Column_PI:
			return "P. I.";
		case Column_Executive:
			return "Executive";
		case Column_Name:
			return "Name";
		case Column_State:
			return "State";
		case Column_CSV:
			return "CSV?";
		case Column_Project:
			return "Project";
		case Column_Note:
			return "Note";
		case Column_minHA:
			return "Min HA";
		case Column_maxHA:
			return "Max HA";
		case Column_RA:
			return "RA";
		case Column_Dec:
			return "Dec";
		case Column_Rank:
			return "Rank";
		case Column_Score:
			return "Score";
		case Column_PrevRank:
			return "Previous Rank";
		case Column_PrevScore:
			return "Previous Score";
		case Column_HourAngle:
			return "HA";
		case Column_Elevation:
			return "Alt";
		case Column_Azimuth:
			return "Az";
		case Column_Mode:
			return "Mode";
		case Column_Frequency:
			return "Freq.";
		case Column_Band:
			return "Band";
		case Column_Revision:
			return "Revision";
		default:
			logger.severe(String.format(
					"column out of bounds in %s.getColumnName(%d)",
					this.getClass().getSimpleName(),
					columnIndex));
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return String.format("<html><b>%s</b></html>",
				getColumnInnerName(columnIndex));
	}
	/* End TableModel implementation
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Displaying scores & ranks
	 * ================================================================
	 */
	/**
	 * The scorer has been run, so refresh the scores and ranks.
	 */
	public void refreshScores() {
	}
	/* End Displaying scores & ranks
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Other external interface specific to this class
	 * ================================================================
	 */
	public int projectIdColumn() {
		return unmappedProjectIdColumn;
	}

	public int sbIdColumn() {
		return unmappedSBIdColumn;
	}

	public String getSchedBlockId(int row) {
		return (String) getValueAt(row, sbIdColumn());
	}
	/* End Other external interface specific to this class
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Comparators for sorting certain columns
	 * ================================================================
	 */
	public void addSpecificComparators(TableRowSorter<SchedBlockTableModel> sorter) {
		for (int i = 0; i < viewToModelColumnMap.length; i++) {
			switch (map(i)) {
			case Column_Dec:
				sorter.setComparator(i, decComparator());
				break;
			default:
				break;
			}
		}
	}
	
	private Comparator<String> decComparator() {
		final Comparator<String> result = new Comparator<String>(){

			@Override
			public int compare(String dec1, String dec2) {
				// First, determine which, if any, are negative decs
				final boolean neg1 = dec1.startsWith("-");
				final boolean neg2 = dec2.startsWith("-");
				if (neg1) {
					if (neg2) {
						// Both negative
						return dec2.compareTo(dec1);
					} else {
						// Dec1 negative, dec2 positive
						return -1;
					}
				} else {
					if (neg2) {
						// Dec1 positive, dec2 negative
						return 1;
					} else {
						// Both positive
						return dec1.compareTo(dec2);
					}
				}
			}};
		return result;
	}
	/* End Comparators for sorting certain columns
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * "Live" display management
	 * ================================================================
	 */
	private class PeriodicUpdater extends Thread {
		private SchedBlockTableModel table;
		private int     milliSecs = 1000; // Default of 1 second
		private boolean running   = true;
		
		public PeriodicUpdater(SchedBlockTableModel table, int milliSecs) {
			this.table = table;
			setInterval(milliSecs);
			setDaemon(true);
//			System.out.format("%nCreating %s @ %h%n%n",
//					this.getClass().getSimpleName(),
//					this.hashCode());
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while (running) {
				try {
					sleep(milliSecs);
//					System.out.format("Updating with %s @ %h%n",
//							this.getClass().getSimpleName(),
//							this.hashCode());
					if (running) {
						for (final int i : liveDisplayColumns ) {
							final TableModelEvent event = new TableModelEvent(
									table,
									0, getRowCount()-1, i);
							fireTableChanged(event);
						}
					}
				} catch (InterruptedException e) {
				}
			}
		}

		public void stopRunning() {
			running = false;
//			System.out.format("%nDeactivating %s @ %h%n%n",
//					this.getClass().getSimpleName(),
//					this.hashCode());
		}
		
		public void setInterval(int milliSecs) {
			this.milliSecs = milliSecs;
			this.interrupt();
		}
	}
	/* End "Live" display management
	 * ============================================================= */
}
