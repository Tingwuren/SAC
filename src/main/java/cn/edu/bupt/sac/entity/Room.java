package cn.edu.bupt.sac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
// 房间类
public class Room {
    @Getter
    private static User user; // 房间用户
    @Getter
    private static BigDecimal temperature; // 房间温度
    @Getter
    private static BigDecimal ambientTemperature; // 室外温度
    @Getter
    private static BigDecimal energy; // 消耗能量，与请求时长和风速有关
    @Getter
    private static BigDecimal cost; // 支付费用，与消耗能量有关
    @Getter
    private static SAC sac; // 房间内的从控机


    public static void setUser(User user) {
        Room.user = user;
    }

    public static void setTemperature(BigDecimal temperature) {
        Room.temperature = temperature;
    }

    public static void setAmbientTemperature(BigDecimal ambientTemperature) {
        Room.ambientTemperature = ambientTemperature;
    }

    public static void setSac(SAC sac) {
        Room.sac = sac;
    }

    public static void setEnergy(BigDecimal energy) {
        Room.energy = energy;
    }

    public static void setCost(BigDecimal cost) {
        Room.cost = cost;
    }

}
