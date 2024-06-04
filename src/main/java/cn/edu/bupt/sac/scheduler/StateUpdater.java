package cn.edu.bupt.sac.scheduler;

import cn.edu.bupt.sac.DTO.AuthResponse;
import cn.edu.bupt.sac.controller.SacController;
import cn.edu.bupt.sac.entity.Room;
import cn.edu.bupt.sac.entity.SAC;
import cn.edu.bupt.sac.service.RoomService;
import cn.edu.bupt.sac.service.SacService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class StateUpdater {
    private int frequency; // 服务状态更新频率
    private final RoomService roomService;
    private final SacService sacService;
    private ScheduledFuture<?> scheduledFuture;
    @Value("${cac.url}")
    private String cacUrl;
    private final RestTemplate restTemplate;



    private final TaskScheduler taskScheduler;

    public StateUpdater(RoomService roomService, SacService sacService,TaskScheduler taskScheduler, RestTemplate restTemplate) {
        this.roomService = roomService;
        this.sacService = sacService;
        this.taskScheduler = taskScheduler;
        this.restTemplate = restTemplate;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void scheduleTask() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        scheduledFuture = taskScheduler.schedule(this::updateState, new PeriodicTrigger(60 / frequency, TimeUnit.SECONDS));
    }

    public void updateState() {
        // 更新房间状态
        roomService.getRoom();
        roomService.updateRoomState();
    }
    @Scheduled(fixedRate = 5000) // 每5秒执行一次，从CAC获取刷新频率
    public void getFrequency() {
        if (!sacService.isOn()) {
            return;
        }
        // 从中央空调获取刷新频率
        Integer newFrequency = restTemplate.getForObject(
                cacUrl + "/cac/getFrequency",
                Integer.class
        );
        // System.out.println("新的刷新频率：" + newFrequency);
        if (newFrequency != null && newFrequency > 0 && newFrequency != frequency) {
            setFrequency(newFrequency);
            rescheduleTask(); // 如果频率发生变化，立即重新调度任务
        }
    }

    private void rescheduleTask() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true); // 取消当前的调度任务
        }
        scheduleTask(); // 立即开始新的调度任务
    }

}
