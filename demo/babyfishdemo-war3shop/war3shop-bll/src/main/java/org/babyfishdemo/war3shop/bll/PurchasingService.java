package org.babyfishdemo.war3shop.bll;

import java.util.Collection;

import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.Purchasing;
import org.babyfishdemo.war3shop.entities.PurchasingItem;
import org.babyfishdemo.war3shop.entities.PurchasingItem__;
import org.babyfishdemo.war3shop.entities.Purchasing__;
import org.babyfishdemo.war3shop.entities.specification.ManufacturerSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;
import org.babyfishdemo.war3shop.entities.specification.PurchasingSpecification;

/**
 * @author Tao Chen
 */
public interface PurchasingService {

    Page<Product> getMyPurchasedProducts(
            ProductSpecification specification,
            int pageIndex, 
            int pageSize, 
            Product__... queryPaths);
    
    Page<Manufacturer> getMyPurchasedManufacturers(
            ManufacturerSpecification specification,
            int pageIndex,
            int pageSize,
            Manufacturer__ ... queryPaths);
    
    Page<Purchasing> getMyPurchasings(
            PurchasingSpecification specification,
            int pageIndex,
            int pageSize,
            Purchasing__ ... queryPaths);
    
    Page<PurchasingItem> getPurchasingItems(
            long purchasingId, 
            int pageIndex,
            int pageSize,
            PurchasingItem__ ... queryPaths);
    
    void createPurchasing(Collection<PurchasingItem> purchasingItems);
}
