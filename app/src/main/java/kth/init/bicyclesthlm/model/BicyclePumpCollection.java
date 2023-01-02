package kth.init.bicyclesthlm.model;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import java.util.ArrayList;

import kth.init.bicyclesthlm.data.Networking;
import kth.init.bicyclesthlm.data.ParseJSON;
import kth.init.bicyclesthlm.data.ParseString;

public class BicyclePumpCollection {

    private static Activity activity;
    private static RequestQueue queue;
    private static ArrayList<BicyclePump> bicyclePumps;
    private static ArrayList<LatLng> latLngs;

    public BicyclePumpCollection(Context context, Activity activityIn) {
        latLngs = new ArrayList<>();
        bicyclePumps = new ArrayList<>();
        activity = activityIn;
        queue = Volley.newRequestQueue(context);
    }

    public ArrayList<BicyclePump> getBicyclePumps() {
        return bicyclePumps;
    }

    public ArrayList<LatLng> getLatLngs() {
        return latLngs;
    }

    //Response listener for bicycle pumps
    public static final Response.Listener<String> stringResponseListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {

            try {
                bicyclePumps = ParseString.getParsedBicyclePumpList(response);

                Networking networking = new Networking(activity);

                //Calls GET request to get latlng for each address
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    bicyclePumps.forEach(bicyclePump ->
                            networking.getLatLngFromAddress(bicyclePump.getAddress()));
                }

                queue.cancelAll(this);
            } catch (Exception e) {
                Log.i("error while parsing", e.toString());
            }
        }
    };

    //Response Listener for latlng of bicycle pump's addresses
    public static final Response.Listener<JSONObject> JSONResponseListener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject responseArr) {

            try {
                LatLng latLng = ParseJSON.getParsedLatLng(responseArr);

                if (latLng != null)
                    latLngs.add(latLng);
                else
                    latLngs.add(new LatLng(-75.250973, -0.071389));

                queue.cancelAll(this);
            } catch (Exception e) {
                Log.i("error while parsing", e.toString());
            }
        }
    };
}