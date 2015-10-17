/*
 * BabyFish, Object Model Framework for Java and JPA.
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
package org.babyfish.collection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.RandomAccess;

import org.babyfish.collection.spi.AbstractMAList;
import org.babyfish.collection.spi.base.ArrayElements;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public class MAArrayList<E> extends AbstractMAList<E> implements RandomAccess, Serializable {
    
    private static final long serialVersionUID = 666759041462320433L;

    private static final LazyResource<BuilderResource> LAZY_BUILDER_RESOURCE = LazyResource.of(BuilderResource.class);
    
    public MAArrayList() {
        super(
                new ArrayElements<E>(
                        null,
                        8,
                        1.5F,
                        .75F
                )
        );
    }

    public MAArrayList(EqualityComparator<? super E> equalityComparator) {
        super(
                new ArrayElements<E>(
                        equalityComparator,
                        8,
                        1.5F,
                        .75F
                )
        );
    }

    public MAArrayList(Comparator<? super E> comparator) {
        super(
                new ArrayElements<E>(
                        comparator,
                        8,
                        1.5F,
                        .75F
                )
        );
    }

    public MAArrayList(UnifiedComparator<? super E> unifiedComparator) {
        super(
                new ArrayElements<E>(
                        unifiedComparator,
                        8,
                        1.5F,
                        .75F
                )
        );
    }

    public MAArrayList(int initCapacity) {
        super(
                new ArrayElements<E>(
                        null,
                        initCapacity,
                        1.5F,
                        .75F
                )
        );
    }

    public MAArrayList(int initCapacity, float expandFactor) {
        super(
                new ArrayElements<E>(
                        null,
                        initCapacity,
                        expandFactor,
                        .75F
                )
        );
    }

    public MAArrayList(
            int initCapacity,
            float expandFactor,
            Float collapseFactor) {
        super(
                new ArrayElements<E>(
                        null,
                        initCapacity,
                        expandFactor,
                        collapseFactor
                )
        );
    }

    public MAArrayList(EqualityComparator<? super E> equalityComparator, int initCapacity) {
        super(
                new ArrayElements<E>(
                        equalityComparator,
                        initCapacity,
                        1.5F,
                        .75F
                )
        );
    }

    public MAArrayList(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            float expandFactor) {
        super(
                new ArrayElements<E>(
                        equalityComparator,
                        initCapacity,
                        expandFactor,
                        .75F
                )
        );
    }

    public MAArrayList(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            float expandFactor,
            Float collapseFactor) {
        super(
                new ArrayElements<E>(
                        equalityComparator,
                        initCapacity,
                        expandFactor,
                        collapseFactor
                )
        );
    }

    public MAArrayList(Comparator<? super E> comparator, int initCapacity) {
        super(
                new ArrayElements<E>(
                        comparator,
                        initCapacity,
                        1.5F,
                        .75F
                )
        );
    }

    public MAArrayList(
            Comparator<? super E> comparator,
            int initCapacity,
            float expandFactor) {
        super(
                new ArrayElements<E>(
                        comparator,
                        initCapacity,
                        expandFactor,
                        .75F
                )
        );
    }

    public MAArrayList(
            Comparator<? super E> comparator,
            int initCapacity,
            float expandFactor,
            Float collapseFactor) {
        super(
                new ArrayElements<E>(
                        comparator,
                        initCapacity,
                        expandFactor,
                        collapseFactor
                )
        );
    }

    public MAArrayList(UnifiedComparator<? super E> unifiedComparator, int initCapacity) {
        super(
                new ArrayElements<E>(
                        unifiedComparator,
                        initCapacity,
                        1.5F,
                        .75F
                )
        );
    }

    public MAArrayList(
            UnifiedComparator<? super E> unifiedComparator,
            int initCapacity,
            float expandFactor) {
        super(
                new ArrayElements<E>(
                        unifiedComparator,
                        initCapacity,
                        expandFactor,
                        .75F
                )
        );
    }

    public MAArrayList(
            UnifiedComparator<? super E> unifiedComparator,
            int initCapacity,
            float expandFactor,
            Float collapseFactor) {
        super(
                new ArrayElements<E>(
                        unifiedComparator,
                        initCapacity,
                        expandFactor,
                        collapseFactor
                )
        );
    }

    public MAArrayList(Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        null,
                        8,
                        1.5F,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(EqualityComparator<? super E> equalityComparator, Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        equalityComparator,
                        8,
                        1.5F,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(Comparator<? super E> comparator, Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        comparator,
                        8,
                        1.5F,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(UnifiedComparator<? super E> unifiedComparator, Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        unifiedComparator,
                        8,
                        1.5F,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(int initCapacity, Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        null,
                        initCapacity,
                        1.5F,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            int initCapacity,
            float expandFactor,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        null,
                        initCapacity,
                        expandFactor,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            int initCapacity,
            float expandFactor,
            Float collapseFactor,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        null,
                        initCapacity,
                        expandFactor,
                        collapseFactor
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        equalityComparator,
                        initCapacity,
                        1.5F,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            float expandFactor,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        equalityComparator,
                        initCapacity,
                        expandFactor,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            EqualityComparator<? super E> equalityComparator,
            int initCapacity,
            float expandFactor,
            Float collapseFactor,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        equalityComparator,
                        initCapacity,
                        expandFactor,
                        collapseFactor
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            Comparator<? super E> comparator,
            int initCapacity,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        comparator,
                        initCapacity,
                        1.5F,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            Comparator<? super E> comparator,
            int initCapacity,
            float expandFactor,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        comparator,
                        initCapacity,
                        expandFactor,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            Comparator<? super E> comparator,
            int initCapacity,
            float expandFactor,
            Float collapseFactor,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        comparator,
                        initCapacity,
                        expandFactor,
                        collapseFactor
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            UnifiedComparator<? super E> unifiedComparator,
            int initCapacity,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        unifiedComparator,
                        initCapacity,
                        1.5F,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            UnifiedComparator<? super E> unifiedComparator,
            int initCapacity,
            float expandFactor,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        unifiedComparator,
                        initCapacity,
                        expandFactor,
                        .75F
                )
        );
        this.addAll(c);
    }

    public MAArrayList(
            UnifiedComparator<? super E> unifiedComparator,
            int initCapacity,
            float expandFactor,
            Float collapseFactor,
            Collection<? extends E> c) {
        super(
                new ArrayElements<E>(
                        unifiedComparator,
                        initCapacity,
                        expandFactor,
                        collapseFactor
                )
        );
        this.addAll(c);
    }

    @Override
    public MAListView<E> subList(int fromIndex, int toIndex) {
        return new SubListImpl<E>(this, fromIndex, toIndex);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeState(out);
    }
    
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.readState(in);
    }

    protected static class SubListImpl<E> extends AbstractMAList.SubListImpl<E> implements RandomAccess {

        protected SubListImpl(MAArrayList<E> parentList, int fromIndex, int toIndex) {
            super(parentList, fromIndex, toIndex);
        }
    }
    
    public static class Builder<E> {

        private EqualityComparator<? super E> equalityComparator = null;

        private Comparator<? super E> comparator = null;

        private UnifiedComparator<? super E> unifiedComparator = null;

        private int initCapacity = 8;

        private float expandFactor = 1.5F;

        private Float collapseFactor = .75F;

        public Builder<E> setEqualityComparator(EqualityComparator<? super E> equalityComparator) {
            if (equalityComparator != null) {
                if (this.comparator != null) {
                    throw new IllegalStateException(
                            LAZY_BUILDER_RESOURCE.get().conflictProperties(
                                    Builder.class,
                                    "comparator",
                                    "equalityComparator"
                            )
                    );
                }
                if (this.unifiedComparator != null) {
                    throw new IllegalStateException(
                            LAZY_BUILDER_RESOURCE.get().conflictProperties(
                                    Builder.class,
                                    "unifiedComparator",
                                    "equalityComparator"
                            )
                    );
                }
            }
            this.equalityComparator = equalityComparator;
            return this;
        }

        public Builder<E> setComparator(Comparator<? super E> comparator) {
            if (comparator != null) {
                if (this.equalityComparator != null) {
                    throw new IllegalStateException(
                            LAZY_BUILDER_RESOURCE.get().conflictProperties(
                                    Builder.class,
                                    "equalityComparator",
                                    "comparator"
                            )
                    );
                }
                if (this.unifiedComparator != null) {
                    throw new IllegalStateException(
                            LAZY_BUILDER_RESOURCE.get().conflictProperties(
                                    Builder.class,
                                    "unifiedComparator",
                                    "comparator"
                            )
                    );
                }
            }
            this.comparator = comparator;
            return this;
        }

        public Builder<E> setUnifiedComparator(UnifiedComparator<? super E> unifiedComparator) {
            if (unifiedComparator != null) {
                if (this.equalityComparator != null) {
                    throw new IllegalStateException(
                            LAZY_BUILDER_RESOURCE.get().conflictProperties(
                                    Builder.class,
                                    "equalityComparator",
                                    "unifiedComparator"
                            )
                    );
                }
                if (this.comparator != null) {
                    throw new IllegalStateException(
                            LAZY_BUILDER_RESOURCE.get().conflictProperties(
                                    Builder.class,
                                    "comparator",
                                    "unifiedComparator"
                            )
                    );
                }
            }
            this.unifiedComparator = unifiedComparator;
            return this;
        }

        public Builder<E> setInitCapacity(int initCapacity) {
            this.initCapacity = initCapacity;
            return this;
        }

        public Builder<E> setExpandFactor(float expandFactor) {
            this.expandFactor = expandFactor;
            return this;
        }

        public Builder<E> setCollapseFactor(Float collapseFactor) {
            this.collapseFactor = collapseFactor;
            return this;
        }

        public MAArrayList<E> build() {
            if (this.equalityComparator != null) {
                return new MAArrayList<E>(
                        this.equalityComparator,
                        this.initCapacity,
                        this.expandFactor,
                        this.collapseFactor
                );
            } else if (this.comparator != null) {
                return new MAArrayList<E>(
                        this.comparator,
                        this.initCapacity,
                        this.expandFactor,
                        this.collapseFactor
                );
            } else {
                return new MAArrayList<E>(
                        this.unifiedComparator,
                        this.initCapacity,
                        this.expandFactor,
                        this.collapseFactor
                );
            }
        }
    }
}
