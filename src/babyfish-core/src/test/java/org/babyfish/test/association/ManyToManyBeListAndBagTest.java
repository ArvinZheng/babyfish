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

import org.babyfish.association.AssociatedCollection;
import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.association.AssociatedList;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.MALinkedList;
import org.babyfish.collection.MAList;
import org.babyfish.collection.UnifiedComparator;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ManyToManyBeListAndBagTest {
    
    private static final TestSite.CourseListFactory[] COURSE_LIST_FACTORIES =
        new TestSite.CourseListFactory[] {
            new TestSite.CourseListFactory() {
                @Override
                public MAList<TestSite.Course> createCourseList() {
                    return new MAArrayList<TestSite.Course>();
                }
            },
            new TestSite.CourseListFactory() {
                @Override
                public MAList<TestSite.Course> createCourseList() {
                    return new MALinkedList<TestSite.Course>();
                }
            },
        };
    
    private static final TestSite.StudentBagFactory[] STUDENT_BAG_FACTORIES =
        new TestSite.StudentBagFactory[] {
            new TestSite.StudentBagFactory() {
                @Override
                public MACollection<TestSite.Student> createStudentBag() {
                    return new MAArrayList<TestSite.Student>();
                }
            },
            new TestSite.StudentBagFactory() {
                @Override
                public MACollection<TestSite.Student> createStudentBag() {
                    return new MALinkedList<TestSite.Student>();
                }
            }
        };
    
    private TestSite[] testSites;
    
    @Before
    public void initalize() {
        TestSite[] sites = 
            new TestSite[COURSE_LIST_FACTORIES.length * STUDENT_BAG_FACTORIES.length];
        int index = 0;
        for (int i = 0; i < COURSE_LIST_FACTORIES.length; i++) {
            for (int ii = 0; ii < STUDENT_BAG_FACTORIES.length; ii++) {
                sites[index++] = new TestSite(COURSE_LIST_FACTORIES[i], STUDENT_BAG_FACTORIES[ii]);
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
        
        private CourseListFactory courseListFactory;
        
        private StudentBagFactory studentListFactory;
        
        TestSite(
                CourseListFactory courseListFactory,
                StudentBagFactory studentListFactory) {
            this.courseListFactory = courseListFactory;
            this.studentListFactory = studentListFactory;
        }

        interface CourseListFactory {
            MAList<Course> createCourseList();
        }
        
        interface StudentBagFactory {
            MACollection<Student> createStudentBag();
        }
        
        void testModifyStudent() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertList(student1.getCourses());
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().add(course1);
            AssociationTests.assertList(student1.getCourses(), course1);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().add(course2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().add(course1);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().add(course2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1, course2);
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            student2.getCourses().remove(course2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().remove(course1);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student1.getCourses().remove(course2);
            AssociationTests.assertList(student1.getCourses(), course1);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().remove(course1);
            AssociationTests.assertList(student1.getCourses());
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
        }
        
        void testModifyStudentByIterator() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertList(student1.getCourses());
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().listIterator(student1.getCourses().size()).add(course1);
            AssociationTests.assertList(student1.getCourses(), course1);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            student1.getCourses().listIterator(student1.getCourses().size()).add(course2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().listIterator(student2.getCourses().size()).add(course1);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            student2.getCourses().listIterator(student2.getCourses().size()).add(course2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1, course2);
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            AssociationTests.getIteratorAndMoveTo(student2.getCourses(), course2).remove();
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(student2.getCourses(), course1).remove();
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(student1.getCourses(), course2).remove();
            AssociationTests.assertList(student1.getCourses(), course1);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            AssociationTests.getIteratorAndMoveTo(student1.getCourses(), course1).remove();
            AssociationTests.assertList(student1.getCourses());
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
        }
        
        void testModifyCourse() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertList(student1.getCourses());
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            course1.getStudents().add(student1);
            AssociationTests.assertList(student1.getCourses(), course1);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            course2.getStudents().add(student1);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            course1.getStudents().add(student2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            course2.getStudents().add(student2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1, course2);
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            course2.getStudents().remove(student2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            course1.getStudents().remove(student2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            course2.getStudents().remove(student1);
            AssociationTests.assertList(student1.getCourses(), course1);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            course1.getStudents().remove(student1);
            AssociationTests.assertList(student1.getCourses());
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
        }
        
        void testModifyCourseByIterator() {
            Student student1 = this.new Student();
            Course course1 = this.new Course();
            Student student2 = this.new Student();
            Course course2 = this.new Course();
            AssociationTests.assertList(student1.getCourses());
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            course1.getStudents().add(student1);
            AssociationTests.assertList(student1.getCourses(), course1);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            course2.getStudents().add(student1);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            course1.getStudents().add(student2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            course2.getStudents().add(student2);
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1, course2);
            AssociationTests.assertCollection(course2.getStudents(), student1, student2);
            
            AssociationTests.getIteratorAndMoveTo(course2.getStudents(), student2).remove();
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1, student2);
            AssociationTests.assertList(student2.getCourses(), course1);
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(course1.getStudents(), student2).remove();
            AssociationTests.assertList(student1.getCourses(), course1, course2);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents(), student1);
            
            AssociationTests.getIteratorAndMoveTo(course2.getStudents(), student1).remove();
            AssociationTests.assertList(student1.getCourses(), course1);
            AssociationTests.assertCollection(course1.getStudents(), student1);
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
            
            AssociationTests.getIteratorAndMoveTo(course1.getStudents(), student1).remove();
            AssociationTests.assertList(student1.getCourses());
            AssociationTests.assertCollection(course1.getStudents());
            AssociationTests.assertList(student2.getCourses());
            AssociationTests.assertCollection(course2.getStudents());
        }
    
        class Student {
        
            private AssociatedList<Student, Course> courses =
                
                new AssociatedList<Student, Course>() {
                                
                    private static final long serialVersionUID = 5529477538615742206L;
                    
                    @Override
                    public Student getOwner() {
                        return Student.this;
                    }
    
                    @Override
                    protected AssociatedEndpointType onGetOppositeEndpointType() {
                        return AssociatedEndpointType.COLLECTION;
                    }

                    @Override
                    public AssociatedEndpoint<Course, Student> onGetOppositeEndpoint(Course opposite) {
                        return opposite.students;
                    }
    
                    @Override
                    protected RootData<Course> createRootData() {
                        return new RootData<Course>() {
                            
                            private static final long serialVersionUID = 6231133521868306261L;

                            @Override
                            protected MAList<Course> createDefaultBase(
                                    UnifiedComparator<? super Course> unifiedComparator) {
                                return TestSite.this.courseListFactory.createCourseList();
                            }
                        };
                    }
                };
                
            MAList<Course> getCourses() {
                return this.courses;
            }
        
        }
        
        class Course {
            
            private AssociatedCollection<Course, Student> students = 
                
                new AssociatedCollection<Course, Student>() {

                    private static final long serialVersionUID = 9123066220483017956L;

                    public Course getOwner() {
                        return Course.this;
                    }
    
                    @Override
                    protected AssociatedEndpointType onGetOppositeEndpointType() {
                        return AssociatedEndpointType.LIST;
                    }

                    @Override
                    public AssociatedEndpoint<Student, Course> onGetOppositeEndpoint(Student opposite) {
                        return opposite.courses;
                    }
    
                    @Override
                    protected RootData<Student> createRootData() {
                        return new RootData<Student>() {
    
                            private static final long serialVersionUID = 4896441866479818197L;
    
                            @Override
                            protected MACollection<Student> createDefaultBase(
                                    UnifiedComparator<? super Student> unifiedComparator) {
                                return TestSite.this.studentListFactory.createStudentBag();
                            }
                            
                        };
                    }
    
                };
                
            MACollection<Student> getStudents() {
                return this.students;
            }
    
        }
    }
    
}
