package org.babyfishdemo.war3shop.dal;

import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Purchasing;
import org.babyfishdemo.war3shop.entities.Purchasing__;
import org.babyfishdemo.war3shop.entities.specification.PurchasingSpecification;

/**
 * @author Tao Chen
 */
public interface PurchasingRepository extends AbstractRepository<Purchasing, Long> {

    Page<Purchasing> getPurchasings(
            PurchasingSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Purchasing__ ... queryPaths);
}
