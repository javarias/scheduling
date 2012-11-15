package alma.scheduling.datamodel.executive.dao;

import java.util.List;

import alma.scheduling.input.executive.generated.PI;


/**
 * DAO works over ALMA's user registry
 * 
 * @author javarias
 * 
 */
public interface UserRegistryDao {

	public List<PI> getAllPI();
}
