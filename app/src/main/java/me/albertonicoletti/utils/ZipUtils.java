package me.albertonicoletti.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Zip utilities.
 */
public class ZipUtils {

    final static int BUFFER_SIZE = 2048;

    /**
     * Writes the file contents inside a zip entry
     * @param zipper ZipOutputStream
     * @param file File
     * @throws IOException
     */
    private static void writeZipEntry(ZipOutputStream zipper, File file)
            throws IOException {
        byte data[] = new byte[BUFFER_SIZE];
        FileInputStream fi = new FileInputStream(file);
        BufferedInputStream inputStream = new BufferedInputStream(fi, BUFFER_SIZE);
        int count;
        // Writes the whole file and closes the stream
        while ((count = inputStream.read(data, 0, data.length)) != -1) {
            zipper.write(data, 0, count);
        }
        inputStream.close();
    }

    /**
     * Add a new entry in a Zip file, the file will be written inside a given directory
     * @param zipper ZipOutputStream
     * @param directory Directory where the file will be written, pass null if the file has no parent
     * @param file File to add
     * @throws IOException
     */
    private static void addZipEntry(ZipOutputStream zipper, File directory, File file) throws IOException {
        String filename = file.getName();
        ZipEntry entry;
        // If there's a directory, the file must be written inside the directory
        if(directory != null) {
            entry = new ZipEntry(directory.getName() + "/" + filename);
        } else {
            entry = new ZipEntry(filename);
        }
        // Adds the new entry and writes it
        zipper.putNextEntry(entry);
        writeZipEntry(zipper, file);
    }

    /**
     * Recursive routine to add a directory and all the files in it
     * @param zipper ZipOutputStream
     * @param directory Directory to add
     * @throws IOException
     */
    private static void addDirectory(ZipOutputStream zipper, File directory) throws IOException {
        File files[] = directory.listFiles();
        if(files != null){
            for(File file : files){
                if(file.isDirectory()){
                    addDirectory(zipper, file);
                } else {
                    addZipEntry(zipper, directory, file);
                }
            }
        }
    }

    /**
     * Creates a new zip file containing the given files
     * @param zipPath Zip's path
     * @param files Files to add to the zip
     * @return The zip file
     */
    public static File newZipFile(String zipPath, File... files){
        int lastIndex = zipPath.length();
        if(zipPath.contains(".")){
            lastIndex = zipPath.indexOf(".");
        }
        lastIndex = Math.max(0, lastIndex);
        File zipFile = new File(zipPath.substring(0, lastIndex) + ".zip");
        try {
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream zipper = new ZipOutputStream(new BufferedOutputStream(dest));

            if(files != null) {
                for (File file : files) {
                    if(file.isDirectory()) {
                        addDirectory(zipper, file);
                    } else {
                        addZipEntry(zipper, null, file);
                    }
                }
            }

            zipper.close();

        } catch(IOException e) {
            Log.e("ZIP", "Error zipping the files: " + e);
        }
        return zipFile;
    }

    /**
     * Creates a new zip file containing the given files
     * @param zipPath Zip's path
     * @param files Files to add to the zip
     * @return The zip file
     */
    public static File newZipFile(String zipPath, List<File> files){
        int lastIndex = Math.min(zipPath.lastIndexOf("."), zipPath.length());
        File zipFile = new File(zipPath.substring(0, lastIndex) + ".zip");
        try {
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream zipper = new ZipOutputStream(new BufferedOutputStream(dest));

            if(files != null) {
                for (File file : files) {
                    if(file.isDirectory()) {
                        addDirectory(zipper, file);
                    } else {
                        addZipEntry(zipper, null, file);
                    }
                }
            }

            zipper.close();

        } catch(IOException ignored) {}
        return zipFile;
    }

}
