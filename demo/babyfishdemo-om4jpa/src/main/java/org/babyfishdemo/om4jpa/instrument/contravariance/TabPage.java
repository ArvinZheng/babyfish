package org.babyfishdemo.om4jpa.instrument.contravariance;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.babyfish.persistence.instrument.ContravarianceInstrument;
import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/*
 * This class is marked by the annotation 
 * @org.babyfish.persistence.instrument.JPAObjectModelInstrument
 * and the ant task "org.babyfish.hibernate.tool.InstrumentTask" is
 * declared in pom.xml, so the byte-code of this class will be 
 * instrumented.
 * 
 * You can use some java decompile tools such as jd-gui.exe to
 * decompile the jar file under the target directory if you 
 * have doubt or you want to research it.
 * 
 * The disadvantage of the byte-code instrument mechanism is, every 
 * time you change the source code of the entity classes, you must 
 * use maven to recompile them because the default compilation 
 * behavior of eclipse will NOT do this byte-code instrument.
 * 
 * Finally, after instrument, this actual code of this class should be
 * (
 *      In java language, java identifiers can not contains invalid characters 
 *      such as "{" and "}", but they can be accepted by JVM, so I generate 
 *      the identifiers with "{" and "}" so that they can not be conflicted by 
 *      the user defined identifiers absolutely.
 * ):
 * 
 * package org.babyfishdemo.om4jpa.instrument.contravariance;
 * 
 * import javax.persistence.DiscriminatorValue;
 * import javax.persistence.Entity;
 * import javax.persistence.Transient;
 * import org.babyfish.model.ObjectModelFactory;
 * import org.babyfish.model.ObjectModelFactoryFactory;
 * import org.babyfish.model.metadata.Contravariance;
 * import org.babyfish.model.metadata.ObjectModelDeclaration;
 * import org.babyfish.model.metadata.ObjectModelMode;
 * import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 * import org.babyfish.persistence.instrument.ContravarianceInstrument;
 * import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
 * import org.babyfish.reference.IndexedReference;
 * 
 * @JPAObjectModelInstrument
 * @Entity
 * @DiscriminatorValue("3")
 * public class TabPage extends Container {
 * 
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 *     
 *     private static final ObjectModelFactory {OM_FACTORY} = 
 *         ObjectModelFactoryFactory.factoryOf({OM}.class);
 *     
 *     private {OM} {om} = ({OM}){OM_FACTORY}.create(this);
 * 
 *     @ContravarianceInstrument("parent")
 *     @Transient
 *     public TabControl getTabControl() {
 *         return (TabControl){om}.getTabControlReference().get();
 *     }
 * 
 *     public void setTabControl(TabControl paramTabControl) {
 *         {om}.getTabControlReference().set(paramTabControl);
 *     }
 * 
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(TabPage paramTabPage) {
 *         return paramTabPage.{om};
 *     }
 * 
 *     public String toString() {
 *         return {om}.toString();
 *     }
 * 
 *     @ObjectModelDeclaration(provider = "jpa", mode = ObjectModelMode.REFERENCE)
 *     private interface {OM} {
 *     
 *         @Contravariance("parentReference")
 *         IndexedReference<TabControl> getTabControlReference();
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@DiscriminatorValue("3")
public class TabPage extends Container {

    @ContravarianceInstrument("parent")
    @Transient
    private TabControl tabControl;

    public TabControl getTabControl() {
        return tabControl;
    }

    public void setTabControl(TabControl tabControl) {
        this.tabControl = tabControl;
    }
}
