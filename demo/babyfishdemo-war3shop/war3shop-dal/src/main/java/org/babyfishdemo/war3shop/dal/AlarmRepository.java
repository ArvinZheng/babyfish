package org.babyfishdemo.war3shop.dal;

import org.babyfishdemo.war3shop.entities.Alarm;
import org.babyfishdemo.war3shop.entities.Alarm__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.specification.AlarmSpecification;

/**
 * @author Tao Chen
 */
public interface AlarmRepository extends AbstractRepository<Alarm, Long> {

    Alarm getAlarmById(long id, Alarm__... queryPaths);
    
    Page<Alarm> getAlarms(
            AlarmSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Alarm__ ... queryPaths);
}
