package cn.edu.bupt.sac.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Data
// 从控机类（Slave Air Conditioner）
public class SAC {
    private boolean isOn; // 空调是否开启
    private boolean isWorking; // 空调是否工作
    private String mode; // 工作模式（制冷/供暖）
    private int defaultTemperature; // 默认温度
    private int targetTemperature; // 目标温度
    private String fanSpeed; // 风速（高/中/低）
}
