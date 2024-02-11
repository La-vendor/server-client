package com.lavendor.model;

import java.util.Objects;

public class InsuranceOffer {

    private long id;
    private long vehicleId;
    private String insurer;
    private float price;

    public InsuranceOffer() {
    }

    public InsuranceOffer(long id, long vehicleId, String insurer, float price) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.insurer = insurer;
        this.price = price;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InsuranceOffer that = (InsuranceOffer) o;
        return id == that.id && vehicleId == that.vehicleId && Float.compare(price, that.price) == 0 && Objects.equals(insurer, that.insurer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vehicleId, insurer, price);
    }
}
