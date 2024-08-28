package com.vitalsync.vital_sync.ui;

import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;

import java.util.ArrayList;
import java.util.List;

public class EcgPlotter {
    private static final String TAG = "EcgPlotter";
    private static final int SECONDS_TO_PLOT = 5;

    private PlotterListener listener;
    private List<Number> plotNumbers;
    private AdvancedLineAndPointRenderer.Formatter formatter;
    private XYSeries series;
    private int dataIndex = 0;

    public EcgPlotter(String title, int ecgFrequency) {
        int ySamplesSize = ecgFrequency * SECONDS_TO_PLOT;
        plotNumbers = new ArrayList<>(ySamplesSize);
        for (int i = 0; i < ySamplesSize; i++) {
            plotNumbers.add(null);
        }
        formatter = new AdvancedLineAndPointRenderer.Formatter();
        formatter.setLegendIconEnabled(false);
        series = new SimpleXYSeries(plotNumbers, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, title);
    }

    public SimpleXYSeries getSeries() {
        return (SimpleXYSeries) series;
    }

    public AdvancedLineAndPointRenderer.Formatter getFormatter() {
        return formatter;
    }

    public void sendSingleSample(float mV) {
        plotNumbers.set(dataIndex, mV);
        if (dataIndex >= plotNumbers.size() - 1) {
            dataIndex = 0;
        }
        if (dataIndex < plotNumbers.size() - 1) {
            plotNumbers.set(dataIndex + 1, null);
        } else {
            plotNumbers.set(0, null);
        }

        ((SimpleXYSeries) series).setModel(plotNumbers, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
        dataIndex++;
        if (listener != null) {
            listener.update();
        }
    }

    public void setListener(PlotterListener listener) {
        this.listener = listener;
    }

    public interface PlotterListener {
        void update();
    }
}

