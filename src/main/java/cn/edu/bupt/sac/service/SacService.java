package cn.edu.bupt.sac.service;

import cn.edu.bupt.sac.entity.Response;
import org.springframework.stereotype.Service;

@Service
public interface SacService {
    void turnOn();
    void turnOff();

    void handleResponse(Response response);
}
