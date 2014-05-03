package alma.scheduling.spt.util;

import java.util.Date;
import java.util.Set;

import alma.scheduling.datamodel.observatory.AntennaInstallation;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.ArrayType;

/**
 * convenience class to categorize according to capabilities only.
 * @author javarias
 *
 */
public class ArrayConfigurationCapabilities{

	private final ArrayConfiguration arrConfig;
	
	public ArrayConfigurationCapabilities(ArrayConfiguration arrConfig) {
		this.arrConfig = arrConfig;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getArrayType() == null) ? 0 : getArrayType().hashCode());
		result += ((getConfigurationName() == null) ? 0 : getConfigurationName().hashCode());
		result += ((getMaxBaseline() == null) ? 0 : getMaxBaseline().hashCode());
		result += ((getMinBaseline() == null) ? 0 : getMinBaseline().hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null && !(o instanceof ArrayConfigurationCapabilities))
			return false;
		ArrayConfigurationCapabilities acc = (ArrayConfigurationCapabilities) o;
		if (!getConfigurationName().equals(acc.getConfigurationName()))
			return false;
		if (!getArrayType().equals(acc.getArrayType()))
			return false;
		if (!getMinBaseline().equals(acc.getMinBaseline()))
			return false;
		if (!getMaxBaseline().equals(acc.getMaxBaseline()))
			return false;
		return true;
	}
	
	//delegated methods
	
	public Long getId() {
		return arrConfig.getId();
	}

	public void setId(Long id) {
		arrConfig.setId(id);
	}

	public Date getStartTime() {
		return arrConfig.getStartTime();
	}

	public void setStartTime(Date startTime) {
		arrConfig.setStartTime(startTime);
	}

	public Date getEndTime() {
		return arrConfig.getEndTime();
	}

	public void setEndTime(Date endTime) {
		arrConfig.setEndTime(endTime);
	}

	public Set<AntennaInstallation> getAntennaInstallations() {
		return arrConfig.getAntennaInstallations();
	}

	public void setAntennaInstallations(
			Set<AntennaInstallation> antennaInstallations) {
		arrConfig.setAntennaInstallations(antennaInstallations);
	}

	public Double getResolution() {
		return arrConfig.getResolution();
	}

	public void setResolution(Double resolution) {
		arrConfig.setResolution(resolution);
	}

	public Double getUvCoverage() {
		return arrConfig.getUvCoverage();
	}

	public void setUvCoverage(Double uvCoverage) {
		arrConfig.setUvCoverage(uvCoverage);
	}

	public String getArrayName() {
		return arrConfig.getArrayName();
	}

	public void setArrayName(String arrayName) {
		arrConfig.setArrayName(arrayName);
	}

	public String getConfigurationName() {
		return arrConfig.getConfigurationName();
	}

	public void setConfigurationName(String configurationName) {
		arrConfig.setConfigurationName(configurationName);
	}

	public Integer getNumberOfAntennas() {
		return arrConfig.getNumberOfAntennas();
	}

	public void setNumberOfAntennas(Integer numberOfAntennas) {
		arrConfig.setNumberOfAntennas(numberOfAntennas);
	}

	public Double getMinBaseline() {
		return arrConfig.getMinBaseline();
	}

	public void setMinBaseline(Double minBaseline) {
		arrConfig.setMinBaseline(minBaseline);
	}

	public Double getMaxBaseline() {
		return arrConfig.getMaxBaseline();
	}

	public void setMaxBaseline(Double maxBaseline) {
		arrConfig.setMaxBaseline(maxBaseline);
	}

	public Double getAntennaDiameter() {
		return arrConfig.getAntennaDiameter();
	}

	public void setAntennaDiameter(Double antennaDiameter) {
		arrConfig.setAntennaDiameter(antennaDiameter);
	}

	public ArrayType getArrayType() {
		return arrConfig.getArrayType();
	}

	public void setArrayType(ArrayType arrayType) {
		arrConfig.setArrayType(arrayType);
	}

	public String toString() {
		return arrConfig.toString();
	}
}
