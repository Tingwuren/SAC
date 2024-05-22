package cn.edu.bupt.sac.scheduler;

import cn.edu.bupt.sac.entity.Room;
import cn.edu.bupt.sac.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TemperatureUpdater {

    private final RoomService roomService;

    @Autowired
    public TemperatureUpdater(RoomService roomService) {
        this.roomService = roomService;
    }

    @Scheduled(fixedRate = 5000) // 每5秒执行一次
    public void updateTemperature() {
        Room room = roomService.getRoom();
        double newTemperature = getNewTemperature(); // 获取新的温度
        room.setTemperature(newTemperature);
    }

    private double getNewTemperature() {
        // 这里是模拟获取新的温度的过程，你可以根据你的需求来修改这个方法
        return Math.random() * 10 + 20; // 返回一个在20到30之间的随机数
    }
}
