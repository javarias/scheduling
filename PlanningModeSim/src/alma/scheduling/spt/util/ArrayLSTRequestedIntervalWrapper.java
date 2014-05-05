package alma.scheduling.spt.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

import alma.scheduling.input.observatory.generated.ArrayLSTRequestedInterval;
import alma.scheduling.input.observatory.generated.IntervalRequested;
import alma.scheduling.input.observatory.generated.types.ArrayTypeT;

public class ArrayLSTRequestedIntervalWrapper {

	private final ArrayLSTRequestedInterval a;

	public ArrayLSTRequestedIntervalWrapper(ArrayLSTRequestedInterval a) {
		super();
		this.a = a;
	}

	public boolean isValid() {
		return a.isValid();
	}

	public void marshal(Writer out) throws MarshalException,
			ValidationException {
		a.marshal(out);
	}

	public void addIntervalRequested(IntervalRequested vIntervalRequested)
			throws IndexOutOfBoundsException {
		a.addIntervalRequested(vIntervalRequested);
	}

	public void addIntervalRequested(int index,
			IntervalRequested vIntervalRequested)
			throws IndexOutOfBoundsException {
		a.addIntervalRequested(index, vIntervalRequested);
	}

	public void marshal(ContentHandler handler) throws IOException,
			MarshalException, ValidationException {
		a.marshal(handler);
	}

	public void deleteMaxBaseLine() {
		a.deleteMaxBaseLine();
	}

	public void deleteMinBaseLine() {
		a.deleteMinBaseLine();
	}

	public void deleteNumberOfAntennas() {
		a.deleteNumberOfAntennas();
	}

	public Enumeration<? extends IntervalRequested> enumerateIntervalRequested() {
		return a.enumerateIntervalRequested();
	}

	public String getArrayName() {
		return a.getArrayName();
	}

	public ArrayTypeT getArrayType() {
		return a.getArrayType();
	}

	public IntervalRequested getIntervalRequested(int index)
			throws IndexOutOfBoundsException {
		return a.getIntervalRequested(index);
	}

	public String getConfigurationName() {
		return a.getConfigurationName();
	}

	public Date getEndTime() {
		return a.getEndTime();
	}

	public double getMaxBaseLine() {
		return a.getMaxBaseLine();
	}

	public void validate() throws ValidationException {
		a.validate();
	}

	public double getMinBaseLine() {
		return a.getMinBaseLine();
	}

	public IntervalRequested[] getIntervalRequested() {
		return a.getIntervalRequested();
	}

	public int getNumberOfAntennas() {
		return a.getNumberOfAntennas();
	}

	public Date getStartTime() {
		return a.getStartTime();
	}

	public boolean hasMaxBaseLine() {
		return a.hasMaxBaseLine();
	}

	public boolean hasMinBaseLine() {
		return a.hasMinBaseLine();
	}

	public int getIntervalRequestedCount() {
		return a.getIntervalRequestedCount();
	}

	public boolean hasNumberOfAntennas() {
		return a.hasNumberOfAntennas();
	}

	public Iterator<? extends IntervalRequested> iterateIntervalRequested() {
		return a.iterateIntervalRequested();
	}

	public void setArrayName(String arrayName) {
		a.setArrayName(arrayName);
	}

	public void removeAllIntervalRequested() {
		a.removeAllIntervalRequested();
	}

	public void setArrayType(ArrayTypeT arrayType) {
		a.setArrayType(arrayType);
	}

	public boolean removeIntervalRequested(IntervalRequested vIntervalRequested) {
		return a.removeIntervalRequested(vIntervalRequested);
	}

	public void setConfigurationName(String configurationName) {
		a.setConfigurationName(configurationName);
	}

	public IntervalRequested removeIntervalRequestedAt(int index) {
		return a.removeIntervalRequestedAt(index);
	}

	public void setEndTime(Date endTime) {
		a.setEndTime(endTime);
	}

	public void setMaxBaseLine(double maxBaseLine) {
		a.setMaxBaseLine(maxBaseLine);
	}

	public void setIntervalRequested(int index,
			IntervalRequested vIntervalRequested)
			throws IndexOutOfBoundsException {
		a.setIntervalRequested(index, vIntervalRequested);
	}

	public void setMinBaseLine(double minBaseLine) {
		a.setMinBaseLine(minBaseLine);
	}

	public void setNumberOfAntennas(int numberOfAntennas) {
		a.setNumberOfAntennas(numberOfAntennas);
	}

	public void setIntervalRequested(IntervalRequested[] vIntervalRequestedArray) {
		a.setIntervalRequested(vIntervalRequestedArray);
	}

	public void setStartTime(Date startTime) {
		a.setStartTime(startTime);
	}

	public String toString() {
		return a.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getArrayType() == null) ? 0 : getArrayType().hashCode());
		result += ((getConfigurationName() == null) ? 0 : getConfigurationName().hashCode());
		result += new Double(getMaxBaseLine()).hashCode();
		result += new Double(getMinBaseLine()).hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null && !(o instanceof ArrayLSTRequestedIntervalWrapper))
			return false;
		ArrayLSTRequestedIntervalWrapper aIntW = (ArrayLSTRequestedIntervalWrapper) o;
		if (!getConfigurationName().equals(aIntW.getConfigurationName()))
			return false;
		if (!getArrayType().equals(aIntW.getArrayType()))
			return false;
		if (!(getMinBaseLine() == aIntW.getMinBaseLine()))
			return false;
		if (!(getMaxBaseLine() == aIntW.getMaxBaseLine()))
			return false;
		return true;
	}
	
}
