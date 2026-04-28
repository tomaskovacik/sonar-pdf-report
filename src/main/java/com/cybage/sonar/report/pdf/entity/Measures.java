package com.cybage.sonar.report.pdf.entity;

import java.util.*;

/**
 * This class encapsulates the measures info.
 */
public class Measures {

	private Map<String, Measure> measuresTable = new HashMap<>();
	private List<LeakPeriod>        periods;

	public int getMeasuresCount() {
		return measuresTable.size();
	}

	public Set<String> getMeasuresKeys() {
		return measuresTable.keySet();
	}

	public Measure getMeasure(final String key) {
		return measuresTable.get(key);
	}

	public void addMeasure(final String name, final Measure value) {
		measuresTable.put(name, value);
	}

	public boolean containsMeasure(final String measureKey) {
		return measuresTable.containsKey(measureKey);
	}

	public Optional<LeakPeriod> getPeriod(Integer index) {
		return periods.stream().filter(p -> p.getIndex().equals(index)).findFirst();
	}

	public Optional<LeakPeriod> getPeriod(String mode) {
		return periods.stream().filter(p -> p.getMode().equals(mode)).findFirst();
	}

	public void setPeriods(List<LeakPeriod> periods) {
		this.periods = periods;
	}
	
	public List<LeakPeriod> getPeriods() {
		return this.periods;
	}

	@Override
	public String toString() {
		return "Measures [measuresTable=" + measuresTable + ", periods=" + periods + "]";
	}

}
