/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File SciPipelineRequest.java
 */
package alma.scheduling.Define;

/**
 * A SciPipelineRequest is an object that incorporates the data needed to
 * start the science pipeline for a particular data reduction procedure
 * that is defined as part of a project's program.  It includes the following
 * items.
 * <ul>
 * <li>	id -- a unique identifier associated with the request
 * <li>	program -- the program that defines the request
 * <li>	results -- a reference to the results of the data reduction
 * <li>	comment -- a comment
 * <li>	status -- the status of the request
 * <li>	reductionProcedureName -- the data reduction procedure name
 * <li>	parms -- an array of parameters needed to control the data reduction 
 * </ul>
 * 
 * @version 1.5 September 16, 2004
 * @author Allen Farris
 */
public class SciPipelineRequest {

	
	private String id;
	private Program program;
	private String results;
	private String comment;
	private Status status;
	private String reductionProcedureName;
	private Object[] parms;
	
	/**
	 * 
	 */
	public SciPipelineRequest(Program program, String comment) throws SchedulingException {
		if (program == null)
			throw new SchedulingException ("program cannot be null.");
		this.program = program;
		this.comment = comment;
		status = new Status ();
		if (program.getDataReductionProcedureName() == null || 
			program.getDataReductionProcedureName().length() == 0)
			throw new SchedulingException("DataReduction procedure name is null.");
		reductionProcedureName = program.getDataReductionProcedureName();
		parms = program.getDataReductionParameters();
	}
	
	public void setReady(String id, DateTime time) {
		this.id = id;
		status.setReady(time);
	}

	public void setStarted(DateTime time) {
		status.setStarted(time);
	}
	
	public void setEnded(DateTime time, int state) {
		status.setEnded(time,state);
	}
	
	/**
	 * @return Returns the results.
	 */
	public String getResults() {
		return results;
	}

	/**
	 * @param results The results to set.
	 */
	public void setResults(String results) {
		this.results = results;
	}

	/**
	 * @return Returns the status.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Returns the parms.
	 */
	public Object[] getParms() {
		return parms;
	}

	/**
	 * @return Returns the program.
	 */
	public Program getProgram() {
		return program;
	}

	/**
	 * @return Returns the reductionProcedureName.
	 */
	public String getReductionProcedureName() {
		return reductionProcedureName;
	}

}
