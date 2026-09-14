package com.dinevista.service;

import com.dinevista.model.EventPackageRecord;
import com.dinevista.repository.EventPackageRepository;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class EventPackageService {
    public static final List<String> CATEGORIES=Collections.unmodifiableList(Arrays.asList("WEDDING","CORPORATE","BIRTHDAY","ANNIVERSARY","PRIVATE","CUSTOM"));
    private final EventPackageRepository repository;
    public EventPackageService(EventPackageRepository repository){this.repository=repository;}
    public List<EventPackageRecord> packages(boolean activeOnly,String search){return repository.findAll(activeOnly,search);}
    public Optional<EventPackageRecord> find(long id){return repository.findById(id);}
    public OperationResult<EventPackageRecord> create(String name,String category,String description,BigDecimal price,int min,int max,int duration,String inclusions){
        List<String> e=validate(name,category,description,price,min,max,duration,inclusions);
        if(!e.isEmpty())return OperationResult.failure(e);
        return OperationResult.success(repository.save(new EventPackageRecord(repository.nextId(),clean(name),category,clean(description),price,min,max,duration,clean(inclusions),true)));
    }
    public OperationResult<EventPackageRecord> update(long id,String name,String category,String description,BigDecimal price,int min,int max,int duration,String inclusions,boolean active){
        if(repository.findById(id).isEmpty())return OperationResult.failure("Event package was not found.");
        List<String> e=validate(name,category,description,price,min,max,duration,inclusions);
        if(!e.isEmpty())return OperationResult.failure(e);
        return OperationResult.success(repository.update(new EventPackageRecord(id,clean(name),category,clean(description),price,min,max,duration,clean(inclusions),active)));
    }
    public OperationResult<Void> deactivate(long id){
        if(!repository.deactivate(id))return OperationResult.failure("Event package was not found.");
        return OperationResult.success(null);
    }
    public OperationResult<Void> delete(long id){
        if(!repository.delete(id))return OperationResult.failure("Event package was not found.");
        return OperationResult.success(null);
    }
    private List<String> validate(String name,String category,String description,BigDecimal price,int min,int max,int duration,String inclusions){
        List<String> e=new ArrayList<>();
        String n=clean(name),c=clean(category),d=clean(description),inc=clean(inclusions);
        if(n.length()<2||n.length()>140)e.add("Package name must contain 2 to 140 characters.");
        if(!CATEGORIES.contains(c))e.add("Select a valid event category.");
        if(d.length()>800)e.add("Description cannot exceed 800 characters.");
        if(price==null||price.compareTo(BigDecimal.ZERO)<=0)e.add("Price per guest must be greater than zero.");
        if(min<1)e.add("Minimum guests must be at least 1.");
        if(max<min)e.add("Maximum guests must be greater than or equal to minimum guests.");
        if(max>2000)e.add("Maximum guests cannot exceed 2000.");
        if(duration<30||duration>1440)e.add("Event duration must be between 30 minutes and 24 hours.");
        if(inc.length()>5000)e.add("Inclusions cannot exceed 5000 characters.");
        return e;
    }
    private static String clean(String s){return s==null?"":s.trim();}
}
