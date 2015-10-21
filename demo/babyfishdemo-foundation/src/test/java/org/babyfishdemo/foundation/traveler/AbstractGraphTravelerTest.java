package org.babyfishdemo.foundation.traveler;


/**
 * @author Tao Chen
 */
public class AbstractGraphTravelerTest {

    protected final Atom createSodiumBenzoate() {
        /*
         * The molecules of "Sodium Benzoate"
         *    (C6H5-CO2-Na) 
         * have simple structure with only one ring,
         * but it is complex enough for this demo
         * 
         *      H4     H5
         *       \     /
         *       C4---C5      O1
         *       /     \      |
         * H3---C3     C6-----C7---O2-----Na1
         *       \     /
         *       C2---C1
         *       /     \
         *      H2     H1
         *      
         * We use "C7" to be the starting point of the graph travel.
         */
        
        /*
         * Because of ObjectModel4Java,
         * the explicit code 
         *      "a.getNeighbors().add(b);"
         * can do 
         *      "b.getNeighbors().add(a);" 
         * implicitly.
         */
        
        Atom c1 = this.createAtom("C1");
        Atom c2 = this.createAtom("C2");
        Atom c3 = this.createAtom("C3");
        Atom c4 = this.createAtom("C4");
        Atom c5 = this.createAtom("C5");
        Atom c6 = this.createAtom("C6");
        Atom c7 = this.createAtom("C7");
        
        Atom h1 = this.createAtom("H1");
        Atom h2 = this.createAtom("H2");
        Atom h3 = this.createAtom("H3");
        Atom h4 = this.createAtom("H4");
        Atom h5 = this.createAtom("H5");
        
        Atom o1 = this.createAtom("O1");
        Atom o2 = this.createAtom("O2");
        
        Atom na1 = this.createAtom("Na1");
        
        /*
         * Create the base ring of the phenyl.
         */
        c1.getNeighbors().add(c2);
        c2.getNeighbors().add(c3);
        c3.getNeighbors().add(c4);
        c4.getNeighbors().add(c5);
        c5.getNeighbors().add(c6);
        c6.getNeighbors().add(c1);
        
        /*
         * Add the hydrogen atoms to the phenyl ring.
         */
        c1.getNeighbors().add(h1);
        c2.getNeighbors().add(h2);
        c3.getNeighbors().add(h3);
        c4.getNeighbors().add(h4);
        c5.getNeighbors().add(h5);
        
        /*
         * Create the carboxyl sodium
         */
        c7.getNeighbors().add(o1);
        c7.getNeighbors().add(o2);
        o2.getNeighbors().add(na1);
        
        /*
         * connect the phenyl and carboxyl.
         */
        c6.getNeighbors().add(c7);
        
        return c7;
    }
    
    protected Atom createAtom(String name) {
        return new Atom(name);
    }
}
