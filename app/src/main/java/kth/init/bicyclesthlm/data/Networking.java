package kth.init.bicyclesthlm.data;

import static kth.init.bicyclesthlm.BuildConfig.MAPS_API_KEY;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import kth.init.bicyclesthlm.model.BicyclePumpCollection;

public class Networking {

    private Activity activity;
    private static RequestQueue queue;

    public Networking(Activity activity) {
        this.activity = activity;
        queue = Volley.newRequestQueue(activity);
    }

    //String GET request
    public void getBicyclePumps() {
        String url = "https://openstreetws.stockholm.se/LvWS-2.2/Lv.asmx/" +
                "CountAttributeValuesForAttribute?" +
                "apiKey=d2f0f023-a168-4f18-816c-2ddedb0d3c0f&" +
                "featureTypeName=Cykelpump&" +
                "attributeName=Adress";

        StringRequest bicyclePumpRequest = new StringRequest(
                Request.Method.GET,
                url,
                BicyclePumpCollection.stringResponseListener,
                error -> Log.i("ERROR", "something went wrong" + error.getMessage())
        );
        queue.add(bicyclePumpRequest);
    }

    //JSON get request
    public void getLatLngFromAddress(String address) {
        String url = null;
        try {
            url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(address, "UTF-8") + "&key=" + MAPS_API_KEY;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonObjectRequest weatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                BicyclePumpCollection.JSONResponseListener,
                error -> Log.i("ERROR", "something went wrong")
        );
        queue.add(weatherRequest);
    }
}