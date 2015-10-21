package org.babyfishdemo.war3shop.dal.impl;

import java.util.Collection;
import java.util.Date;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfishdemo.war3shop.dal.TemporaryUploadedFileRepository;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile_;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile__;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class TemporaryUploadedFileRepositoryImpl 
extends AbstractRepositoryImpl<TemporaryUploadedFile, Long> 
implements TemporaryUploadedFileRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryUploadedFileRepositoryImpl.class);
    
    @PersistenceContext
    private XEntityManager em;

    @Override
    public TemporaryUploadedFile getTemporaryUploadedFileById(Long id, TemporaryUploadedFile__... queryPaths) {
        return this.em.find(TemporaryUploadedFile.class, id, queryPaths);
    }

    @Override
    public void removeTemporaryUploadedFilesByIds(Collection<Long> ids) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaDelete<TemporaryUploadedFile> cd = cb.createCriteriaDelete(TemporaryUploadedFile.class);
        Root<TemporaryUploadedFile> temporaryUploadedFile = cd.from(TemporaryUploadedFile.class);
        cd.where(
                cb.in(
                        temporaryUploadedFile.get(TemporaryUploadedFile_.id), 
                        ids
                )
        );
        this.em.createQuery(cd).executeUpdate();
    }

    @Override
    public void removeTemporaryUploadedFilesByGcThreshold(Date gcThreshold) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaDelete<TemporaryUploadedFile> cd = cb.createCriteriaDelete(TemporaryUploadedFile.class);
        Root<TemporaryUploadedFile> temporaryUploadedFile = cd.from(TemporaryUploadedFile.class);
        cd.where(
                cb.lessThanOrEqualTo(
                        temporaryUploadedFile.get(TemporaryUploadedFile_.gcThreshold), 
                        gcThreshold
                )
        );
        int removedCount = this.em.createQuery(cd).executeUpdate();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Removed " + removedCount + " temporary orders");
        }
    }
}
