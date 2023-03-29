package org.pytorch.serve.metrics.format.prometheous;

import java.util.ArrayList;
import java.util.List;
import org.pytorch.serve.metrics.IMetric;
import org.pytorch.serve.metrics.MetricBuilder;

public abstract class PrometheusMetric extends IMetric {
    public PrometheusMetric(
            MetricBuilder.MetricContext context,
            MetricBuilder.MetricType type,
            String name,
            String unit,
            List<String> dimensionNames) {
        super(context, type, name, unit, dimensionNames);

        if (this.context == MetricBuilder.MetricContext.BACKEND) {
            this.dimensionNames.add("Hostname");
        }
    }

    @Override
    public void addOrUpdate(
            List<String> dimensionValues, String hostname, String requestIds, double value) {
        ArrayList<String> modifiedDimensionValues = new ArrayList<String>(dimensionValues);
        modifiedDimensionValues.add(hostname);
        this.addOrUpdate(modifiedDimensionValues, value);
    }
}