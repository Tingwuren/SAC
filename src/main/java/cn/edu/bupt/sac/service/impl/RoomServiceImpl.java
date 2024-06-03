package cn.edu.bupt.sac.service.impl;

import cn.edu.bupt.sac.entity.Room;
import cn.edu.bupt.sac.entity.SAC;
import cn.edu.bupt.sac.entity.User;
import cn.edu.bupt.sac.service.RoomService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

@Service
public class RoomServiceImpl implements RoomService {
    private final Room room = new Room();

    @PostConstruct
    public void init() {
        User user = new User();
        SAC sac = new SAC();
        Room.setUser(user);
        Room.setSac(sac);
    }

    @Override
    public Room getRoom() {
        return room;
    }

    @Override
    public BigDecimal getTemperature() {
        return Room.getTemperature();  // 获取 Room 对象的温度
    }

    public BigDecimal setAmbientTemperature() {
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;  // 获取当前月份，注意月份是从0开始的，所以需要加1
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);  // 获取当前小时

        int minTemperature, maxTemperature;
        switch (month) {
            case 1: minTemperature = -8; maxTemperature = 2; break;
            case 2: minTemperature = -6; maxTemperature = 5; break;
            case 3: minTemperature = 1; maxTemperature = 12; break;
            case 4: minTemperature = 8; maxTemperature = 21; break;
            case 5: minTemperature = 14; maxTemperature = 27; break;
            case 6: minTemperature = 19; maxTemperature = 30; break;
            case 7: minTemperature = 22; maxTemperature = 31; break;
            case 8: minTemperature = 21; maxTemperature = 30; break;
            case 9: minTemperature = 15; maxTemperature = 26; break;
            case 10: minTemperature = 8; maxTemperature = 19; break;
            case 11: minTemperature = 0; maxTemperature = 10; break;
            default: minTemperature = -6; maxTemperature = 4; break;  // 12月
        }

        double temperature;
        if (hour == 14) {
            temperature = maxTemperature;
        } else {
            double diff = Math.abs(hour - 14) / 14.0;  // 计算当前时间与14点的差距，差距越大，温度越接近最低温度
            temperature = minTemperature + (maxTemperature - minTemperature) * (1 - diff);  // 根据时间差调整温度
        }

        BigDecimal temperatureBD = BigDecimal.valueOf(temperature).setScale(2, RoundingMode.HALF_UP);  // 设置精度为小数点后两位，四舍五入

        Room.setAmbientTemperature(temperatureBD);  // 设置 Room 对象的室外温度
        return temperatureBD;
    }

    @Override
    public SAC getSAC() {
        return Room.getSac();  // 获取 Room 对象的 SAC
    }

    @Override
    public BigDecimal getAmbientTemperature() {
        return Room.getAmbientTemperature();  // 获取 Room 对象的室外温度
    }
}
