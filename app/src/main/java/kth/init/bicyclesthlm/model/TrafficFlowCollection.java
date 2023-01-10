package kth.init.bicyclesthlm.model;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import kth.init.bicyclesthlm.data.Networking;
import kth.init.bicyclesthlm.data.ParseJSON;
import kth.init.bicyclesthlm.data.ParseString;

public class TrafficFlowCollection {

    private static Activity activity;
    private static RequestQueue queue;
    private ArrayList<String> linkIds;
    private ArrayList<String> objectIds;
    private static ArrayList<TrafficFlowObject> trafficFlowObjectArrayList;
    private TrafficFlowCollection thisCollection;
    private static int counter;


    public TrafficFlowCollection(Context context, Activity activityIn) {
        linkIds = new ArrayList();
        objectIds = new ArrayList<>();
        trafficFlowObjectArrayList = new ArrayList<>();
        activity = activityIn;
        queue = Volley.newRequestQueue(context);
        thisCollection = this;
        counter = 0;
    }

    public ArrayList<TrafficFlowObject> getTrafficFlowObjectArrayList() {
        return trafficFlowObjectArrayList;
    }

    /*
    response listener for link id
     */
    public Response.Listener<String> linkIdResponseListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {

            try {
                linkIds = ParseString.getParsedLinkIdsList(response, "AttributeValue");
                Networking networking = new Networking(activity);

                //Calls GET for object ids
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    linkIds.forEach(String -> networking.getObjectIds(String, thisCollection));
                }
                queue.cancelAll(this);
            } catch (Exception e) {
                Log.i("error while parsing", e.toString());
            }
        }
    };


    /*
    response listener for object id
     */
    public Response.Listener<JSONArray> objectIdResponseListener = new Response.Listener<JSONArray>() {

        @Override
        public void onResponse(JSONArray responseArr) {
            objectIds = ParseJSON.getParsedObjectId(responseArr);
            Networking networking = new Networking(activity);

            try {
                //Calls GET for traffic flow info
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    objectIds.forEach(String -> networking.getTrafficFlowInfo(String, thisCollection));
                }
                queue.cancelAll(this);
            } catch (Exception e) {
                Log.i("error while parsing", e.toString());
            }
        }
    };

    /*
    response listener for traffic info
     */
    public Response.Listener<JSONArray> trafficFlowInfoResponseListener = new Response.Listener<JSONArray>() {

        @Override
        public void onResponse(JSONArray responseArr) {
            Networking networking = new Networking(activity);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    TrafficFlowObject obj = ParseJSON.getParsedTrafficInfo(responseArr);

                    //Calls GET for addresses latitude and longditude, also adds trafficobject to arraylist
                    if (!TrafficFlowObject.containsStreet(trafficFlowObjectArrayList, obj.getStreet())) {
                        networking.getLatLngFromTrafficFlowAddress(obj.getStreet());
                        trafficFlowObjectArrayList.add(obj);
                    }
                }
                queue.cancelAll(this);
            } catch (Exception e) {
                Log.i("error while parsing", e.toString());
            }
        }
    };

    /*
    response listener for traffic info addresses
     */
    public static Response.Listener<JSONObject> trafficFlowCordinates = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            try {
                LatLng latLng = ParseJSON.getParsedLatLng(response);

                if (latLng != null) {
                    trafficFlowObjectArrayList.get(counter).setLatLng(latLng);
                }
                counter++;
                queue.cancelAll(this);
            } catch (Exception e) {
                Log.i("error while parsing", e.toString());
            }
        }
    };
}
