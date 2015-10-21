package org.babyfishdemo.war3shop.dal;

import java.util.List;

import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Preferential;
import org.babyfishdemo.war3shop.entities.Preferential__;
import org.babyfishdemo.war3shop.entities.specification.PreferentialSpecification;

/**
 * @author Tao Chen
 */
public interface PreferentialRepository extends AbstractRepository<Preferential, Long> {
    
    Preferential getPreferentialById(long id, Preferential__ ... queryPaths);

    Page<Preferential> getPreferentials(
            PreferentialSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Preferential__ ... queryPaths);
    
    List<Preferential> getPreferentialsThatCanBeAffectedByProduct(
            long productId, 
            Preferential__ ... queryPaths);
    
    void mergePreferential(Preferential preferential);
}
