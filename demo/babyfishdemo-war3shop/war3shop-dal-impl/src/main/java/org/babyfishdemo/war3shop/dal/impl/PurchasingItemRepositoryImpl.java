package org.babyfishdemo.war3shop.dal.impl;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XTypedQuery;
import org.babyfishdemo.war3shop.dal.PurchasingItemRepository;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.PurchasingItem;
import org.babyfishdemo.war3shop.entities.PurchasingItem_;
import org.babyfishdemo.war3shop.entities.PurchasingItem__;
import org.babyfishdemo.war3shop.entities.Purchasing_;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class PurchasingItemRepositoryImpl implements PurchasingItemRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public Page<PurchasingItem> getPurchasingItemsByPurchasingId(
            long purchasingId,
            int pageIndex,
            int pageSize,
            PurchasingItem__ ... queryPaths) {
        
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<PurchasingItem> cq = cb.createQuery(PurchasingItem.class);
        Root<PurchasingItem> purchasingItem = cq.from(PurchasingItem.class);
        cq.where(
                cb.equal(
                        purchasingItem.get(PurchasingItem_.purchasing).get(Purchasing_.id), 
                        purchasingId
                )
        );
        XTypedQuery<PurchasingItem> typedQuery = this.em.createQuery(cq).setQueryPaths(queryPaths);
        return new PageBuilder<>(typedQuery, pageIndex, pageSize).build();
    }
}
