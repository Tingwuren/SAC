package cn.edu.bupt.sac.controller;

import cn.edu.bupt.sac.DTO.AuthRequest;
import cn.edu.bupt.sac.DTO.AuthResponse;
import cn.edu.bupt.sac.entity.*;
import cn.edu.bupt.sac.service.RoomService;
import cn.edu.bupt.sac.service.SacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sac")
public class SacController {
    @Value("${cac.url}")
    private String cacUrl;
    private final RoomService roomService;

    private final SacService sacService;
    private final RestTemplate restTemplate;

    private static int nextId = 1;

    @Autowired
    public SacController(RoomService roomService, SacService sacService) {
        this.roomService = roomService;
        this.sacService = sacService;
        this.restTemplate = new RestTemplate();
    }
    // 从控机开启
    @PostMapping(path = "/on", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> on() {
        sacService.turnOn();
        System.out.println("从控机已开启");
        Map<String, String> response = new HashMap<>();
        response.put("message", "从控机已开启");
        return ResponseEntity.ok(response);
    }

    // 从控机关闭
    @PostMapping(path = "/off", consumes = "application/json", produces = "application/json")
    public String off() {
        sacService.turnOff();
        System.out.println("从控机已关闭");
        return "从控机已关闭";
    }


    // 从控机获取房间温度
    @GetMapping(path = "/temperature", consumes = "application/json", produces = "application/json")
    public BigDecimal getRoomTemperature() {
        return roomService.getTemperature();
    }

    @PostMapping("/auth")
    public AuthResponse auth(@RequestBody AuthRequest request) {
        // 验证房间号和身份证号
        if (request.getRoomID().isEmpty() || request.getIdNumber().isEmpty()) {
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

        Room.setId(request.getRoomID());
        BigDecimal ambientTemperature = roomService.setAmbientTemperature();
        System.out.println("当前室外温度：" + ambientTemperature);
        Room.setTemperature(ambientTemperature); // 设置房间温度为室外温度
        // 创建一个 User 对象
        User user = new User();
        user.setRoomID(request.getRoomID());
        user.setIdNumber(request.getIdNumber());

        // 获取 SAC 对象并更新其属性
        SAC sac = Room.getSac();
        sac.setMode(response.getBody().getMode());
        sac.setDefaultTemperature(response.getBody().getDefaultTemperature());
        System.out.println("从中央空调获取的工作模式：" + response.getBody().getMode());
        System.out.println("从中央空调获取的缺省工作温度：" + response.getBody().getDefaultTemperature());

        // 更新 Room 实例的状态
        Room.setUser(user);

        return response.getBody();
    }

    @PostMapping("/setTemperature")
    public ResponseEntity<Void> setTemperature(@RequestBody Map<String, Integer> payload) {
        // 从请求体中获取目标温度
        int targetTemperature = payload.get("targetTemperature");

        // 验证目标温度是否在有效范围内
        if (targetTemperature < 18 || targetTemperature > 30) {
            throw new IllegalArgumentException("目标温度必须在18°C和30°C之间");
        }


        // 获取 SAC 对象并更新其目标温度
        SAC sac = Room.getSac();
        sac.setTargetTemperature(targetTemperature);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/setFanSpeed")
    public ResponseEntity<Void> setFanSpeed(@RequestBody Map<String, String> payload) {
        // 从请求体中获取风速
        String fanSpeed = payload.get("fanSpeed");

        // 验证风速是否有效
        if (!fanSpeed.equals("low") && !fanSpeed.equals("medium") && !fanSpeed.equals("high")) {
            throw new IllegalArgumentException("风速必须是'low'、'medium'或'high'");
        }

        // 获取 SAC 对象并更新其风速
        SAC sac = Room.getSac();
        sac.setFanSpeed(fanSpeed);
        System.out.println(fanSpeed);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request")
    public Response request(@RequestBody Map<String, String> payload) {
        // 从请求体中获取请求类型，开启或停止温控
        String type = payload.get("type");

        Request request = sacService.getRequest(type);

        // 发送请求到中央空调
        ResponseEntity<Response> responseEntity = restTemplate.postForEntity(
                cacUrl + "/cac/request",
                request,
                Response.class
        );

        Response response = responseEntity.getBody();
        System.out.println("请求处理结果：" + response);
        sacService.handleResponse(response);
        return responseEntity.getBody();
    }
}
