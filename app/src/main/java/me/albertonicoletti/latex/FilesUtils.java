package me.albertonicoletti.latex;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class that provide utilities for managing files.
 */
public class FilesUtils {

    static int filesNumber = 0;

    /**
     * Convenient method to create a new Latex file and save it in Documents folder.
     * @return Latex file created
     */
    public static File newFile(){
        /*File newDoc = null;
        Integer seedNumber = 0;
        // Checking if I can write on external storage
        Log.v("FILE", "Checking if I can write on external storage");
        if(isExternalStorageWritable()){
            File directoryPath = getDocumentsDir();
            String filename;
            // Incrementing seedNumber until there's no file having that name
            do{
                filename = "untitled" + seedNumber + ".tex";
                Log.v("FILE", "Trying to open " + filename + " file");
                newDoc = new File(filename);
                seedNumber++;
            } while(newDoc.exists());

            newDoc = newFile(filename);

        } else {
            Log.e("FILE", "Can't write on external storage");
        }

        return newDoc;*/
        File file;
        do {
            file = new File(getDocumentsDir(), "untitled" + filesNumber++);
        } while(file.exists());
        return newFile(getDocumentsDir(), file.getName());
    }

    /**
     * Creates a new file and saves it to the Documents folder
     * @param name Filename
     * @return File created
     */
    public static File newFile(File directory, String name){
        File newDoc = null;
        // Checking if I can write on external storage
        Log.v("FILE", "Checking if I can write on external storage");
        if(isExternalStorageWritable()){
            File directoryPath = getDocumentsDir();

            Log.v("FILE", "Trying to open " + name + " file");
            newDoc = new File(name);
            Log.v("FILE", "Filename: " + name);
        } else {
            Log.e("FILE", "Can't write on external storage");
        }
        return newDoc;
    }

    public static File getFileInternalMemory(Context context, String filename){
        File internalFile = new File(context.getFilesDir(), filename);
        return internalFile;
    }

    public static void saveFileInternalMemory(Context context, File file){
        File internalFile = new File(context.getFilesDir(), file.getName());
        String toSave = readTextFile(file);
        writeFile(internalFile, toSave);
    }

    /**
     * Deletes a file
     * @param file File to delete
     */
    public static void deleteFile(File file){
        file.delete();
    }

    public static void deleteFileInDirectory(File directory){
        File[] files = directory.listFiles();
        for(File f : files){
            deleteFile(f);
        }
    }

    public static void deleteInternalFiles(Context context){
        deleteFileInDirectory(context.getFilesDir());
    }

    public static String readTextFile(File file){
        String fileContent = "";
        Log.v("FILE", "Trying to read file: " + file.getPath());
        try {
            Scanner s = new Scanner(file);
            while(s.hasNextLine()){
                fileContent += s.nextLine() + "\n";
            }
        } catch (FileNotFoundException e) {
            Log.e("FILE", "Can't read file: " + e.getMessage());
        }
        return fileContent;
    }

    /**
     * Read the given binary file, and return its contents as a byte array.
     * @param file File to read
     * @return Byte array
     */
    public static byte[] readBinaryFile(File file){
        byte[] result = new byte[(int) file.length()];
        try {
            InputStream input;
            int totalBytesRead = 0;
            input = new BufferedInputStream(new FileInputStream(file));
            // Until it reads the whole file:
            while(totalBytesRead < result.length){
                int bytesRemaining = result.length - totalBytesRead;
                int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
                if (bytesRead > 0){
                    totalBytesRead = totalBytesRead + bytesRead;
                }
            }
            // Closes the file
            input.close();

        }
        catch (FileNotFoundException ex) {
            Log.e("FILE", "File not found.");
        }
        catch (IOException ex) {
            Log.e("FILE", ""+ex.getMessage());
        }
        return result;
    }

    /**
     * Writes a byte array to the given file.
     * @param bytes Bytes array
     * @param file File to write
     */
    public static void writeBinaryFile(File file, byte[] bytes){
        try {
            OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
            // Writes the whole array into the file
            output.write(bytes);
            output.close();
        }
        catch(FileNotFoundException ex){
            Log.e("FILE", "File not found.");
        }
        catch(IOException ex){
            Log.e("FILE", "" + ex.getMessage());
        }
    }

    public static void writeFile(File file, String string){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(string, 0, string.length());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("FILE", "Can't write in the file: " + e.getMessage());
        }
    }

    public static File saveFileRenaming(String newName, String text){
        File newFile = new File(getDocumentsDir(), newName);
        writeFile(newFile, text);
        return newFile;
    }

    /**
     * Renames the given filename to newName
     * @param file
     * @param newName
     */
    public static File renameFile(File file, String newName){
        File directory = getDocumentsDir();
        File newFile = new File(directory, newName);
        file.renameTo(newFile);
        file = newFile;
        return file;
    }

    /**
     * Gets the files in Documents folder
     * @return Files in Documents folder
     */
    public static LinkedList<File> getExistingFiles(){
        LinkedList<File> filenames = new LinkedList<>();
        File directory = getDocumentsDir();
        File[] files = directory.listFiles();
        for(File file : files){
            if(file.isFile()){
                String filename = file.getName();
                if(filename.endsWith(".tex")){
                    filenames.add(file);
                }
            }
        }
        return filenames;
    }

    /**
     * Gets the files in a given directory
     * @param directory Directory
     * @return Files in the directory
     */
    public static LinkedList<File> getExistingFiles(File directory){
        LinkedList<File> filenames = new LinkedList<>();
        File[] files = directory.listFiles();
        for(File file : files){
            if(file.isFile()){
                String filename = file.getName();
                if(filename.endsWith(".tex")){
                    filenames.add(file);
                }
            }
        }
        return filenames;
    }

    /**
     * Gets the directories
     * @param directory Directory where to search
     * @return Files in Documents folder
     */
    public static LinkedList<File> getDirectories(File directory){
        LinkedList<File> directories = new LinkedList<>();
        File[] files = directory.listFiles();
        for(File file : files){
            if(file.isDirectory()){
                String filename = file.getName();
                directories.add(file);
            }
        }
        return directories;
    }

    /**
     * Gets the default document's directory
     * @return Default document's directory
     */
    public static File getDocumentsDir() {
        // Get the directory for the user's public pictures directory.
        File directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);
        if(!directory.exists()){
            directory.mkdir();
        }
        return directory;
    }

    /**
     * Gets the file's URI from the default directory
     * @param filename Filename
     * @return URI
     */
    public static Uri getUriFromFilename(String filename){
        Uri fileUri;
        File directory = getDocumentsDir();
        File file = new File(directory, filename);
        fileUri = Uri.fromFile(file);
        return fileUri;
    }

    /**
     * Gets the file's URI from a custom directory
     * @param filename Filename
     * @return URI
     */
    public static Uri getUriFromFilename(File directory, String filename){
        Uri fileUri;
        File file = new File(directory, filename);
        fileUri = Uri.fromFile(file);
        return fileUri;
    }

    /**
     *
     * @param files
     * @return
     */
    public static File zipFiles(LinkedList<File> files){
        final int BUFFER = 2048;
        File zip = new File(getDocumentsDir(), "zip.zip");
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zip);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            for(File f : files){
                Log.v("Compress", "Adding: " + f);
                FileInputStream fi = new FileInputStream(f);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(f.getPath().substring(f.getPath().lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return zip;

    }

    /**
     * Checks if external storage is available for read and write
     * @return True if writable
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     * @return True if readable
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}
