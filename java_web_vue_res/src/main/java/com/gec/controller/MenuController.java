package com.gec.controller;

import com.gec.common.SessionString;
import com.gec.entity.Result;
import com.gec.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/menu")

public class MenuController {
    @RequestMapping("/getMenu")
    public Result getMenu( HttpServletRequest request) {
        return new Result(request.getSession().getAttribute(SessionString.USER_MENU),"请求成功");
    }
}
