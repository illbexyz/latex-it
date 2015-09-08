package me.albertonicoletti.latex;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

import me.albertonicoletti.utils.FilesUtils;

/**
 * Provides utilities to maintain data.
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */
public class DataPersistenceUtil {

    /**
     * Writes in the internal memory a file named open_documents, containing the paths of the
     * open files.
     * The file is formatted as:
     *  filename
     *  t/f
     * t/f means if the file is open is the editor or not
     * @param context Context
     * @param documents Documents list
     */
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


    /**
     * Reads the open_documents file and returns a Document list
     * @param context Context
     * @return Document list
     */
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
