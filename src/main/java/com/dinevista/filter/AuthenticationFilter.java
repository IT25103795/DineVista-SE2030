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

        // Derive path relative to the context root reliably across all containers
        String contextPath = req.getContextPath();
        String requestUri = req.getRequestURI();
        String path = requestUri.startsWith(contextPath)
                ? requestUri.substring(contextPath.length())
                : requestUri;

        // Normalise — remove trailing slash except bare "/"
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            path = "/";
        }

        // Allow public paths
        boolean isPublic = PUBLIC_PATHS.contains(path);

        // Allow static assets
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

        // Require an authenticated session for everything else
        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("userId") != null;

        if (loggedIn) {
            chain.doFilter(request, response);
        } else {
            if (path.startsWith("/staff/") || path.startsWith("/manager/")) {
                res.sendRedirect(contextPath + "/manager/login");
            } else {
                res.sendRedirect(contextPath + "/login");
            }
        }
    }

    @Override
    public void destroy() {
    }
}
