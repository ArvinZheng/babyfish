package org.babyfishdemo.om4jpa.instrument.navigable;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.NavigableSet;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenComparator;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XSet;
import org.babyfish.persistence.model.metadata.JPAMetadatas;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.junit.Test;

import junit.framework.Assert;

/*
 * This test class is similar with "org.babyfishdemo.om4jpa.instrument.s2r.InstrumentTest",
 * it's unnecessary to do the tests of that test class here again.
 * 
 * In this class, only the difference between this test class and that test class are shown.
 */
public class InstrumentTest {
    
    private JPAObjectModelMetadata companyOMM = JPAMetadatas.of(Company.class);
    
    private JPAObjectModelMetadata investorOMM = JPAMetadatas.of(Investor.class);
    
    @Test
    public void testInvestorSide() {
        
        Comparator<Company> expectedSetComparator = this.companyOMM.getOwnerComparator("name");
        Assert.assertTrue(expectedSetComparator instanceof FrozenComparator<?>);
        
        /*
         * Metadata test
         */
        Assert.assertSame(
                NavigableSet.class,
                this.investorOMM.getAssociationProperty("companys").getReturnClass()
        );
        Assert.assertSame(
                expectedSetComparator,
                this.investorOMM.getAssociationProperty("companys").getCollectionUnifiedComparator().comparator(true)
        );
        
        /*
         * Runtime test
         */
        Assert.assertTrue(
                new Investor().getCompanys() instanceof MANavigableSet<?>
        );
        Assert.assertSame(
                expectedSetComparator, 
                (((XSet<Company>)new Investor().getCompanys()).unifiedComparator().comparator(true))
        );
    }
    
    @Test
    public void testCompanySide() {
        
        EqualityComparator<Investor> expectedMapValueEqualityComparator = 
                this.investorOMM.getOwnerEqualityComparator("name");
        Assert.assertTrue(expectedMapValueEqualityComparator instanceof FrozenEqualityComparator<?>);
        
        /*
         * Metadata test
         */
        Assert.assertSame(
                NavigableMap.class,
                this.companyOMM.getAssociationProperty("investors").getReturnClass()
        );
        Assert.assertSame(
                expectedMapValueEqualityComparator,
                this.companyOMM.getAssociationProperty("investors").getCollectionUnifiedComparator().equalityComparator(true)
        );
        
        /*
         * Runtime test
         */
        Assert.assertTrue(
                new Company().getInvestors() instanceof MANavigableMap<?, ?>
        );
        Assert.assertSame(
                expectedMapValueEqualityComparator,
                ((XMap<?, ?>)new Company().getInvestors()).valueUnifiedComparator().equalityComparator(true)
        );
    }
}
