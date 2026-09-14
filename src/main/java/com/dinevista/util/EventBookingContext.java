package com.dinevista.util;
import com.dinevista.repository.*;
import com.dinevista.service.EventBookingService;
import javax.servlet.ServletContext;
public final class EventBookingContext {
    private static final String REPO=EventBookingRepository.class.getName(), SERVICE=EventBookingService.class.getName();
    private EventBookingContext(){}
    public static EventBookingService service(ServletContext c){
        synchronized(c){
            EventBookingService s=(EventBookingService)c.getAttribute(SERVICE);
            if(s==null){
                DatabaseConfig cfg=DatabaseConfig.load(); EventBookingRepository r;
                if(cfg.isMysqlEnabled()){try{r=new JdbcEventBookingRepository(cfg);c.setAttribute("eventBookingStorageMode","mysql");}
                catch(Exception e){throw new IllegalStateException("Event Booking persistence could not start.",e);}}
                else{r=new InMemoryEventBookingRepository();c.setAttribute("eventBookingStorageMode","memory");}
                c.setAttribute(REPO,r);s=new EventBookingService(r,EventPackageContext.repository(c));c.setAttribute(SERVICE,s);
            } return s;
        }
    }
}
