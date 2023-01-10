package kth.init.bicyclesthlm.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class TrafficFlowObject {

    private String meanValue;
    private String flowEstimation;
    private String street;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    private LatLng latLng;

    public TrafficFlowObject(String meanValue, String flowEstimation, String street) {
        this.meanValue = meanValue;
        this.flowEstimation = flowEstimation;
        this.street = street;
        this.latLng = new LatLng(-75.250973, -0.071389);
    }

    public String getMeanValue() {
        return meanValue;
    }

    public String getFlowEstimation() {
        return flowEstimation;
    }

    public String getStreet() {
        return street;
    }

    /*
    checks if arraylist already contains that street
     */
    public static boolean containsStreet(ArrayList<TrafficFlowObject> trafficList, String streetName) {
        for (TrafficFlowObject o : trafficList) {
            if (o != null && o.getStreet().equals(streetName)) {
                return true;
            }
        }
        return false;
    }
}
