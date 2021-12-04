package cz.ucenislovicek.BakalariAPI.rozvrh;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class AppSingleton {

    @SuppressLint("StaticFieldLeak")
    private static AppSingleton instance;
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;
    private RequestQueue requestQueue;
    private RozvrhAPI rozvrhAPI;


    private AppSingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized AppSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new AppSingleton(context.getApplicationContext());
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx);
        }
        return requestQueue;
    }

    public RozvrhAPI getRozvrhAPI() {
        if (rozvrhAPI == null) {
            rozvrhAPI = new RozvrhAPI(getRequestQueue(), ctx.getApplicationContext());
        }
        return rozvrhAPI;
    }



}

