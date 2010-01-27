package alma.scheduling.common.test;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import alma.scheduling.persistence.HibernateUtil;


public class ExecutiveDataModelTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        //Configuration cfg = new Configuration().configure();
        //SchemaExport schemaExport = new SchemaExport(cfg);
        //schemaExport.create(false, true);
        Session session =
            HibernateUtil.getSessionFactory().openSession();
        session.close();
        HibernateUtil.shutdown();
    }

}
