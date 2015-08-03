package me.albertonicoletti.latex;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Class representing a Latex Document
 */
public class Document {

    /** Document's title */
    private String title;
    /** Datetime of the last modify */
    private String lastModified;

    /**
     * Document's constructor
     * @param file File representing the document
     */
    public Document(File file) {
        this.title = file.getName();
        file.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.lastModified = sdf.format(file.lastModified());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
