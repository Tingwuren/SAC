package cn.edu.bupt.sac.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    // 用户认证
    @PostMapping("/authenticate")
    public String authenticate() {
        return "用户认证成功";
    }


}
