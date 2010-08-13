package alma.scheduling.dataload;

import alma.scheduling.datamodel.output.dao.OutputDao;

public class OutputDataLoader implements DataLoader {

    private OutputDao outDao;
    
    public OutputDao getOutDao() {
        return outDao;
    }

    public void setOutDao(OutputDao outDao) {
        this.outDao = outDao;
    }

    @Override
    public void clear() {
        outDao.deleteAll();
    }

    @Override
    public void load() throws Exception {
        // Do Nothing.
    }

}
