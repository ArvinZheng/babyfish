package org.babyfishdemo.war3shop.dal;

import java.util.Collection;
import java.util.Date;

import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile__;

/**
 * @author Tao Chen
 */
public interface TemporaryUploadedFileRepository extends AbstractRepository<TemporaryUploadedFile, Long> {
    
    TemporaryUploadedFile getTemporaryUploadedFileById(Long id, TemporaryUploadedFile__ ... queryPaths);

    void removeTemporaryUploadedFilesByIds(Collection<Long> ids);
    
    void removeTemporaryUploadedFilesByGcThreshold(Date gcThreshold);
}
