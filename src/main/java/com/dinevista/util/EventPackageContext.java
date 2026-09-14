package com.dinevista.util;
import com.dinevista.repository.*;
import com.dinevista.service.EventPackageService;
import javax.servlet.ServletContext;
public final class EventPackageContext {
    private static final String REPO=EventPackageRepository.class.getName(), SERVICE=EventPackageService.class.getName();
    private EventPackageContext(){}
    public static EventPackageService service(ServletContext c){
        synchronized(c){
            EventPackageService s=(EventPackageService)c.getAttribute(SERVICE);
            if(s==null){
                DatabaseConfig cfg=DatabaseConfig.load(); EventPackageRepository r;
                if(cfg.isMysqlEnabled()){try{r=new JdbcEventPackageRepository(cfg);c.setAttribute("eventPackageStorageMode","mysql");}
                catch(Exception e){throw new IllegalStateException("Event Package persistence could not start.",e);}}
                else{r=new InMemoryEventPackageRepository();c.setAttribute("eventPackageStorageMode","memory");}
                c.setAttribute(REPO,r);s=new EventPackageService(r);c.setAttribute(SERVICE,s);
            } return s;
        }
    }
    public static EventPackageRepository repository(ServletContext c){service(c);return (EventPackageRepository)c.getAttribute(REPO);}
}
