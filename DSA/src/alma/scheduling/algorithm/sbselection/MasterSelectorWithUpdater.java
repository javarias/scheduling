package alma.scheduling.algorithm.sbselection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.DSAErrorStruct;
import alma.scheduling.utils.ErrorHandling;

public class MasterSelectorWithUpdater extends MasterSelector implements
        SchedBlockSelector, ModelUpdater{

    private Collection<ModelUpdater> partialUpdates;
    private Collection<ModelUpdater> fullUpdates;
    private Collection<SchedBlock> sbs; 
   
    public Collection<ModelUpdater> getPartialUpdates() {
        return partialUpdates;
    }

    public void setPartialUpdates(Collection<ModelUpdater> partialUpdates) {
        this.partialUpdates = partialUpdates;
    }

    public Collection<ModelUpdater> getFullUpdates() {
        return fullUpdates;
    }

    public void setFullUpdates(Collection<ModelUpdater> fullUpdates) {
        this.fullUpdates = fullUpdates;
    }

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        return sbs;
    }

    @Override
    public synchronized void update(Date date, Collection<SchedBlock> sbs) {
        this.sbs = sbs;
        ArrayList<SchedBlock> trash = new ArrayList<SchedBlock>();
        ArrayList<DSAErrorStruct> updateErrors =  new ArrayList<DSAErrorStruct>();
        ArrayList<DSAErrorStruct> selectorErrors =  new ArrayList<DSAErrorStruct>();
       // for(ModelUpdater up: fullUpdates)
       //     up.update(date, sbs);
        for(SchedBlock sb: sbs){
            for(ModelUpdater up: partialUpdates){
            	try {
            		up.update(date, sb);
            	} catch(Exception ex) {
            		updateErrors.add(new DSAErrorStruct(up.getClass().getCanonicalName(), 
            				sb.getUid(), "SchedBlock", ex));
            		trash.add(sb);
            	}
            }
            for(SchedBlockSelector s: selectors){
            	try {
            		if(!s.canBeSelected(sb)){
            			trash.add(sb);
            			break;
            		}
            	}catch (Exception ex) {
            		selectorErrors.add(new DSAErrorStruct(s.getClass().getCanonicalName(), 
            				sb.getUid(), "SchedBlock", ex));
            		trash.add(sb);
            	}
            }
        }
        //Report errors
        if (updateErrors.size() > 0 && updateErrors.size() != sbs.size()) {
        	for(DSAErrorStruct struct: updateErrors)
        		ErrorHandling.getInstance().warning(
        				"Failed " + struct.getDSAPart() + 
        				" when updating " + struct.getEntityType() + 
        				" Id: " + struct.getEntityId(), struct.getException());
        }
        else if (updateErrors.size() == sbs.size()) {
        	ErrorHandling.getInstance().severe("Failed " + updateErrors.get(0).getDSAPart()
        			+ ". No " + updateErrors.get(0).getEntityType() + "s were updated"
        			, updateErrors.get(0).getException());
        }
        
        if (selectorErrors.size() > 0 && selectorErrors.size() != sbs.size()) {
        	for(DSAErrorStruct struct: selectorErrors)
        		ErrorHandling.getInstance().warning(
        				"Failed " + struct.getDSAPart() + 
        				" when selecting " + struct.getEntityType() + 
        				" Id: " + struct.getEntityId(), struct.getException());
        }
        else if (selectorErrors.size() == sbs.size()) {
        	ErrorHandling.getInstance().severe("Failed " + selectorErrors.get(0).getDSAPart()
        			+ ". No " + selectorErrors.get(0).getEntityType() + "s were removed from prvious selection"
        			, selectorErrors.get(0).getException());
        }
        for(SchedBlock sb: trash) {
        	try{
        		this.sbs.remove(sb);
        	} catch (Exception ex) {
        		//Do nothing, it could be that the sb was already removed
        	}
        }
    }

    @Override
    public void update(Date date, SchedBlock sb) {
        //Do nothing
    }

    @Override
    public boolean needsToUpdate(Date date) {
        for(ModelUpdater up: fullUpdates)
            if(up.needsToUpdate(date))
                return true;
        for(ModelUpdater up: partialUpdates){
            if(up.needsToUpdate(date))
                return true;
        }
        return false;
    }

    @Override
    public void update(Date date) {
      //Do nothing
    }
    
}
