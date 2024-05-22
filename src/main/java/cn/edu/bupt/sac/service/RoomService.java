package cn.edu.bupt.sac.service;

import cn.edu.bupt.sac.entity.Room;
import cn.edu.bupt.sac.entity.SAC;
import cn.edu.bupt.sac.entity.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class RoomService {
    private Room room;

    @PostConstruct
    public void init() {
        User user = new User();
        SAC sac = new SAC();
        room = new Room();
        room.setUser(user);
        room.setSac(sac);
    }

    public Room getRoom() {
        return room;
    }
}
