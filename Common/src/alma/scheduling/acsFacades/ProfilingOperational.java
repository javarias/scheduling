/*
 * ALMA - Atacama Large Millimetre Array
 * (c) European Southern Observatory, 2010
 * (c) Associated Universities Inc., 2010
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 */
package alma.scheduling.acsFacades;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Cursor;
import alma.xmlstore.OperationalOperations;
import alma.xmlstore.OperationalPackage.DirtyEntity;
import alma.xmlstore.OperationalPackage.IllegalEntity;
import alma.xmlstore.OperationalPackage.MalformedURI;
import alma.xmlstore.OperationalPackage.NotFound;
import alma.xmlstore.OperationalPackage.NotYetThere;
import alma.xmlstore.OperationalPackage.StatusStruct;
import alma.xmlstore.OperationalPackage.TimestampInconsistency;


/**
 * A facade for the XMLstore Operational which logs calls made to it.
 *
 * @version $Id: ProfilingOperational.java,v 1.2 2011/10/10 17:39:24 javarias Exp $
 * @author David Clarke
 */
public class ProfilingOperational
	extends AbstractProfilingComponent
	implements OperationalOperations {

	/** The object for which we are a facade. */
	private OperationalOperations delegate;
	
	/**
	 * Construct this object
	 * 
	 * @throws AcsJContainerServicesEx 
	 */
	public ProfilingOperational(ContainerServices     containerServices,
			                  OperationalOperations delegate)
		throws AcsJContainerServicesEx {
		super(containerServices);
        this.delegate = delegate;
	}


	
	/*
	 * ================================================================
	 * Delegation
	 * ================================================================
	 */
	/**
	 * @param uid
	 * @param schema
	 * @param xPath
	 * @param xmlElement
	 * @throws NotYetThere
	 * @throws MalformedURI
	 * @throws IllegalEntity
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#addElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addElement(String uid, String schema, String xPath,
			String xmlElement) throws NotYetThere, MalformedURI, IllegalEntity,
			ArchiveInternalError {
		profiler.start("addElement");
		delegate.addElement(uid, schema, xPath, xmlElement);
		profiler.end();
	}

	/**
	 * @param password
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#close(java.lang.String)
	 */
	public void close(String password) throws ArchiveInternalError {
		profiler.start("close");
		delegate.close(password);
		profiler.end();
	}

	/**
	 * @param identifier
	 * @throws MalformedURI
	 * @throws NotFound
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#delete(java.lang.String)
	 */
	public void delete(String identifier) throws MalformedURI, NotFound,
			ArchiveInternalError {
		profiler.start("delete");
		delegate.delete(identifier);
		profiler.end();
	}

	/**
	 * @param uid
	 * @param schema
	 * @param xPath
	 * @throws NotYetThere
	 * @throws MalformedURI
	 * @throws IllegalEntity
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#deleteElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void deleteElement(String uid, String schema, String xPath)
			throws NotYetThere, MalformedURI, IllegalEntity,
			ArchiveInternalError {
		profiler.start("deleteElement");
		delegate.deleteElement(uid, schema, xPath);
		profiler.end();
	}

	/**
	 * @param identifier
	 * @return
	 * @throws DirtyEntity
	 * @throws MalformedURI
	 * @throws NotFound
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#exists(java.lang.String)
	 */
	public boolean exists(String identifier) throws DirtyEntity, MalformedURI,
			NotFound, ArchiveInternalError {
		boolean result;
		profiler.start("exists");
		result = delegate.exists(identifier);
		profiler.end();
		return result;
	}

	/**
	 * @param entity
	 * @throws IllegalEntity
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#forceUpdate(alma.xmlentity.XmlEntityStruct)
	 */
	public void forceUpdate(XmlEntityStruct entity) throws IllegalEntity,
			ArchiveInternalError, TimestampInconsistency {
		profiler.start("forceUpdate");
		delegate.forceUpdate(entity);
		profiler.end();
	}

	/**
	 * @param query
	 * @param schema
	 * @return
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#query(java.lang.String, java.lang.String)
	 */
	public Cursor query(String query, String schema)
			throws ArchiveInternalError {
		Cursor result;
		profiler.start("query");
		result = delegate.query(query, schema);
		profiler.end();
		return result;
	}

	/**
	 * @param query
	 * @param schema
	 * @return
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#queryContent(java.lang.String, java.lang.String)
	 */
	public Cursor queryContent(String query, String schema)
			throws ArchiveInternalError {
		Cursor result;
		profiler.start("queryContent");
		result = delegate.queryContent(query, schema);
		profiler.end();
		return result;
	}

	/**
	 * @param query
	 * @param schema
	 * @return
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#queryDirty(java.lang.String, java.lang.String)
	 */
	public Cursor queryDirty(String query, String schema)
			throws ArchiveInternalError {
		Cursor result;
		profiler.start("queryDirty");
		result = delegate.queryDirty(query, schema);
		profiler.end();
		return result;
	}

	/**
	 * @param schemaname
	 * @param timestamp
	 * @return
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#queryRecent(java.lang.String, java.lang.String)
	 */
	public String[] queryRecent(String schemaname, String timestamp)
			throws ArchiveInternalError {
		String[] result;
		profiler.start("queryRecent");
		result = delegate.queryRecent(schemaname, timestamp);
		profiler.end();
		return result;
	}

	/**
	 * @param query
	 * @param schema
	 * @return
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#queryUIDs(java.lang.String, java.lang.String)
	 */
	public String[] queryUIDs(String query, String schema)
			throws ArchiveInternalError {
		String[] result;
		profiler.start("queryUIDs");
		result = delegate.queryUIDs(query, schema);
		profiler.end();
		return result;
	}

	/**
	 * @param query
	 * @param schema
	 * @return
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#queryUIDsDirty(java.lang.String, java.lang.String)
	 */
	public String[] queryUIDsDirty(String query, String schema)
			throws ArchiveInternalError {
		String[] result;
		profiler.start("queryUIDsDirty");
		result = delegate.queryUIDsDirty(query, schema);
		profiler.end();
		return result;
	}

	/**
	 * @param identifier
	 * @return
	 * @throws DirtyEntity
	 * @throws MalformedURI
	 * @throws NotFound
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#retrieve(java.lang.String)
	 */
	public XmlEntityStruct retrieve(String identifier) throws DirtyEntity,
			MalformedURI, NotFound, ArchiveInternalError {
		XmlEntityStruct result;
		profiler.start("retrieve");
		result = delegate.retrieve(identifier);
		profiler.end();
		return result;
	}

	/**
	 * @param identifier
	 * @return
	 * @throws MalformedURI
	 * @throws NotFound
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#retrieveDirty(java.lang.String)
	 */
	public XmlEntityStruct retrieveDirty(String identifier)
			throws MalformedURI, NotFound, ArchiveInternalError {
		XmlEntityStruct result;
		profiler.start("retrieveDirty");
		result = delegate.retrieveDirty(identifier);
		profiler.end();
		return result;
	}

	/**
	 * @param identifier
	 * @param id
	 * @return
	 * @throws DirtyEntity
	 * @throws MalformedURI
	 * @throws NotFound
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#retrieveFragment(java.lang.String, java.lang.String)
	 */
	public String[] retrieveFragment(String identifier, String id)
			throws DirtyEntity, MalformedURI, NotFound, ArchiveInternalError {
		String[] result;
		profiler.start("retrieveFragment");
		result = delegate.retrieveFragment(identifier, id);
		profiler.end();
		return result;
	}

	/**
	 * @param identifier
	 * @return
	 * @throws MalformedURI
	 * @throws NotFound
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#status(java.lang.String)
	 */
	public StatusStruct status(String identifier) throws MalformedURI,
			NotFound, ArchiveInternalError {
		StatusStruct result;
		profiler.start("status");
		result = delegate.status(identifier);
		profiler.end();
		return result;
	}

	/**
	 * @param entity
	 * @throws IllegalEntity
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#store(alma.xmlentity.XmlEntityStruct)
	 */
	public void store(XmlEntityStruct entity) throws IllegalEntity,
			ArchiveInternalError {
		profiler.start("store");
		delegate.store(entity);
		profiler.end();
	}

	/**
	 * @param identifier
	 * @throws MalformedURI
	 * @throws NotFound
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#undelete(java.lang.String)
	 */
	public void undelete(String identifier) throws MalformedURI, NotFound,
			ArchiveInternalError {
		profiler.start("undelete");
		delegate.undelete(identifier);
		profiler.end();
	}

	/**
	 * @param entity
	 * @throws IllegalEntity
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#update(alma.xmlentity.XmlEntityStruct)
	 */
	public void update(XmlEntityStruct entity) throws IllegalEntity,
			ArchiveInternalError, TimestampInconsistency{
		profiler.start("update");
		delegate.update(entity);
		profiler.end();
	}

	/**
	 * @param uid
	 * @param schema
	 * @param xPath
	 * @param xmlElement
	 * @throws NotYetThere
	 * @throws MalformedURI
	 * @throws IllegalEntity
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#updateElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void updateElement(String uid, String schema, String xPath,
			String xmlElement) throws NotYetThere, MalformedURI, IllegalEntity,
			ArchiveInternalError {
		profiler.start("updateElement");
		delegate.updateElement(uid, schema, xPath, xmlElement);
		profiler.end();
	}

	/**
	 * @param identifier
	 * @return
	 * @throws DirtyEntity
	 * @throws MalformedURI
	 * @throws NotFound
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#updateRetrieve(java.lang.String)
	 */
	public XmlEntityStruct updateRetrieve(String identifier)
			throws DirtyEntity, MalformedURI, NotFound, ArchiveInternalError {
		XmlEntityStruct result;
		profiler.start("updateRetrieve");
		result = delegate.updateRetrieve(identifier);
		profiler.end();
		return result;
	}

	/**
	 * @param uid
	 * @param schema
	 * @param newChild
	 * @throws NotYetThere
	 * @throws MalformedURI
	 * @throws IllegalEntity
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#updateXML(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void updateXML(String uid, String schema, String newChild)
			throws NotYetThere, MalformedURI, IllegalEntity,
			ArchiveInternalError {
		profiler.start("updateXML");
		delegate.updateXML(uid, schema, newChild);
		profiler.end();
	}
	/* Delegation
	 * ------------------------------------------------------------- */
}
