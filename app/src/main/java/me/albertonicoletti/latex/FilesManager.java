package me.albertonicoletti.latex;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.LinkedList;

/**
 * Utility class that provide utilities for managing files.
 */
public class FilesManager {

    /**
     * Convenient method to create a new Latex file and save it in Documents folder.
     * @return Latex file created
     */
    public File newFile(){
        File newDoc = null;
        Integer seedNumber = 0;
        // Checking if I can write on external storage
        Log.v("FILE", "Checking if I can write on external storage");
        if(this.isExternalStorageWritable()){
            File directoryPath = getDocumentsDir();
            String filename;
            // Incrementing seedNumber until there's no file having that name
            do{
                filename = "untitled" + seedNumber + ".tex";
                Log.v("FILE", "Trying to open " + filename + " file");
                newDoc = new File(directoryPath, filename);
                seedNumber++;
            } while(newDoc.exists());

            newDoc = this.newFile(filename);

        } else {
            Log.e("FILE", "Can't write on external storage");
        }
        return newDoc;
    }

    /**
     * Creates a new file and saves it to the Documents folder
     * @param name Filename
     * @return File created
     */
    public File newFile(String name){
        File newDoc = null;
        // Checking if I can write on external storage
        Log.v("FILE", "Checking if I can write on external storage");
        if(this.isExternalStorageWritable()){
            File directoryPath = getDocumentsDir();

            Log.v("FILE", "Trying to open " + name + " file");
            newDoc = new File(directoryPath, name);
            Log.v("FILE", "Filename: " + name);
            try {
                if(!newDoc.createNewFile()){
                    Log.e("FILE", "Can't create a new document.");
                }
            } catch (IOException e) {
                Log.e("FILE", "Can't create a new document. " + e.getMessage());
            }
        } else {
            Log.e("FILE", "Can't write on external storage");
        }
        return newDoc;
    }

    /**
     * Read the given binary file, and return its contents as a byte array.
     * @param file File to read
     * @return Byte array
     */
    byte[] readBinaryFile(File file){
        byte[] result = new byte[(int) file.length()];
        try {
            InputStream input = null;
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
    public void writeBinaryFile(byte[] bytes, File file){
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

    /**
     * Renames the given filename to newName
     * @param filename
     * @param newName
     */
    public void renameDocument(String filename, String newName){
        File directory = getDocumentsDir();
        File file = new File(directory, filename);
        File newFile = new File(directory, newName);
        file.renameTo(newFile);
    }

    /**
     * Gets the files in Documents folder
     * @return Files in Documents folder
     */
    public LinkedList<Document> getExistingFiles(){
        LinkedList<Document> filenames = new LinkedList<Document>();
        File directory = getDocumentsDir();
        File[] files = directory.listFiles();
        for(File file : files){
            if(file.isFile()){
                String filename = file.getName();
                if(filename.endsWith(".tex")){
                    Document doc = new Document(file);
                    filenames.add(doc);
                }
            }
        }
        return filenames;
    }

    /**
     * Gets the default document's directory
     * @return Default document's directory
     */
    public File getDocumentsDir() {
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
    public Uri getUriFromFilename(String filename){
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
    public Uri getUriFromFilename(File directory, String filename){
        Uri fileUri;
        File file = new File(directory, filename);
        fileUri = Uri.fromFile(file);
        return fileUri;
    }

    /**
     * Checks if external storage is available for read and write
     * @return True if writable
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     * @return True if readable
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
