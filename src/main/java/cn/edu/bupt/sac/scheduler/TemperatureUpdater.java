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

        if (sac.isWorking()) {
            BigDecimal targetTemperature = BigDecimal.valueOf(sac.getTargetTemperature());
            BigDecimal temperatureDifference = targetTemperature.subtract(temperature).abs();

            BigDecimal time = BigDecimal.ONE; // 假设每度温差需要1分钟

            // 根据风速调整温度变化值
            switch (sac.getFanSpeed()) {
                case "low":
                    time = time.multiply(BigDecimal.valueOf(1.25));
                    break;
                case "medium":
                    break;
                case "high":
                    time = time.multiply(BigDecimal.valueOf(0.75));
                    break;
                default:
                    throw new IllegalArgumentException("无效的风速");
            }

            // 将每分钟的温度变化分解为每5秒的温度变化
            time = time.divide(BigDecimal.valueOf(12), RoundingMode.HALF_UP);

            System.out.println("当前温度：" + temperature + "，目标温度：" + targetTemperature + "，每5秒变化：" + time);

            // 根据当前温度和目标温度调整温度
            if (temperature.compareTo(targetTemperature) < 0) {
                temperature = temperature.add(time);
                // 如果温度超过目标温度，从控机发送停风请求给中央空调
                if (temperature.compareTo(targetTemperature) >= 0) {
                    temperature = targetTemperature;
                    System.out.println("温度已达到目标温度，发送停风请求给中央空调");
                    // Todo: 发送停风请求给中央空调
                    // 创建停风请求
                    Map<String, String> payload = new HashMap<>();
                    payload.put("type", "stop");

                    // 使用SacController实例发送停风请求给中央空调
                    sacController.request(payload);
                }
            } else if (temperature.compareTo(targetTemperature) > 0) {
                temperature = temperature.subtract(time);
            }
        }

        return temperature;
    }
}
