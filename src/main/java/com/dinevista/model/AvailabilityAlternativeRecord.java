package com.dinevista.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AvailabilityAlternativeRecord {
    private final LocalDate date;
    private final LocalTime time;
    private final List<RestaurantTableRecord> tables;

    public AvailabilityAlternativeRecord(LocalDate date, LocalTime time,
                                         List<RestaurantTableRecord> tables) {
        this.date = date;
        this.time = time;
        this.tables = new ArrayList<>(tables);
    }

    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public List<RestaurantTableRecord> getTables() {
        return Collections.unmodifiableList(tables);
    }
    public int getAvailableTableCount() { return tables.size(); }

    public String getDateInputValue() { return date.toString(); }
    public String getTimeInputValue() { return time.toString(); }
    public String getDateDisplay() {
        return date.format(DateTimeFormatter.ofPattern("EEE, dd MMM"));
    }
    public String getTimeDisplay() {
        return time.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
}
