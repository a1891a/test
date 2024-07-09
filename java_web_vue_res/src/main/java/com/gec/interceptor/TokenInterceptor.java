package com.gec.interceptor;

import com.alibaba.fastjson.JSON;
import com.gec.common.SessionString;
import com.gec.entity.Permission;
import com.gec.entity.Result;
import com.gec.entity.User;
import com.gec.utils.JWTUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
//        return  true;
        //从请求头中获取所需参数
        try {
            System.out.println("前端请求方法路径: " + request.getRequestURI());
            //验证token是否还有效
            String verify = JWTUtils.verify(request.getHeader(("token")));
            if (verify.equals("请求成功")) {
                HttpSession session = request.getSession();
                //统一拦截（查询当前session是否存在user）(这里user会在每次登录成功后，写入session)
                User user = (User) session.getAttribute(SessionString.USER);
                if (user != null) {
                    //权限控制
                    List<Permission> permissionList = (List<Permission>) session.getAttribute(SessionString.USER_PERMISSION);
                    for (Permission item : permissionList) {
                        if (item.getUri().equals(request.getRequestURI())) {
                            System.out.println("权限通过");
                            return true;
                        }
                    }

                    response.setContentType("application/json;charset=utf-8");
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter printWriter = response.getWriter();

                    String resultJson = JSON.toJSON(new Result("3", "你没有权限执行该操作!")).toString();
                    printWriter.write(resultJson);
                    printWriter.close();
                    System.out.println("权限不通过");
                    return false;
//                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter printWriter = response.getWriter();

        String resultJson = JSON.toJSON(new Result("2", "令牌失效")).toString();
        printWriter.write(resultJson);
        printWriter.close();
        return false;
    }
}
