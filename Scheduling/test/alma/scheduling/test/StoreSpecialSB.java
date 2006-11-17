package alma.scheduling.test;

import alma.acs.container.ContainerServices;
import alma.acs.component.client.ComponentClient;

import alma.xmlstore.Operational;
import alma.xmlstore.ArchiveConnection;

import alma.entity.xmlbinding.specialsb.*;
import alma.entity.xmlbinding.specialsb.types.*;

import alma.xmlentity.XmlEntityStruct;

import alma.scheduling.Define.DateTime;

public class StoreSpecialSB {

    private ContainerServices cs;
    //private SpecialSB sb;
    private ArchiveConnection archConn;
    private Operational archive;
    private XmlEntityStruct xml;

    public StoreSpecialSB() {
        String name = "SpecialSBTest";
        String manager = System.getProperty("ACS.manager");
        try {
            ComponentClient client = new ComponentClient (null, manager, name);
            cs = client.getContainerServices();
            this.archConn = alma.xmlstore.ArchiveConnectionHelper.narrow( 
                    cs.getComponent("ARCHIVE_CONNECTION"));
            this.archive = archConn.getOperational("SCHEDULING");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void createSpecialSB() {
        SpecialSB sb = new SpecialSB();

        sb.setCalendarId("Calendar Id");
        sb.setDescription("This is for testing fixed-time sbs");
        sb.setStartTime(DateTime.unixToAcs(System.currentTimeMillis() + 10000));
        sb.setEndTime(DateTime.unixToAcs(System.currentTimeMillis() + 20000));
        sb.setName("Special SB");
        sb.setPriority("high");
        sb.setReason(ReasonT.TEST); 
        sb.setReqId(1);
        sb.setStaffMember("Sohaila");
        
        SpecialSBEntityT entity = new SpecialSBEntityT();
        try {
            cs.assignUniqueEntityId(entity);
        } catch(Exception e) {
            e.printStackTrace();
        }
        sb.setSpecialSBEntity(entity);
        try {
            xml = alma.acs.entityutil.EntitySerializer.getEntitySerializer
                (cs.getLogger()).serializeEntity(sb, entity);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    public void store() {
        try { 
            archive.store(xml);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args){

        StoreSpecialSB storer = new StoreSpecialSB();
        storer.createSpecialSB();
        storer.store();
        
        
    }
}
