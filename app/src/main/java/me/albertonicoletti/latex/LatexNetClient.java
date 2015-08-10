package me.albertonicoletti.latex;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Utility class for networking
 */
public class LatexNetClient {

    private static final String BASE_URL = "http://192.168.1.106:8080/";
    private static AsyncHttpClient client = new AsyncHttpClient();
    /*
    private File file;

    public LatexNetClient(File file){
        this.file = file;
    }

    @Override
    protected File doInBackground(String... params) {
        post(getAbsoluteUrl(params[0]), file);
        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
    }

    public void post(String url, final File file) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            entityBuilder.addBinaryBody("zip_file", file);

            HttpEntity entity = entityBuilder.build();
            post.setEntity(entity);


            HttpResponse response = client.execute(post);

            /*HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity);
            Log.v("result", result);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    */

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
