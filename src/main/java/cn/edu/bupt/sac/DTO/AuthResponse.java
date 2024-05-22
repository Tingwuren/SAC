package cn.edu.bupt.sac.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String mode;
    private int defaultTemperature;

    public AuthResponse(String mode, int defaultTemperature) {
        this.mode = mode;
        this.defaultTemperature = defaultTemperature;
    }
}
