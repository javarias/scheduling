/**
 * ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2005 
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 */

package alma.scheduling.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

import alma.Control.ExecBlockEndedEvent;
import alma.Control.SystemState;
import alma.Control.SystemSubstate;
import alma.Control.Common.Name;
import alma.ControlCommon.AutomaticArray;
import alma.ControlCommon.AutomaticArrayHelper;
import alma.ControlCommon.DeviceConfig;
import alma.ControlCommon.Master;
import alma.ControlCommon.MasterHelper;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerException;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.nc.AbstractNotificationChannel;
import alma.acs.nc.Receiver;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT;
import alma.hla.runtime.asdm.types.EntityId;
import alma.offline.ASDMArchivedEvent;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveConnectionHelper;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Identifier;
import alma.xmlstore.IdentifierHelper;
import alma.xmlstore.Operational;
import alma.xmlstore.OperationalPackage.IllegalEntity;
import alma.asdmIDLTypes.IDLEntityId;
import alma.asdm.Archiver;
import alma.asdm.ASDM;

/**
 * Test the Data Capture Interface.
 *
 * This test case will exercise the interfaces between the CONTROL
 * subsystem and the OFFLINE/DataCapturer component that are used when
 * scheduling blocks are executed. 
 */
public class InitControlMaster extends ComponentClientTestCase {

    private static final String m_masterCurl = "CONTROL/MASTER";
    private static final String[] stateNames = {"INACCESSIBLE", "OPERATIONAL"};
    private static final String[] subStateNames = {"STARTING_UP_PASS1", 
                                                   "STARTED_UP_PASS1", 
                                                   "STARTING_UP_PASS2",
                                                   "WAITING", 
                                                   "SHUTTING_DOWN_PASS1", 
                                                   "SHUT_DOWN_PASS1", 
                                                   "SHUTTING_DOWN_PASS2",
                                                   "STOPPED",
                                                   "NOERROR", 
                                                   "ERROR"};

    private Logger m_logger;
    private Master m_master;
    private AutomaticArray m_array;
    private Receiver m_receiver;
    private boolean m_execBlkReceived = false;
    private boolean m_asdmArchived = false;
    private ExecBlockEndedEvent m_execBlockEndedEvent = null;
    private ASDMArchivedEvent m_asdmArchivedEvent = null;
    
    // The archive's components
    private ArchiveConnection m_archConnectionComp;
    private Operational m_archOperationComp;
    private Identifier m_archIdentifierComp;


    /**
     * Constructor.
     * @throws Exception
     */
    public InitControlMaster() throws Exception {
        super(InitControlMaster.class.getName());
                
    }

    
    /**
     * Initialize the Archive components.
     * @throws Exception
    private void initArchive() throws Exception {
        
        m_archConnectionComp = ArchiveConnectionHelper.narrow(
                getContainerServices().getComponent(Name.ArchiveComponent));
        assertNotNull(m_archConnectionComp);

        m_archOperationComp = m_archConnectionComp.getOperational("InitControlMaster");
        assertNotNull(m_archOperationComp);

        m_archIdentifierComp = IdentifierHelper.narrow(
                                 getContainerServices().getComponent(Name.ArchiveIdentifierComponent));
        assertNotNull(m_archIdentifierComp);

        m_logger.info("Connection to the ALMA Archive has been constructed.");
    }
     */

    /**
     * Release Archive components.
     * @throws Exception
    private void releaseArchive() throws Exception {
        getContainerServices().releaseComponent(Name.ArchiveComponent);
        getContainerServices().releaseComponent(Name.ArchiveIdentifierComponent);
    }

     */
    /**
     * Load an XML document into the Archive.
     * 
     * This function uses the Archive identifier component to get a UID
     * to store an XML document into the Archive. It creates an XmlEntityStruct
     * "by hand" and uses the Archive Operational component to store it into the
     * Archive.
     * 
     * There is an inconsistency in the XML document, as the internal
     * EntityT is not updated with the UID that will be used to store the
     * document. The Archive allows to store the document anyway... One way
     * to make this work would be to manually replace the EntityT for the
     * one got from the Archive. This is unnecessary when using XML entity
     * classes like the APDM, though.
     * 
     * This function is not used by the test cases. I leave it here just 
     * for reference.
     * 
     * @param testSB Scheduling Block XML document
     * @return Entity ID
     * @throws Exception
     * @deprecated
     */
    private String loadXMLDocIntoArchive(String xmlDoc) throws Exception {

        // Assign entity-ids to the container and tables.
        int numberEntities = 1;
        
        // Get entity-ids from the container.
        String[] idList = m_archIdentifierComp.getUIDs((short) numberEntities);
        EntityId[] id = new EntityId [numberEntities];
        id[0] = new EntityId(idList[0]);

        // Store it into the Archive
        try {
            XmlEntityStruct xml = new XmlEntityStruct();
            xml.xmlString = xmlDoc;
            xml.entityId = id[0].toString();
            xml.entityTypeName = "SchedBlock";
            xml.schemaVersion = "1";
            m_archOperationComp.store(xml);
            return xml.entityId;
        } catch(IllegalEntity ex1) {
            m_logger.severe("Illegal entity: " + ex1.toString());
            fail();
        } catch(ArchiveInternalError ex2) {
            m_logger.severe("Archive internal error: " + ex2.toString());
            fail();
        }
        
        return null;
    }

