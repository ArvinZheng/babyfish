package org.babyfishdemo.war3shop.dal.impl;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.babyfish.lang.Nulls;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.criteria.LikeMode;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfishdemo.war3shop.dal.AlarmRepository;
import org.babyfishdemo.war3shop.entities.Alarm;
import org.babyfishdemo.war3shop.entities.Alarm_;
import org.babyfishdemo.war3shop.entities.Alarm__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.User_;
import org.babyfishdemo.war3shop.entities.specification.AlarmSpecification;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class AlarmRepositoryImpl 
extends AbstractRepositoryImpl<Alarm, Long> 
implements AlarmRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public Alarm getAlarmById(long id, Alarm__... queryPaths) {
        return this.em.find(Alarm.class, id, queryPaths);
    }

    @Override
    public Page<Alarm> getAlarms(
            AlarmSpecification specification,
            int pageIndex, 
            int pageSize, 
            Alarm__... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Alarm> cq = cb.createQuery(Alarm.class);
        Root<Alarm> alarm = cq.from(Alarm.class);
        if (specification != null) {
            Predicate userIdPredicate = null;
            Predicate creationTimePredicate = null;
            Predicate acknowledgedPredicate = null;
            Predicate keywordPredicate = null;
            if (specification.getUserId() != null) {
                userIdPredicate = cb.equal(
                        alarm.get(Alarm_.user).get(User_.id), 
                        specification.getUserId()
                );
            }
            if (specification.getMinCreationTime() != null || specification.getMaxCreationTime() != null) {
                creationTimePredicate = cb.between(
                        alarm.get(Alarm_.creationTime), 
                        specification.getMinCreationTime(), 
                        specification.getMaxCreationTime()
                );
            }
            if (specification.getAcknowledged() != null) {
                acknowledgedPredicate = cb.equal(
                        alarm.get(Alarm_.acknowledged),
                        specification.getAcknowledged()
                );
            }
            if (!Nulls.isNullOrEmpty(specification.getKeyword())) {
                keywordPredicate = cb.insensitivelyLike(
                        alarm.get(Alarm_.message), 
                        specification.getKeyword(), 
                        LikeMode.ANYWHERE);
            }
            cq
            .where(
                    userIdPredicate,
                    creationTimePredicate,
                    acknowledgedPredicate,
                    keywordPredicate
            )
            .orderBy(cb.desc(alarm.get(Alarm_.id)));
        }
        return new PageBuilder<>(
                this.em.createQuery(cq).setQueryPaths(queryPaths),
                pageIndex,
                pageSize
        ).build();
    }
}
