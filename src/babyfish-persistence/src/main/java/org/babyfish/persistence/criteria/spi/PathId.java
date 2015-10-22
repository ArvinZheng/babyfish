/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2015, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.persistence.criteria.spi;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XAbstractQuery;
import org.babyfish.persistence.criteria.XCommonAbstractCriteria;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public final class PathId implements Serializable {
    
    private static final long serialVersionUID = -754466267057075292L;
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);

    private PathStackNode stackNode;
    
    private int position;
    
    private transient int directlyReferencedByTopmostSelection;
    
    private PathId(PathStackNode stackNode, int position) {
        this.stackNode = stackNode;
        this.position = position;
    }
    
    public Path<?> getPath() {
        return (Path<?>)this.stackNode.value;
    }
    
    public int getPosition() {
        return this.position;
    }
    
    public boolean isDirectlyReferencedByTopmostSelection() {
        int dr = this.directlyReferencedByTopmostSelection;
        if (dr == 0) {
            PathStackNode selectionNode = stackNode.parent;
            if (selectionNode.value instanceof CompoundSelection) {
                selectionNode = selectionNode.parent;
            }
            if (selectionNode.value == Allocator.SELECTION && 
                    selectionNode.parent.value instanceof CriteriaQuery<?>) {
                dr = 1;
            } else {
                dr = -1;
            }
            this.directlyReferencedByTopmostSelection = dr;
        }
        return dr == 1;
    }

    @Override
    public int hashCode() {
        return 
                this.stackNode.hashCode() ^ 
                this.position;
    }
    
    @Override
    public boolean equals(Object obj) {
        /*
         * This is a final class, need not use org.babyfish.lang.Equality
         */
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PathId)) {
            return false;
        }
        PathId other = (PathId)obj;
        return 
                this.stackNode.equals(other.stackNode) && 
                this.position == other.position;
    }
    
    static Allocator primaryAllocator() {
        return new PrimaryAllocator();
    }
    
    public static final class PathStackNode implements Serializable {
        
        private static final long serialVersionUID = -7133928240710119848L;

        private PathStackNode parent;
        
        private Object value;
        
        private transient int hash;
    
        private PathStackNode(PathStackNode parent, Object value) {
            this.parent = parent;
            this.value = value;
        }
        
        public PathStackNode getParent() {
            return this.parent;
        }
        
        public Object getValue() {
            return this.value;
        }

        @Override
        public int hashCode() {
            int hash = this.hash;
            if (hash == 0) {
                PathStackNode parent = this.parent;
                hash = parent != null ? parent.hashCode() * 31 : 0;
                hash += System.identityHashCode(this.value);
                if (hash == 0) {
                    hash = -1;
                }
                this.hash = hash;
            }
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            /*
             * This is final class, need not use org.babyfish.lang.Equality
             */
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PathStackNode)) {
                return false;
            }
            PathStackNode other = (PathStackNode)obj;
            return this.value == other.value &&
                    (this.parent == null ? 
                            other.parent == null : 
                            this.parent.equals(other.parent));
        }
    }
    
    public static abstract class Allocator {
        
        public static final Object SELECTION = new SpecialObject("SELECTION");
        
        public static final Object ON_TREE = new SpecialObject("ON_TREE");
        
        public static final Object ASSIGNMENT_LIST = new SpecialObject("ASSIGNMENT_LIST");

        public static final Object RESTRICTION = new SpecialObject("RESTRICTION");
        
        public static final Object GROUP_LIST = new SpecialObject("GROUP_LIST");
        
        public static final Object GROUP_RESTRICTION = new SpecialObject("GROUP_RESTRICTION");
        
        public static final Object ORDER_LIST = new SpecialObject("ORDER_LIST");
            
        private PathStackNode stackNode;
        
        private Map<Path<?>, Integer> pathRepeatedNumMap = new HashMap<>();
        
        private Allocator() {
            
        }
        
        public boolean isEmpty() {
            return this.stackNode == null;
        }
        
        public void push(Object o) {
            PathStackNode stackNode = this.stackNode;
            if (o instanceof CriteriaQuery<?>) {
                if (stackNode != null) {
                    throw new IllegalStateException(LAZY_RESOURCE.get().commonAbstractCriteriaMustBeRoot(CriteriaQuery.class));
                }
            } else if (o instanceof CriteriaUpdate<?>) {
                if (stackNode != null) {
                    throw new IllegalStateException(LAZY_RESOURCE.get().commonAbstractCriteriaMustBeRoot(CriteriaUpdate.class));
                }
            } else if (o instanceof CriteriaDelete<?>) {
                if (stackNode != null) {
                    throw new IllegalStateException(LAZY_RESOURCE.get().commonAbstractCriteriaMustBeRoot(CriteriaDelete.class));
                }
            } else if (o instanceof SpecialObject) {
                if (stackNode == null) {
                    throw new IllegalStateException(LAZY_RESOURCE.get().specialObjectMustNotBeRoot(SpecialObject.class));
                }
                if (!(stackNode.value instanceof XCommonAbstractCriteria)) {
                    throw new IllegalStateException(
                            LAZY_RESOURCE.get().specialObjectMustBePushedAfterXAbstractQuery(
                                    SpecialObject.class, 
                                    XCommonAbstractCriteria.class
                            )
                    );
                }
            } else if (!(o instanceof Selection<?>) && !(o instanceof Order)) {
                Arguments.mustNotBeNull("o", o);
                if (stackNode == null || stackNode.value instanceof XAbstractQuery<?>) {
                    throw new IllegalStateException(
                            LAZY_RESOURCE.get().nodeThatIsNotSelectionOrOrderMustNotBeRootOrUnderXAbstractQuery(
                                    Selection.class,
                                    Order.class,
                                    XAbstractQuery.class
                            )
                    );
                }
            }
            this.stackNode = new PathStackNode(stackNode, o);
        }
        
        public void pop() {
            PathStackNode stackNode = this.stackNode;
            if (stackNode == null) {
                throw new IllegalStateException(LAZY_RESOURCE.get().canNotPopOnEmptyAllocator(Allocator.class));
            }
            this.stackNode = stackNode.parent;
        }
        
        public Object peek() {
            PathStackNode stackNode = this.stackNode;
            return stackNode != null ? stackNode.value : null;
        }
        
        public PathStackNode peekNode() {
            return this.stackNode;
        }
        
        public PathId allocate() {
            PathStackNode stackNode = this.stackNode;
            if (stackNode == null || !(stackNode.value instanceof Path<?>)) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().canNotAllocatePathIdBecauseNoNodeOrNodeIsNotPath(Path.class)
                );
            }
            Path<?> path = (Path<?>)stackNode.value;
            Map<Path<?>, Integer> positionMap = this.pathRepeatedNumMap;
            Integer position = positionMap.get(path);
            if (position == null) {
                position = 0;
            } else {
                position = position + 1;
            }
            positionMap.put(path, position);
            PathStackNode selectionNode = stackNode.parent;
            if (selectionNode.value instanceof CompoundSelection) {
                selectionNode = selectionNode.parent;
            }
            return new PathId(stackNode, position);
        }
        
        public abstract Allocator secondaryAllocator();     
    }

    private static final class PrimaryAllocator extends Allocator {
        
        private List<PathId> pathIdList = new ArrayList<>();

        @Override
        public PathId allocate() {
            PathId pathId = super.allocate();
            this.pathIdList.add(pathId);
            return pathId;
        }

        @Override
        public Allocator secondaryAllocator() {
            return new SecondaryAllocator(this);
        }
    }
    
    private static final class SecondaryAllocator extends Allocator {
        
        private PrimaryAllocator primaryAllocator;
        
        private int index;
        
        private SecondaryAllocator(PrimaryAllocator primaryAllocator) {
            this.primaryAllocator = primaryAllocator;
        }

        @Override
        public PathId allocate() {
            int index = this.index;
            List<PathId> primaryPathIdList = this.primaryAllocator.pathIdList;
            if (index >= primaryPathIdList.size()) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().secondaryPathIdOutOfPriamryIds()
                );
            }
            PathId pathId = super.allocate();
            if (!primaryPathIdList.get(index).equals(pathId)) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().secondaryPathIdNotEqualsPrimaryPathId()
                );
            }
            this.index = index + 1;
            return pathId;
        }

        @Override
        public Allocator secondaryAllocator() {
            return this.primaryAllocator.secondaryAllocator();
        }
    }
    
    private static final class SpecialObject {
        
        private String text;
        
        SpecialObject(String text) {
            this.text = text;
        }
        
        @Override
        public String toString() {
            return this.text;
        }
    }
    
    private interface Resource {

        String commonAbstractCriteriaMustBeRoot(Class<? extends CommonAbstractCriteria> criteriaQueryType);

        String specialObjectMustNotBeRoot(Class<SpecialObject> specialObjectType);

        String specialObjectMustBePushedAfterXAbstractQuery(
                Class<SpecialObject> specialObjectType, 
                Class<? extends XCommonAbstractCriteria> xAbstractQueryType);

        @SuppressWarnings("rawtypes")
        String nodeThatIsNotSelectionOrOrderMustNotBeRootOrUnderXAbstractQuery(
                Class<Selection> selectionType, 
                Class<Order> orderType,
                Class<XAbstractQuery> xAbstractQueryType);

        String canNotPopOnEmptyAllocator(Class<Allocator> allocatorType);

        @SuppressWarnings("rawtypes")
        String canNotAllocatePathIdBecauseNoNodeOrNodeIsNotPath(Class<Path> path);

        String secondaryPathIdOutOfPriamryIds();
        
        String secondaryPathIdNotEqualsPrimaryPathId();
    }
}
