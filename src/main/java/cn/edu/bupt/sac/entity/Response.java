package cn.edu.bupt.sac.entity;

import lombok.Data;

@Data
public class Response {
    private String id; // 请求ID
    private String state; // 请求状态（waiting/processing/finished）
}
