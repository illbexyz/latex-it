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

    public static void saveFilesPath(Context context, LinkedList<Document> documents){
        FilesUtils.deleteInternalFiles(context);
        File persistentFile = new File(context.getFilesDir(), "open_documents");
        String filepaths = "";
        for (Document d : documents) {
            String open = "f";
            if(d.isOpen()){
                open = "t";
            }
            String path = d.getPath();
            filepaths += path + "\n";
            filepaths += open + "\n";
        }
        FilesUtils.writeFile(persistentFile, filepaths);
    }

    public static LinkedList<Document> readSavedOpenFiles(Context context){
        LinkedList<Document> files = new LinkedList<>();
        File savedDocuments = new File(context.getFilesDir(), "open_documents");
        try {
            Scanner scanner = new Scanner(savedDocuments);
            while(scanner.hasNextLine()){
                files.add(new Document(scanner.nextLine()));
                if(scanner.hasNextLine()) {
                    if (scanner.nextLine().equals("t")) {
                        files.getLast().setOpen(true);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return files;
    }

}
