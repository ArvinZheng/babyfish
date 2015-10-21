package org.babyfishdemo.war3shop.dal.impl;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XTypedQuery;
import org.babyfishdemo.war3shop.dal.PreferentialItemRepository;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.PreferentialItem;
import org.babyfishdemo.war3shop.entities.PreferentialItem_;
import org.babyfishdemo.war3shop.entities.PreferentialItem__;
import org.babyfishdemo.war3shop.entities.Preferential_;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class PreferentialItemRepositoryImpl implements PreferentialItemRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public Page<PreferentialItem> getPreferentialItemsByParent(
            long preferentialId,
            int pageIndex, 
            int pageSize, 
            PreferentialItem__... queryPaths) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<PreferentialItem> cq = cb.createQuery(PreferentialItem.class);
        Root<PreferentialItem> preferentialItem = cq.from(PreferentialItem.class);
        cq.where(
                cb.equal(
                        preferentialItem.get(PreferentialItem_.preferential).get(Preferential_.id), 
                        preferentialId
                )
        );
        XTypedQuery<PreferentialItem> typedQuery = this.em.createQuery(cq).setQueryPaths(queryPaths);
        return new PageBuilder<>(typedQuery, pageIndex, pageSize).build();
    }
}
