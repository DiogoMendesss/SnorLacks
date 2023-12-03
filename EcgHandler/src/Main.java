import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        
        short[] ecgData = new short[0];

        String datFilePath = "C:\\Users\\romeu\\Desktop\\SnorLacks\\EcgHandler\\assets\\ecg-id-database-1.0.0\\ecg-id-database-1.0.0\\Person_01\\rec_1.dat";

        try {
            // Read ECG data from the .dat file
            ecgData = readEcgData(datFilePath);

            // Process or visualize the ECG data as needed
            for (short value : ecgData) {
                System.out.println("ECG Value: " + value);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        // Assuming 3000 samples for ECG
        int numSamples = 3000;

        // Sample frequency of the data acquisition (Hz)
        double sampleFrequency = 500.0;

        // Create an array to store ECG samples
        double[] ecg = generateEcgSamples(numSamples, sampleFrequency);

        // Apply low-pass filter
        double[] lowPassFilteredEcg = lowPassFilter(ecg, sampleFrequency, 50.0); // Cut frequency of 50 Hz

        // Apply high-pass filter
        double[] highPassFilteredEcg = highPassFilter(lowPassFilteredEcg, sampleFrequency, 0.5); // Cut frequency of 0.5 Hz

        // Now you can process the filtered ECG data as needed
        processEcgData(ecg);
        
         */

        // Create a graph for the filtered ECG data
        createEcgGraph(ecgData);
    }

    // Method to read 12-bit ECG data from a .dat file
    private static short[] readEcgData(String datFilePath) throws IOException {
        // Open .dat file for reading
        FileInputStream fis = new FileInputStream(datFilePath);
        DataInputStream dis = new DataInputStream(fis);

        // Read the raw ECG data
        int dataSize = dis.available() * 8 / 12; // Assuming 12-bit data
        short[] ecgData = new short[dataSize];

        for (int i = 0; i < dataSize; i++) {
            // Read the next 12-bit signed data point
            // Combine two bytes into a 12-bit value
            int firstByte = dis.readUnsignedByte();
            int secondByte = dis.readUnsignedByte();
            int twelveBitValue = (firstByte << 4) | (secondByte >> 4);

            // Convert to signed representation (assuming 12-bit is signed)
            ecgData[i] = (short) ((twelveBitValue << 4) >> 4);
        }

        // Close the streams
        dis.close();
        fis.close();

        return ecgData;
    }


    // Method to generate and normalize ECG-like samples
    private static double[] generateEcgSamples(int numSamples, double sampleFrequency) {
        double[] ecgSamples = new double[numSamples];

        // Generate an ECG-like signal using the sum of sinusoidal waves
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (int i = 0; i < numSamples; i++) {
            double t = i / sampleFrequency;
            double ecgValue = 0.5 * Math.sin(2 * Math.PI * 0.5 * t) +
                    0.2 * Math.sin(2 * Math.PI * 10 * t) +
                    0.1 * Math.sin(2 * Math.PI * 20 * t);

            // Add noise (optional)
            ecgValue += 0.02 * Math.random();

            // Update min and max values
            if (ecgValue < min) {
                min = ecgValue;
            }
            if (ecgValue > max) {
                max = ecgValue;
            }

            ecgSamples[i] = ecgValue;
        }

        // Normalize the values to the range [0, 240]
        for (int i = 0; i < numSamples; i++) {
            ecgSamples[i] = 240 * (ecgSamples[i] - min) / (max - min);
        }

        return ecgSamples;
    }


    // Low-pass filter with cut frequency
    private static double[] lowPassFilter(double[] input, double sampleFrequency, double cutFrequency) {
        double alpha = 1.0 / (1.0 + Math.PI * cutFrequency / sampleFrequency);
        double[] output = new double[input.length];

        output[0] = input[0];

        for (int i = 1; i < input.length; i++) {
            output[i] = alpha * input[i] + (1 - alpha) * output[i - 1];
        }

        return output;
    }

    // High-pass filter with cut frequency
    private static double[] highPassFilter(double[] input, double sampleFrequency, double cutFrequency) {
        double alpha = 1.0 / (1.0 + Math.PI * cutFrequency / sampleFrequency);
        double[] output = new double[input.length];

        output[0] = input[0];

        for (int i = 1; i < input.length; i++) {
            output[i] = alpha * (output[i - 1] + input[i] - input[i - 1]);
        }

        return output;
    }


    // Placeholder method for processing ECG data
    private static void processEcgData(double[] ecgSamples) {
        // Replace this with your actual processing logic
        System.out.println("ECG data processing logic goes here.");
    }

    // Method to create a graph for the ECG data
    private static void createEcgGraph(short[] ecgSamples) {
        // Create a new XYSeries for the ECG data
        XYSeries ecgSeries = new XYSeries("ECG Data");

        // Populate the series with the ECG samples
        for (int i = 0; i < ecgSamples.length; i++) {
            ecgSeries.add(i, ecgSamples[i]);
        }

        // Create a dataset and add the series to it
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(ecgSeries);

        // Create a chart based on the dataset
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Filtered ECG Data Chart", // Chart title
                "Sample Index",            // X-axis label
                "ECG Value",               // Y-axis label
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
        JFrame frame = new JFrame("Filtered ECG Data Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
}
