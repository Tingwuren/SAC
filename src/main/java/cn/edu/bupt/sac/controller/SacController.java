package cn.edu.bupt.sac.controller;

import cn.edu.bupt.sac.DTO.AuthRequest;
import cn.edu.bupt.sac.DTO.AuthResponse;
import cn.edu.bupt.sac.DTO.StateRequest;
import cn.edu.bupt.sac.DTO.StateResponse;
import cn.edu.bupt.sac.entity.*;
import cn.edu.bupt.sac.scheduler.StateUpdater;
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

    private final StateUpdater stateUpdater;

    @Autowired
    public SacController(RoomService roomService, SacService sacService, StateUpdater stateUpdater) {
        this.roomService = roomService;
        this.sacService = sacService;
        this.restTemplate = new RestTemplate();
        this.stateUpdater = stateUpdater;
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
        // 创建一个 Map 并将 isService 设置为 false
        Map<String, Boolean> payload = new HashMap<>();
        payload.put("isService", false);

        // 调用 stop 方法来停止温控服务
        stop(payload);
        sacService.turnOff();
        sacService.authCancel();
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

        // 从中央空调获取工作模式、缺省工作温度、默认风速、默认频率
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                cacUrl + "/cac/auth",
                request,
                AuthResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("无法从中央空调获取工作模式和缺省工作温度");
        }

        sacService.authSuccess();
        System.out.println("用户认证成功！");

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
        sac.setDefaultFanSpeed(response.getBody().getDefaultFanSpeed());
        sac.setDefaultFrequency(response.getBody().getDefaultFrequency());

        stateUpdater.setFrequency(response.getBody().getDefaultFrequency());
        stateUpdater.scheduleTask();

        System.out.println("从中央空调获取的工作模式：" + response.getBody().getMode());
        System.out.println("从中央空调获取的缺省工作温度：" + response.getBody().getDefaultTemperature());
        System.out.println("从中央空调获取的默认风速：" + response.getBody().getDefaultFanSpeed());
        System.out.println("从中央空调获取的默认频率：" + response.getBody().getDefaultFrequency());

        // 更新 Room 实例的状态
        Room.setUser(user);

        return response.getBody();
    }

    @PostMapping("/setTemperature")
    public ResponseEntity<String> setTemperature(@RequestBody Map<String, Integer> payload) {
        // 从请求体中获取目标温度
        int targetTemperature = payload.get("targetTemperature");

        // 获取 SAC 对象
        SAC sac = Room.getSac();

        // 获取 SAC 的工作模式
        String mode = sac.getMode();

        // 根据工作模式设置温度范围
        int[] temperatureRange;
        if ("cooling".equals(mode)) {
            temperatureRange = new int[]{18, 25};
        } else if ("heating".equals(mode)) {
            temperatureRange = new int[]{25, 30};
        } else {
            return ResponseEntity.badRequest().body("无效的工作模式");
        }

        // 验证目标温度是否在有效范围内
        if (targetTemperature < temperatureRange[0] || targetTemperature > temperatureRange[1]) {
            return ResponseEntity.badRequest().body("目标温度必须在" + temperatureRange[0] + "°C和" + temperatureRange[1] + "°C之间");
        }

        // 更新 SAC 的目标温度
        sac.setTargetTemperature(targetTemperature);

        return ResponseEntity.ok("温度已成功设置为 " + targetTemperature + "°C");
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
        sac.setSetFanSpeed(fanSpeed);
        System.out.println("设置请求风速为"+fanSpeed);

        // 如果空调处于服务状态，立即发送开启温控请求
        if (sac.isService()) {
            Map<String, String> requestPayload = new HashMap<>();
            requestPayload.put("type", "start");
            request(requestPayload);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/request")
    public Response request(@RequestBody Map<String, String> payload) {
        // 从请求体中获取请求类型，开启或停止温控
        String type = payload.get("type");

        Request request = sacService.getRequest(type);

        System.out.println("发送请求：" + request);
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

    @PostMapping("/start") // 开启温控服务
    public void start(@RequestBody Map<String, Boolean> payload) {
        boolean isService = payload.get("isService");
        SAC sac = Room.getSac();
        sac.setIsService(isService);
    }

    @PostMapping("/stop") // 关闭温控服务
    public void stop(@RequestBody Map<String, Boolean> payload) {
        boolean isService = payload.get("isService");
        SAC sac = Room.getSac();
        sac.setIsService(isService);
        if (sac.isWorking()) {
            Map<String, String> request = new HashMap<>();
            request.put("type", "stop");
            request(request);
        }
    }

    @GetMapping("/status") // 获取用量和金额
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("energy", Room.getEnergy());
        response.put("cost", Room.getCost());
        response.put("isService", Room.getSac().isService());
        response.put("isWorking", Room.getSac().isWorking());
        return response;
    }
}
