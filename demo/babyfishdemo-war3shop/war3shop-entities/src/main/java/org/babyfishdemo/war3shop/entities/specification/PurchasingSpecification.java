package org.babyfishdemo.war3shop.entities.specification;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * @author Tao Chen
 */
public class PurchasingSpecification {

    private Date minCreationTime;
    
    private Date maxCreationTime;
    
    private BigDecimal minTotalPurchasedPrice;
    
    private BigDecimal maxTotalPurchasedPrice;
        
    private Collection<Long> includedProductIds;
    
    private Collection<Long> excludedProductIds;
        
    private Collection<Long> includedManufacturerIds;
    
    private Collection<Long> excludedManufacturerIds;
    
    private Collection<Long> includedPurchaserIds;
    
    private Collection<Long> excludedPurchaserIds;

    public Date getMinCreationTime() {
        return minCreationTime;
    }

    public void setMinCreationTime(Date minCreationTime) {
        this.minCreationTime = minCreationTime;
    }

    public Date getMaxCreationTime() {
        return maxCreationTime;
    }

    public void setMaxCreationTime(Date maxCreationTime) {
        this.maxCreationTime = maxCreationTime;
    }

    public BigDecimal getMinTotalPurchasedPrice() {
        return minTotalPurchasedPrice;
    }

    public void setMinTotalPurchasedPrice(BigDecimal minTotalPurchasedPrice) {
        this.minTotalPurchasedPrice = minTotalPurchasedPrice;
    }

    public BigDecimal getMaxTotalPurchasedPrice() {
        return maxTotalPurchasedPrice;
    }

    public void setMaxTotalPurchasedPrice(BigDecimal maxTotalPurchasedPrice) {
        this.maxTotalPurchasedPrice = maxTotalPurchasedPrice;
    }

    public Collection<Long> getIncludedProductIds() {
        return includedProductIds;
    }

    public void setIncludedProductIds(Collection<Long> includedProductIds) {
        this.includedProductIds = includedProductIds;
    }

    public Collection<Long> getExcludedProductIds() {
        return excludedProductIds;
    }

    public void setExcludedProductIds(Collection<Long> excludedProductIds) {
        this.excludedProductIds = excludedProductIds;
    }

    public Collection<Long> getIncludedManufacturerIds() {
        return includedManufacturerIds;
    }

    public void setIncludedManufacturerIds(Collection<Long> includedManufacturerIds) {
        this.includedManufacturerIds = includedManufacturerIds;
    }

    public Collection<Long> getExcludedManufacturerIds() {
        return excludedManufacturerIds;
    }

    public void setExcludedManufacturerIds(Collection<Long> excludedManufacturerIds) {
        this.excludedManufacturerIds = excludedManufacturerIds;
    }

    public Collection<Long> getIncludedPurchaserIds() {
        return includedPurchaserIds;
    }

    public void setIncludedPurchaserIds(Collection<Long> includedPurchaserIds) {
        this.includedPurchaserIds = includedPurchaserIds;
    }

    public Collection<Long> getExcludedPurchaserIds() {
        return excludedPurchaserIds;
    }

    public void setExcludedPurchaserIds(Collection<Long> excludedPurchaserIds) {
        this.excludedPurchaserIds = excludedPurchaserIds;
    }
}
