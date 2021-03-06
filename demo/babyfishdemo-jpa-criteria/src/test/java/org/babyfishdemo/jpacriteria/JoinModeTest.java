package org.babyfishdemo.jpacriteria;

import javax.persistence.criteria.JoinType;

import junit.framework.Assert;

import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.LikeMode;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XJoin;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfishdemo.jpacriteria.base.AbstractTest;
import org.babyfishdemo.jpacriteria.entities.Department;
import org.babyfishdemo.jpacriteria.entities.Department_;
import org.babyfishdemo.jpacriteria.entities.Employee;
import org.babyfishdemo.jpacriteria.entities.Employee_;
import org.junit.Test;

/** 
 * In this demo-project, I use "org.babyfish.persistence.criteria.QueryTemplate"
 * to show you what JPQL has been generated by babyfish-jpa-criteria.
 * 
 *      In real projects, please don't use it, you'd better use the
 * JPA standard API "EntityManager.createQuery(CriteriaQuery<T>)" and 
 * "org.babyfish.persistence.criteria.QueryTemplate" should be consider as internal interface
 *
 * @author Tao Chen
 */
public class JoinModeTest extends AbstractTest {

    @Test
    public void testCreateNew() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        
        cq.select(employee);
        employee.join(Employee_.department, JoinMode.REQUIRED_TO_CREATE_NEW);
        employee.join(Employee_.department, JoinMode.REQUIRED_TO_MERGE_EXISTS);
        /*
         * In this test, we joined the same association "Employee_.department" for twice,
         * and two join objects are created.
         * 
         * The join mode of first join is "create new", so it will NEVER be merged
         * with other join with the same association, whatever what the join mode of other
         * join is.
         * 
         * Finally, in the generated JPQL, the association is joined twice.
         */
        Assert.assertEquals(
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                + "inner join babyfish_shared_alias_0.department babyfish_not_shared_alias_1 " // first join
                + "inner join babyfish_shared_alias_0.department babyfish_shared_alias_2", // second join
                createQueryTemplate(cq).toString()
        );
    }
    
    @Test
    public void testMergeExists() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        
        cq.select(employee);
        employee.join(Employee_.department, JoinType.LEFT, JoinMode.REQUIRED_TO_MERGE_EXISTS);
        employee.join(Employee_.department, JoinType.RIGHT, JoinMode.REQUIRED_TO_MERGE_EXISTS);
        /*
         * In this test, we joined the same association "Employee_.department" for twice,
         * and two join objects are created.
         * 
         * The join mode of all the join object are "merge exists", so they will be merged 
         * to be one; but these two join objects have difference join type(LEFT and RIGHT),
         * so the join type of the merged join is INNER.
         * 
         * Finally, in the generated JPQL, the association is joined only once.
         */
        Assert.assertEquals(
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                + "inner join babyfish_shared_alias_0.department babyfish_shared_alias_1", // merged together, inner join
                createQueryTemplate(cq).toString()
        );
        
        /*
         * This functionality is very useful for dynamic query 
         * whose structure can only be determined at runtime.
         *  
         * For example(DynamicQuery style-I):
         *
         * XRoot<Employee> employee = cq.from(Employee.class);
         * 
         * Predicate selfNamePredicate = null;
         * Predicate departmentNamePredicate = null;
         * Predicate departmentCityPredicate = null;
         * 
         * if (...) {
         *      selfNamePredicate = cb.equal(employee.get(Employee_.name), ...);
         * }
         * if (...) {
         *      departmentNamePredicate = cb.equal( //first join
         *          employee.join(Employee_.department).get(Department_.name),
         *          ...
         *      );
         *      // Don't worry, this join can be MERGED with the other joins 
         *      // created in outer "if" statements.
         * }
         * if (...) {
         *      departmentCityPredicate = cb.equal( //second join
         *          employee.join(Employee_.department).get(Department_.city),
         *          ...
         *      );
         *      // Don't worry, this join can be MERGED with the other joins in outer
         *      // created in outer "if" statements.
         * }
         * 
         * cq.where(
         *      selfNamePredicate,
         *      departmentNamePredicate,'
         *      departmentCityPredicate
         * ); 
         * // Any one of these tree arguments may be null, it is supported by babyfish
         * // Please see DynamicQueryTest of this project.      
         */
    }
    
    @Test
    public void testOptionalJoinIsIgnored() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        
        cq.select(employee);
        // You can also don't specify JoinMode because OPTIONALLY_MERGE_EXISTS is default behavior
        employee.join(Employee_.department, JoinMode.OPTIONALLY_MERGE_EXISTS);
        
        /*
         * This join mode of this join is "optionally" and it is
         * NEVER used in the query, so it will ignored.
         * 
         * Finally, in the generated JPQL, no joins
         */
        Assert.assertEquals(
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0",
                createQueryTemplate(cq).toString()
        );
        
        // If you think this test is strange, please view 
        // testOptionalJoinIsApplied to resolve your perplexity
    }
    
    @Test
    public void testOptionalJoinIsApplied() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        
        cq.select(employee);
        //You can also don't specify JoinMode because OPTIONALLY_MERGE_EXISTS is default behavior
        XJoin<Employee, Department> department = 
                employee.join(Employee_.department, 
                JoinMode.OPTIONALLY_MERGE_EXISTS
        );
        cq.where(
                cb.like(
                        department.get(Department_.name), //use that join
                        "A", 
                        LikeMode.ANYWHERE
                )
        ); 
        
        /*
         * This join mode of this join is "optionally" and it is used in 
         * the query, so it will applied, not ignored.
         * 
         * Finally, in the generated JPQL, the optional join is applied, not ignored.
         */
        Assert.assertEquals(
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                        
                  // That optional join is applied, not ignored
                + "inner join babyfish_shared_alias_0.department babyfish_shared_alias_1 "
                
                + "where babyfish_shared_alias_1.name like :babyfish_literal_0", // use that join
                createQueryTemplate(cq).toString()
        );
        
        /*
         * This functionality is very useful for dynamic query 
         * whose structure can only be determined at runtime.
         *  
         * For example(DynamicQuery style-II):
         * 
         * XRoot<Employee> employee = cq.from(Employee.class);
         * 
         * // (1) Don't worry, this join will be IGNORED if it is NOT used by the query
         * // (2) You can also don't specify the JoinMode because 
         * // OPTINALLY_MERGE_EXISTS is the default behavior
         * XJoin<Employee, Department> department = 
         *      employee.join(Employee_.department, JoinMode.OPTINALLY_MERGE_EXISTS);
         * 
         * if (...) {
         *      selfNamePredicate = cb.equal(employee.get(Employee_.name), ...);
         * }
         * if (...) {
         *      departmentNamePredicate = cb.equal( //first join
         *          department.get(Department_.name),
         *          ...
         *      );
         * }
         * if (...) {
         *      departmentCityPredicate = cb.equal( //second join
         *          department.get(Department_.city),
         *          ...
         *      );
         * }
         * 
         * cq.where(
         *      selfNamePredicate,
         *      departmentNamePredicate,'
         *      departmentCityPredicate
         * ); 
         * // Any one of these tree arguments may be null, it is supported by babyfish
         * // Please see DynamicQueryTest of this project.
         */
    }
}
