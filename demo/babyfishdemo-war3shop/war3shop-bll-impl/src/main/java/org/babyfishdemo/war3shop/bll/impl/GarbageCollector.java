package org.babyfishdemo.war3shop.bll.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.babyfishdemo.war3shop.dal.OrderRepository;
import org.babyfishdemo.war3shop.dal.TemporaryUploadedFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/*
 * No interface, requires 
 * <tx:annotation-driven proxy-target-class="true"/>
 * and
 * <task:annotation-drivern proxy-target-class="true"/>
 */
/**
 * @author Tao Chen
 */
@Component
public class GarbageCollector {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GarbageCollector.class);

    @Resource
    private TemporaryUploadedFileRepository temporaryUploadedFileRepository;
    
    @Resource
    private OrderRepository orderRepository;
    
    @Transactional
    @Scheduled(fixedRate = 20 * 60 * 1000, initialDelay = 0)
    public void collectTemporaryUploadedFiles() {
        preCollect("temporary uploaded files");
        this.temporaryUploadedFileRepository.removeTemporaryUploadedFilesByGcThreshold(new Date());
    }
    
    @Transactional
    @Scheduled(fixedRate = 7 * 24 * 60 * 60 * 1000, initialDelay = 0)
    public void collectTemporaryOrders() {
        preCollect("temporary orders");
        this.orderRepository.removeTemporaryOrderByGcThreshold(new Date());
    }
    
    private static void preCollect(String entityDescription) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Scheduling action"
                    + "\n-----------------------------------------------------"
                    + "\nDo garbage collection for " + entityDescription
                    +"\n-----------------------------------------------------"
            );
        }
    }
}
