package me.albertonicoletti.latex;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Created by alberto on 08/08/15.
 */
public class DataPersistenceUtil {

    public static LinkedList<File> readSavedOpenFiles(Context context){
        LinkedList<File> files = new LinkedList<>();
        File savedDocuments = new File(context.getFilesDir(), "open_documents");
        try {
            Scanner scanner = new Scanner(savedDocuments);
            while(scanner.hasNextLine()){
                files.add(new File(scanner.nextLine()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return files;
    }

    public static void saveFilesPath(Context context, LinkedList<File> files){
        FilesManager.deleteInternalFiles(context);
        File persistentFile = new File(context.getFilesDir(), "open_documents");
        String filepaths = "";
        for (File f : files) {
            String path = f.getPath();
            filepaths += path + "\n";
        }
        FilesManager.writeFile(persistentFile, filepaths);
    }

}
