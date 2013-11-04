package alma.scheduling.datamodel.observation.dao;

import java.util.List;

import alma.scheduling.datamodel.observation.CreatedArray;
import alma.scheduling.datamodel.observation.ExecBlock;
import alma.scheduling.datamodel.observation.Session;

/**
 * Declares the basic operations available for Observing data: <br>
 * <ul>
 * <li> {@link ExecBlock}
 * <li> {@link Array}
 * <li> {@link Session}
 * </ul>
 * 
 * @since ALMA-10.6
 * @author javarias
 *
 */
public interface ObservationDao {

	/**
	 * Saves a ExecBlock in storage
	 * @param eb The new ExecBlock to save in storage
	 */
	public void save(ExecBlock eb);
	
	/**
	 * Saves a Array info in storage
	 * @param array The new Array to save
	 */
	public void save(CreatedArray array);
	
	/**
	 * Updates the array info in the storage
	 * @param array The Array to update
	 */
	public void update(CreatedArray array);
	
	/**
	 * Saves an observation session in storage
	 * @param session The new Session to save 
	 */
	public void save(Session session);
	
	/**
	 * Updates an observation Session already in storage
	 * @param session The Session to update
	 */
	public void update(Session session);
	
	
	public List<ExecBlock> getAllExecBlocksForSB(String SbUid);
	
	
	
}
