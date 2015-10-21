package org.babyfishdemo.war3shop.entities.specification;

import java.util.Date;

/**
 * @author Tao Chen
 */
public class AlarmSpecification {

    private Long userId;
    
    private Date minCreationTime;
    
    private Date maxCreationTime;
    
    private Boolean acknowledged;
    
    private String keyword;
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getMinCreationTime() {
        return minCreationTime;
    }

    public void setMinCreationTime(Date minCreationTime) {
        this.minCreationTime = minCreationTime;
    }

    public Date getMaxCreationTime() {
        return maxCreationTime;
    }

    public void setMaxCreationTime(Date maxCreationTime) {
        this.maxCreationTime = maxCreationTime;
    }

    public Boolean getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Boolean acknownledged) {
        this.acknowledged = acknownledged;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
