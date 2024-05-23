package cn.edu.bupt.sac.scheduler;

import cn.edu.bupt.sac.entity.Room;
import cn.edu.bupt.sac.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TemperatureUpdater {

    private final RoomService roomService;

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
        // 生成一个在20到30之间的随机数
        double randomTemperature = Math.random() * 10 + 20;
        // 将随机数转换为 BigDecimal 对象并设置精度为两位小数
        return BigDecimal.valueOf(randomTemperature).setScale(2, RoundingMode.HALF_UP);
    }
}
