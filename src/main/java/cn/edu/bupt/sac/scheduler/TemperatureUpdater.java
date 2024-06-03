package cn.edu.bupt.sac.scheduler;

import cn.edu.bupt.sac.controller.SacController;
import cn.edu.bupt.sac.entity.Room;
import cn.edu.bupt.sac.entity.SAC;
import cn.edu.bupt.sac.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
public class TemperatureUpdater {

    private final RoomService roomService;
    @Autowired
    private SacController sacController;

    public TemperatureUpdater(RoomService roomService) {
        this.roomService = roomService;
    }

    @Scheduled(fixedRate = 5000) // 每5秒执行一次
    public void updateTemperature() {
        BigDecimal newTemperature = getNewTemperature(); // 获取新的温度
        Room.setTemperature(newTemperature);
        // System.out.println("房间温度更新为：" + newTemperature);
    }

    private BigDecimal getNewTemperature() {
        BigDecimal temperature = roomService.getTemperature();
        SAC sac = roomService.getSAC();
        BigDecimal targetTemperature = BigDecimal.valueOf(sac.getTargetTemperature());

        if (sac.isWorking()) {

            BigDecimal timePerDegree = BigDecimal.ONE; // 假设每度温差需要1分钟

            // 根据风速调整温度变化值
            switch (sac.getServiceFanSpeed()) {
                case "low":
                    timePerDegree = timePerDegree.multiply(BigDecimal.valueOf(1.25));
                    break;
                case "medium":
                    break;
                case "high":
                    timePerDegree = timePerDegree.multiply(BigDecimal.valueOf(0.75));
                    break;
                default:
                    throw new IllegalArgumentException("无效的风速");
            }

            // 计算1分钟内的温度变化
            BigDecimal temperatureChangePerMinute = BigDecimal.ONE.divide(timePerDegree, 2, RoundingMode.HALF_UP);
            // 计算5秒内的温度变化
            BigDecimal temperatureChangePer5Seconds = temperatureChangePerMinute.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

            System.out.println("正在送风，每5秒的温度变化：" + temperatureChangePer5Seconds +
                    "，当前温度：" + temperature + "，目标温度：" + targetTemperature);

            // 根据当前温度和目标温度调整温度
            if (temperature.compareTo(targetTemperature) < 0) {
                temperature = temperature.add(temperatureChangePer5Seconds);
                // 如果温度超过目标温度，从控机发送停风请求给中央空调
                if (temperature.compareTo(targetTemperature) >= 0) {
                    temperature = targetTemperature;
                    System.out.println("温度已达到目标温度，发送停风请求给中央空调");
                    // 发送停风请求给中央空调
                    // 创建停风请求
                    Map<String, String> payload = new HashMap<>();
                    payload.put("type", "stop");

                    // 使用SacController实例发送停风请求给中央空调
                    sacController.request(payload);
                }
            } else if (temperature.compareTo(targetTemperature) > 0) {
                temperature = temperature.subtract(temperatureChangePer5Seconds);
            }
        }
        else if (temperature != null) {
            // 空调未工作时，房间温度随着环境温度变化（需求7.a、7.b）
            BigDecimal ambientTemperature = roomService.getAmbientTemperature(); // 获取室外环境温度
            BigDecimal degreePerSecond = BigDecimal.valueOf(0.5); // 假设每分钟温度变化0.5度
            BigDecimal degreePer5Seconds = degreePerSecond.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP); // 每5秒温度变化
            // 根据当前温度和环境温度调整温度
            // 根据当前温度和环境温度调整温度
            if (temperature.compareTo(ambientTemperature) != 0) {
                if (temperature.compareTo(ambientTemperature) < 0) {
                    temperature = temperature.add(degreePer5Seconds);
                    if (temperature.compareTo(ambientTemperature) > 0) {
                        temperature = ambientTemperature;
                    }
                }
                else {
                    temperature = temperature.subtract(degreePer5Seconds);
                    if (temperature.compareTo(ambientTemperature) < 0) {
                        temperature = ambientTemperature;
                    }
                }
            }
            System.out.println("无送风，每5秒的温度变化：" + degreePer5Seconds +
                    "，当前温度：" + temperature + "，环境温度：" + ambientTemperature);

            // 当房间温度与目标温度相差大于等于1度时，发送送风请求给中央空调（需求7.a）
            if (temperature.subtract(targetTemperature).abs().compareTo(BigDecimal.ONE) >= 0 && sac.isService()) {
                // 创建送风请求
                System.out.println("温度与目标温度相差大于等于1度，发送送风请求给中央空调");
                Map<String, String> payload = new HashMap<>();
                payload.put("type", "start");

                // 使用SacController实例发送送风请求给中央空调
                sacController.request(payload);
            }
        }

        return temperature;
    }
}
