package com.loop.hyh.sample.servlet;

import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.request.Request;
import com.loop.hyh.core.response.Response;
import com.loop.hyh.core.servlet.base.HTTPServlet;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class UserServlet extends HTTPServlet {
    @Override
    public void doGet(Request request, Response response) throws ServletException, IOException {
        if (request.getSession().getAttribute("username") != null) {
            log.info("已经登录");
            request.getRequestDispatcher("/views/user.html").forward(request, response);
        } else {
            log.info("尚未登录，跳转至login");
            //必须使用从浏览器角度看的路径，凡是静态资源，前面都没有/static or /views
            //但是forward是从服务器角度看的路径，是真实的、相对的路径
            response.sendRedirect("/views/login.html");
        }
    }
}
