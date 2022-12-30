package com.ShortBurstOpenCV;

import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceFinder {

    File file = null;


    public void showMenu(){

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println();
            System.out.println("Menu: ");
            System.out.println("1. Load new image");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    try {
                        // Get image file
                        file = GetFile();
                        // Detect faces in file
                        detectFaces(file);
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
    private void detectFaces(File file) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        ArrayList<String> ar = new ArrayList<>();

        try {
            String imgFile = saveImage(file);

            Mat src = Imgcodecs.imread(imgFile);

            String xmlFile = "xml/lbpcascade_frontalface.xml";
            CascadeClassifier cc = new CascadeClassifier(xmlFile);

            MatOfRect faceDetection = new MatOfRect();

            // Detect faces in loaded image
            cc.detectMultiScale(src, faceDetection);

            // Create an executor to handle multithreading
            ExecutorService executor = Executors.newFixedThreadPool(8);

            int count = 1;
            for (Rect rect : faceDetection.toArray()) {
                // Draw a rectangle around the detected face
                Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);

                // Crop the face image from the source image
                Mat face = src.submat(rect);

                // Convert the face image to grayscale
                Mat faceGrayscale = new Mat();
                Imgproc.cvtColor(face, faceGrayscale, Imgproc.COLOR_BGR2GRAY);

                // Resize the face image to 150x150 pixels
                Mat faceResized = new Mat();
                Size size = new Size(150, 150);
                Imgproc.resize(faceGrayscale, faceResized, size);

                // Generate a timestamp and a filename for the face image
                long timestamp = System.currentTimeMillis() / 1000L;
                String filename = "images/faces/face_" + timestamp + "_" + String.format("%03d", count) + ".jpg";
                //faces.add(filename);

                // Create a Runnable task to save the face image in a separate thread
                int finalCount = count;
                Runnable saveTask = () -> {
                    // Save the face image to a file
                    Imgcodecs.imwrite(filename, faceResized);
                    System.out.println("Saved face #" + String.format("%03d", finalCount) + ": " + filename);
                    ar.add(filename);
                };

                // Execute the save task in a separate thread
                executor.execute(saveTask);

                count++;
            }

            // Shutdown the executor
            executor.shutdown();

            System.out.println();
            ar.forEach(System.out::println);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private File GetFile() throws IOException {
        // Open a file chooser dialog to select an image file
        FileDialog fileDialog = new FileDialog((Frame)null, "Select an image file");
        fileDialog.setMode(FileDialog.LOAD);
        fileDialog.setVisible(true);

        // Get the selected file path
        String filePath = fileDialog.getFile();
        if (filePath == null) {
            throw new IOException("No file was selected");
        }
        String dirPath = fileDialog.getDirectory();

        return new File(dirPath + filePath);
    }

    public String saveImage(File file) throws IOException {

        String filename = file.getName();

        // Get the current Unix timestamp
        long timestamp = System.currentTimeMillis() / 1000L;

        // Find the index of the last dot and the extension
        int lastDotIndex = filename.lastIndexOf('.');
        String extension = filename.substring(lastDotIndex);

        // build the modified filename
        String newFilename_out = filename.substring(0, lastDotIndex) + "-" + timestamp + extension;

        // Save the image files to the "images" directory
        ImageIO.write(ImageIO.read(file), "jpg", new File("images/" + newFilename_out));

        return "images/" + newFilename_out;
    }



}
