package org.babyfishdemo.war3shop.entities.specification;

import java.util.Collection;

import org.babyfishdemo.war3shop.entities.Race;

/**
 * @author Tao Chen
 */
public class ManufacturerSpecification {

    private String likeName;
    
    private String likeEmail;
    
    private String likePhone;
    
    private Collection<Race> races;
    
    private Boolean hasProducts;

    private Collection<Long> includedProductIds;
    
    private Collection<Long> excludedProductIds;
    
    private Boolean hasPurchasers;
    
    private Collection<Long> includedPurchaserIds;
    
    private Collection<Long> excludedPurchaserIds;

    public String getLikeName() {
        return likeName;
    }

    public void setLikeName(String likeName) {
        this.likeName = likeName;
    }

    public String getLikeEmail() {
        return likeEmail;
    }

    public void setLikeEmail(String likeEmail) {
        this.likeEmail = likeEmail;
    }

    public String getLikePhone() {
        return likePhone;
    }

    public void setLikePhone(String likePhone) {
        this.likePhone = likePhone;
    }

    public Collection<Race> getRaces() {
        return races;
    }

    public void setRaces(Collection<Race> races) {
        this.races = races;
    }

    public Boolean getHasProducts() {
        return hasProducts;
    }

    public void setHasProducts(Boolean hasProducts) {
        this.hasProducts = hasProducts;
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

    public Boolean getHasPurchasers() {
        return hasPurchasers;
    }

    public void setHasPurchasers(Boolean hasPurchasers) {
        this.hasPurchasers = hasPurchasers;
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
