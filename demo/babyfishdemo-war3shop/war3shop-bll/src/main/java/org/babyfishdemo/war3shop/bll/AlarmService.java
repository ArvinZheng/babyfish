package org.babyfishdemo.war3shop.bll;

import org.babyfishdemo.war3shop.entities.Alarm;
import org.babyfishdemo.war3shop.entities.Alarm__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.User;
import org.babyfishdemo.war3shop.entities.specification.AlarmSpecification;

/**
 * @author Tao Chen
 */
public interface AlarmService {

    Page<Alarm> getMyAlarms(
            AlarmSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Alarm__ ... queryPaths);
    
    void sendAlarm(User targetUser, String message);
    
    void sendAlarmForcibly(User targetUser, String message);

    void acknowledgeAlarm(int alarmId);

    void unacknowledgeAlarm(int alarmId);

    void deleteAlarm(int alarmId);
}
