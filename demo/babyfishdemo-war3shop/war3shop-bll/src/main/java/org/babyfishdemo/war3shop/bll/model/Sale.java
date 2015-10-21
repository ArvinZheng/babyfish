package org.babyfishdemo.war3shop.bll.model;

import org.babyfishdemo.war3shop.entities.Preferential;
import org.babyfishdemo.war3shop.entities.Product;

/**
 * @author Tao Chen
 */
public class Sale {

    private Product product;
    
    private Preferential preferential;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Preferential getPreferential() {
        return preferential;
    }

    public void setPreferential(Preferential preferential) {
        this.preferential = preferential;
    }
}
