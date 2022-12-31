/* *********************************************************************************
 * Name: Jan Vermeerbergen
 * Assignment: Java short burst project: Face detection using the OpenCV library
 * School: Erasmushogeschool, Brussel
 * *********************************************************************************
 */

package com.ShortBurstOpenCV;

import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceFinder {

    public void showMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println();
            System.out.println("Menu: ");
            System.out.println("1. Detect faces in image");
            System.out.println("2. Detect eyes in image");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    try {
                        // Get image file
                        File file = getFile();
                        // Detect faces in file
                        detectFeatures(file, "xml/lbpcascade_frontalface.xml", "faces", true);
                    } catch (IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 2:
                    try {
                        // Get image file
                        File file = getFile();
                        // Detect faces in file
                        detectFeatures(file, "xml/haar_two_eyes_big.xml", "eyes", false);
                    } catch (IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;


                case 0:
                    // Exit the program
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
                    break;
            }
        }
    }

    private void detectFeatures(File file, String path, String featureType, boolean resize) throws IOException {

        // Load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String filename = file.getName();
        // Generate a timestamp and a filename for the face image
        long timestamp = System.currentTimeMillis() / 1000L;

        Mat src = Imgcodecs.imread(file.getAbsolutePath());
        CascadeClassifier cc = new CascadeClassifier(path);

        MatOfRect featureDetection = new MatOfRect();

        // Detect faces in loaded image
        cc.detectMultiScale(src, featureDetection);

        // Create an executor to handle multithreading
        ExecutorService executor = Executors.newFixedThreadPool(8);

        int count = 1;
        System.out.println();
        for (Rect rect: featureDetection.toArray()) {
            // Draw a rectangle around the detected face
            Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);

            // Crop the face image from the source image
            Mat faceFeature = src.submat(rect);

            if(resize){
                // Convert the face image to grayscale
                Mat faceFeatureGrayscale = new Mat();
                Imgproc.cvtColor(faceFeature, faceFeatureGrayscale, Imgproc.COLOR_BGR2GRAY);
                // Resize the face image to 150x150 pixels
                Mat faceResized = new Mat();
                Size size = new Size(150, 150);
                Imgproc.resize(faceFeatureGrayscale, faceResized, size);
            }

            // Find the index of the last dot and the extension of the file
            int lastDotIndex = filename.lastIndexOf('.');
            String extension = filename.substring(lastDotIndex);

            String featureName = "images/" + featureType + "/" + filename.substring(0, lastDotIndex) + "_" + timestamp + "_" + String.format("%03d", count) + extension;

            // Create a Runnable task to save the face image in a separate thread
            int finalCount = count;
            Runnable saveTask = () -> {
                    // Save the face or eyes image to a file
                    Imgcodecs.imwrite(featureName, faceFeature);
            System.out.println("Saved #" + String.format("%03d", finalCount) + ": " + featureName);
            };

            // Execute the save task in a separate thread
            executor.execute(saveTask);

            count++;
        }

        // Shutdown the executor
        executor.shutdown();

        // Find the index of the last dot and the extension of the file
        int lastDotIndex = filename.lastIndexOf('.');
        String extension = filename.substring(lastDotIndex);

        // build the modified filename
        String newFilenameOut = "images/photos/" + filename.substring(0, lastDotIndex) + timestamp + "-out" + extension;

        // Write the updated file with all faces marked
        Imgcodecs.imwrite(newFilenameOut, src);

    }

    private File getFile() throws IOException {
        // Open a file chooser dialog to select an image file
        FileDialog fileDialog = new FileDialog((Frame)null, "Select a JPG image file");
        fileDialog.setMode(FileDialog.LOAD);
        fileDialog.setVisible(true);

        // Get the selected file path
        String filePath = fileDialog.getFile();
        if (filePath == null) {
            throw new IOException("No file was selected");
        }
        String dirPath = fileDialog.getDirectory();
        File file = new File(dirPath + filePath);

        // Check if the selected file has a JPG extension
        if (!file.getName().toLowerCase().endsWith(".jpg") && !file.getName().toLowerCase().endsWith(".jpeg")) {
            throw new IOException("Selected file is not a JPG image");
        }

        return file;
    }


}