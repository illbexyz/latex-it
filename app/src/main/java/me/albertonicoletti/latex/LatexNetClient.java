package me.albertonicoletti.latex;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Utility class for networking
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */
public class LatexNetClient {

    private static final String BASE_URL = "http://compiler.latexit.illbe.xyz:1234/";
    // private static final String BASE_URL = "http://latex-compiler.bitnamiapp.com:1234/";
    // private static final String BASE_URL = "http://192.168.1.104:1234/";
    private static AsyncHttpClient client = new AsyncHttpClient();

    /**
     * Performs a post http method
     * @param path Relative path where to execute the post action. E.g. http://baseurl/path
     * @param params Parameters
     * @param responseHandler Response handler
     */
    public static void post(String path, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(path), params, responseHandler);
    }

    /**
     * Routine to get the absolute url given the relative path
     * @param relativeUrl Relative path
     * @return Absolute path
     */
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
