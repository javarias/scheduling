package alma.scheduling.datamodel;

import java.util.Date;

/**
 * Marker interface to denote a part of the data model that will be
 * periodically updated, because its attributes are time dependent.
 * @author rhiriart
 *
 */
public interface Updateable {

    public Date getLastUpdate();

    public void setLastUpdate(Date lastUpdate);

    public Date getValidUntil();

    public void setValidUntil(Date validUntil);
    
}
