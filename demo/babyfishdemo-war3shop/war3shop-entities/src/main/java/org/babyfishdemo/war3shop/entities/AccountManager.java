package org.babyfishdemo.war3shop.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
@JPAObjectModelInstrument
@Entity
@DiscriminatorValue("2")
public class AccountManager extends User {
    
}
