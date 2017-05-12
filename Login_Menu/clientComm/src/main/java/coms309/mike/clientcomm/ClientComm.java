package coms309.mike.clientcomm;

/**
 * Created by Mike
 */
import android.content.Context;
import android.net.Uri;
import android.util.JsonReader;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

//TODO many ErrorListeners won't work. Some of the callbacks aren't meant to return Strings (I'll have to make JSON objects/arrays to hold the error)
//TODO also, fix the else blocks for the same reasons. They shouldn't be reached, since my types seem to be correct, but still useful in an unfinished product
// TODO finally, better typechecks on the callbacks.
public class ClientComm {
    final private SingletonQueue singletonQueue;
    /**
     * @param context I need the activity context. This doesn't run in any context. So you'll need to supply it for me.
     */
    public ClientComm(Context context) {
        singletonQueue = SingletonQueue.GetInstance(context);
    }
    /**
     *
     * @param serverPath A string with the last part of the URL. (ex. if you were to go to http://proj-309-yt-05.cs.iastate.edu/login.php. You would put "login.php" as the server path
     * @param jsonArray the request
     * @param callback returns a JsonArray which is the server response. Simply make a new one in your parameter.
     */
    public void serverPostRequest(String serverPath, final JSONArray jsonArray, final VolleyCallback<JSONArray> callback){
        JsonArrayRequest jArrReq = new JsonArrayRequest(Request.Method.POST, buildUrl(serverPath), jsonArray, new Response.Listener<JSONArray>(){
            @Override
            public void onResponse(JSONArray response){
                callback.onSuccess(response);
            }
        },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        try {
                            callback.onSuccess(new JSONArray("VolleyError: " + error.toString()));
                        }
                        catch(JSONException e){
                            //need to put the new JSONArray in a try/catch block for some reason
                        }
                    }
                });
        singletonQueue.addToRequestQueue(jArrReq);
    }
    //builds a URL, using the path supplied by the caller (which receives it from the client, as they are the only ones who know where they want to go)
    private String buildUrl(String path){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("warofages.heliohost.org")
                .path(path);
        return builder.toString();
    }
}