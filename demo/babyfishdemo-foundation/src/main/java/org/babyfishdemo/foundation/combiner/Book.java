package org.babyfishdemo.foundation.combiner;

import java.math.BigDecimal;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.Combiner;
import org.babyfish.lang.Combiners;
import org.babyfish.lang.Nulls;
import org.babyfishdemo.foundation.combiner.event.PropertyChanagedEvent;
import org.babyfishdemo.foundation.combiner.event.PropertyChangedListener;

/**
 * @author Tao Chen
 */
public class Book {
    
    private static final Combiner<PropertyChangedListener> PROPERTY_CHANGED_LISTENER_COMBINER =
            Combiners.of(PropertyChangedListener.class);

    private String name;
    
    private BigDecimal price;
    
    private PropertyChangedListener propertyChangedListener;
    
    public Book(String name, BigDecimal price) {
        this.setName(name);
        this.setPrice(price);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Arguments.mustNotBeEmpty(
                "name", 
                Arguments.mustNotBeNull("name", name)
        );
        String oldName = this.name;
        if (Nulls.equals(oldName, name)) {
            return;
        }
        this.name = name;
        if (this.propertyChangedListener != null) {
            this.propertyChangedListener.propertyChanged(
                    new PropertyChanagedEvent(this, "name", oldName, name)
            );
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        Arguments.mustBeGreaterThanValue(
                "price", 
                Arguments.mustNotBeNull("price", price), 
                BigDecimal.ZERO
        );
        BigDecimal oldPrice = this.price;
        if (Nulls.equals(oldPrice, price)) {
            return;
        }
        this.price = price;
        if (this.propertyChangedListener != null) {
            this.propertyChangedListener.propertyChanged(
                    new PropertyChanagedEvent(this, "price", oldPrice, price)
            );
        }
    }
    
    public void addPropertyChangedListener(PropertyChangedListener listener) {
        this.propertyChangedListener = PROPERTY_CHANGED_LISTENER_COMBINER.combine(
                this.propertyChangedListener, 
                listener
        );
    }
    
    public void removePropertyChangedListener(PropertyChangedListener listener) {
        this.propertyChangedListener = PROPERTY_CHANGED_LISTENER_COMBINER.remove(
                this.propertyChangedListener, 
                listener
        );
    }
}
