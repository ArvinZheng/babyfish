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
package org.babyfish.persistence.tool.instrument;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashSet;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public abstract class AbstractInstrumentTask extends Task {

private List<FileSet> filesets = new ArrayList<>();
    
    public void addFileset(FileSet set) {
        this.filesets.add(Arguments.mustNotBeNull("set", set));
    }

    @Override
    public final void execute() throws BuildException {
        Set<File> files = this.files();
        try {
            this.execute(files);
        } catch (Throwable ex) {
            throw new BuildException(ex);
        }
    }
    
    protected abstract AbstractInstrumenter createInstrumenter(Set<File> bytecodeFiles);

    private void execute(Set<File> files) throws IOException {
        AbstractInstrumenter instrumenter = this.createInstrumenter(files);
        Collection<InstrumentedResult> instrumentedResults = 
                instrumenter.getInstrumentedResults().values();
        byte[] buf = new byte[1024];
        for (InstrumentedResult instrumentedResult : instrumentedResults) {
            File file = instrumentedResult.getMetadataClass().getBytecodeFile();
            try (ByteArrayInputStream bin = new ByteArrayInputStream(instrumentedResult.getOwnerBytecode());
                    FileOutputStream fout = new FileOutputStream(file.getPath())) {
                int len;
                while ((len = bin.read(buf)) != -1) {
                    fout.write(buf, 0, len);
                }
            }
            String omFilePath = file.getPath();
            omFilePath = omFilePath.substring(0, omFilePath.lastIndexOf(".")) + "${OM}.class";
            try (ByteArrayInputStream bin = new ByteArrayInputStream(instrumentedResult.getObjectModelBytecode());
                    FileOutputStream fout = new FileOutputStream(omFilePath)) {
                int len;
                while ((len = bin.read(buf)) != -1) {
                    fout.write(buf, 0, len);
                }
            }
        }
    }
    
    private Set<File> files() {
        HashSet<File> files = new HashSet<>();
        Project project = this.getProject();
        for (FileSet fs : this.filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            String[] includedFiles = ds.getIncludedFiles();
            File dir = fs.getDir(project);
            for (String includedFile : includedFiles) {
                files.add(new File(dir, includedFile));
            }
        }
        return files;
    }
}
