package kth.init.bicyclesthlm.model;

public class FiltersDialogModel {

    private boolean bicyclePaths;
    private boolean cityBikes;
    private boolean bicyclePumps;
    private boolean bicycleParking;
    private boolean bicycleTrafficFlow;
    private boolean standardMap;
    private boolean satelliteMap;

    public FiltersDialogModel() {
        bicyclePaths = false;
        cityBikes = false;
        bicyclePumps = false;
        bicycleParking = false;
        bicycleTrafficFlow = false;
        standardMap = false;
        satelliteMap = false;
    }

    public boolean isBicyclePaths() {
        return bicyclePaths;
    }

    public void setBicyclePaths(boolean bicyclePaths) {
        this.bicyclePaths = bicyclePaths;
    }

    public boolean isCityBikes() {
        return cityBikes;
    }

    public void setCityBikes(boolean cityBikes) {
        this.cityBikes = cityBikes;
    }

    public boolean isBicyclePumps() {
        return bicyclePumps;
    }

    public void setBicyclePumps(boolean bicyclePumps) {
        this.bicyclePumps = bicyclePumps;
    }

    public boolean isBicycleParking() {
        return bicycleParking;
    }

    public void setBicycleParking(boolean bicycleParking) {
        this.bicycleParking = bicycleParking;
    }

    public boolean isBicycleTrafficFlow() {
        return bicycleTrafficFlow;
    }

    public void setBicycleTrafficFlow(boolean bicycleTrafficFlow) {
        this.bicycleTrafficFlow = bicycleTrafficFlow;
    }

    public boolean isStandardMap() {
        return standardMap;
    }

    public void setStandardMap(boolean standardMap) {
        this.standardMap = standardMap;
    }

    public boolean isSatelliteMap() {
        return satelliteMap;
    }

    public void setSatelliteMap(boolean satelliteMap) {
        this.satelliteMap = satelliteMap;
    }

    public boolean[] isAllChecked() {
        return new boolean[]{bicyclePaths, cityBikes, bicyclePumps, bicycleParking, bicycleTrafficFlow};
    }
}
