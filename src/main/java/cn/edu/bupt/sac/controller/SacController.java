package cn.edu.bupt.sac.controller;

import cn.edu.bupt.sac.DTO.AuthRequest;
import cn.edu.bupt.sac.DTO.AuthResponse;
import cn.edu.bupt.sac.entity.*;
import cn.edu.bupt.sac.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sac")
public class SacController {
    @Value("${cac.url}")
    private String cacUrl;
    private final RoomService roomService;
    private final RestTemplate restTemplate;

    private static int nextId = 1;

    @Autowired
    public SacController(RoomService roomService) {
        this.roomService = roomService;
        this.restTemplate = new RestTemplate();
    }
    // 从控机开启
    @PostMapping(path = "/on", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> on() {
        Room room = roomService.getRoom();
        SAC sac = Room.getSac();
        sac.turnOn();
        Map<String, String> response = new HashMap<>();
        response.put("message", "从控机已开启");
        return ResponseEntity.ok(response);
    }

    // 从控机关闭
    @PostMapping(path = "/off", consumes = "application/json", produces = "application/json")
    public String off() {
        Room room = roomService.getRoom();
        SAC sac = Room.getSac();
        sac.turnOff();
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

        // 获取 Room 实例
        Room room = roomService.getRoom();
        Room.setId(request.getRoomID());

        // 创建一个 User 对象
        User user = new User();
        user.setRoomID(request.getRoomID());
        user.setIdNumber(request.getIdNumber());

        // 获取 SAC 对象并更新其属性
        SAC sac = Room.getSac();
        sac.setMode(response.getBody().getMode());
        sac.setDefaultTemperature(response.getBody().getDefaultTemperature());

        // 更新 Room 实例的状态
        Room.setUser(user);

        return response.getBody();
    }

    @PostMapping("/setTemperature")
    public ResponseEntity<Void> setTemperature(@RequestBody Map<String, Integer> payload) {
        // 从请求体中获取目标温度
        int targetTemperature = payload.get("targetTemperature");

        // 验证目标温度是否在有效范围内
        if (targetTemperature < 16 || targetTemperature > 30) {
            throw new IllegalArgumentException("目标温度必须在16°C和30°C之间");
        }

        // 获取 Room 实例
        Room room = roomService.getRoom();

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

        return ResponseEntity.ok().build();
    }

    @PostMapping("/adjustTemperature")
    public ResponseEntity<Void> adjustTemperature(@RequestBody Map<String, String> payload) {
        // 从请求体中获取温度调节方向
        String direction = payload.get("direction");

        // 获取 SAC 对象
        SAC sac = Room.getSac();

        // 获取当前温度和目标温度
        BigDecimal currentTemperature = Room.getTemperature();
        int targetTemperature = sac.getTargetTemperature();

        // 根据调节方向调整当前温度
        BigDecimal adjustment = new BigDecimal("0.5");
        if (direction.equals("up")) {
            currentTemperature = currentTemperature.add(adjustment);
        } else if (direction.equals("down")) {
            currentTemperature = currentTemperature.subtract(adjustment);
        } else {
            throw new IllegalArgumentException("调节方向必须是'up'或'down'");
        }

        // 验证调整后的温度是否在有效范围内
        BigDecimal minTemperature = new BigDecimal("16");
        BigDecimal maxTemperature = new BigDecimal("30");
        if (currentTemperature.compareTo(minTemperature) < 0 || currentTemperature.compareTo(maxTemperature) > 0) {
            throw new IllegalArgumentException("调整后的温度必须在16°C和30°C之间");
        }

        // 更新当前温度
        Room.setTemperature(currentTemperature);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/request")
    public Response request(@RequestBody Map<String, String> payload) {
        // 从请求体中获取请求类型
        String type = payload.get("type");

        Request request = new Request();
        request.setType(type);
        request.setCount(nextId++);

        // 获取 Room 实例
        Room room = roomService.getRoom();
        // System.out.println(room);
        User user = Room.getUser();
        // System.out.println(user);
        request.setRoomId(user.getRoomID());
        request.setStartTemp(Room.getTemperature());

        // 获取 SAC 对象
        SAC sac = Room.getSac();
        request.setFanSpeed(sac.getFanSpeed());
        request.setEndTemp(sac.getTargetTemperature());

        // 根据风速和温度差计算时间和能量消耗
        BigDecimal currentTemperature = Room.getTemperature();
        BigDecimal targetTemperature = BigDecimal.valueOf(sac.getTargetTemperature());
        BigDecimal temperatureDifference = currentTemperature.subtract(targetTemperature).abs();
        BigDecimal time = temperatureDifference.multiply(BigDecimal.TEN); // 假设每度温差需要10分钟
        BigDecimal energy;

        // 根据风速调整温度变化值和能量消耗
        switch (sac.getFanSpeed()) {
            case "low":
                time = time.multiply(BigDecimal.valueOf(1.25));
                energy = time.multiply(BigDecimal.valueOf(1.2));
                break;
            case "medium":
                energy = time;
                break;
            case "high":
                time = time.multiply(BigDecimal.valueOf(0.75));
                energy = time.multiply(BigDecimal.valueOf(0.8));
                break;
            default:
                throw new IllegalArgumentException("无效的风速");
        }

        // 设置能量和时长的精度和舍入模式
        energy = energy.setScale(2, RoundingMode.HALF_UP);
        time = time.setScale(2, RoundingMode.HALF_UP);

        request.setDuration(String.valueOf(time));
        request.setEnergy(energy);

        // 设置请求的开始和结束时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        request.setStartTime(now.format(formatter));
        request.setEndTime(now.plusMinutes(time.longValue()).format(formatter));

        // 根据能量消耗计算费用
        BigDecimal cost = energy.multiply(BigDecimal.valueOf(5)); // 每单位能量需要5元
        cost = cost.setScale(2, RoundingMode.HALF_UP);

        request.setCost(cost);

        // 发送请求到中央空调
        ResponseEntity<Response> response = restTemplate.postForEntity(
                cacUrl + "/cac/request",
                request,
                Response.class
        );

        return response.getBody();
    }
}
