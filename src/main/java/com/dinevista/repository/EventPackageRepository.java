package com.dinevista.repository;

import com.dinevista.model.EventPackageRecord;
import java.util.List;
import java.util.Optional;

public interface EventPackageRepository {
    EventPackageRecord save(EventPackageRecord record);
    EventPackageRecord update(EventPackageRecord record);
    boolean deactivate(long id);
    boolean delete(long id);
    Optional<EventPackageRecord> findById(long id);
    List<EventPackageRecord> findAll(boolean activeOnly, String search);
    long nextId();
}
