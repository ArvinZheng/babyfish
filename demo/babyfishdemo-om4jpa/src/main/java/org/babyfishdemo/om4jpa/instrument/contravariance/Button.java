package org.babyfishdemo.om4jpa.instrument.contravariance;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

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
 * import org.babyfish.model.ObjectModelFactory;
 * import org.babyfish.model.ObjectModelFactoryFactory;
 * import org.babyfish.model.metadata.ObjectModelDeclaration;
 * import org.babyfish.model.metadata.ObjectModelMode;
 * import org.babyfish.model.metadata.Scalar;
 * import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 * import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
 * 
 * @JPAObjectModelInstrument
 * @Entity
 * @DiscriminatorValue("1")
 * public class Button extends Component {
 * 
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 *     
 *     private static final ObjectModelFactory {OM_FACTORY} = 
 *         ObjectModelFactoryFactory.factoryOf({OM}.class);
 *         
 *     private {OM} {om} = ({OM}){OM_FACTORY}.create(this);
 * 
 *     public String getText() {
 *         return {om}.getText();
 *     }
 * 
 *     public void setText(String paramString) {
 *         {om}.setText(paramString);
 *     }
 * 
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(Button paramButton) {
 *         return paramButton.{om};
 *     }
 * 
 *     public String toString() {
 *         return {om}.toString();
 *     }
 * 
 *     @ObjectModelDeclaration(provider = "jpa", mode=ObjectModelMode.REFERENCE)
 *     private static abstract interface {OM} {
 *     
 *         @Scalar
 *         String getText();
 *         void setText(String paramString);
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@DiscriminatorValue("1")
public class Button extends Component {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
