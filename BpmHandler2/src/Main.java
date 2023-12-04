import com.mathworks.engine.MatlabExecutionException;
import com.mathworks.engine.MatlabSyntaxException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.analysis.function.Pow;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class Main {

    public static void main(String[] args) {
        Random random = new Random();

        int[] bpmi = generateHeartRateArray(40, 50, 100);

        ArrayList<Boolean> apneaEvents = new ArrayList<>();
        apneaEvents.add(true);
        apneaEvents.add(false);
        apneaEvents.add(true);
        apneaEvents.add(true);
        apneaEvents.add(false);

        for (int i = 10; i<20;i++){
            bpmi[i] = random.nextInt(90 - 70 + 1) + 70;
        }

        for (int i = 60; i<70;i++){
            bpmi[i] = random.nextInt(90 - 70 + 1) + 70;
        }

        // Create a graph for the filtered ECG data
        createBpmGraph(bpmi);

        if(checkApneaEvent(bpmi, 60, 10)){
            System.out.println("Threshold exceeded");
        }
        else
            System.out.println("Threshold not exceeded");

        long apneaEventsNumber = apneaEvents.stream().filter(Boolean::booleanValue).count();

        System.out.println(apneaEvents.stream().filter(Boolean::booleanValue).count());
    }

    public static int[] generateHeartRateArray(int minRate, int maxRate, int arraySize) {
        if (minRate >= maxRate || arraySize <= 0) {
            throw new IllegalArgumentException("Invalid parameters for heart rate array generation.");
        }

        int[] heartRateArray = new int[arraySize];
        Random random = new Random();

        for (int i = 0; i < arraySize; i++) {
            heartRateArray[i] = random.nextInt(maxRate - minRate + 1) + minRate;
        }

        return heartRateArray;
    }

    //checkApneaEvent() returns true if for an event, a consecutive number of samples exceeds a threshold
    public static boolean checkApneaEvent(int[] heartRateArray, int threshold, int consecutiveSamples) {
        int consecutiveCount = 0;

        for (int heartRate : heartRateArray) {
            if (heartRate > threshold) {
                consecutiveCount++;
                if (consecutiveCount >= consecutiveSamples) {
                    return true;
                }
            } else {
                consecutiveCount = 0; // Reset count if heart rate falls below the threshold
            }
        }

        return false;
    }




    // Method to create a graph for the BPM data
    private static void createBpmGraph(int[] bpmSamples) {
        // Create a new XYSeries for the ECG data
        XYSeries bpmSeries = new XYSeries("Heartrate Data");

        // Populate the series with the ECG samples
        for (int i = 0; i < bpmSamples.length; i++) {
            bpmSeries.add(i, bpmSamples[i]);
        }

        // Create a dataset and add the series to it
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(bpmSeries);

        // Create a chart based on the dataset
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Instant BPM Data Chart", // Chart title
                "Sample Index",            // X-axis label
                "Instant BPM Value",               // Y-axis label
                dataset,                   // Dataset
                PlotOrientation.VERTICAL,
                true,                      // Include legend
                true,                      // Include tooltips
                false                      // Include URLs
        );

        // Create a Swing component to display the chart
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        // Create a JFrame to hold the chart
        JFrame frame = new JFrame("BPM Data Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }



}




    /*
            try {
            ecgData1 = convertDatFileToJavaArray(datFilePath1);
            ecgData2 = convertDatFileToJavaArray(datFilePath2);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("deu merda");
        }


    private static double[] convertDatFileToJavaArray(String datFilePath) throws FileNotFoundException {
        System.out.println("entrou na funcao");
        File file = new File(datFilePath);
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter(","); // Set the delimiter to comma

        List<Integer> ecgDataList = new ArrayList<>();

        while (scanner.hasNextInt()) {
            int value = scanner.nextInt();
            ecgDataList.add(value);
            System.out.println("peta");
        }

        // Convert List to array
        double[] ecgData = new double[ecgDataList.size()];
        for (int i = 0; i < ecgDataList.size(); i++) {
            ecgData[i] = ecgDataList.get(i);
        }

        // Close the scanner
        scanner.close();

        return ecgData;
    }

     */

