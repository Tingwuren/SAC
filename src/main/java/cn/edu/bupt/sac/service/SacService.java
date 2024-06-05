package cn.edu.bupt.sac.service;

import cn.edu.bupt.sac.entity.Request;
import cn.edu.bupt.sac.entity.Response;
import org.springframework.stereotype.Service;

@Service
public interface SacService {
    int getFrequency();

    void turnOn();
    void turnOff();

    void handleResponse(Response response);

    Request getRequest(String type);

    boolean isOn();

    boolean isAuth();

    void authSuccess();

    void authCancel();
}
