/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
 * XML Schema.
 * $Id: EntityT.java,v 1.1 2003/11/06 23:29:23 sroberts Exp $
 */

package alma.scheduling.planning_mode_sim.define.acs.commonentity;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

/**
 * 
 * 
 * @version $Revision: 1.1 $ $Date: 2003/11/06 23:29:23 $
**/
public class EntityT implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _entityId;

    private java.lang.String _entityIdEncrypted;

    private java.lang.String _entityTypeName;

    private java.lang.String _schemaVersion;

    private java.lang.String _documentVersion;


      //----------------/
     //- Constructors -/
    //----------------/

    public EntityT() {
        super();
    } //-- alma.entities.commonentity.EntityT()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'documentVersion'.
     * 
     * @return the value of field 'documentVersion'.
    **/
    public java.lang.String getDocumentVersion()
    {
        return this._documentVersion;
    } //-- java.lang.String getDocumentVersion() 

    /**
     * Returns the value of field 'entityId'.
     * 
     * @return the value of field 'entityId'.
    **/
    public java.lang.String getEntityId()
    {
        return this._entityId;
    } //-- java.lang.String getEntityId() 

    /**
     * Returns the value of field 'entityIdEncrypted'.
     * 
     * @return the value of field 'entityIdEncrypted'.
    **/
    public java.lang.String getEntityIdEncrypted()
    {
        return this._entityIdEncrypted;
    } //-- java.lang.String getEntityIdEncrypted() 

    /**
     * Returns the value of field 'entityTypeName'.
     * 
     * @return the value of field 'entityTypeName'.
    **/
    public java.lang.String getEntityTypeName()
    {
        return this._entityTypeName;
    } //-- java.lang.String getEntityTypeName() 

    /**
     * Returns the value of field 'schemaVersion'.
     * 
     * @return the value of field 'schemaVersion'.
    **/
    public java.lang.String getSchemaVersion()
    {
        return this._schemaVersion;
    } //-- java.lang.String getSchemaVersion() 

    /**
     * Sets the value of field 'documentVersion'.
     * 
     * @param documentVersion the value of field 'documentVersion'.
    **/
    public void setDocumentVersion(java.lang.String documentVersion)
    {
        this._documentVersion = documentVersion;
    } //-- void setDocumentVersion(java.lang.String) 

    /**
     * Sets the value of field 'entityId'.
     * 
     * @param entityId the value of field 'entityId'.
    **/
    public void setEntityId(java.lang.String entityId)
    {
        this._entityId = entityId;
    } //-- void setEntityId(java.lang.String) 

    /**
     * Sets the value of field 'entityIdEncrypted'.
     * 
     * @param entityIdEncrypted the value of field
     * 'entityIdEncrypted'.
    **/
    public void setEntityIdEncrypted(java.lang.String entityIdEncrypted)
    {
        this._entityIdEncrypted = entityIdEncrypted;
    } //-- void setEntityIdEncrypted(java.lang.String) 

    /**
     * Sets the value of field 'entityTypeName'.
     * 
     * @param entityTypeName the value of field 'entityTypeName'.
    **/
    public void setEntityTypeName(java.lang.String entityTypeName)
    {
        this._entityTypeName = entityTypeName;
    } //-- void setEntityTypeName(java.lang.String) 

    /**
     * Sets the value of field 'schemaVersion'.
     * 
     * @param schemaVersion the value of field 'schemaVersion'.
    **/
    public void setSchemaVersion(java.lang.String schemaVersion)
    {
        this._schemaVersion = schemaVersion;
    } //-- void setSchemaVersion(java.lang.String) 


}
