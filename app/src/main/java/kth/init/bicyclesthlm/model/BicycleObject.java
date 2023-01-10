package kth.init.bicyclesthlm.model;

public class BicycleObject {
    private String address;

    public BicycleObject(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "address='" + address +
                '}';
    }
}
