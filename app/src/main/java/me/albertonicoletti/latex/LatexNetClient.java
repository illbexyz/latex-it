package me.albertonicoletti.latex;


import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Utility class for networking
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */
public class LatexNetClient {

    private Context ctx;
    private AsyncHttpClient client = new AsyncHttpClient();

    public LatexNetClient(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Get the base url of the server
     * @return The base url from the preferences
     */
    private String getBaseUrl() {
        return PreferenceHelper.getServerAddress(ctx);
    }

    /**
     * Routine to get the absolute url given the relative path
     * @param relativeUrl Relative path
     * @return Absolute path
     */
    private String getAbsoluteUrl(String relativeUrl) {
        return this.getBaseUrl() + relativeUrl;
    }

    /**
     * Performs a post http method
     * @param path Relative path where to execute the post action. E.g. http://baseurl/path
     * @param params Parameters
     * @param responseHandler Response handler
     */
    public void post(String path, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(path), params, responseHandler);
    }

}
