package cn.edu.bupt.sac.service;

import cn.edu.bupt.sac.entity.Room;
import cn.edu.bupt.sac.entity.SAC;
import cn.edu.bupt.sac.entity.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

@Service
public interface RoomService {
    Room getRoom();
    BigDecimal getTemperature();
    BigDecimal setAmbientTemperature(); // 获取室外温度

    SAC getSAC();

    BigDecimal getAmbientTemperature();

    void updateRoomState();
}
