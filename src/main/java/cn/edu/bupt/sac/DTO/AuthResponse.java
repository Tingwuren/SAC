package cn.edu.bupt.sac.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String mode; // 工作模式
    private int defaultTemperature; // 默认温度
    private String defaultFanSpeed; // 默认风速
    private int defaultFrequency; // 默认频率

    public AuthResponse(String mode, int defaultTemperature) {
        this.mode = mode;
        this.defaultTemperature = defaultTemperature;
    }
}
