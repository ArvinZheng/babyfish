package org.babyfishdemo.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Assert;

import org.babyfish.collection.LockMode;
import org.babyfish.collection.LockingObjectDisposedException;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.RequiredLockingOperationException;
import org.babyfish.collection.RequiredWritableLockingOperationException;
import org.babyfish.lang.Action;
import org.babyfish.lang.Func;
import org.babyfish.lang.IllegalThreadException;
import org.babyfish.lang.Ref;
import org.babyfishdemo.collections.mock.MockedReadWriteLock;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class LockPropagationTest {

    @Test
    public void testSimplePropagation() {
        
        StringBuilder builder = new StringBuilder();
        final List<Integer> lockedList = MACollections.locked(
                new java.util.ArrayList<Integer>(), 
                new MockedReadWriteLock("rwl", builder)
        );
        
        lockedList.add(32); //Lock and unlock
        lockedList.add(29); //Lock and unlock
        
        Assert.assertEquals(
                "rwl.writeLock().lock();rwl.writeLock().unlock();" +
                "rwl.writeLock().lock();rwl.writeLock().unlock();", 
                builder.toString());
        
        // Clear the StringBuilder
        builder.setLength(0);
        
        String str = MACollections.locking( // Lock and unlock ONLY ONCE
                lockedList, 
                LockMode.WRITE, 
                new Func<List<Integer>, String>() { // If environment is Java8, please use lambda
                    @Override
                    public String run(List<Integer> lockingList) {
                        Assert.assertNotSame(lockedList, lockingList);
                        // 4 operations without lock(MAcollections.locking has done it):
                        // (1) Invoke lockingList.get(int) for twice
                        // (2) Invoke lockingList.add() for once
                        // (3) Invoke lockingList.toString() for once
                        // These 4 operations do NOT use the lock :)
                        lockingList.add(lockingList.get(0) + lockingList.get(1));
                        return lockingList.toString();
                    }
                }
        );
        Assert.assertEquals("rwl.writeLock().lock();rwl.writeLock().unlock();", builder.toString());
        Assert.assertEquals("[32, 29, 61]", str);
    }
    
    @Test
    public void testWriteFailedInReadingLockingScope() {
        final StringBuilder builder = new StringBuilder();
        final Map<String, Integer> lockedMap = MACollections.locked(
                new java.util.HashMap<String, Integer>(), 
                new MockedReadWriteLock("rwl", builder)
        );
        
        try {
            MACollections.locking(
                    lockedMap,
                    LockMode.READ, //This locking scope uses readLock
                    new Action<Map<String, Integer>>() { // If environment is Java8, please use lambda
                        @Override
                        public void run(Map<String, Integer> lockingMap) {
                            Assert.assertNotSame(lockedMap, lockingMap);
                            builder.append("before read lockingList;");
                            lockingMap.keySet().size();
                            builder.append("after read lockingList;");
                            builder.append("before write lockingList;");
                            lockingMap.keySet().clear();
                            builder.append("after write lockingList;");
                        }
                    }
            );
            Assert.fail(RequiredWritableLockingOperationException.class.getName() + " is required");
        } catch (RequiredWritableLockingOperationException ex) {
            builder.append(RequiredWritableLockingOperationException.class.getName() + " raised;");
        }
        
        Assert.assertEquals(
                "rwl.readLock().lock();" +
                "before read lockingList;" +
                "after read lockingList;" +
                "before write lockingList;" +
                "rwl.readLock().unlock();" +
                RequiredWritableLockingOperationException.class.getName() + " raised;", 
                builder.toString()
        );
    }
    
    @Test
    public void testIteratorRequiresTheLockingScope() {
        final Collection<Integer> lockedCollection = MACollections.locked(new java.util.LinkedHashSet<Integer>());
        lockedCollection.add(109);
        lockedCollection.add(53);
        lockedCollection.add(-65);
        lockedCollection.add(72);
        
        /*
         * For "java.util.Collections.synchronizedXXX(...)", the user must 
         * manually synchronize on the returned collection when iterating over it.
         * If this point is forgot by user, a bug will be appear.
         * 
         * Be different with it, the returned collection of "MACollections.locked(...)" 
         * doesn't support the original iterator.
         */
        try {
            lockedCollection.iterator().next();
            Assert.fail(RequiredLockingOperationException.class + "");
        } catch (RequiredLockingOperationException ex) {
            /*
             * Though you can create an iterator, but the exception will be 
             * raised if you use any method of it("next" in this example).
             */
        }
        
        /*
         * In order to avoid the RequiredLockingOperationException,
         * You have 2 choices
         */
        
        /*
         * The first choice is to use the locking iterator of locked collection.
         */
        int sum = MACollections.locking(
                lockedCollection.iterator(), 
                LockMode.READ, 
                new Func<Iterator<Integer>, Integer>() {
                    @Override
                    public Integer run(Iterator<Integer> lockingIterator) { // If environment is Java8, please use lambda
                        int sum = 0;
                        while (lockingIterator.hasNext()) {
                            Integer e = lockingIterator.next();
                            if (e != null) {
                                sum += e;
                            }
                        }
                        return sum; 
                    } // If environment is Java8, please use lambda
                }
        ); 
        Assert.assertEquals(169, sum);
        
        /*
         * The second choice is to use the locking collection.
         * A benefit of this choice is you can use the java5 for-each statement
         * when you will not use the "iterator.remove".
         */
        sum = MACollections.locking(
                lockedCollection, 
                LockMode.READ, 
                new Func<Collection<Integer>, Integer>() {
                    @Override
                    public Integer run(Collection<Integer> lockingCollection) {
                        int sum = 0;
                        for (Integer e : lockingCollection) {
                            if (e != null) {
                                sum += e;
                            }
                        }
                        return sum;
                    } // If environment is Java8, please use lambda
                }
        );
        Assert.assertEquals(169, sum);
    }
    
    @Test
    public void testLockingObjectCanNotBeUsedByOtherThread() throws InterruptedException {
        final NavigableSet<String> lockedNavigableSet = MACollections.locked(new java.util.TreeSet<String>());
        final AtomicReference<Throwable> throwableRef = new AtomicReference<>();
        final Semaphore semaphore = new Semaphore(0);
        MACollections.locking(
                lockedNavigableSet, 
                LockMode.WRITE, 
                new Action<NavigableSet<String>>() { // If environment is Java8, please use lambda
                    @Override
                    public void run(final NavigableSet<String> lockingNavigableSet) {
                        Assert.assertNotSame(lockedNavigableSet, lockingNavigableSet);
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    lockingNavigableSet.clear();
                                } catch (Throwable ex) {
                                    throwableRef.set(ex);
                                }
                                semaphore.release();
                            }
                        }
                        .start();
                    }
                }
        );
        semaphore.acquire();
        Assert.assertSame(IllegalThreadException.class, throwableRef.get().getClass());
    }
    
    @Test
    public void testLockingObjectCanNotBeUsedAfterLockingScope() {
        final MAList<String> lockedMAList = MACollections.locked(new org.babyfish.collection.MAArrayList<String>());
        final Ref<MAList<String>> lockingMAListRef = new Ref<>();
        MACollections.locking(
                lockedMAList, 
                LockMode.WRITE, 
                new Action<MAList<String>>() { // If environment is Java8, please use lambda
                    @Override
                    public void run(final MAList<String> lockingMAList) {
                        Assert.assertNotSame(lockedMAList, lockingMAList);
                        lockingMAListRef.set(lockingMAList);
                    }
                }
        );
        
        // After the locking scope, the lockingList is disposed. 
        // You can't not call any method of disposed object.
        
        try {
            lockingMAListRef.get().isEmpty();
            Assert.fail(LockingObjectDisposedException.class.getName() + " is expected");
        } catch (LockingObjectDisposedException ex) {
            
        }
        /*
         * Uses Junit @Test(expect = LockingObjectDisposedException.class)
         * can only assert the current test method should be failed,
         * but can not test which statement throws the exception.
         * So I never use @Test(expect = ...)
         */     
    }
    
    @Test
    public void testLockingNonLockedObject() {
        /*
         * If the collection(or map, iterator, map-entry) is not locked,
         * you still can do the "locking" operation on it successfully.
         * 
         * But, this is useless, nothing will happen. Just make your program can run.
         */
        final List<String> nonLockedList = new java.util.ArrayList<String>();
        MACollections.locking(
                nonLockedList, 
                LockMode.WRITE, 
                new Action<List<String>>() { // If environment is Java8, please use lambda
                    @Override
                    public void run(final List<String> lockingList) {
                        // lockingList is the original list itself, nothing will happed
                        // This is useless programming
                        Assert.assertSame(nonLockedList, lockingList);
                    }
                }
        );
    }
}
