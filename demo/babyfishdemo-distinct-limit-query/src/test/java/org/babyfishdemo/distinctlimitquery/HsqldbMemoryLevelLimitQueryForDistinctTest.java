package org.babyfishdemo.distinctlimitquery;

import junit.framework.Assert;

import org.babyfish.persistence.QueryType;
import org.babyfishdemo.distinctlimitquery.base.AbstractDAOTest;
import org.babyfishdemo.distinctlimitquery.base.LimitedResult;
import org.babyfishdemo.distinctlimitquery.entities.Department;
import org.babyfishdemo.distinctlimitquery.entities.Department__;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * This test case shows the paging query of QueryType.DISTINCT
 * with collection fetches.
 * 
 * When the query has collection fetches, Hibernate can not do 
 * real paging query on database level, but do it on memory level.
 * This is an low performance solution. Please see 
 * <a href="https://hibernate.atlassian.net/browse/HHH-1412">HHH-1412</a>
 * to know more.
 * 
 * The hibernate memory paging query behavior(Fixed Result of HHH-1412) is forbidden by babyfish-hibernate,
 * unless you specify this property in your Hibernate/JPA configuration:
 *     "babyfish.hibernate.enable_limit_in_memory" = "true",
 * this can remind you to know what you are doing now very carefully.
 * 
 * This test case does NOT show the DistinctLimitQuery, 
 * please see DistinctLimitQueryTest to know more.
 *
 * @author Tao Chen
 */
public class HsqldbMemoryLevelLimitQueryForDistinctTest extends AbstractDAOTest {

    @BeforeClass
    public static void initEntityManagerFactory() {
        initEntityManagerFactory(false);
    }
    
    @Test
    public void test() {
        //        +---+      /------->+Barracks(1)
        //        |   |      |        |
        //        | 0 +------/        +-----Jim Raynor(1, Marine)
        //        |   |               |
        //        |   |               +-----Tychus Findlay(4, Marine)
        //        +---+               |    
        //        |   |      /------->+Ghost Academy(2)
        //        | 1 +------/        |
        //        |   |               +-----Nova Terra(2, Ghost)
        //        |   |               |
        //        +---+               +-----Gabriel Tosh(3, Ghost)
        //        |   |               |
        // First->| 2 +-------------->+Star Port(3)
        //        |   |               |
        //        |   |               +-----Matt Horner(5, Battlecruiser)
        //        +---+               |
        //        |   |      /------->+Templar Archives(4)
        //  Last->| 3 +------/        |
        //        |   |               +-----Tassadar(6, High Templar)
        //        |   |               |
        //        +---+               +-----Karass(12, High Templar)
        //        |   |               |
        //        | 4 +-------------->+Dark Shrine(5)
        //        |   |               |
        //        |   |               +-----Zeratul(7, Dark Templar)
        //        +---+               |    
        //        |   |      /------->+Star Gate(6)
        //        | 5 +------/        |
        //        |   |               +-----Mohandar(9, Void Ray)
        //        |   |               |
        //        +---+               +-----Urun(10, Phoenix)
        //        |   |               |
        //        | 6 +-------------->+Fleet Beacon(7)
        //        |   |               |
        //        |   |               +-----Artanis(8, Mothership)
        //        +---+               |
        //                            \-----Selendis(11, Carrier)
        LimitedResult<Department> limitedResult = getLimitedDepartments(
                QueryType.DISTINCT, 
                2,
                2,
                Department__.begin().employees().end(),
                Department__.preOrderBy().id().asc(),
                Department__.preOrderBy().employees().id().asc()
        );
        
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select count(department0_.DEPARTMENT_ID) "
                + "from DEPARTMENT department0_", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of employees1_...> "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.department_DEPARTMENT_ID "
                + "order by "
                +     "department0_.DEPARTMENT_ID asc, "
                +     "employees1_.EMPLOYEE_ID asc", 
                this.preparedSqlList.get(1)
        );
        
        
        Assert.assertEquals(7, limitedResult.getUnlimitedRowCount());
        Assert.assertEquals(2, limitedResult.getLimitedRows().size());
        
        assertDepartment(limitedResult.getLimitedRows().get(0), "Star Port", "Matt Horner");
        assertDepartment(limitedResult.getLimitedRows().get(1), "Templar Archives", "Tassadar", "Karass");
    }
}
