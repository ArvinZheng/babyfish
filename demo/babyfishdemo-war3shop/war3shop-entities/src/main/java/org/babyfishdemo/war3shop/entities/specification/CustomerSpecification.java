package org.babyfishdemo.war3shop.entities.specification;

/**
 * @author Tao Chen
 */
public class CustomerSpecification extends UserSpecification {

    private String likePhone;
    
    private String likeAddress;

    public String getLikePhone() {
        return likePhone;
    }

    public void setLikePhone(String likePhone) {
        this.likePhone = likePhone;
    }

    public String getLikeAddress() {
        return likeAddress;
    }

    public void setLikeAddress(String likeAddress) {
        this.likeAddress = likeAddress;
    }
}
