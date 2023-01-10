package kth.init.bicyclesthlm.data;

import static kth.init.bicyclesthlm.BuildConfig.MAPS_API_KEY;
import static kth.init.bicyclesthlm.BuildConfig.STOCKHOLM_API_KEY;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import kth.init.bicyclesthlm.model.BicycleCollection;
import kth.init.bicyclesthlm.model.TrafficFlowCollection;
import kth.init.bicyclesthlm.model.TrafficFlowObject;

public class Networking {

    private Activity activity;
    private static RequestQueue queue;

    public Networking(Activity activity) {
        this.activity = activity;
        queue = Volley.newRequestQueue(activity);
    }

    //String GET request
    public void getBicyclePumps(BicycleCollection collection) {

        String url = "https://openstreetws.stockholm.se/LvWS-2.2/Lv.asmx/" +
                "CountAttributeValuesForAttribute?" +
                "apiKey=" + STOCKHOLM_API_KEY + "&" +
                "featureTypeName=Cykelpump&" +
                "attributeName=Adress";

        StringRequest bicyclePumpRequest = new StringRequest(
                Request.Method.GET,
                url,
                collection.stringResponseListener,
                error -> Log.i("ERROR", "something went wrong" + error.getMessage())
        );
        queue.add(bicyclePumpRequest);
    }

    //String GET request
    public void getCityBikes(BicycleCollection collection) {
        String url = "https://openstreetws.stockholm.se/LvWS-2.2/Lv.asmx/" +
                "CountAttributeValuesForAttribute?" +
                "apiKey=" + STOCKHOLM_API_KEY + "&" +
                "featureTypeName=CityBikes&" +
                "attributeName=Beskrivning";

        StringRequest cityBikesRequest = new StringRequest(
                Request.Method.GET,
                url,
                collection.stringResponseListener,
                error -> Log.i("ERROR", "something went wrong" + error.getMessage())
        );
        queue.add(cityBikesRequest);
    }

    //String GET request
    public void getBikeParking(BicycleCollection collection) {
        String url = "https://openstreetws.stockholm.se/LvWS-2.2/Lv.asmx/" +
                "CountAttributeValuesForAttribute?" +
                "apiKey=" + STOCKHOLM_API_KEY + "&" +
                "featureTypeName=Cykelparkering&" +
                "attributeName=Plats";

        StringRequest bikeParkingRequest = new StringRequest(
                Request.Method.GET,
                url,
                collection.stringResponseListener,
                error -> Log.i("ERROR", "something went wrong" + error.getMessage())
        );
        queue.add(bikeParkingRequest);
    }

    /*
    Does this because we need to get the linkIds, to get the object Ids
     */
    public void getTraficFlowLinkIds(TrafficFlowCollection collection){
        String url = "https://openstreetws.stockholm.se/LvWS-2.2/Lv.asmx/" +
                "CountAttributeValuesForAttribute?" +
                "apiKey=" + STOCKHOLM_API_KEY + "&" +
                "featureTypeName=Trafikflöde Cykel&" +
                "attributeName=ID";

        StringRequest bikeTraficFlowRequest = new StringRequest(
                Request.Method.GET,
                url,
                collection.linkIdResponseListener,
                error -> Log.i("ERROR", "something went wrong" + error.getMessage())
        );
        queue.add(bikeTraficFlowRequest);
    }

    /*
    Gets object id to be able to get specific information about object
     */

    public void getObjectIds(String linkIds, TrafficFlowCollection collection){

        String url = "https://openstreetws.stockholm.se/LvWS-4.0/Lv.svc/json/" +
                "GetFeatureListForLinks?" +
                "apiKey=" + STOCKHOLM_API_KEY + "&" +
                "linkIds=[" + linkIds +"]&" +
                "featureTypeIds=[15489448]";

        JsonArrayRequest bikeTraficFlowRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                collection.objectIdResponseListener,
                error -> Log.i("ERROR", "something went wrong" + error.getMessage())
        );
        queue.add(bikeTraficFlowRequest);
    }

    /*
    Gets specific info by using objectId
     */
    public void getTrafficFlowInfo(String id, TrafficFlowCollection collection){
        String url = "https://openstreetws.stockholm.se/LvWS-4.0/Lv.svc/json/" +
                "GetFeatures?" +
                "apiKey=" + STOCKHOLM_API_KEY + "&" +
                "objectIds=[" + id +"]&" +
                "includeWktForExtents=false";

        JsonArrayRequest bikeTraficFlowRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                collection.trafficFlowInfoResponseListener,
                error -> Log.i("ERROR", "something went wrong" + error.getMessage())
        );
        queue.add(bikeTraficFlowRequest);
    }


    //JSON get request
    public void getLatLngFromAddress(String address, BicycleCollection collection) {
        String url = null;
        try {
            url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(address, "UTF-8") + "&key=" + MAPS_API_KEY;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonObjectRequest addressRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                collection.JSONResponseListener,
                error -> Log.i("ERROR", "something went wrong")
        );
        queue.add(addressRequest);
    }

    //JSON get request
    public void getLatLngFromTrafficFlowAddress(String address) {
        String url = null;
        try {
            url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(address, "UTF-8") + "&key=" + MAPS_API_KEY;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonObjectRequest addressRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                TrafficFlowCollection.trafficFlowCordinates,
                error -> Log.i("ERROR", "something went wrong")
        );
        queue.add(addressRequest);
    }
}