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
package org.babyfish.test.hibernate.model;

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.babyfish.hibernate.association.EntityReferenceComparator;
import org.babyfish.hibernate.cfg.Configuration;
import org.babyfish.lang.Action;
import org.babyfish.lang.Func;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * @author Tao Chen
 */
@SuppressWarnings("deprecation")
public class AbstractHibernateTest {

    private static SessionFactory sessionFactory;
    
    static {
        Configuration cfg = new Configuration();
        cfg.configure(
                AbstractHibernateTest
                .class
                .getPackage()
                .getName()
                .replace('.', '/') +
                "/hibernate.cfg.xml");
        sessionFactory = cfg.buildSessionFactory();
    }
    
    protected static void execute(Action<Session> handler) {
        Session session = sessionFactory.openSession();
        try {
            Transaction tx = session.beginTransaction();
            try {
                handler.run(session);
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            } catch (Error err) {
                tx.rollback();
                throw err;
            }
            tx.commit();
        } finally {
            session.close();
        }
    }
    
    protected static <T> T execute(Func<Session, T> handler) {
        T result;
        Session session = sessionFactory.openSession();
        try {
            Transaction tx = session.beginTransaction();
            try {
                result = handler.run(session);
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            } catch (Error err) {
                tx.rollback();
                throw err;
            }
            tx.commit();
        } finally {
            session.close();
        }
        return result;
    }
    
    protected static <T> void assertCollection(Collection<T> c) {
        Assert.assertTrue(c.isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    protected static <T> void assertCollection(Collection<T> c, T ... elements) {
        Assert.assertEquals(elements.length, c.size());
        if (c instanceof List<?>) {
            int index = 0;
            for (T o : c) {
                Assert.assertTrue(EntityReferenceComparator.getInstance().same(elements[index++], o));
            }
        } else {
            for (T expected : elements) {
                boolean matched = false;
                for (T actual : c) {
                    if (EntityReferenceComparator.getInstance().same(expected, actual)) {
                        matched = true;
                        break;
                    }
                }
                Assert.assertTrue(matched);
            }
        }
    }
    
    protected static <T> void assertReference(T ref) {
        Assert.assertNull(ref);
    }
    
    protected static <T> void assertReference(T ref, T expected) {
        if (expected == null) {
            Assert.assertNull(ref);
            return;
        }
        Assert.assertNotNull(ref);
        Assert.assertTrue(EntityReferenceComparator.getInstance().same(ref, expected));
    }
    
}
