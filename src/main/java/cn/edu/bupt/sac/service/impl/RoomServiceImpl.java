package cn.edu.bupt.sac.service.impl;

import cn.edu.bupt.sac.entity.Room;
import cn.edu.bupt.sac.entity.SAC;
import cn.edu.bupt.sac.entity.User;
import cn.edu.bupt.sac.service.RoomService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

@Service
public class RoomServiceImpl implements RoomService {
    private final Room room = new Room();

    @PostConstruct
    public void init() {
        User user = new User();
        SAC sac = new SAC();
        room.setUser(user);
        room.setSac(sac);
    }

    @Override
    public Room getRoom() {
        return room;
    }

    @Override
    public BigDecimal getTemperature() {
        return room.getTemperature();  // 获取 Room 对象的温度
    }
}
