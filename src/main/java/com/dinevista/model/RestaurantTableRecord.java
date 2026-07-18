package com.dinevista.model;

import java.io.Serializable;

public class RestaurantTableRecord implements Serializable {
    private final long id;
    private final String code;
    private final String seatingArea;
    private final int capacity;
    private String status;

    public RestaurantTableRecord(long id, String code, String seatingArea, int capacity, String status) {
        this.id = id;
        this.code = code;
        this.seatingArea = seatingArea;
        this.capacity = capacity;
        this.status = status;
    }

    public long getId() { return id; }
    public String getCode() { return code; }
    public String getSeatingArea() { return seatingArea; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSeatingAreaDisplay() {
        return seatingArea == null ? "" : seatingArea.replace('_', ' ');
    }
}
