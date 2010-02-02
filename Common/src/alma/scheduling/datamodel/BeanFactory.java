package alma.scheduling.datamodel;

import java.util.HashMap;
import java.util.List;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.input.executive.generated.ExecutiveData;

public class BeanFactory {
    
    /**
     * Converts the data from XML generated classes to POJO domain classes.
     * 
     * The references to the lists cannot be null.
     * 
     * @param data the data to be converted generated from XML (castor)
     * @param execOut the List where to store the of converted Executives
     * @param piOut the List where to store the converted PIs
     * @param epOut the List where to store the converted ExecutivePercentages
     * @param osOut the List where to store the converted ObservingSeasons
     * @param etsOut the List to store the converted ExecutiveTimeSpent
     * @throws NullPointerException when any of the Lists is null
     */
    public static void copyExecutiveFromXMLGenerated(ExecutiveData data,
            List<Executive> execOut, List<PI> piOut, List<ExecutivePercentage> epOut,
            List<ObservingSeason> osOut){
        if (data == null)
            throw new NullPointerException("Executive data input cannot be null");
        if (execOut == null)
            throw new NullPointerException("execOut cannot be null");
        if (piOut == null)
            throw new NullPointerException("piOut cannot be null");
        if (epOut == null)
            throw new NullPointerException("epOut cannot be null");
        if (osOut == null)
            throw new NullPointerException("osOut cannot be null");
        
        HashMap<String, Executive> exec =  new HashMap<String, Executive>();
        HashMap<String, ExecutivePercentage> ep = new HashMap<String, ExecutivePercentage>();
        HashMap<String, ObservingSeason> os= new HashMap<String, ObservingSeason>();
        
        for(int i = 0; i < data.getExecutivePercentageCount(); i++){
            ep.put(data.getExecutivePercentage(i).getId(),
                    ExecutivePercentage.copy(data.getExecutivePercentage(i)));
        }
        for(int i = 0; i < data.getObservingSeasonCount(); i++){
            ObservingSeason tmp = ObservingSeason.copy(data.getObservingSeason(i));
            os.put(data.getObservingSeason(i).getId(), tmp);
            for (int j = 0; j < data.getObservingSeason(i).getExecutivePercentageRefCount(); j++){
                ExecutivePercentage epTmp = ep.get(
                        data.getObservingSeason(i).getExecutivePercentageRef(j).getIdRef());
                tmp.getExecutivePercentage().add(epTmp);
            }
        }
        for(int i = 0; i < data.getExecutiveCount(); i++){
            Executive tmp = Executive.copy(data.getExecutive(i));
            exec.put(data.getExecutive(i).getName(),
                    tmp);
            for(int j = 0 ; j < data.getExecutive(i).getExecutivePercentageRefCount(); j++){
                ExecutivePercentage epTmp= 
                    ep.get(data.getExecutive(i).getExecutivePercentageRef(j).getIdRef());
                tmp.getExecutivePercentage().add(epTmp);
            }
        }
        
        for (int i = 0; i < data.getPICount(); i++){
            PI tmp = PI.copy(data.getPI(i), exec);
            piOut.add(tmp);
        }
        
        execOut.addAll(exec.values());
        epOut.addAll(ep.values());
        osOut.addAll(os.values());
        
    }
}