package cn.edu.bupt.sac.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class StartController {
    @RequestMapping("")
    public String start() {
        return "start";
    }

    @RequestMapping("/auth")
    public String auth() {
        return "auth";
    }

}
