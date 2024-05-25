package cn.edu.bupt.sac.entity;

import lombok.Data;

@Data
public class Response {
    private String roomID; // 房间ID
    private String message; // 返回信息
    private String state; // 请求状态（waiting/processing/finished）
}
