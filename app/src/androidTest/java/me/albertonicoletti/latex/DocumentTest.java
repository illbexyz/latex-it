package me.albertonicoletti.latex;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by alberto on 14/02/16.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DocumentTest {

    @Test
    public void documentIsNotALogOnStartup() {
        String path = "mypath.tex";
        Document document = new Document(path);
        assertThat(document.isLog(), is(false));
    }

    @Test
    public void isLog() {
        Document document = new Document("file.tex");
        document.setLog();
        assertThat(document.isLog(), is(true));
    }

}
