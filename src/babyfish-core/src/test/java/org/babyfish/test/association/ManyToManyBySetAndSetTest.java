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
package org.babyfish.test.association;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.association.AssociatedSet;
import org.babyfish.collection.MAHashSet;
import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ManyToManyBySetAndSetTest {
    
    private static final TestSite.CourseSetFactory[] COURSE_SET_FACTORIES =
        new TestSite.CourseSetFactory[] {
            new TestSite.CourseSetFactory() {
                @Override
                public MASet<TestSite.Course> createCourseSet() {
                    return new MAHashSet<TestSite.Course>();
                }
            },
            new TestSite.CourseSetFactory() {
                @Override
                public MASet<TestSite.Course> createCourseSet() {
                    return new MALinkedHashSet<TestSite.Course>();
                }
            },
            new TestSite.CourseSetFactory() {
                @Override
                public MASet<TestSite.Course> createCourseSet() {
                    return new MATreeSet<TestSite.Course>();
                }
            }
        };
    
    private static final TestSite.StudentSetFactory[] STUDENT_SET_FACTORIES =
        new TestSite.StudentSetFactory[] {
            new TestSite.StudentSetFactory() {
                @Override
                public MASet<TestSite.Student> createStudentSet() {
                    return new MAHashSet<TestSite.Student>();
                }
            },
            new TestSite.StudentSetFactory() {
                @Override
                public MASet<TestSite.Student> createStudentSet() {
                    return new MALinkedHashSet<TestSite.Student>();
                }
            },
            new TestSite.StudentSetFactory() {
                @Override
                public MASet<TestSite.Student> createStudentSet() {
                    return new MATreeSet<TestSite.Student>();
                }
            }
        };
    
    private TestSite[] testSites;
    
    @Before
    public void initalize() {
        TestSite[] sites = 
            new TestSite[COURSE_SET_FACTORIES.length * STUDENT_SET_FACTORIES.length];
        int index = 0;
        for (int i = 0; i < COURSE_SET_FACTORIES.length; i++) {
            for (int ii = 0; ii < STUDENT_SET_FACTORIES.length; ii++) {
                sites[index++] = new TestSite(COURSE_SET_FACTORIES[i], STUDENT_SET_FACTORIES[ii]);
            }
        }
        this.testSites = sites;
    }
    
    @Test
    public void testModifyStudent() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyStudent();
        }
    }
    
    @Test
    public void testModifyStudentByIterator() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyStudentByIterator();
        }
    }
    
    @Test
    public void testModifyCourse() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyCourse();
        }
    }
    
    @Test
    public void testModifyCourseByIterator() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyCourseByIterator();
        }
    }
    
    private static class TestSite {
        
        private CourseSetFactory courseSetFactory;
        
        private StudentSetFactory studentSetFactory;
        
        TestSite(
                CourseSetFactory courseSetFactory,
                StudentSetFactory studentSetFactory) {
            this.courseSetFactory = courseSetFactory;
            this.studentSetFactory = studentSetFactory;
        }

        interface CourseSetFactory {
            MASet<Course> createCourseSet();
        }
        
        interface StudentSetFactory {
            MASet<Student> createStudentSet();
        }
            
        void testModifyStudent() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            assertSet(student1.getCourses());
            assertSet(course1.getStudents());
            assertSet(student2.getCourses());
            assertSet(course2.getStudents());
            
            student1.getCourses().add(course1);
            assertSet(student1.getCourses(), course1);
            assertSet(course1.getStudents(), student1);
            assertSet(student2.getCourses());
            assertSet(course2.getStudents());
            
            student1.getCourses().add(course2);
            assertSet(student1.getCourses(), course1, course2);
            assertSet(course1.getStudents(), student1);
            assertSet(student2.getCourses());
            assertSet(course2.getStudents(), student1);
            
            student2.getCourses().add(course1);
            assertSet(student1.getCourses(), course1, course2);
            assertSet(course1.getStudents(), student1, student2);
            assertSet(student2.getCourses(), course1);
            assertSet(course2.getStudents(), student1);
            
            student2.getCourses().add(course2);
            assertSet(student1.getCourses(), course1, course2);
            assertSet(course1.getStudents(), student1, student2);
            assertSet(student2.getCourses(), course1, course2);
            assertSet(course2.getStudents(), student1, student2);
            
            student2.getCourses().remove(course2);
            assertSet(student1.getCourses(), course1, course2);
            assertSet(course1.getStudents(), student1, student2);
            assertSet(student2.getCourses(), course1);
            assertSet(course2.getStudents(), student1);
            
            student2.getCourses().remove(course1);
            assertSet(student1.getCourses(), course1, course2);
            assertSet(course1.getStudents(), student1);
            assertSet(student2.getCourses());
            assertSet(course2.getStudents(), student1);
            
            student1.getCourses().remove(course2);
            assertSet(student1.getCourses(), course1);
            assertSet(course1.getStudents(), student1);
            assertSet(student2.getCourses());
            assertSet(course2.getStudents());
            
            Iterator<Course> itr = student1.getCourses().iterator();
            itr.next();
            itr.remove();
            assertSet(student1.getCourses());
            assertSet(course1.getStudents());
            assertSet(student2.getCourses());
            assertSet(course2.getStudents());
        }
        
        void testModifyStudentByIterator() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertSet(student1.getCourses());
            AssociationTests.assertSet(course1.getStudents());
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents());
            
            student1.getCourses().add(course1);
            AssociationTests.assertSet(student1.getCourses(), course1);
            AssociationTests.assertSet(course1.getStudents(), student1);
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents());
            
            student1.getCourses().add(course2);
            AssociationTests.assertSet(student1.getCourses(), course1, course2);
            AssociationTests.assertSet(course1.getStudents(), student1);
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents(), student1);
            
            student2.getCourses().add(course1);
            AssociationTests.assertSet(student1.getCourses(), course1, course2);
            AssociationTests.assertSet(course1.getStudents(), student1, student2);
            AssociationTests.assertSet(student2.getCourses(), course1);
            AssociationTests.assertSet(course2.getStudents(), student1);
            
            student2.getCourses().add(course2);
            AssociationTests.assertSet(student1.getCourses(), course1, course2);
            AssociationTests.assertSet(course1.getStudents(), student1, student2);
            AssociationTests.assertSet(student2.getCourses(), course1, course2);
            AssociationTests.assertSet(course2.getStudents(), student1, student2);
            
            AssociationTests.getIteratorAndMoveTo(student2.getCourses(), course2).remove();
            AssociationTests.assertSet(student1.getCourses(), course1, course2);
            AssociationTests.assertSet(course1.getStudents(), student1, student2);
            AssociationTests.assertSet(student2.getCourses(), course1);
            AssociationTests.assertSet(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(student2.getCourses(), course1).remove();
            AssociationTests.assertSet(student1.getCourses(), course1, course2);
            AssociationTests.assertSet(course1.getStudents(), student1);
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(student1.getCourses(), course2).remove();
            AssociationTests.assertSet(student1.getCourses(), course1);
            AssociationTests.assertSet(course1.getStudents(), student1);
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents());
            
            AssociationTests.getIteratorAndMoveTo(student1.getCourses(), course1).remove();
            AssociationTests.assertSet(student1.getCourses());
            AssociationTests.assertSet(course1.getStudents());
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents());
        }
        
        void testModifyCourse() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            assertSet(student1.getCourses());
            assertSet(course1.getStudents());
            assertSet(student2.getCourses());
            assertSet(course2.getStudents());
            
            course1.getStudents().add(student1);
            assertSet(student1.getCourses(), course1);
            assertSet(course1.getStudents(), student1);
            assertSet(student2.getCourses());
            assertSet(course2.getStudents());
            
            course2.getStudents().add(student1);
            assertSet(student1.getCourses(), course1, course2);
            assertSet(course1.getStudents(), student1);
            assertSet(student2.getCourses());
            assertSet(course2.getStudents(), student1);
            
            course1.getStudents().add(student2);
            assertSet(student1.getCourses(), course1, course2);
            assertSet(course1.getStudents(), student1, student2);
            assertSet(student2.getCourses(), course1);
            assertSet(course2.getStudents(), student1);
            
            course2.getStudents().add(student2);
            assertSet(student1.getCourses(), course1, course2);
            assertSet(course1.getStudents(), student1, student2);
            assertSet(student2.getCourses(), course1, course2);
            assertSet(course2.getStudents(), student1, student2);
            
            course2.getStudents().remove(student2);
            assertSet(student1.getCourses(), course1, course2);
            assertSet(course1.getStudents(), student1, student2);
            assertSet(student2.getCourses(), course1);
            assertSet(course2.getStudents(), student1);
            
            course1.getStudents().remove(student2);
            assertSet(student1.getCourses(), course1, course2);
            assertSet(course1.getStudents(), student1);
            assertSet(student2.getCourses());
            assertSet(course2.getStudents(), student1);
            
            course2.getStudents().remove(student1);
            assertSet(student1.getCourses(), course1);
            assertSet(course1.getStudents(), student1);
            assertSet(student2.getCourses());
            assertSet(course2.getStudents());
            
            Iterator<Student> itr = course1.getStudents().iterator();
            itr.next();
            itr.remove();
            assertSet(student1.getCourses());
            assertSet(course1.getStudents());
            assertSet(student2.getCourses());
            assertSet(course2.getStudents());
        }
        
        void testModifyCourseByIterator() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertSet(student1.getCourses());
            AssociationTests.assertSet(course1.getStudents());
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents());
            
            course1.getStudents().add(student1);
            AssociationTests.assertSet(student1.getCourses(), course1);
            AssociationTests.assertSet(course1.getStudents(), student1);
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents());
            
            course2.getStudents().add(student1);
            AssociationTests.assertSet(student1.getCourses(), course1, course2);
            AssociationTests.assertSet(course1.getStudents(), student1);
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents(), student1);
            
            course1.getStudents().add(student2);
            AssociationTests.assertSet(student1.getCourses(), course1, course2);
            AssociationTests.assertSet(course1.getStudents(), student1, student2);
            AssociationTests.assertSet(student2.getCourses(), course1);
            AssociationTests.assertSet(course2.getStudents(), student1);
            
            course2.getStudents().add(student2);
            AssociationTests.assertSet(student1.getCourses(), course1, course2);
            AssociationTests.assertSet(course1.getStudents(), student1, student2);
            AssociationTests.assertSet(student2.getCourses(), course1, course2);
            AssociationTests.assertSet(course2.getStudents(), student1, student2);
            
            AssociationTests.getIteratorAndMoveTo(course2.getStudents(), student2).remove();
            AssociationTests.assertSet(student1.getCourses(), course1, course2);
            AssociationTests.assertSet(course1.getStudents(), student1, student2);
            AssociationTests.assertSet(student2.getCourses(), course1);
            AssociationTests.assertSet(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(course1.getStudents(), student2).remove();
            AssociationTests.assertSet(student1.getCourses(), course1, course2);
            AssociationTests.assertSet(course1.getStudents(), student1);
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(course2.getStudents(), student1).remove();
            AssociationTests.assertSet(student1.getCourses(), course1);
            AssociationTests.assertSet(course1.getStudents(), student1);
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents());
            
            AssociationTests.getIteratorAndMoveTo(course1.getStudents(), student1).remove();
            AssociationTests.assertSet(student1.getCourses());
            AssociationTests.assertSet(course1.getStudents());
            AssociationTests.assertSet(student2.getCourses());
            AssociationTests.assertSet(course2.getStudents());
        }
        
        @SuppressWarnings("unchecked")
        private static <E> void assertSet(Set<E> set, E ... elements) {
            Set<E> other = new HashSet<E>();
            for (E element : elements) {
                other.add(element);
            }
            Assert.assertEquals(other, set);
        }
    
        class Student implements Comparable<Student> {
        
            private final int id = AssociationTests.nextValOfSequence();
            
            private AssociatedSet<Student, Course> courses = 
                new AssociatedSet<Student, Course>() {
                    
                    private static final long serialVersionUID = 7187336509940801621L;

                    public Student getOwner() {
                        return Student.this;
                    }
    
                    @Override
                    protected AssociatedEndpointType onGetOppositeEndpointType() {
                        return AssociatedEndpointType.SET;
                    }

                    @Override
                    public AssociatedEndpoint<Course, Student> onGetOppositeEndpoint(Course opposite) {
                        return opposite.students;
                    }
    
                    @Override
                    protected RootData<Course> createRootData() {
                        return new RootData<Course>() {

                            private static final long serialVersionUID = 9179304985423303782L;

                            @Override
                            protected MASet<Course> createDefaultBase(
                                    UnifiedComparator<? super Course> unifiedComparator) {
                                return TestSite.this.courseSetFactory.createCourseSet();
                            }
                            
                        };
                    }

                };
                
            Set<Course> getCourses() {
                return this.courses;
            }
    
            @Override
            public int compareTo(Student o) {
                return this.id - o.id;
            }
            
        }
        
        class Course implements Comparable<Course> {
            
            private final int id = AssociationTests.nextValOfSequence();
            
            private AssociatedSet<Course, Student> students = 
                new AssociatedSet<Course, Student>() {
                    
                    private static final long serialVersionUID = 2943425674790550393L;

                    public Course getOwner() {
                        return Course.this;
                    }
                    
                    @Override
                    protected AssociatedEndpointType onGetOppositeEndpointType() {
                        return AssociatedEndpointType.SET;
                    }

                    @Override
                    public AssociatedEndpoint<Student, Course> onGetOppositeEndpoint(Student opposite) {
                        return opposite.courses;
                    }
                    
                    @Override
                    protected RootData<Student> createRootData() {
                        return new RootData<Student>() {

                            private static final long serialVersionUID = 8346284764800166818L;

                            @Override
                            protected MASet<Student> createDefaultBase(
                                    UnifiedComparator<? super Student> unifiedComparator) {
                                return TestSite.this.studentSetFactory.createStudentSet();
                            }
                            
                        };
                    }
                };
                
            Set<Student> getStudents() {
                return this.students;
            }
    
            @Override
            public int compareTo(Course o) {
                return this.id - o.id;
            }
            
        }
    
    }
    
}
