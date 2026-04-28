package com.cybage.sonar.report.pdf.entity;

import java.util.Optional;

/**
 * This class contains the configuration of the LeakPeriod.
 */
public class LeakPeriodConfiguration {
    private String configurationValue = null;

    /**
     * Update the configuration using a property provided by the SonarScanner configuration.
     *
     * @param configurationValue the configuration value.
     */
    public void update(String configurationValue) {

        this.configurationValue = configurationValue;
    }

    /**
     * Gets period.
     *
     * @param measures the measures
     * @return the period
     */
    public Optional<Period_> getPeriod(Measures measures) {
        if (configurationValue != null) {
            return measures.getPeriod(this.configurationValue);
        } else if (measures != null && measures.getPeriods() != null && !measures.getPeriods().isEmpty()) {
            Period_ period_ = measures.getPeriods().get(0);
            this.configurationValue = period_.getMode();
            return Optional.of(period_);
        } else {
            throw new UnsupportedOperationException("Cannot find the LeakPeriodConfiguration to read the value, please specify the sonar.leakperiod property.");
        }
    }

    @Override
    public String toString() {
        return "LeakPeriodConfiguration{" +
                "configurationValue='" + configurationValue + '\'' +
                '}';
    }
}
