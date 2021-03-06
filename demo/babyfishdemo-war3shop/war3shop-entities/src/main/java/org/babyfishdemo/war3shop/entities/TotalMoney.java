package org.babyfishdemo.war3shop.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/*
 * Unfortunately, if delete the fields/methods of "reducedMoney" and "expectedMoney"
 * and let this class extend TotalPreferential,
 * but Hiberante does not support it when the class is mared by @Embeddable
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Embeddable
public class TotalMoney {
    
    @Column(name = "TOTAL_REDUCED_MONEY", nullable = false)
    private BigDecimal reducedMoney;
    
    @Column(name = "TOTAL_GIFT_MONEY", nullable = false)
    private BigDecimal giftMoney;
    
    @Column(name = "TOTAL_EXPECTED_MONEY", nullable = false)
    private BigDecimal expectedMoney;
    
    @Column(name = "TOTAL_ACTUAL_MONEY", nullable = false)
    private BigDecimal actualMoney;
    
    public TotalMoney() {
        this.reducedMoney = BigDecimal.ZERO;
        this.giftMoney = BigDecimal.ZERO;
        this.expectedMoney = BigDecimal.ZERO;
        this.actualMoney = BigDecimal.ZERO;
    }

    public BigDecimal getReducedMoney() {
        return reducedMoney;
    }

    public void setReducedMoney(BigDecimal reducedMoney) {
        this.reducedMoney = reducedMoney;
    }

    public BigDecimal getGiftMoney() {
        return giftMoney;
    }

    public void setGiftMoney(BigDecimal giftMoney) {
        this.giftMoney = giftMoney;
    }

    public BigDecimal getExpectedMoney() {
        return expectedMoney;
    }

    public void setExpectedMoney(BigDecimal expectedMoney) {
        this.expectedMoney = expectedMoney;
    }

    public BigDecimal getActualMoney() {
        return actualMoney;
    }

    public void setActualMoney(BigDecimal actualMoney) {
        this.actualMoney = actualMoney;
    }
}
