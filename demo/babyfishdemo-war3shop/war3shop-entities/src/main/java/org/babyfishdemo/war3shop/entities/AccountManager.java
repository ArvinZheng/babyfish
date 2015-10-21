package org.babyfishdemo.war3shop.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@DiscriminatorValue("2")
public class AccountManager extends User {
    
}
