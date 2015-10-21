package org.babyfishdemo.distinctlimitquery.base;

import java.util.List;

import org.babyfish.persistence.QueryType;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XTypedQuery;
import org.babyfishdemo.distinctlimitquery.entities.Department;
import org.babyfishdemo.distinctlimitquery.entities.Department__;

/**
 * @author Tao Chen
 */
public class AbstractDAOTest extends AbstractTest {

    protected static List<Department> getAllDepartments(QueryType queryType, Department__ ... queryPaths) {
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            return 
                    em
                    .createQuery("select d from Department d", Department.class)
                    .setQueryType(queryType) //If delete this invocation, default mode is QueryType.DISTINCT
                    .setQueryPaths(queryPaths)
                    .getResultList();
        }
    }
    
    protected static LimitedResult<Department> getLimitedDepartments(
            QueryType queryType,
            int firstResult, 
            int maxResults, 
            Department__ ... queryPaths) {
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            XTypedQuery<Department> query =
                    em
                    .createQuery("select d from Department d", Department.class)
                    .setQueryType(queryType) //If delete this invocation, default mode is QueryType.DISTINCT
                    .setQueryPaths(queryPaths)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults);
            return new LimitedResult<>(query.getUnlimitedCount(), query.getResultList());
        }
    }
}
