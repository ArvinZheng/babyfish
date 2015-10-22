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
package org.babyfish.collection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;

import org.babyfish.collection.spi.AbstractXList;
import org.babyfish.collection.spi.base.LinkedElements;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class LinkedList<E> extends AbstractXList<E> implements Serializable {

    private static final long serialVersionUID = 7877161748067929127L;
    
    private static final LazyResource<BuilderResource> LAZY_BUILDER_RESOURCE = LazyResource.of(BuilderResource.class);
    
    public LinkedList() {
        super(
                new LinkedElements<E>(
                        ReaderOptimizationType.OPTIMIZE_READING,
                        null
                )
        );
    }

    public LinkedList(ReaderOptimizationType readerOptimizationType) {
        super(
                new LinkedElements<E>(
                        readerOptimizationType,
                        null
                )
        );
    }

    public LinkedList(EqualityComparator<? super E> equalityComparator) {
        super(
                new LinkedElements<E>(
                        ReaderOptimizationType.OPTIMIZE_READING,
                        equalityComparator
                )
        );
    }

    public LinkedList(Comparator<? super E> comparator) {
        super(
                new LinkedElements<E>(
                        ReaderOptimizationType.OPTIMIZE_READING,
                        comparator
                )
        );
    }

    public LinkedList(UnifiedComparator<? super E> unifiedComparator) {
        super(
                new LinkedElements<E>(
                        ReaderOptimizationType.OPTIMIZE_READING,
                        unifiedComparator
                )
        );
    }

    public LinkedList(ReaderOptimizationType readerOptimizationType, EqualityComparator<? super E> equalityComparator) {
        super(
                new LinkedElements<E>(
                        readerOptimizationType,
                        equalityComparator
                )
        );
    }

    public LinkedList(ReaderOptimizationType readerOptimizationType, Comparator<? super E> comparator) {
        super(
                new LinkedElements<E>(
                        readerOptimizationType,
                        comparator
                )
        );
    }

    public LinkedList(ReaderOptimizationType readerOptimizationType, UnifiedComparator<? super E> unifiedComparator) {
        super(
                new LinkedElements<E>(
                        readerOptimizationType,
                        unifiedComparator
                )
        );
    }

    public LinkedList(Collection<? extends E> c) {
        super(
                new LinkedElements<E>(
                        ReaderOptimizationType.OPTIMIZE_READING,
                        null
                )
        );
        this.addAll(c);
    }

    public LinkedList(ReaderOptimizationType readerOptimizationType, Collection<? extends E> c) {
        super(
                new LinkedElements<E>(
                        readerOptimizationType,
                        null
                )
        );
        this.addAll(c);
    }

    public LinkedList(EqualityComparator<? super E> equalityComparator, Collection<? extends E> c) {
        super(
                new LinkedElements<E>(
                        ReaderOptimizationType.OPTIMIZE_READING,
                        equalityComparator
                )
        );
        this.addAll(c);
    }

    public LinkedList(Comparator<? super E> comparator, Collection<? extends E> c) {
        super(
                new LinkedElements<E>(
                        ReaderOptimizationType.OPTIMIZE_READING,
                        comparator
                )
        );
        this.addAll(c);
    }

    public LinkedList(UnifiedComparator<? super E> unifiedComparator, Collection<? extends E> c) {
        super(
                new LinkedElements<E>(
                        ReaderOptimizationType.OPTIMIZE_READING,
                        unifiedComparator
                )
        );
        this.addAll(c);
    }

    public LinkedList(
            ReaderOptimizationType readerOptimizationType,
            EqualityComparator<? super E> equalityComparator,
            Collection<? extends E> c) {
        super(
                new LinkedElements<E>(
                        readerOptimizationType,
                        equalityComparator
                )
        );
        this.addAll(c);
    }

    public LinkedList(
            ReaderOptimizationType readerOptimizationType,
            Comparator<? super E> comparator,
            Collection<? extends E> c) {
        super(
                new LinkedElements<E>(
                        readerOptimizationType,
                        comparator
                )
        );
        this.addAll(c);
    }

    public LinkedList(
            ReaderOptimizationType readerOptimizationType,
            UnifiedComparator<? super E> unifiedComparator,
            Collection<? extends E> c) {
        super(
                new LinkedElements<E>(
                        readerOptimizationType,
                        unifiedComparator
                )
        );
        this.addAll(c);
    }

    public ReaderOptimizationType readerOptimizationType() {
        return this.<LinkedElements<E>>getBaseElements().readerOptimizationType();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeState(out);
    }
    
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.readState(in);
    }
    
    public static class Builder<E> {

        private ReaderOptimizationType readerOptimizationType = ReaderOptimizationType.OPTIMIZE_READING;

        private EqualityComparator<? super E> equalityComparator = null;

        private Comparator<? super E> comparator = null;

        private UnifiedComparator<? super E> unifiedComparator = null;

        public Builder<E> setReaderOptimizationType(ReaderOptimizationType readerOptimizationType) {
            this.readerOptimizationType = readerOptimizationType != null ? readerOptimizationType : ReaderOptimizationType.OPTIMIZE_READING;
            return this;
        }

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

        public LinkedList<E> build() {
            if (this.equalityComparator != null) {
                return new LinkedList<E>(
                        this.readerOptimizationType,
                        this.equalityComparator
                );
            } else if (this.comparator != null) {
                return new LinkedList<E>(
                        this.readerOptimizationType,
                        this.comparator
                );
            } else {
                return new LinkedList<E>(
                        this.readerOptimizationType,
                        this.unifiedComparator
                );
            }
        }
    }
}
