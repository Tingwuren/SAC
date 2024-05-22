package cn.edu.bupt.sac.controller;

import cn.edu.bupt.sac.DTO.AuthRequest;
import cn.edu.bupt.sac.DTO.AuthResponse;
import cn.edu.bupt.sac.entity.Room;
import cn.edu.bupt.sac.entity.SAC;
import cn.edu.bupt.sac.entity.User;
import cn.edu.bupt.sac.service.RoomService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sac")
public class SacController {
    @Value("${cac.url}")
    private String cacUrl;
    private final RoomService roomService;
    private final RestTemplate restTemplate;
    private Room room;
    private SAC sac;
    private User user;

    @Autowired
    public SacController(RoomService roomService) {
        this.roomService = roomService;
        this.restTemplate = new RestTemplate();
    }
    @PostConstruct
    public void init() {
        room = roomService.getRoom();
        sac = roomService.getRoom().getSac();
        user = roomService.getRoom().getUser();
    }
    // 从控机开启
    @PostMapping(path = "/on", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> on() {
        sac.turnOn();
        Map<String, String> response = new HashMap<>();
        response.put("message", "从控机已开启");
        return ResponseEntity.ok(response);
    }

    // 从控机关闭
    @PostMapping(path = "/off", consumes = "application/json", produces = "application/json")
    public String off() {
        sac.turnOff();
        return "从控机已关闭";
    }


    // 从控机获取房间温度
    @GetMapping(path = "/temperature", consumes = "application/json", produces = "application/json")
    public double getRoomTemperature() {
        return room.getTemperature();
    }

    @PostMapping("/auth")
    public AuthResponse auth(@RequestBody AuthRequest request) {
        // 验证房间号和身份证号
        if (request.getRoomNumber().isEmpty() || request.getIdNumber().isEmpty()) {
            throw new IllegalArgumentException("房间号和身份证号不能为空");
        }

        // 从中央空调获取工作模式和缺省工作温度
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                cacUrl + "/cac/auth",
                request,
                AuthResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("无法从中央空调获取工作模式和缺省工作温度");
        }

        return response.getBody();
    }



}
