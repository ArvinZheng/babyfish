package org.babyfishdemo.foundation.traveler;

import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.lang.Func;
import org.babyfish.util.GraphTravelAction;
import org.babyfish.util.GraphTravelContext;
import org.babyfish.util.GraphTraveler;
import org.babyfish.util.Joins;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class Style1GraphTravelerTest extends AbstractGraphTravelerTest {
    
    /*
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
     * "C7 is the root atom"
     */
    private Atom rootAtom = this.createSodiumBenzoate();
    
    @Test
    public void testDepthFirst() {
        StringBuilder builder = new StringBuilder();
        new AtomTraveler(builder).depthFirstTravel(this.rootAtom);
        Assert.assertEquals(
                "C7 (indexPath = 1, namePath = C7)\n" +
                "    O1 (indexPath = 1.1, namePath = C7->O1)\n" +
                "    O2 (indexPath = 1.2, namePath = C7->O2)\n" +
                "        Na1 (indexPath = 1.2.2, namePath = C7->O2->Na1)\n" +
                "    C6 (indexPath = 1.3, namePath = C7->C6)\n" +
                "        C5 (indexPath = 1.3.1, namePath = C7->C6->C5)\n" +
                "            C4 (indexPath = 1.3.1.1, namePath = C7->C6->C5->C4)\n" +
                "                C3 (indexPath = 1.3.1.1.1, namePath = C7->C6->C5->C4->C3)\n" +
                "                    C2 (indexPath = 1.3.1.1.1.1, namePath = C7->C6->C5->C4->C3->C2)\n" +
                "                        C1 (indexPath = 1.3.1.1.1.1.1, namePath = C7->C6->C5->C4->C3->C2->C1)\n" +
                "                            H1 (indexPath = 1.3.1.1.1.1.1.3, namePath = C7->C6->C5->C4->C3->C2->C1->H1)\n" +
                "                        H2 (indexPath = 1.3.1.1.1.1.3, namePath = C7->C6->C5->C4->C3->C2->H2)\n" +
                "                    H3 (indexPath = 1.3.1.1.1.3, namePath = C7->C6->C5->C4->C3->H3)\n" +
                "                H4 (indexPath = 1.3.1.1.3, namePath = C7->C6->C5->C4->H4)\n" +
                "            H5 (indexPath = 1.3.1.3, namePath = C7->C6->C5->H5)\n", 
                builder.toString()
        );
    }
    
    @Test
    public void testBreadthFirst() {
        StringBuilder builder = new StringBuilder();
        new AtomTraveler(builder).breadthFirstTravel(this.rootAtom);
        Assert.assertEquals(
                "C7 (indexPath = 1, namePath = C7)\n" +
                "    O1 (indexPath = 1.1, namePath = C7->O1)\n" +
                "    O2 (indexPath = 1.2, namePath = C7->O2)\n" +
                "    C6 (indexPath = 1.3, namePath = C7->C6)\n" +
                "        Na1 (indexPath = 1.2.2, namePath = C7->O2->Na1)\n" +
                "        C5 (indexPath = 1.3.1, namePath = C7->C6->C5)\n" +
                "        C1 (indexPath = 1.3.2, namePath = C7->C6->C1)\n" +
                "            C4 (indexPath = 1.3.1.1, namePath = C7->C6->C5->C4)\n" +
                "            H5 (indexPath = 1.3.1.3, namePath = C7->C6->C5->H5)\n" +
                "            C2 (indexPath = 1.3.2.1, namePath = C7->C6->C1->C2)\n" +
                "            H1 (indexPath = 1.3.2.3, namePath = C7->C6->C1->H1)\n" +
                "                C3 (indexPath = 1.3.1.1.1, namePath = C7->C6->C5->C4->C3)\n" +
                "                H4 (indexPath = 1.3.1.1.3, namePath = C7->C6->C5->C4->H4)\n" +
                "                H2 (indexPath = 1.3.2.1.3, namePath = C7->C6->C1->C2->H2)\n" +
                "                    H3 (indexPath = 1.3.1.1.1.3, namePath = C7->C6->C5->C4->C3->H3)\n", 
                builder.toString()
        );
    }
    
    /*
     * GraphTravelContext.stopTravel(),
     * GraphTravelContext.stopTravelSiblingNodes()
     * and 
     * GraphTravelContext.stopTravelNeighborNodes()
     * 
     * have been demonstrated in TreeTravelerTest, 
     * so it is unnecessary to demonstrate them again in this test class
     */

    private static class AtomTraveler extends GraphTraveler<Atom> {
        
        private StringBuilder target;
        
        private Set<Atom> visitedAtoms = new HashSet<>(ReferenceEqualityComparator.getInstance());
        
        public AtomTraveler(StringBuilder target) {
            this.target = target;
        }

        // For Graph(With ring) traveler, "isVistable" and "visited" are required
        @Override
        protected boolean isVisitable(GraphTravelContext<Atom> ctx) {
            return !this.visitedAtoms.contains(ctx.getNode());
        }

        // For Graph(With ring) traveler, "isVistable" and "visited" are required
        @Override
        protected void visited(GraphTravelContext<Atom> ctx) {
            this.visitedAtoms.add(ctx.getNode());
        }

        @Override
        protected Iterator<Atom> getNeighborNodeIterator(Atom atom) {
            return atom.getNeighbors().iterator();
        }

        @Override
        protected void preTravelNeighborNodes(
                GraphTravelContext<Atom> ctx,
                GraphTravelAction<Atom> optionalGraphTravelAction) {
            
            super.preTravelNeighborNodes(ctx, optionalGraphTravelAction);
            
            StringBuilder sb = this.target;
            for (int i = ctx.getDepth(); i > 0; i--) {
                sb.append("    ");
            }
            sb.append(ctx.getNode().getName()).append(" (indexPath = ");
            Joins.join(
                    ctx.getBranchNodeIndexes(), 
                    ".", 
                    new Func<Integer, String>() {
                        @Override
                        public String run(Integer x) {
                            return Integer.toString(x.intValue() + 1);
                        }
                    }, 
                    sb
            );
            sb.append(", namePath = ");
            Joins.join(
                    ctx.getBranchNodes(), 
                    "->", 
                    new Func<Atom, String>() {
                        @Override
                        public String run(Atom x) {
                            return x.getName();
                        }
                    }, 
                    sb
            );
            sb.append(")\n");
        }
    }
}
