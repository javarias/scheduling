package alma.archive.xml.dao;

import java.util.Collection;
import java.util.List;

import alma.archive.xml.ObsProjectEntity;
import alma.archive.xml.ObsProposalEntity;
import alma.archive.xml.ObsReviewEntity;
import alma.archive.xml.SchedBlockEntity;

/**
 * Generic interface declaring basic methods to access the XMLStore.
 * 
 * @since ALMA-9.1.4
 * @author javarias
 *
 */
public interface XmlStoreReaderDao {

	/**
	 * Execute a XPath Query over the ObsProsals in the DB and return the results.
	 * <br/>
	 * <b>Note: </b>This query could take long time to bring the results.
	 * 
	 * @param XPathQuery a valid xpath query.
	 * @return A list with the results or an empty list if the query failed or the query returned an empty set.
	 */
	public List<ObsProposalEntity> getObsProposals(String XPathQuery);
	
	public Iterable<ObsProposalEntity> getObsProposalsIterator(String XPathQuery);

	/**
	 * Execute a XPath Query over the ObsProjects in the DB and return the results.
	 * <br/>
	 * <b>Note: </b>This query could take long time to bring the results.
	 * 
	 * @param XPathQuery a valid xpath query.
	 * @return A list with the results or an empty list if the query failed or the query returned an empty set.
	 */
	public List<ObsProjectEntity> getObsProjects(String XPathQuery);
	
	public Iterable<ObsProjectEntity> getObsProjectsIterator(String XPathQuery);

	/**
	 * Execute a XPath Query over the SchedBlocks in the DB and return the results.
	 * <br/>
	 * <b>Note: </b>This query could take long time to bring the results.
	 * 
	 * @param XPathQuery a valid xpath query.
	 * @return A list with the results or an empty list if the query failed or the query returned an empty set.
	 */
	public List<SchedBlockEntity> getSchedBlocks(String XPathQuery);
	
	public Iterable<SchedBlockEntity> getSchedBlocksIterator(String XPathQuery);

	/**
	 * Returns the ObsProposals according to the given uids. 
	 * 
	 * @param uids the uids to look into the table. if it is null, this method will retrieve all elements in the table
	 * @return 
	 */
	public List<ObsProposalEntity> getObsProposals(Collection<String> uids);
	
	public Iterable<ObsProposalEntity> getObsProposalsIterator(Collection<String> uids);

	/**
	 * Returns the ObsProjects according to the given uids. 
	 * 
	 * @param uids the uids to look into the table. if it is null, this method will retrieve all elements in the table
	 * @return 
	 */
	public List<ObsProjectEntity> getObsProjects(Collection<String> uids);
	
	public Iterable<ObsProjectEntity> getObsProjectsIterator(Collection<String> uids);

	/**
	 * Returns the SchedBlocks according to the given uids. 
	 * 
	 * @param uids the uids to look into the table. if it is null, this method will retrieve all elements in the table
	 * @return 
	 */
	public List<SchedBlockEntity> getSchedBlocks(Collection<String> uids);
	
	
	public Iterable<SchedBlockEntity> getSchedBlocksIterator(Collection<String> uids);
	
	
	/**
	 * Returns the SchedBlocks according to the given uids. 
	 * 
	 * @param uids the uids to look into the table. if it is null, this method will retrieve all elements in the table
	 * @return 
	 */
	public List<ObsReviewEntity> getObsReviews(Collection<String> uids);
	
	
	public Iterable<ObsReviewEntity> getObsReviewsIterator(Collection<String> uids);
	
	
	/**
	 * Clean up and releases resources of the underlying Data Storage backend
	 */
	public void cleanUp();
	
	

}