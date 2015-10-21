package org.babyfishdemo.collections;

import junit.framework.Assert;

import org.babyfish.collection.LockMode;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.ReaderOptimizationType;
import org.babyfish.collection.XList;
import org.babyfish.lang.Action;
import org.babyfishdemo.collections.mock.MockedReadWriteLock;
import org.junit.Test;

/**
 * Before you learn this test case,
 * you'd better learn other at first.
 * 
 * BabyFish supports an interface like this
 * 
 *    package org.babyfish.lang;
 * 
 *    public interface LockDescriptor {
 *      boolean isNonFairLockSupported();
 *    }
 * 
 * It will be implementation by the collections, maps, iterators 
 * and map-entries of BabyFish Collection Framework.
 * 
 * For most implementations, this method return true, that means
 * that collection(map, iterator or map-entry) supports non-fair-lock.
 * BabyFish will used the readLock if you only use the methods with
 * ONLY read-only behaviors of that collection.
 * 
 * For few implementations, this method return false, thet means
 * that collection(map, iterator or map-entry) dose NOT support non-fair-lock.
 * BabyFish will used the WRITELock even if you only use the methods with
 * read-only behaviors of that collection.
 * 
 * Specially, the collection frameworks that are not supported BabyFish
 * may not implement this interface.
 * (1) If one class is under the package "java.util", BabyFish consider
 * it supports the non-pair-lock
 * (2) Otherwise, consider it does NOT support non-pair-lock.
 * 
 * @author Tao Chen
 */
public class NonFairLockSupportingTest {

    @Test
    public void testNonFairLockBehaviors() {
        /*
         * org.babyfish.collection.LinkedList supports 2 mode
         * (1) OPTIMIZE_READING: In this mode, "get(int)" can be optimized
         * but the non-fair-lock is not supported(This is default behavior).
         * (2) OPTIMIZE_READ_LOCK: In this mode, non-fair-lock is supported
         * by the "get(int)" can not be optimized.
         */
        XList<String> listSupportsNonFairLock = 
                new org.babyfish.collection.LinkedList<>(ReaderOptimizationType.OPTIMIZE_READ_LOCK);
        XList<String> listDoesNotSupportNonFairLock = 
                new org.babyfish.collection.LinkedList<>(ReaderOptimizationType.OPTIMIZE_READING);
        Assert.assertTrue(listSupportsNonFairLock.isNonFairLockSupported());
        Assert.assertFalse(listDoesNotSupportNonFairLock.isNonFairLockSupported());
        
        /*
         * Use locked collection to test it.
         */
        {
            StringBuilder builder = new StringBuilder();
            MACollections.locked(listSupportsNonFairLock, new MockedReadWriteLock("a", builder)).size();
            MACollections.locked(listDoesNotSupportNonFairLock, new MockedReadWriteLock("b", builder)).size();
            Assert.assertEquals(
                    // If non-fiar-lock is supported, read-only method such as "size()" uses read-lock;
                    "a.readLock().lock();a.readLock().unlock();" +
                    // otherwise, uses writeLock-lock
                    "b.writeLock().lock();b.writeLock().unlock();", 
                    builder.toString()
            );
        }
        
        /*
         * Use locking collection to test it.
         */
        {
            StringBuilder builder = new StringBuilder();
            MACollections.locking(
                    MACollections.locked(listSupportsNonFairLock, new MockedReadWriteLock("a", builder)), 
                    LockMode.READ, // Expect = read lock, Actual = read lock
                    new Action<XList<String>>() { // If environment is Java8, please use lambda
                        @Override
                        public void run(XList<String> lockingList) {
                            lockingList.size();
                        }
                    }
            );
            MACollections.locking(
                    MACollections.locked(listDoesNotSupportNonFairLock, new MockedReadWriteLock("b", builder)), 
                    LockMode.READ, // Expect = read lock, Actual = write lock
                    new Action<XList<String>>() { // If environment is Java8, please use lambda
                        @Override
                        public void run(XList<String> lockingList) {
                            lockingList.size();
                        }
                    }
            );
            Assert.assertEquals(
                    // If non-fiar-lock is supported, read-only method such as "size()" uses read-lock;
                    "a.readLock().lock();a.readLock().unlock();" +
                    // otherwise, uses writeLock-lock
                    "b.writeLock().lock();b.writeLock().unlock();", 
                    builder.toString()
            );
        }
    }
}
