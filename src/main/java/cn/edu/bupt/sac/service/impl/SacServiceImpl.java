package cn.edu.bupt.sac.service.impl;

import cn.edu.bupt.sac.entity.*;
import cn.edu.bupt.sac.service.SacService;
import org.springframework.stereotype.Service;

@Service
public class SacServiceImpl implements SacService {
    SAC sac = Room.getSac();
    @Override
    public void turnOn() {
        sac.setOn(true);
    }

    @Override
    public void turnOff() {
        sac.setOn(false);
        System.out.println("从控机关闭");
    }

    @Override
    public void handleResponse(Response response) {
        if ("processing".equals(response.getState())) {
            sac.setWorking(true);
            sac.setServiceFanSpeed(sac.getSetFanSpeed()); // 设置服务风速
        } else if ("waiting".equals(response.getState()) || "finished".equals(response.getState())) {
            sac.setWorking(false);
        }
    }

    @Override
    public Request getRequest(String type) {
        Request request = new Request();
        User user = Room.getUser();
        SAC sac = Room.getSac();

        // 设置请求类型，开启或停止温控
        request.setType(type);

        // 设置请求房间ID
        request.setRoomId(user.getRoomID());

        // 设置请求风速
        String speed;
        if (sac.getSetFanSpeed() != null) {
            speed = sac.getSetFanSpeed();
        } else {
            speed = sac.getDefaultFanSpeed();
            sac.setSetFanSpeed(speed);
        }
        request.setFanSpeed(speed);
        System.out.println("设置请求风速为" + sac.getSetFanSpeed());

        // 设置目标温度，由于需求4.b，该字段保留
        int endTemp;
        if (sac.getTargetTemperature() != 0) {
            endTemp = sac.getTargetTemperature();
        } else {
            endTemp = sac.getDefaultTemperature();
            sac.setTargetTemperature(endTemp);
        }
        request.setTargetTemp(endTemp);

        return request;
    }

    @Override
    public boolean isOn() {
        return sac.isOn();
    }

    @Override
    public int getFrequency() {
        return sac.getFrequency();
    }
}
