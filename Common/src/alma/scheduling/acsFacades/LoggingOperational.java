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


/**
 * A facade for the XMLstore Operational which logs calls made to it.
 *
 * @version $Id: LoggingOperational.java,v 1.1 2010/04/20 23:04:14 dclarke Exp $
 * @author David Clarke
 */
public class LoggingOperational
	extends AbstractLoggingComponent
	implements OperationalOperations {

	/** The object for which we are a facade. */
	private OperationalOperations delegate;
	
	private final static int AmountOfXMLToPrint = 25;
	
	/**
	 * Construct this object
	 * 
	 * @throws AcsJContainerServicesEx 
	 */
	public LoggingOperational(ContainerServices     containerServices,
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
		logger.fine(String.format(
				"calling Archive.addElement(%s, %s, %s, %s)",
				uid, schema, xPath, first(AmountOfXMLToPrint, xmlElement)));
		delegate.addElement(uid, schema, xPath, xmlElement);
	}

	/**
	 * @param password
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#close(java.lang.String)
	 */
	public void close(String password) throws ArchiveInternalError {
		logger.fine(String.format(
				"calling Archive.close(%s)",
				password));
		delegate.close(password);
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
		logger.fine(String.format(
				"calling Archive.delete(%s)",
				identifier));
		delegate.delete(identifier);
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
		logger.fine(String.format(
				"calling Archive.deleteElement(%s, %s, %s)",
				uid, schema, xPath));
		delegate.deleteElement(uid, schema, xPath);
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
		logger.fine(String.format(
				"calling Archive.exists(%s)",
				identifier));
		return delegate.exists(identifier);
	}

	/**
	 * @param entity
	 * @throws IllegalEntity
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#forceUpdate(alma.xmlentity.XmlEntityStruct)
	 */
	public void forceUpdate(XmlEntityStruct entity) throws IllegalEntity,
			ArchiveInternalError {
		logger.fine(String.format(
				"calling Archive.forceUpdate(%s)",
				format(entity)));
		delegate.forceUpdate(entity);
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
		logger.fine(String.format(
				"calling Archive.query(%s, %s)",
				query, schema));
		return delegate.query(query, schema);
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
		logger.fine(String.format(
				"calling Archive.queryContent(%s, %s)",
				query, schema));
		return delegate.queryContent(query, schema);
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
		logger.fine(String.format(
				"calling Archive.queryDirty(%s, %s)",
				query, schema));
		return delegate.queryDirty(query, schema);
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
		logger.fine(String.format(
				"calling Archive.queryRecent(%s, %s)",
				schemaname, timestamp));
		return delegate.queryRecent(schemaname, timestamp);
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
		logger.fine(String.format(
				"calling Archive.queryUIDs(%s, %s)",
				query, schema));
		return delegate.queryUIDs(query, schema);
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
		logger.fine(String.format(
				"calling Archive.queryUIDsDirty(%s, %s)",
				query, schema));
		return delegate.queryUIDsDirty(query, schema);
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
		logger.fine(String.format(
				"calling Archive.retrieve(%s)",
				identifier));
		return delegate.retrieve(identifier);
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
		logger.fine(String.format(
				"calling Archive.retrieveDirty(%s)",
				identifier));
		return delegate.retrieveDirty(identifier);
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
		logger.fine(String.format(
				"calling Archive.retrieveFragment(%s, %s)",
				identifier, id));
		return delegate.retrieveFragment(identifier, id);
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
		logger.fine(String.format(
				"calling Archive.status(%s)",
				identifier));
		return delegate.status(identifier);
	}

	/**
	 * @param entity
	 * @throws IllegalEntity
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#store(alma.xmlentity.XmlEntityStruct)
	 */
	public void store(XmlEntityStruct entity) throws IllegalEntity,
			ArchiveInternalError {
		logger.fine(String.format(
				"calling Archive.store(%s)",
				format(entity)));
		delegate.store(entity);
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
		logger.fine(String.format(
				"calling Archive.undelete(%s)",
				identifier));
		delegate.undelete(identifier);
	}

	/**
	 * @param entity
	 * @throws IllegalEntity
	 * @throws ArchiveInternalError
	 * @see alma.xmlstore.OperationalOperations#update(alma.xmlentity.XmlEntityStruct)
	 */
	public void update(XmlEntityStruct entity) throws IllegalEntity,
			ArchiveInternalError {
		logger.fine(String.format(
				"calling Archive.update(%s)",
				format(entity)));
		delegate.update(entity);
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
		logger.fine(String.format(
				"calling Archive.updateElement(%s, %s, %s, %s)",
				uid, schema, xPath, first(AmountOfXMLToPrint, xmlElement)));
		delegate.updateElement(uid, schema, xPath, xmlElement);
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
		logger.fine(String.format(
				"calling Archive.updateRetrieve(%s)",
				identifier));
		return delegate.updateRetrieve(identifier);
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
		logger.fine(String.format(
				"calling Archive.updateXML(%s, %s, %s, %s)",
				uid, schema, first(AmountOfXMLToPrint, newChild)));
		delegate.updateXML(uid, schema, newChild);
	}
	/* Delegation
	 * ------------------------------------------------------------- */
}
