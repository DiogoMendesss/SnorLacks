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

        ArrayList<Integer> bpmi = generateHeartRateArray(50, 60, 200);



        for (int i = 0; i<30;i++){
            bpmi.set(i, (90-i+random.nextInt( 4) -2));
        }

        for (int i = 170; i<200;i++){
            bpmi.set(i, (90-(200-i)+random.nextInt( 4) -2));
        }

        bpmi.set(70, 90);
        bpmi.set(130, 100);
        bpmi.set(160, 80);



        System.out.println("Median value: " + calculateMedian(bpmi));

        // Create a graph for the filtered ECG data
        createBpmGraph(bpmi);
        cropBpmArray(bpmi);
        createBpmGraph(bpmi);

        ArrayList<Boolean> apneaEvents = checkApneaEvents(bpmi, 20);

        long apneaEventsNumber = apneaEvents.stream().filter(Boolean::booleanValue).count();

        System.out.println("Number of apnea events: " + apneaEvents.stream().filter(Boolean::booleanValue).count());
    }

    public static void cropBpmArray(ArrayList<Integer> bpmList){

        double median = calculateMedian(bpmList);

        // Remove the first values until a sample is lesser than the median
        while (!bpmList.isEmpty() && bpmList.get(0) >= median) {
            bpmList.remove(0);
        }

        // Remove the last values until a sample is lesser than the median
        while (!bpmList.isEmpty() && bpmList.get(bpmList.size() - 1) >= median) {
            bpmList.remove(bpmList.size() - 1);
        }
    }

    public static ArrayList<Integer> generateHeartRateArray(int minRate, int maxRate, int arraySize) {
        if (minRate >= maxRate || arraySize <= 0) {
            throw new IllegalArgumentException("Invalid parameters for heart rate array generation.");
        }

        ArrayList<Integer> heartRateArray = new ArrayList<Integer>();
        Random random = new Random();

        for (int i = 0; i < arraySize; i++) {
            heartRateArray.add(random.nextInt(maxRate - minRate + 1) + minRate);
        }

        return heartRateArray;
    }

    //checkApneaEvent() returns true if for an event, the average bpm exceeds the median bpm of the night plus a threshold
    public static ArrayList<Boolean> checkApneaEvents(ArrayList<Integer> bpmList, int threshold) {

        ArrayList<Boolean> apneaEvents = new ArrayList<Boolean>();
        double median = calculateMedian(bpmList);
        for (int i = 0; i < bpmList.size(); i++) {
            if (bpmList.get(i) > median + threshold) {
               apneaEvents.add(Boolean.TRUE);
            }
            else apneaEvents.add(Boolean.FALSE);
        }

        return apneaEvents;
    }




    // Method to create a graph for the BPM data
    private static void createBpmGraph(ArrayList<Integer> bpmSamples) {
        // Create a new XYSeries for the ECG data
        XYSeries bpmSeries = new XYSeries("Heartrate Data");

        // Populate the series with the ECG samples
        for (int i = 0; i < bpmSamples.size(); i++) {
            bpmSeries.add(i, bpmSamples.get(i));
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

    public static double calculateMedian(ArrayList<Integer> numbers) {
        // Check for empty list
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("The list is empty");
        }

        // Sort the ArrayList
        ArrayList<Integer> sorted_numbers = new ArrayList<Integer>(numbers);
        Collections.sort(sorted_numbers);

        int size = sorted_numbers.size();
        double median;

        if (size % 2 == 0) {
            // If the size is even, average the two middle elements
            int middle1 = sorted_numbers.get(size / 2 - 1);
            int middle2 = sorted_numbers.get(size / 2);
            median = (middle1 + middle2) / 2.0;
        } else {
            // If the size is odd, take the middle element
            median = sorted_numbers.get(size / 2);
        }

        return median;
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
