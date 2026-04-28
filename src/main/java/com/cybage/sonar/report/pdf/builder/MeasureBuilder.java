package com.cybage.sonar.report.pdf.builder;

import com.cybage.sonar.report.pdf.entity.Measure;
import com.cybage.sonar.report.pdf.entity.Period;
import com.cybage.sonar.report.pdf.entity.Period_;
import org.sonarqube.ws.Common.Metric;
import org.sonarqube.ws.Measures;

import java.util.ArrayList;
import java.util.List;

public class MeasureBuilder {

    /**
     * Init measure from WS response node.
     *
     * @param measureNode the measure node
     * @param periods_    the response-level periods (may be empty in SonarQube 10.x+)
     * @param metric      the metric
     * @return measure
     */
    public static Measure initFromNode(final Measures.Measure measureNode, List<Period_> periods_,
                                       Metric metric) {
        List<Period> periods = new ArrayList<>();
        // SonarQube 10.x+ returns a single new-code period value per measure instead of a list.
        if (measureNode.hasPeriod()) {
            Measures.PeriodValue pv = measureNode.getPeriod();
            periods.add(new Period(pv.getIndex(), pv.getValue()));
        }
        return newMeasure(measureNode, periods, metric);
    }

    private static Measure newMeasure(final Measures.Measure measureNode, final List<Period> periods, final Metric metric) {
        return new Measure(measureNode.getMetric(),
                measureNode.getValue(),
                metric.getName(),
                metric.getType(),
                metric.getDomain(),
                metric.getHigherValuesAreBetter(), periods);
    }
}