    /**
     * Load a scheduling block into the Archive.
     * 
     * @param schedBlock APDM SchedBlock object
     * @return UID used to store the SchedBlock into the Archive
     * @throws Exception
    private String loadSBIntoArchive(SchedBlock schedBlock) throws Exception {
        
        try {
            EntitySerializer serializer = 
                EntitySerializer.getEntitySerializer(m_logger);
            XmlEntityStruct ent = serializer.serializeEntity(schedBlock, 
                    schedBlock.getSchedBlockEntity());
            m_archOperationComp.store(ent);
            return ent.entityId;
        } catch(IllegalEntity ex1) {
            m_logger.severe("Illegal entity: " + ex1.toString());
            fail();
        } catch(ArchiveInternalError ex2) {
            m_logger.severe("Archive internal error: " + ex2.toString());
            fail();
        }
        
        return null;
    }
     */

    /**
     * Read a file containing a scheduling block XML and creates a SchedBlock
     * object from it.
     * @param filePath Path of the scheduling block XML file 
     * @return SchedBlock object
     * @throws Exception
    public SchedBlock getSchedBlockFromFile(String filePath) throws Exception {
        
        String dirName;
        String fileName;
        int loc;
        if ((loc = filePath.lastIndexOf('/')) >= 0) {
            fileName = filePath.substring(loc + 1);
            dirName = filePath.substring(0, loc);
        } else {
            fileName = filePath;
            dirName = ".";
        }
        
        String xmlDoc = readSBFile(dirName, fileName);
        SchedBlock schedBlock = 
            SchedBlock.unmarshalSchedBlock(new StringReader(xmlDoc));
        SchedBlockEntityT entity = schedBlock.getSchedBlockEntity();
        getContainerServices().assignUniqueEntityId(entity);
        schedBlock.setSchedBlockEntity(entity);
        return schedBlock;
    }
     */
    
    /**
     * Read SB from a file.
     * @param dirName Directory name
     * @param fileName File name
     * @return XML document
     * @throws Exception
    public String readSBFile(String dirName, String fileName) throws Exception {
        
        // Check that the directory exists
        File dir = new File(dirName);
        if (!dir.isDirectory())
            throw new Exception ("Directory " + dirName + " does not exist.");
        
        // Check that the file exists
        File file = new File(dir, fileName);
        if (!file.exists())
            throw new Exception ("File " + fileName + "in directory " + dirName 
                    + " does not exist.");
        
        // Read file contents
        BufferedReader in = null;
        StringBuffer xmlDoc = null;
        String line = null;
        try {
            in = new BufferedReader(new FileReader(file));
            xmlDoc = new StringBuffer ();
            line = in.readLine();
            while (line != null) {
                xmlDoc.append(line + "\n");
                line = in.readLine();
            }
            in.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        return new String(xmlDoc);
    }

     */
    /**
     * Get an XML entity from the Archive.
     * @param uid SB UID
     * @return
     * @throws Exception
    public XmlEntityStruct getEntityFromArchive(String uid) throws Exception {
        return m_archOperationComp.retrieveDirty(uid);
    }
*/

    /**
     * Get scheduling block from the Archive.
     * @param uid SchedBlock UID
     * @return SchedBlock object
     * @throws Exception
     */
    /**
     * Test case fixture setup.
     * 
     * The following operations are performed:
     * <OL>
     *   <LI> Get a reference to the CONTROL/MASTER component.
     *   <LI> Subscribe to the ExecBlockEndedEvent in the CONTROL external
     *   notification channel.
     *   <LI> Get references to the ARCHIVE client components.
     * </OL>
     */
    protected void setUp() throws Exception {
        
        System.out.println("ComponentClientTestCase.setUp()...");
        super.setUp();

        // Get the Logger.
        System.out.println("Getting the logger...");
        // m_logger = getContainerServices().getLogger();
        m_logger = Logger.getLogger("InitControlMaster");

        // Get the Master component.
        m_logger.info("Getting the Master component...");
        m_master = MasterHelper.narrow(getContainerServices().getComponent(m_masterCurl));
        assertNotNull(m_master);

        // Create a consumer for the ExecBlockEndEvent and ASDMArchivedEvent

        m_execBlkReceived = false;
        m_execBlockEndedEvent = null;
        m_asdmArchived = false;
        m_asdmArchivedEvent = null;
    }

    /**
     * Test case fixture clean up.
     */
    protected void tearDown() throws Exception {
        //m_receiver.end();
        //releaseArchive();
        //getContainerServices().releaseComponent(m_masterCurl);
        super.tearDown();
        
    }

    public void performArrayObservation(boolean initMaster) 
        throws Exception {


        if (initMaster) {
            
            // Create a DeviceConfig structure to start the Antenna and its subdevices.
            DeviceConfig[] devConfigs = new DeviceConfig[1];
            devConfigs[0] = new DeviceConfig();
            devConfigs[0].Name = "CONTROL/ALMA00";
//            devConfigs[0].CtrlIdl = "IDL:alma/Control/Antenna:1.0";
//            devConfigs[0].Container = "CONTROL/Test/cppContainer";
//            devConfigs[0].Impl = "antenna";
//            devConfigs[0].subDevice = new DeviceConfig[0];
            devConfigs[0].subdevice = new DeviceConfig[0];
//            devConfigs[0].NodeNumber = -1;
//            devConfigs[0].UniqueIdentifier = "Generic";

            // Pass the DeviceConfig structure to the Master.
            m_master.fillAntennaTables((short) 1, devConfigs);

            // Initialize the Master component.
            m_master.startupPass1();
            m_master.startupPass2();
            
        }

    }


    /**
     * Test the execution of an optical pointing scheduling block.
     * 
     * @throws Exception
     */
    public void testOpticalPointingSchedBlock() throws Exception {
        
        String asdmUID;
        performArrayObservation(true);
    }
    
    
}
