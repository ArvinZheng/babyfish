package org.babyfishdemo.querypath.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * Note: In JPA, the class marked by @Embededable must support 
 * hashCode and equals(Object).
 * But in ObjectModel4JPA, the hashCode and equals
 * must NOT be supported.
 *
 * @author Tao Chen
 */
@JPAObjectModelInstrument(declaredPropertiesOrder = "mineral, gas")
@Embeddable
public class Cost {

    @Column(name = "MINERAL", nullable = false)
    private int mineral;
    
    @Column(name = "GAS", nullable = false)
    private int gas;
    
    public Cost() {
        
    }
    
    public Cost(int mineral, int gas) {
        this.mineral = mineral;
        this.gas = gas;
    }

    public int getMineral() {
        return mineral;
    }

    public void setMineral(int mineral) {
        this.mineral = mineral;
    }

    public int getGas() {
        return gas;
    }

    public void setGas(int gas) {
        this.gas = gas;
    }
}
