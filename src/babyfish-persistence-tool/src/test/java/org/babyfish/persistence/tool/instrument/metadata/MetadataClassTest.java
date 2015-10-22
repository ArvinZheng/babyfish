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
package org.babyfish.persistence.tool.instrument.metadata;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.babyfish.collection.HashSet;
import org.babyfish.persistence.tool.instrument.AbstractInstrumenter;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MetadataClassTest {

    @Test
    public void test() throws IOException {
        String[] paths = new String[] {
                "NamedEntity.class",
                "Department.class", 
                "Employee.class", 
                "Skill.class",
                "EntityId.class",
                "SecondaryEntityId.class",
        };
        Set<File> files = new HashSet<>((paths.length * 4 + 2) / 3);
        for (String path : paths) {
            files.add(new File(MetadataClassTest.class.getClassLoader().getResource("org/babyfish/persistence/tool/instrument/entities/" + path).toString().substring(5)));
        }
        new AbstractInstrumenter(files, null) {};
    }
}
