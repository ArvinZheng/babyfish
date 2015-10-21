package org.babyfishdemo.war3shop.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "VARIABLE")
public class Variable {

    @Id
    @Column(name = "NAME", length=30, nullable = false)
    private String name;
    
    @Column(name = "VALUE", length = 100)
    private String value;
    
    @Column(name = "ENCRYPTED_VALUE", length = 256)
    private byte[] encryptedValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public byte[] getEncryptedValue() {
        return encryptedValue;
    }

    public void setEncryptedValue(byte[] encryptedValue) {
        this.encryptedValue = encryptedValue;
    }
}
