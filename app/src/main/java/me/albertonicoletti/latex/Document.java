package me.albertonicoletti.latex;

import java.io.File;

/**
 * Created by alberto on 01/09/15.
 */
public class Document extends File {

    private boolean log = false;
    private boolean open = false;

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
}
