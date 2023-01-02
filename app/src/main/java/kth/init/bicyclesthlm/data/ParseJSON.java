package kth.init.bicyclesthlm.data;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
}
