package org.babyfishdemo.spring.dal.impl;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;

import org.babyfish.persistence.XEntityManager;
import org.babyfishdemo.spring.dal.DepartmentRepository;
import org.babyfishdemo.spring.entities.Department;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class DepartmentRepositoryImpl implements DepartmentRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public Department mergeDepartment(Department department) {
        return this.em.merge(department);
    }

    @Override
    public int deleteAllDepartments() {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaDelete<Department> cd = cb.createCriteriaDelete(Department.class);
        cd.from(Department.class);
        return this.em.createQuery(cd).executeUpdate();
    }
}
