package me.albertonicoletti.latex;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
     * Writes in the internal memory a file named open_documents.json, containing the paths of the
     * open files.
     * @param context Context
     * @param documents Documents list
     */
    public static void saveFilesPath(Context context, LinkedList<Document> documents){
        FilesUtils.deleteInternalFiles(context);
        JSONObject json = new JSONObject();
        JSONArray files = new JSONArray();
        File persistentFile = new File(context.getFilesDir(), "open_documents.json");
        try {
            for (Document d : documents) {
                JSONObject doc = new JSONObject();
                doc.put("path", d.getPath());
                doc.put("open", d.isOpen());
                doc.put("savedText", d.getSavedText());
                files.put(doc);
            }
            json.put("documents", files);
        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
        }
        FilesUtils.writeFile(persistentFile, json.toString());
    }


    /**
     * Reads the open_documents.json file and returns a Document list
     * @param context Context
     * @return Document list
     */
    public static LinkedList<Document> readSavedOpenFiles(Context context){
        LinkedList<Document> files = new LinkedList<>();
        File file = new File(context.getFilesDir(), "open_documents.json");
        if(file.exists()) {
            try {
                JSONObject json;
                JSONArray savedDocuments;
                String text = FilesUtils.readTextFile(file);
                json = new JSONObject(text);
                savedDocuments = (JSONArray) json.get("documents");
                for (int i = 0; i < savedDocuments.length(); i++) {
                    JSONObject doc = (JSONObject) savedDocuments.get(i);
                    Document d = new Document(doc.getString("path"));
                    d.setOpen(doc.getBoolean("open"));
                    String savedText = doc.getString("savedText");
                    if(savedText.length() > 0) {
                        d.setSavedText(savedText);
                    }
                    files.add(d);
                }
            } catch(JSONException e) {
                Log.e("JSON", e.getMessage());
            }
        }
        return files;
    }

}
