package me.albertonicoletti.latex;

import java.io.File;

/**
 * Class representing a document.
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */
public class Document extends File {

    /** Set to true if the file is a log from pdflatex */
    private boolean log = false;

    /** Set to true if the file is open in the editor */
    private boolean open = false;

    private boolean isSaved = true;

    public Document(String path) {
        super(path);
    }

    public Document(File file){
        super(file.getPath());
    }

    public boolean isLog() {
        return log;
    }

    public void setLog() {
        this.log = true;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}
