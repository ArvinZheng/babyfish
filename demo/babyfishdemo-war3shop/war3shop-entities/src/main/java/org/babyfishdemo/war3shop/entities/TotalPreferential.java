package org.babyfishdemo.war3shop.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Embeddable
public class TotalPreferential {

    @Column(name = "TOTAL_REDUCED_MONEY", nullable = false)
    private BigDecimal reducedMoney;
    
    @Column(name = "TOTAL_GIFT_MONEY", nullable = false)
    private BigDecimal giftMoney;
    
    public TotalPreferential() {
        this.reducedMoney = BigDecimal.ZERO;
        this.giftMoney = BigDecimal.ZERO;
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
}
