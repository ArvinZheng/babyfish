package org.babyfishdemo.om4java.contravariance;

import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.reference.Reference;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ContravarianceReferenceTest {
 
    private static final ObjectModelFactory<?> COVARIANCE_OBJECT_MODEL_FACTORY;
    
    private static final int COVARIANCE_PARENT_REFERENCE_ID;
 
    @Test(expected = IllegalArgumentException.class)
    public void testCovarianceReferenceFailed() {
        /*
         * The covariance property requires the element must be instance of TabControl,
         * but here the argument is instance of Container, so the java.lang.IllegalArgumentException will be raised
         */
        Reference<Container> covarianceReference = covarianceReference(new TabPage());
        covarianceReference.set(new Container());
    }
    
    @Test
    public void testCovarianceReferenceSucessed() {
        TabPage tabPage = new TabPage();
        TabControl tabControl = new TabControl();
        Reference<Container> covarianceReference = covarianceReference(tabPage);
        
        /*
         * Change the covariance property
         */
        covarianceReference.set(tabControl);
        
        /*
         * The covariance property has been modified
         */
        Assert.assertSame(tabControl, covarianceReference.get());
        
        /*
         * The contravariance property has been modifed too because it shares data with covariance property
         */
        Assert.assertSame(tabControl, tabPage.getParent());
        
        /*
         * The opposite one-to-many assocation is modified automatically and impplicitly
         */
        assertTabControl(tabControl, tabPage);
    }
    
    @Test
    public void testContravarianceReference() {
        TabPage tabPage = new TabPage();
        TabControl tabControl = new TabControl();
        Reference<Container> covarianceReference = covarianceReference(tabPage);
        
        /*
         * Change the covariance property
         */
        covarianceReference.set(tabControl);
        
        /*
         * The contravariance property has been modifed
         */
        Assert.assertSame(tabControl, tabPage.getParent());
        
        /*
         * The covariance property has been modified too because it shares data with contravariance property
         */
        Assert.assertSame(tabControl, covarianceReference.get());
        
        /*
         * The opposite one-to-many assocation is modified automatically and impplicitly
         */
        assertTabControl(tabControl, tabPage);
    }
    
    @SuppressWarnings("unchecked")
    private Reference<Container> covarianceReference(TabPage tabPage) {
        ObjectModel om = (ObjectModel)COVARIANCE_OBJECT_MODEL_FACTORY.get(tabPage);
        return (Reference<Container>)om.getAssociation(COVARIANCE_PARENT_REFERENCE_ID);
    }
    
    private static void assertTabControl(TabControl tabControl, TabPage ... tabPages) {
        Assert.assertEquals(tabPages.length, tabControl.getTabPages().size());
        int index = 0;
        for (TabPage tabPage : tabControl.getTabPages()) {
            Assert.assertSame(tabPages[index++], tabPage);
        }
    }
 
    static {
        ObjectModelMetadata metadata = Metadatas.of(Component.class);
        COVARIANCE_OBJECT_MODEL_FACTORY = 
            metadata.getProvider().getFactory(
                metadata.getObjectModelClass());
        COVARIANCE_PARENT_REFERENCE_ID = 
            metadata.getDeclaredAssociationProperty("parentReference").getId();
    }
}
