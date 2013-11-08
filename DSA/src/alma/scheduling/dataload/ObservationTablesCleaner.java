package alma.scheduling.dataload;

import org.hibernate.Transaction;

import alma.scheduling.datamodel.GenericDaoImpl;

public class ObservationTablesCleaner extends GenericDaoImpl implements DataLoader{

	@Override
	public void load() throws Exception {
		//Nothing to load, the observation data is created during observation executions
	}

	@Override
	public void clear() {
		logger.info("Starting cleaning of Observation generated data");
		String qStrS = new String("delete from Session");
		String qStrEb = new String("delete from ExecBlock");
		String qStrCa = new String("delete from CreatedArray");
		Transaction tx = getSession().beginTransaction();
		try {
			int r = getSession().createQuery(qStrS).executeUpdate();
			logger.debug(qStrS + " Modified " +  r + "rows.");
			r = getSession().createQuery(qStrEb).executeUpdate();
			logger.debug(qStrEb + " Modified " +  r + "rows.");
			r = getSession().createQuery(qStrCa).executeUpdate();
			logger.debug(qStrCa + " Modified " +  r + "rows.");
			tx.commit();
		} catch (RuntimeException ex) {
			tx.rollback();
			logger.error("Problem found :" + ex.getMessage());
			throw ex;
		}
		finally {
			logger.info("Cleaning of Observation generated data completed.");
		}
	}

}
