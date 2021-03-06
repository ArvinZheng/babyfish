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
@Table(name = "GIFT_ITEM")
@SequenceGenerator(
        name = "giftItemSequence",
        sequenceName = "GIFT_ITEM_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class GiftItem {

    @Id
    @Column(name = "GIFT_ITEM_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "giftItemSequence")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID")
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;
    
    @Column(name = "INSTANT_UNIT_PRICE", nullable = false)
    private BigDecimal instantUnitPrice;
    
    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getInstantUnitPrice() {
        return instantUnitPrice;
    }

    public void setInstantUnitPrice(BigDecimal instantUnitPrice) {
        this.instantUnitPrice = instantUnitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
