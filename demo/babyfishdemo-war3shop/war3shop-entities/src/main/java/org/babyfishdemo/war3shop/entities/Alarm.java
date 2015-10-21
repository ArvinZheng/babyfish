package org.babyfishdemo.war3shop.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "ALARM")
@SequenceGenerator(
        name = "alarmSequence",
        sequenceName = "ALARM_ID_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Alarm {

    @Id
    @Column(name = "ALARM_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alarmSequence")
    private Long id;
    
    @Version
    @Column(name = "VERSION")
    private int version;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATION_TIME", nullable = false)
    private Date creationTime;
    
    @Column(name = "MESSAGE", length = 400, nullable = false)
    private String message;
    
    @Column(name = "ACKNOWLEDGED", nullable = false)
    private boolean acknowledged;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
