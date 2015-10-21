package org.babyfishdemo.querypath;

import org.babyfish.persistence.XEntityManager;
import org.babyfishdemo.querypath.entities.Company;
import org.babyfishdemo.querypath.entities.Company__;

/**
 * @author Tao Chen
 */
public class CompanyRepository {

    public Company getCompanyByName(String name, Company__ ... queryPaths) {
        
        // Close the entity manager soon, say goodbye for spring-open-session-in-view
        // (In real project, please close the entity manager in services, not repositories)
        try (XEntityManager em = JPAContext.createEntityManager()) {
            return 
                    em
                    .createQuery("select c from Company c where c.name = :name", Company.class)
                    .setParameter("name", name)
                    .setQueryPaths(queryPaths)
                    .getSingleResult(true);
        }
    }
}
