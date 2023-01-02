package kth.init.bicyclesthlm.model;

public class BicyclePump {
    private String address;

    public BicyclePump(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "BicyclePump{" +
                "address='" + address +
                '}';
    }
}
