package com.lavendor.model;

public class InsuranceOffer {

    private long id;
    private long vehicleId;
    private String insurer;
    private float price;

    public InsuranceOffer() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getInsurer() {
        return insurer;
    }

    public void setInsurer(String insurer) {
        this.insurer = insurer;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "InsuranceOffer{" +
                "vehicleId=" + vehicleId +
                ", insurer='" + insurer + '\'' +
                ", price=" + price +
                '}';
    }
}
