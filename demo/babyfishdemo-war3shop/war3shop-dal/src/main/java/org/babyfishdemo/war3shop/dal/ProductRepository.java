package org.babyfishdemo.war3shop.dal;

import java.util.List;

import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;

/**
 * @author Tao Chen
 */
public interface ProductRepository extends AbstractRepository<Product, Long> {

    Product getProductById(long id, Product__ ... queryPaths);
    
    List<Product> getProductsByIds(Iterable<Long> ids, Product__ ... queryPaths);

    Product getProductLikeNameInsensitively(String name, Product__ ... queryPaths);

    Page<Product> getProducts(
            ProductSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Product__ ... queryPaths);
}
