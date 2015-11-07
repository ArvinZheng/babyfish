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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.babyfish.association.AssociatedCollection;
import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.association.AssociatedMap;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.MAHashMap;
import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MALinkedList;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.hibernate.collection.type.MAListType;
import org.babyfish.hibernate.collection.type.MANavigableMapType;
import org.babyfish.hibernate.collection.type.MAOrderedMapType;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ManyToManyByMapAndBagTest {
    
    private static final TestSite.CourseMapFactory[] COURSE_MAP_FACTORIES =
        new TestSite.CourseMapFactory[] {
            new TestSite.CourseMapFactory() {
                @Override
                public MAMap<String, TestSite.Course> createCourseMap() {
                    return new MAHashMap<String, TestSite.Course>();
                }
            },
            new TestSite.CourseMapFactory() {
                @Override
                public MAMap<String, TestSite.Course> createCourseMap() {
                    return new MALinkedHashMap<String, TestSite.Course>();
                }
            },
            new TestSite.CourseMapFactory() {
                @Override
                public MAMap<String, TestSite.Course> createCourseMap() {
                    return new MATreeMap<String, TestSite.Course>();
                }
            },
            new TestSite.CourseMapFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public MAMap<String, TestSite.Course> createCourseMap() {
                    return (MAMap<String, org.babyfish.test.association.ManyToManyByMapAndBagTest.TestSite.Course>) new MAOrderedMapType().wrap(null, new MALinkedHashMap<String, TestSite.Course>());
                }
            },
            new TestSite.CourseMapFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public MAMap<String, TestSite.Course> createCourseMap() {
                    return (MAMap<String, org.babyfish.test.association.ManyToManyByMapAndBagTest.TestSite.Course>) new MANavigableMapType().wrap(null, new MATreeMap<String, TestSite.Course>());
                }
            },
        };
    
    private static final TestSite.StudentBagFactory[] STUDENT_BAG_FACTORIES =
        new TestSite.StudentBagFactory[] {
            new TestSite.StudentBagFactory() {
                @Override
                public MAList<TestSite.Student> createStudentBag() {
                    return new MAArrayList<TestSite.Student>();
                }
            },
            new TestSite.StudentBagFactory() {
                @Override
                public MAList<TestSite.Student> createStudentBag() {
                    return new MALinkedList<TestSite.Student>();
                }
            },
            new TestSite.StudentBagFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public MAList<TestSite.Student> createStudentBag() {
                    return (MAList<org.babyfish.test.association.ManyToManyByMapAndBagTest.TestSite.Student>) new MAListType().wrap(null, new MAArrayList<TestSite.Student>());
                }
            },
            new TestSite.StudentBagFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public MAList<TestSite.Student> createStudentBag() {
                    return (MAList<org.babyfish.test.association.ManyToManyByMapAndBagTest.TestSite.Student>) new MAListType().wrap(null, new MALinkedList<TestSite.Student>());
                }
            },
        };
    
    private TestSite[] testSites;
    
    @Before
    public void initalize() {
        TestSite[] sites = 
            new TestSite[COURSE_MAP_FACTORIES.length * STUDENT_BAG_FACTORIES.length];
        int index = 0;
        for (int i = 0; i < COURSE_MAP_FACTORIES.length; i++) {
            for (int ii = 0; ii < STUDENT_BAG_FACTORIES.length; ii++) {
                sites[index++] = new TestSite(COURSE_MAP_FACTORIES[i], STUDENT_BAG_FACTORIES[ii]);
            }
        }
        this.testSites = sites;
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testAddByCourse() {
        for (TestSite testSite : this.testSites) {
            testSite.testAddByCourse();
        }
    } 
    
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllByCourse() {
        for (TestSite testSite : this.testSites) {
            testSite.testAddAllByCourse();
        }
    }
    
    @Test
    public void testModifyStudentByKey() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyStudentByKey();
        }
    }
    
    @Test
    public void testModifyStudentByValue() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyStudentByValue();
        }
    }
    
    @Test
    public void testModifyStudentByIterator() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyStudentByIterator();
        }
    }
    
    @Test
    public void testModifyStudentByEntry() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyStudentByEntry();
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
        
        private CourseMapFactory courseMapFactory;
        
        private StudentBagFactory studentBagFactory;
        
        TestSite(
                CourseMapFactory courseMapFactory,
                StudentBagFactory studentBagFactory) {
            this.courseMapFactory = courseMapFactory;
            this.studentBagFactory = studentBagFactory;
        }

        interface CourseMapFactory {
            MAMap<String, Course> createCourseMap();
        }
        
        interface StudentBagFactory {
            MAList<Student> createStudentBag();
        }
        
        void testAddByCourse() {
            Student student = new Student();
            Course course = new Course();
            course.getStudents().add(student);
        }
        
        void testAddAllByCourse() {
            Student student = new Student();
            Course course = new Course();
            List<Student> students = new ArrayList<Student>();
            students.add(student);
            course.getStudents().addAll(students);
        }
        
        void testModifyStudentByKey() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertMap(student1.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(student2.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0],
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            student2.getCourses().remove("second-course");
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().remove("first-course");
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student1.getCourses().remove("second-course");
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().remove("first-course");
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
        }
        
        void testModifyStudentByValue() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertMap(student1.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(student2.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0],
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            student2.getCourses().values().remove(course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().values().remove(course1);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student1.getCourses().values().remove(course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().values().remove(course1);
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
        }
        
        void testModifyStudentByIterator() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertMap(student1.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(student2.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0],
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            AssociationTests.getIteratorAndMoveTo(student2.getCourses(), "second-course").remove();
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(student2.getCourses(), "first-course").remove();
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(student1.getCourses(), "second-course").remove();
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            AssociationTests.getIteratorAndMoveTo(student1.getCourses(), "first-course").remove();
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
        }
        
        void testModifyStudentByEntry() {
            
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertMap(student1.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(student2.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0],
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            AssociationTests.getEntry(student2.getCourses(), "first-course").setValue(course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course2 });
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            AssociationTests.getEntry(student1.getCourses(), "second-course").setValue(course1);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "second-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course2 });
            AssociationTests.assertCollection(course2.getStudents(), student2);
        }
        
        void testModifyCourse() {
            
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertMap(student1.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(student2.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0],
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            course2.getStudents().remove(student2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            course1.getStudents().remove(student2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            course2.getStudents().remove(student1);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            course1.getStudents().remove(student1);
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
        }
        
        void testModifyCourseByIterator() {
            
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertMap(student1.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(student2.getCourses(), new String[0], new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0],
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("first-course", course1);
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().put("second-course", course2);
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            AssociationTests.getIteratorAndMoveTo(course2.getStudents(), student2).remove();
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertMap(
                    student2.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(course1.getStudents(), student2).remove();
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course", "second-course" }, 
                    new Course[] { course1, course2 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(course2.getStudents(), student1).remove();
            AssociationTests.assertMap(
                    student1.getCourses(), 
                    new String[] { "first-course" }, 
                    new Course[] { course1 });
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
            
            AssociationTests.getIteratorAndMoveTo(course1.getStudents(), student1).remove();
            AssociationTests.assertMap(
                    student1.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertMap(
                    student2.getCourses(),
                    new String[0], 
                    new Course[0]);
            AssociationTests.assertCollection(course2.getStudents());
        }
    
        class Student {
            
            private AssociatedMap<Student, String, Course> courses = 
                
                new AssociatedMap<Student, String, Course>() {
    
                    private static final long serialVersionUID = 6853904610079659839L;

                    public Student getOwner() {
                        return Student.this;
                    }
                    
                    @Override
                    protected AssociatedEndpointType onGetOppositeEndpointType() {
                        return AssociatedEndpointType.COLLECTION;
                    }

                    @Override
                    public AssociatedEndpoint<Course, Student> onGetOppositeEndpoint(
                            Course opposite) {
                        return opposite.students;
                    }
    
                    @Override
                    protected RootData<String, Course> createRootData() {
                        return new RootData<String, Course>() {
                            
                            private static final long serialVersionUID = 7185962265624614911L;

                            @Override
                            protected MAMap<String, Course> createDefaultBase(
                                    UnifiedComparator<? super String> keyUnifiedComparator,
                                    UnifiedComparator<? super Course> valueUnifiedComparator) {
                                return TestSite.this.courseMapFactory.createCourseMap();
                            }
                        };
                    }
                    
                };
                
            Map<String, Course> getCourses() {
                return this.courses;
            }
            
        }
        
        class Course {
            
            private AssociatedCollection<Course, Student> students =
                
                new AssociatedCollection<Course, Student>() {
                    
                    private static final long serialVersionUID = -5716082923650364470L;

                    public Course getOwner() {
                        return Course.this;
                    }
                    
                    @Override
                    protected AssociatedEndpointType onGetOppositeEndpointType() {
                        return AssociatedEndpointType.MAP;
                    }

                    @Override
                    public AssociatedEndpoint<Student, Course> onGetOppositeEndpoint(Student opposite) {
                        return opposite.courses;
                    }
        
                    @Override
                    protected RootData<Student> createRootData() {
                        return new RootData<Student>() {

                            private static final long serialVersionUID = 7901208800396357966L;

                            @Override
                            protected MACollection<Student> createDefaultBase(
                                    UnifiedComparator<? super Student> unifiedComparator) {
                                return TestSite.this.studentBagFactory.createStudentBag();
                            }
                            
                        };
                    }

                };
            
            public Collection<Student> getStudents() {
                return this.students;
            }
            
        }
        
    }
}
