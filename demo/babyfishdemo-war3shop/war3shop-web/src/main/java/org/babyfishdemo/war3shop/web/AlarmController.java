package org.babyfishdemo.war3shop.web;

import javax.annotation.Resource;

import org.babyfishdemo.war3shop.bll.AlarmService;
import org.babyfishdemo.war3shop.entities.Alarm;
import org.babyfishdemo.war3shop.entities.Alarm__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.specification.AlarmSpecification;
import org.babyfishdemo.war3shop.web.json.JsonpModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Tao Chen
 */
@Controller
@RequestMapping("/alarm")
public class AlarmController {

    @Resource
    private AlarmService alarmService;
    
    @RequestMapping("/my-alarms")
    public JsonpModelAndView myAlarms(
            AlarmSpecification specification,
            @RequestParam("pageIndex") int pageIndex,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "queryPath", required = false) String queryPath) {
        Alarm__[] queryPaths = Alarm__.compile(queryPath);
        Page<Alarm> page = this.alarmService.getMyAlarms(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths); 
        return new JsonpModelAndView(page);
    }
    
    @RequestMapping("/acknowledge-alarm")
    public JsonpModelAndView acknowledgeAlarm(
            @RequestParam("alarmId") int alarmId) {
        this.alarmService.acknowledgeAlarm(alarmId);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/unacknowledge-alarm")
    public JsonpModelAndView unacknowledgeAlarm(
            @RequestParam("alarmId") int alarmId) {
        this.alarmService.unacknowledgeAlarm(alarmId);
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/delete-alarm")
    public JsonpModelAndView deleteAlarm(
            @RequestParam("alarmId") int alarmId) {
        this.alarmService.deleteAlarm(alarmId);
        return new JsonpModelAndView(null);
    }
}
