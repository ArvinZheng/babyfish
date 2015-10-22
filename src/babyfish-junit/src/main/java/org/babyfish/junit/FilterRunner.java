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
package org.babyfish.junit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.UncheckedException;
import org.babyfish.util.LazyResource;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * @author Tao Chen
 */
public class FilterRunner extends Runner {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class); 
    
    private Class<?> klass;
    
    private boolean accept;
    
    private Runner secondaryRunner;

    public FilterRunner(Class<?> klass) throws InitializationError {
        this.klass = Arguments.mustNotBeNull("klass", klass);
        /*
         * Only use the first return value
         * if ignore()/secondaryRunner() returns different values for different invocations
         */
        this.accept = this.accept();
        this.secondaryRunner = this.secondaryRunner();
    }

    @Override
    public void run(RunNotifier notifier) {
        if (this.accept) {
            this.secondaryRunner.run(notifier);
        } else {
            notifier.fireTestIgnored(this.getDescription());
        }
    }
    
    @Override
    public Description getDescription() {
        if (this.accept) {
            return this.secondaryRunner.getDescription();
        }
        return Description.createSuiteDescription(this.klass);
    }
    
    private boolean accept() {
        for (Class<?> klass = this.klass; 
                klass != Object.class; 
                klass = klass.getSuperclass()) {
            FilterDeclaration filterDeclaration =
                    klass.getAnnotation(FilterDeclaration.class);
            FilterDeclarations filterDeclarations = 
                    klass.getAnnotation(FilterDeclarations.class);
            if (filterDeclaration != null && filterDeclarations != null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().canNotUseBoth(
                                klass, 
                                FilterDeclaration.class, 
                                FilterDeclarations.class)
                        );
            }
            if (filterDeclaration != null) {
                if (!accept(filterDeclaration)) {
                    return false;
                }
            } else if (filterDeclarations != null) {
                for (FilterDeclaration declaration : filterDeclarations.value()) {
                    if (!accept(declaration)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private static boolean accept(FilterDeclaration filterDeclaration) {
        Object[] arguments = filterDeclaration.arguments();
        Class<?>[] argumentTypes = new Class[arguments.length];
        Arrays.fill(argumentTypes, 0, argumentTypes.length, String.class);
        Constructor<? extends Filter> constructor;
        try {
            constructor = filterDeclaration.filterClass().getConstructor(argumentTypes);
        } catch (NoSuchMethodException ex) {
            try {
                constructor = filterDeclaration.filterClass().getConstructor(String[].class);
                arguments = new Object[] { arguments };
            } catch (NoSuchMethodException innerEx) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().filterClassMissConstructor(
                                filterDeclaration.filterClass(), 
                                String.class, 
                                String[].class
                        )
                );
            }
        }
        
        Filter filter;
        try {
            filter = constructor.newInstance(arguments);
        } catch (InstantiationException |
                IllegalAccessException |
                InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex);
        }
        return filter.accept();
    }
    
    private Runner secondaryRunner() throws InitializationError {
        Class<? extends Runner> runnerClass = BlockJUnit4ClassRunner.class;
        for (Class<?> klass = this.klass;
                klass != Object.class; 
                klass = klass.getSuperclass()) {
            ThenRunWith secondaryRunner = klass.getAnnotation(ThenRunWith.class);
            if (secondaryRunner != null) {
                runnerClass = secondaryRunner.value();
                break;
            }
        }
        Constructor<? extends Runner> constructor;
        try {
            constructor = runnerClass.getConstructor(Class.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().runnerMissConstructor(runnerClass, Class.class)
            );
        }
        try {
            return constructor.newInstance(this.klass);
        } catch (InstantiationException ex) {
            throw new IllegalProgramException(ex);
        } catch (IllegalAccessException ex) {
            throw new AssertionError();
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof InitializationError) {
                throw (InitializationError)targetException;
            }
            throw UncheckedException.rethrow(targetException);
        }
    }
    
    private interface Resource {
        
        String canNotUseBoth(
                Class<?> testCaseClass,
                Class<FilterDeclaration> filterDeclarationType,
                Class<FilterDeclarations> filterDeclarationsType);
        
        String filterClassMissConstructor(
                Class<? extends Filter> filerType,
                Class<String> stringParameterType,
                Class<String[]> stringArrayParameterType);
        
        @SuppressWarnings("rawtypes")
        String runnerMissConstructor(
                Class<? extends Runner> runnerClass,
                Class<Class> classParameterType);
    }
}
