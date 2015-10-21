package org.babyfishdemo.war3shop.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "INVENTORY")
@SequenceGenerator(
        name = "inventorySequence",
        sequenceName = "INVENTORY_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Inventory {
    
    @Id
    @Column(name = "INVENTORY_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventorySequence")
    private Long id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private int version;

    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", unique = true, nullable = false)
    private Product product;

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
