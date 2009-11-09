package alma.scheduling.test;

import java.util.logging.Logger;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.client.AdvancedComponentClient;
import alma.acs.component.client.ComponentClient;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.logging.ClientLogManager;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Cursor;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;
import alma.xmlstore.ArchiveConnectionPackage.ArchiveException;
import alma.xmlstore.ArchiveConnectionPackage.PermissionException;
import alma.xmlstore.ArchiveConnectionPackage.UserDoesNotExistException;
import alma.xmlstore.CursorPackage.QueryResult;

public class TestArchive extends ComponentClientTestCase {


    // The archive's components
    private ArchiveConnection archConnectionComp;
    private Identifier archIdentifierComp;
    private Operational archOperationComp;
    //Entity deserializer - makes entities from the archive human readable
    private EntityDeserializer entityDeserializer;
    //Entity Serializer - prepares entites for the archive
    private EntitySerializer entitySerializer;
    private Logger m_logger = null;
    ContainerServices cs;
    String manager;
    ComponentClient m_componentClient = null;

    public TestArchive() throws Exception {
        super("Test Archive");
	testQueryDirty();
    }

    protected void setup() throws Exception{
        super.setUp();
    }
    protected void tearDown() throws Exception{
        super.tearDown();
    }

    private void getArchiveComponents() {
	m_logger =ClientLogManager.getAcsLogManager().getLoggerForApplication("SchedulingAlarmTestClient",true);
        m_logger.info("about to get archive comps");
        System.out.println("getting comps");
	manager = System.getProperty("ACS.manager");
        try {
	    m_componentClient = new AdvancedComponentClient(m_logger,manager,"PIWebPage");
	    cs = m_componentClient.getContainerServices();
            m_logger.info("SCHED_TEST: Getting archive components");
            org.omg.CORBA.Object obj = cs.getDefaultComponent("IDL:alma/xmlstore/ArchiveConnection:1.0");
            this.archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(obj);
            
            this.archConnectionComp.getAdministrative("SCHED_TEST").init();
            this.archOperationComp = archConnectionComp.getOperational("SCHED_TEST");
            this.archIdentifierComp = alma.xmlstore.IdentifierHelper.narrow(
                    cs.getDefaultComponent(
                        "IDL:alma/xmlstore/Identifier:1.0"));
        } catch(AcsJContainerServicesEx e) {
            m_logger.severe("SCHED_TEST: AcsJContainerServicesEx: "+e.toString());
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
            e.printStackTrace();
            System.exit(0);
        } catch (ArchiveException e) {
            m_logger.severe("SCHED_TEST: Archive error: "+e.toString());
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
            e.printStackTrace();
            System.exit(0);
        } catch(UserDoesNotExistException e) {
            m_logger.severe("SCHED_TEST: Archive error: "+e.toString());
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
            e.printStackTrace();
            System.exit(0);
        } catch (PermissionException e) {
            m_logger.severe("SCHED_TEST: Archive error: "+e.toString());
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
            e.printStackTrace();
            System.exit(0);
        } catch(ArchiveInternalError e) {
            m_logger.severe("SCHED_TEST: Archive error: "+e.toString());
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
            e.printStackTrace();
            System.exit(0);
        } catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("can not get the component client");
	}
        entitySerializer = EntitySerializer.getEntitySerializer(
            cs.getLogger());
        entityDeserializer = EntityDeserializer.getEntityDeserializer(
            cs.getLogger());
        m_logger.fine("SCHED_TEST: The ALMAArchive has been constructed.");
    }

    public void testQueryDirty() throws Exception {
        getArchiveComponents();
        XmlEntityStruct xml = null;
        String query = "/prj:ObsProject";
        String schema = "ObsProject";
        Cursor cursor = archOperationComp.queryDirty(query,schema);
        if(cursor == null) {
            m_logger.severe("SCHEDULING: cursor was null when querying ObsProjects");
            return;
        } else {
            m_logger.finest("SCHEDULING: cursor not null!");
        }
        while(cursor.hasNext()) {
            QueryResult res = cursor.next();
            try {
                xml = archOperationComp.retrieveDirty(res.identifier);
                m_logger.info(xml.xmlString);
                ObsProject p = (ObsProject)entityDeserializer.deserializeEntity
                    (xml, ObsProject.class);
                assertNotNull(p);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
	    TestArchive testArchive = new TestArchive();
            //alma.acs.testsupport.tat.TATJUnitRunner.run(TestArchive.class);
	    //junit.textui.TestRunner.run(TestArchive.class);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

