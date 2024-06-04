package cn.edu.bupt.sac.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StateResponse {
    private BigDecimal energy; // 消耗能量，与请求时长和风速有关
    private BigDecimal cost; // 支付费用，与消耗能量有关
    private int frequency;  //刷新频率（每分钟刷新次数） 例如 frequency = 12 代表每分钟刷新12次 用于监测各房间的状态
}
