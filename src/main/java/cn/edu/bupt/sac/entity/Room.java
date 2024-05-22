package cn.edu.bupt.sac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
// 房间类
public class Room {
    @TableId(type= IdType.AUTO)
    private Long id; // 房间ID，自增主键
    private User user; // 房间用户
    private double temperature; // 房间温度
    private double ambientTemperature; // 室外温度
    private SAC sac; // 房间内的从控机
}
