package alma.scheduling.algorithm.modelupd;

import java.util.Date;

public interface ModelUpdater {
    
    public boolean needsToUpdate(Date date);
    
    public void update();
}
