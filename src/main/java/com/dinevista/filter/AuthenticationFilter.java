package com.dinevista.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/", "/index.jsp", "/login", "/manager/login", "/register", "/manager/register", 
            "/menu", "/events", "/about", "/health"
    );

    private static final List<String> ASSET_PREFIXES = Arrays.asList(
            "/assets/"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        
        if (req.getPathInfo() != null) {
            path += req.getPathInfo();
        }

        boolean isPublic = PUBLIC_PATHS.contains(path);
        
        if (!isPublic) {
            for (String prefix : ASSET_PREFIXES) {
                if (path.startsWith(prefix)) {
                    isPublic = true;
                    break;
                }
            }
        }

        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("userId") != null;

        if (loggedIn) {
            chain.doFilter(request, response);
        } else {
            if (path.startsWith("/staff/") || path.startsWith("/manager/")) {
                res.sendRedirect(req.getContextPath() + "/manager/login");
            } else {
                res.sendRedirect(req.getContextPath() + "/login");
            }
        }
    }

    @Override
    public void destroy() {
    }
}
