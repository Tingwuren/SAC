package cn.edu.bupt.sac.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
// 从控机类（Slave Air Conditioner）
public class SAC {
    private boolean isOn; // 空调是否开启
    private boolean isAuth; // 空调是否认证
    private boolean isWorking; // 空调是否工作
    private boolean isService; // 空调是否服务
    private String mode; // 工作模式（制冷/供暖）
    private int defaultTemperature; // 默认温度
    private String defaultFanSpeed; // 默认风速
    private int defaultFrequency; // 默认状态更新频率
    private int targetTemperature; // 目标温度
    private String setFanSpeed; // 设置风速（高/中/低）
    private String serviceFanSpeed; // 服务风速（高/中/低）
    private int frequency; // 服务状态更新频率

    public void setIsService(boolean isService) {
        this.isService = isService;
    }
}
