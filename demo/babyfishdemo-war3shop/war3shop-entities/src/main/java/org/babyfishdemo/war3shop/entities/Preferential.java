package org.babyfishdemo.war3shop.entities;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name = "PREFERENTIAL")
@SequenceGenerator(
        name = "preferentialSequence",
        sequenceName = "PREFERENTIAL_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Preferential {

    @Id
    @Column(name = "PREFERENTIAL_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "preferentialSequence")
    private Long id;

    @Version
    @Column(name = "VERSION")
    private int version;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active;

    @Temporal(TemporalType.DATE)
    @Column(name = "START_DATE", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "END_DATE", nullable = false)
    private Date endDate;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "THRESHOLD_TYPE", nullable = false)
    private PreferentialThresholdType thresholdType;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ACTION_TYPE", nullable = false)
    private PreferentialActionType actionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @OneToMany(mappedBy = "preferential", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<PreferentialItem> items;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public PreferentialThresholdType getThresholdType() {
        return thresholdType;
    }

    public void setThresholdType(PreferentialThresholdType thresholdType) {
        this.thresholdType = thresholdType;
    }

    public PreferentialActionType getActionType() {
        return actionType;
    }

    public void setActionType(PreferentialActionType actionType) {
        this.actionType = actionType;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Set<PreferentialItem> getItems() {
        return items;
    }

    public void setItems(Set<PreferentialItem> items) {
        this.items = items;
    }
}
