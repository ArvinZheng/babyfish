package org.babyfishdemo.war3shop.dal;

import java.util.Date;

import org.babyfishdemo.war3shop.entities.Order;
import org.babyfishdemo.war3shop.entities.Order__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.specification.OrderSpecification;

/**
 * @author Tao Chen
 */
public interface OrderRepository extends AbstractRepository<Order, Long> {
    
    Order getOrderById(long id, Order__ ... queryPaths);

    Order getNewestTemporaryOrderByCustomerId(long customerId, Order__ ... queryPaths);
    
    Page<Order> getAssuredOrders(OrderSpecification specification, int pageIndex, int pageSize, Order__ ... queryPaths);
    
    void removeTemporaryOrderByGcThreshold(Date gcThreshold);
}
