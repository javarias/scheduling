package alma.scheduling.algorithm;

import java.util.List;

public interface AlgorithmPart {

    List<AlgorithmPart> getAlgorithmDependencies();
}
