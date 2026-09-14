package com.dinevista.repository;

import com.dinevista.model.EventPackageRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryEventPackageRepository implements EventPackageRepository {
    private final List<EventPackageRecord> records = new CopyOnWriteArrayList<>();
    private final AtomicLong ids = new AtomicLong(100);

    public InMemoryEventPackageRepository() {
        records.add(new EventPackageRecord(1, "Joyful Gatherings", "BIRTHDAY",
                "Birthday and family celebration package.", new java.math.BigDecimal("4500.00"),
                20, 180, 240, "Buffet or set menu, basic styling, welcome beverage, service staff", true));
        records.add(new EventPackageRecord(2, "Everlasting Elegance", "WEDDING",
                "Premium wedding reception package.", new java.math.BigDecimal("7900.00"),
                50, 350, 360, "Premium menu, venue styling, coordinator, bridal table, cake service", true));
        records.add(new EventPackageRecord(3, "Professional Impact", "CORPORATE",
                "Corporate meeting and launch package.", new java.math.BigDecimal("5800.00"),
                20, 300, 300, "Meeting setup, food service, audio-visual essentials, registration support", true));
    }

    @Override public EventPackageRecord save(EventPackageRecord r) {
        records.add(r); return r;
    }
    @Override public EventPackageRecord update(EventPackageRecord r) {
        delete(r.getId()); records.add(r); return r;
    }
    @Override public boolean deactivate(long id) {
        Optional<EventPackageRecord> r=findById(id);
        if (r.isEmpty()) return false;
        EventPackageRecord x=r.get();
        update(new EventPackageRecord(x.getId(),x.getName(),x.getCategory(),x.getDescription(),x.getPricePerGuest(),
                x.getMinimumGuests(),x.getMaximumGuests(),x.getDurationMinutes(),x.getInclusions(),false));
        return true;
    }
    @Override public boolean delete(long id) { return records.removeIf(r -> r.getId()==id); }
    @Override public Optional<EventPackageRecord> findById(long id) {
        return records.stream().filter(r->r.getId()==id).findFirst();
    }
    @Override public List<EventPackageRecord> findAll(boolean activeOnly, String search) {
        String q=search==null?"":search.trim().toLowerCase();
        return records.stream()
                .filter(r -> !activeOnly || r.isActive())
                .filter(r -> q.isEmpty() || r.getName().toLowerCase().contains(q)
                        || r.getCategory().toLowerCase().contains(q)
                        || r.getDescription().toLowerCase().contains(q))
                .sorted(Comparator.comparing(EventPackageRecord::getName))
                .collect(Collectors.toList());
    }
    @Override public long nextId() { return ids.incrementAndGet(); }
}
