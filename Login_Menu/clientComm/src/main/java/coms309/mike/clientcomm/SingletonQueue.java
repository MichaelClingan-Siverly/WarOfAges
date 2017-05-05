package coms309.mike.clientcomm;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Mike
 * A request queue using the singleton design pattern.
 * How it works: The actual request queue is a private member of this class. I've created a
 * constructor and accessor method.
 */

//no modifier. I didn't need it public, and it wouldn't let me make it protected.
class SingletonQueue {
    //this will be checked to ensure that there is only one queue
    private static SingletonQueue singleQ;
    //context should always be application, since this will probably be running for the life of the app
    private static Context context;
    private RequestQueue requestQ;

    private SingletonQueue(Context context){
        this.context = context;
        requestQ = getRequestQueue();

        //TODO I read you need an imageloader for images. Do it.
    }
    private RequestQueue getRequestQueue(){
        if(requestQ == null){
            //this is done in app context, because it will leak memory if created in an activity
            requestQ = Volley.newRequestQueue(context.getApplicationContext());
            /*
            Also, it's important to note the difference between Volley.newRequestQueue and just
            new RequestQueue. The volley one takes context as parameters and the other takes cache
            and network -- NOT what I need to be messing with
            But I do want to mess with context - that's the whole reason to make this a singleton.
            */
        }
        return requestQ;
    }

    /**
     * The constructor of the singleton request queue.
     * @param context
     * the context you're calling from. (I normally just do this.getApplicationContext(), since that
     * is where the singleton's context should be anyway, but I force
     * @return SingletonQueue
     * your request queue. All good to go.
     */
    //TODO test if I even need to have context as a parameter - why not make it default to application?
    protected static synchronized SingletonQueue GetInstance(Context context){
        if(singleQ == null){
            singleQ = new SingletonQueue(context);
        }
        return singleQ;
    }

    //I'd like it if this returned a useful value, but it only returns the passed-in request.
    protected  <T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
    }
}
