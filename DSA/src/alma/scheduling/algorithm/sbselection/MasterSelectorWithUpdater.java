package alma.scheduling.algorithm.sbselection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

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
       // for(ModelUpdater up: fullUpdates)
       //     up.update(date, sbs);
        for(SchedBlock sb: sbs){
            for(ModelUpdater up: partialUpdates){
                up.update(date, sb);
            }
            for(SchedBlockSelector s: selectors){
                if(!s.canBeSelected(sb)){
                    trash.add(sb);
                    break;
                }
            }
        }
        for(SchedBlock sb: trash)
            this.sbs.remove(sb);
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
