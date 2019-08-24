package com.loop.hyh.core;

import com.loop.hyh.core.servlet.context.ServletContext;

public class WebApplication {
    private static ServletContext servletContext = new ServletContext();
    
    public static ServletContext getServletContext() {
        return servletContext;
    }
}
