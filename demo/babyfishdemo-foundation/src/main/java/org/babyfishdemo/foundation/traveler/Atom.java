package org.babyfishdemo.foundation.traveler;

import java.util.Set;

import org.babyfish.collection.XOrderedSet;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;

/*
 * This class uses another functionality called "ObjectModel4Java"
 * which is NOT the demonstration purpose of this demo project.
 * 
 * You need not to fully know what the "ObjectModel4Java" is when 
 * you are learning this demo because this demo wants to tell you
 * what the "GraphTraveler" is. You only need to know a little
 * about it.
 *      When you do 
 *          "a.getNeighbors().add(b)"
 *      , The 
 *          "b.getNeighbors().add(a)" 
 *      will be executed automatically and implicitly
 */
/**
 * @author Tao Chen
 */
public class Atom {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Atom atom) {
        return atom.om;
    }
    
    public Atom(String name) {
        this.setName(name);
    }
    
    public String getName() {
        return om.getName();
    }

    public void setName(String name) {
        om.setName(name);
    }

    public Set<Atom> getNeighbors() {
        return om.getNeighbors();
    }

    @ObjectModelDeclaration
    private interface OM {
        
        @Scalar
        String getName();
        void setName(String name);
        
        @Association(opposite = "neighbors")
        XOrderedSet<Atom> getNeighbors(); 
    }
}
