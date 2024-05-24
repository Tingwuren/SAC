package cn.edu.bupt.sac.service.impl;

import cn.edu.bupt.sac.entity.Response;
import cn.edu.bupt.sac.entity.Room;
import cn.edu.bupt.sac.entity.SAC;
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
        } else if ("waiting".equals(response.getState()) || "finished".equals(response.getState())) {
            sac.setWorking(false);
        }
    }
}
