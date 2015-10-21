package org.babyfishdemo.war3shop.dal;

import java.util.Collection;
import java.util.List;

import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.specification.ManufacturerSpecification;

/**
 * @author Tao Chen
 */
public interface ManufacturerRepository extends AbstractRepository<Manufacturer, Long> {

    Page<Manufacturer> getManufacturers(
            ManufacturerSpecification specification,
            int pageIndex,
            int pageSize,
            Manufacturer__ ... queryPaths);
    
    Manufacturer getManufacturerById(long id, Manufacturer__ ... queryPaths);
    
    List<Manufacturer> getManufacturerByIds(Collection<Long> ids, Manufacturer__ ... queryPaths);

    Manufacturer getManufacturerByNameInsensitively(String name, Manufacturer__ ... queryPaths);

    void persistManufacturer(Manufacturer manufacturer);
}
