package org.babyfishdemo.war3shop.entities;

import java.math.BigDecimal;

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

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "PURCHASING_ITEM")
@SequenceGenerator(
        name = "purchasingItemSequence",
        sequenceName = "PURCHASING_ITEM_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class PurchasingItem {

    @Id
    @Column(name = "PURCHASING_ITEM_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchasingItemSequence")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PURCHASING_ID", nullable = false)
    private Purchasing purchasing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    @Column(name = "PURCHASED_UNIT_PRICE", nullable = false)
    private BigDecimal purchasedUnitPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Purchasing getPurchasing() {
        return purchasing;
    }

    public void setPurchasing(Purchasing purchasing) {
        this.purchasing = purchasing;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPurchasedUnitPrice() {
        return purchasedUnitPrice;
    }

    public void setPurchasedUnitPrice(BigDecimal purchasedUnitPrice) {
        this.purchasedUnitPrice = purchasedUnitPrice;
    }
}
