package warofages.gamebackend;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;

/**
 * Created by Mike on 10/29/2016.
 */

/**
 * Only inactive player and spectators need to use this. Only handles the polling of the server.
 * Returns as soon as a response has been given, so caller may want to call this again.
 * AsyncTask types indicate: Params, progress, results
 */
public class PollServerTask extends AsyncTask<Context, JSONArray, JSONArray> {
    private JSONArray myNameAndArmy;
    //holds an instance of the calling class. This allows me to use the callback there to do work with the
    private AsyncResultHandler receiver;
    String myName = "a player has no name";

    public PollServerTask(JSONArray myNameAndArmy, AsyncResultHandler callback){
        this.myNameAndArmy = myNameAndArmy;
        receiver = callback;
    }

    /**
     * Ends when it is cancelled in the displayPollResult callback
     * @param contexts I have to take in multiple parameters, but I'm only checking the first.
     * @return JSONArray with all unitIDs and mapIDs
     */
    public JSONArray doInBackground(Context... contexts){

        //What to do in the thread
        ClientComm comm = new ClientComm(contexts[0]);

        try {
            myName = myNameAndArmy.getJSONObject(0).getString("userID");
        }
        catch(JSONException e){
            //TODO
        }

        while(!isCancelled()){
            long waitTimeBetweenPolls = 1250; //in milliseconds. 1 s = 1000 ms
            /*
             *  I want to make the request synchronous, so I don't keep needlessly polling,
             *  but that requires me to change the ClientComm requests. So thats not going to happen
             */
            comm.serverPostRequest("inactivePoll.php", new JSONArray(), new VolleyCallback<JSONArray>() {
                @Override
                public void onSuccess(JSONArray result) {
                    Log.d("poll returned", result.toString());
                    myNameAndArmy = result;
                }
            });
            //I moved the sleep until after the request in order to give it a change to return first.
            SystemClock.sleep(waitTimeBetweenPolls);
            //allows UI to show whats going on. This calls onProgressUpdate
            if(!isCancelled()) {
                publishProgress(myNameAndArmy);
            }
            else{
                break;
            }

        }
        //onPostExecute will be ran, which will give this JSON array to the calling class
        return myNameAndArmy;
    }

    /**
     * Allows me to give changes to main thread even before this thread finishes
     * Its done on the UI thread when publishProgress is called above
     * @param progress the JSONArrays to be returned as progress. Only checks the first one given
     */
    @Override
    public void onProgressUpdate(JSONArray... progress){
        if(!isCancelled()){
            Log.d("polling loop", "publishing progress");
            receiver.handlePollResult(progress[0]);
        }
    }

    /**
     * gives result to ProcessFinish in calling class (yes, this is a callback, like the Volley one)
     * @param result JSONArray with my name and all units (mine and enemies)
     */
    @Override
    public void onPostExecute(JSONArray result){
        Log.d("polling loop", "onPostExecute ran");
        if(!isCancelled()) {
            //Nothing done with this, as the progressUpdate already does what I need...
            receiver.handlePollResult(result);
        }
    }
    @Override
    protected void onCancelled(JSONArray result){
        Log.d("onCancelled", "AsyncTask cancelled");
//        receiver.displayPollResult(result);
    }
}
