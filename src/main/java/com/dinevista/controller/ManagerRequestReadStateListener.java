package com.dinevista.controller;

import com.dinevista.util.ManagerRequestReadState;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** Establish the old-request baseline before any new customer submissions arrive. */
@WebListener
public class ManagerRequestReadStateListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            ManagerRequestReadState.get(event.getServletContext());
        } catch (IllegalStateException ex) {
            // Other modules initialize lazily too; retry on the first manager request.
            event.getServletContext().log("Manager request read state will retry on first use.", ex);
        }
    }
}
