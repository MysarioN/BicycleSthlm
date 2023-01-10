package kth.init.bicyclesthlm.data;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kth.init.bicyclesthlm.model.TrafficFlowCollection;
import kth.init.bicyclesthlm.model.TrafficFlowObject;

public class ParseJSON {

    //Takes JSON response from GET request and parses into latlng
    public static LatLng getParsedLatLng(JSONObject object) {

        try {
            JSONObject root = object;
            Map<String, String> ret = new HashMap<String, String>();
            JSONArray results = root.getJSONArray("results");
            if (results.length() > 0) {
                JSONObject jsonObject;
                jsonObject = results.getJSONObject(0);
                ret.put("lat", jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                ret.put("lng", jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng"));

                return new LatLng(Double.parseDouble(ret.get("lat")), Double.parseDouble(ret.get("lng")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    //Takes JSON response from GET request and parses into an Arraylist containing strings with objectId
    public static ArrayList<String> getParsedObjectId(JSONArray results) {
        ArrayList<String> stringList = new ArrayList<>();
        try {
            if (results.length() > 0) {
                JSONObject newObj = results.getJSONObject(0);
                stringList.add( newObj.getString("ObjectId"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stringList;
    }

    //Takes JSON response from GET request and parses Traffic flow objects
    public static TrafficFlowObject getParsedTrafficInfo(JSONArray results) {

        TrafficFlowObject trafficFlowObj = null;

        try {
            if (results.length() > 0) {
                for(int i = 0; i < results.length(); i++) {
                    JSONObject newObj = results.getJSONObject(i);

                    String mean = newObj.getJSONArray("AttributeValues").getJSONObject(1).getString("Value");
                    String street = newObj.getJSONArray("AttributeValues").getJSONObject(2).getString("Value");
                    String flowEstimation = newObj.getJSONArray("AttributeValues").getJSONObject(3).getString("Value");

                    if(mean != null || street != null || flowEstimation != null){
                        trafficFlowObj = new TrafficFlowObject(mean, flowEstimation, street);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trafficFlowObj;
    }
}
