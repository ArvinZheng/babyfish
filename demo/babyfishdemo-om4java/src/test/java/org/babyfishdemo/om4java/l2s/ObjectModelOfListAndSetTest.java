package org.babyfishdemo.om4java.l2s;

import java.util.Set;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfishdemo.om4java.l2s.Company;
import org.babyfishdemo.om4java.l2s.Investor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ObjectModelOfListAndSetTest {

    @Test
    public void test() {
        Company company1 = new Company();
        Company company2 = new Company();
        Company company3 = new Company();
        Investor investor1 = new Investor();
        Investor investor2 = new Investor();
        Investor investor3 = new Investor();
        
        /*
         * Validate the initialized state of these objects
         */
        assertCompany(company1);
        assertCompany(company2);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2);
        assertInvestor(investor3);
        
        /*
         * Add investor2 and investor3 into company1.
         * company1 will be added into the collection property "companys" of 
         * investor2 and investor3 automatically and implicitly.
         */
        company1.getInvestors().addAll(MACollections.wrap(investor2, investor3));
        assertCompany(company1, investor2, investor3);
        assertCompany(company2);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1);
        
        /*
         * Add all the investors of company1 into company2,
         * this is many-to-many association, so company2 will NOT seize these investors, 
         * just share them with company1.
         * company2 will be added into the collection property "companys" of 
         * investor2 and investor3 automatically and implicitly.
         */
        company2.getInvestors().addAll(company1.getInvestors());
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor2, investor3);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2, company1, company2);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Remove company2 from investor2.
         * investor2 will be removed from company2 automatically and implicitly.
         */
        investor2.getCompanys().remove(company2);
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Add company2 into investor1.
         * investor1 will be added into company2 automatically and implicitly.
         */
        investor1.getCompanys().add(company2);
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3, investor1);
        assertCompany(company3);
        assertInvestor(investor1, company2);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Add all the investors of company2 into company3,
         * this is many-to-many association, so company3 will NOT seize these investors, 
         * just share them with company2.
         * company3 will be added into the collection property "companys" of 
         * investor3 and investor1 automatically and implicitly.
         */
        company3.getInvestors().addAll(company2.getInvestors());
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3, investor1);
        assertCompany(company3, investor3, investor1);
        assertInvestor(investor1, company2, company3);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2, company3);
        
        /*
         * Remove company3 from investor3.
         * investor3 will be removed from company3 automatically and implicitly.
         */
        investor3.getCompanys().remove(company3);
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3, investor1);
        assertCompany(company3, investor1);
        assertInvestor(investor1, company2, company3);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Add company3 into investor2.
         * investor1 will be added into company2 automatically and implicitly.
         */
        investor2.getCompanys().add(company3);
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3, investor1);
        assertCompany(company3, investor1, investor2);
        assertInvestor(investor1, company2, company3);
        assertInvestor(investor2, company3, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Clear companys of investor1.
         * investor1 will be removed from company2 and company3 automatically and implicitly.
         */
        investor1.getCompanys().clear();
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3);
        assertCompany(company3, investor2);
        assertInvestor(investor1);
        assertInvestor(investor2, company3, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Clear companys of investor2.
         * investor2 will be removed from company3 and company1 automatically and implicitly.
         */
        investor2.getCompanys().clear();
        assertCompany(company1, investor3);
        assertCompany(company2, investor3);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Clear companys of investor3.
         * investor3 will be removed from company1 and company2 automatically and implicitly.
         */
        investor3.getCompanys().clear();
        assertCompany(company1);
        assertCompany(company2);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2);
        assertInvestor(investor3);
    }
 
    private static void assertCompany(Company company, Investor ... investors) {
        Assert.assertEquals(investors.length, company.getInvestors().size());
        int index = 0;
        for (Investor investor : company.getInvestors()) {
            Assert.assertSame(investors[index++], investor);
        }
    }
    
    private static void assertInvestor(Investor investor, Company ... companys) {
        Assert.assertEquals(companys.length, investor.getCompanys().size());
        Set<Company> set = new HashSet<>((companys.length * 4 + 2) / 3);
        for (Company company : companys) {
            set.add(company);
        }
        Assert.assertEquals(set, investor.getCompanys());
    }
}
