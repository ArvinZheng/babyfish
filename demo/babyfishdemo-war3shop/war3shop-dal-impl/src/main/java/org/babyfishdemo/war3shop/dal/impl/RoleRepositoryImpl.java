package org.babyfishdemo.war3shop.dal.impl;

import java.util.List;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfishdemo.war3shop.dal.RoleRepository;
import org.babyfishdemo.war3shop.entities.Role;
import org.babyfishdemo.war3shop.entities.Role_;
import org.babyfishdemo.war3shop.entities.Role__;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class RoleRepositoryImpl extends AbstractRepositoryImpl<Role, Long> implements RoleRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public Role getRoleById(Long id, Role__... queryPaths) {
        return this.em.find(Role.class, id, queryPaths);
    }

    @Override
    public List<Role> getRolesByIds(Iterable<Long> ids, Role__... queryPaths) {
        return this.em.find(Role.class, ids, queryPaths);
    }

    @Override
    public List<Role> getAllRoles(Role__... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Role> cq = cb.createQuery(Role.class);
        Root<Role> role = cq.from(Role.class);
        cq.orderBy(cb.asc(role.get(Role_.name)));
        return this.em.createQuery(cq).setQueryPaths(queryPaths).getResultList();
    }

    @Override
    public Role getRoleByNameInsensitively(String name, Role__... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Role> cq = cb.createQuery(Role.class);
        Root<Role> role = cq.from(Role.class);
        cq.where(cb.equal(cb.upper(role.get(Role_.name)), name.toUpperCase()));
        return this.em.createQuery(cq).setQueryPaths(queryPaths).getSingleResult(true);
    }
}
