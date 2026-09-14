package com.dinevista.controller;

import com.dinevista.model.EventPackageRecord;
import com.dinevista.service.EventPackageService;
import com.dinevista.service.OperationResult;
import com.dinevista.util.EventPackageContext;
import com.dinevista.util.ReservationOrderContext;
import com.dinevista.util.RequestUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns={"/staff/event-packages","/staff/event-packages/*"})
public class EventPackageServlet extends HttpServlet {
    private EventPackageService service;
    @Override public void init(){service=EventPackageContext.service(getServletContext());}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        if(!manager(req,res))return;
        String path=path(req);
        if(path.isEmpty()){renderList(req,res);return;}
        if("/new".equals(path)){req.setAttribute("packageFormMode","create");forwardForm(req,res);return;}
        if("/edit".equals(path)){Optional<EventPackageRecord> p=service.find(id(req));if(p.isEmpty()){res.sendError(404);return;}req.setAttribute("eventPackage",p.get());req.setAttribute("packageFormMode","edit");forwardForm(req,res);return;}
        if("/view".equals(path)){Optional<EventPackageRecord> p=service.find(id(req));if(p.isEmpty()){res.sendError(404);return;}req.setAttribute("eventPackage",p.get());req.getRequestDispatcher("/WEB-INF/views/staff-event-package-detail.jsp").forward(req,res);return;}
        res.sendError(404);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        if(!manager(req,res))return;
        String path=path(req);
        if("/create".equals(path)||"/update".equals(path)){
            BigDecimal price=null;try{price=new BigDecimal(RequestUtil.clean(req,"price"));}catch(Exception ignored){}
            int min=RequestUtil.integer(req,"minimumGuests",0),max=RequestUtil.integer(req,"maximumGuests",0),duration=RequestUtil.integer(req,"durationMinutes",0);
            OperationResult<EventPackageRecord> result="/create".equals(path)
                    ?service.create(RequestUtil.clean(req,"name"),RequestUtil.clean(req,"category"),RequestUtil.clean(req,"description"),price,min,max,duration,RequestUtil.clean(req,"inclusions"))
                    :service.update(id(req),RequestUtil.clean(req,"name"),RequestUtil.clean(req,"category"),RequestUtil.clean(req,"description"),price,min,max,duration,RequestUtil.clean(req,"inclusions"),"true".equals(req.getParameter("active")));
            if(!result.isSuccess()){req.setAttribute("errors",result.getErrors());req.setAttribute("packageFormMode","/create".equals(path)?"create":"edit");if("/update".equals(path))req.setAttribute("eventPackage",service.find(id(req)).orElse(null));copy(req);forwardForm(req,res);return;}
            res.sendRedirect(req.getContextPath()+"/staff/event-packages/view?id="+result.getValue().getId()+"&saved=1");return;
        }
        if("/deactivate".equals(path)){OperationResult<Void> r=service.deactivate(id(req));res.sendRedirect(req.getContextPath()+"/staff/event-packages"+(r.isSuccess()?"?deactivated=1":"?error="+java.net.URLEncoder.encode(r.getErrors().get(0),"UTF-8")));return;}
        if("/delete".equals(path)){OperationResult<Void> r=service.delete(id(req));res.sendRedirect(req.getContextPath()+"/staff/event-packages"+(r.isSuccess()?"?deleted=1":"?error="+java.net.URLEncoder.encode(r.getErrors().get(0),"UTF-8")));return;}
        res.sendError(404);
    }
    private void renderList(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        req.setAttribute("eventPackages",service.packages(false,RequestUtil.clean(req,"search")));req.getRequestDispatcher("/WEB-INF/views/staff-event-packages.jsp").forward(req,res);
    }
    private void forwardForm(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{req.getRequestDispatcher("/WEB-INF/views/staff-event-package-form.jsp").forward(req,res);}
    private void copy(HttpServletRequest r){r.setAttribute("formName",RequestUtil.clean(r,"name"));r.setAttribute("formCategory",RequestUtil.clean(r,"category"));r.setAttribute("formDescription",RequestUtil.clean(r,"description"));r.setAttribute("formPrice",RequestUtil.clean(r,"price"));r.setAttribute("formMinimumGuests",RequestUtil.clean(r,"minimumGuests"));r.setAttribute("formMaximumGuests",RequestUtil.clean(r,"maximumGuests"));r.setAttribute("formDurationMinutes",RequestUtil.clean(r,"durationMinutes"));r.setAttribute("formInclusions",RequestUtil.clean(r,"inclusions"));}
    private long id(HttpServletRequest r){try{return Long.parseLong(RequestUtil.clean(r,"id"));}catch(Exception e){return -1;}}
    private String path(HttpServletRequest r){String p=r.getPathInfo();if(p==null||"/".equals(p))return "";if(p.endsWith("/"))p=p.substring(0,p.length()-1);return p;}
    private boolean manager(HttpServletRequest r,HttpServletResponse s)throws IOException{if(!ReservationOrderContext.isSignedIn(r)){s.sendRedirect(r.getContextPath()+"/manager/login");return false;}if(!ReservationOrderContext.isManager(r)){s.sendError(403);return false;}return true;}
}
