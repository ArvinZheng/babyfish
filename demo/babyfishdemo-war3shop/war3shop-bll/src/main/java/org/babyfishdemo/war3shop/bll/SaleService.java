package org.babyfishdemo.war3shop.bll;

import org.babyfishdemo.war3shop.bll.model.Sale;
import org.babyfishdemo.war3shop.entities.Order;
import org.babyfishdemo.war3shop.entities.Order__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Preferential__;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.specification.OrderSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;

/**
 * @author Tao Chen
 */
public interface SaleService {

    Page<Sale> getSales(
            ProductSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Product__[] productQueryPaths,
            Preferential__[] preferentialQueryPaths);
    
    Order getTemporaryOrder(Order__ ... queryPaths);
    
    void addProductIntoCart(long productId);
    
    void setProductQuantityOfCart(long productId, int quantity);
    
    void removeProductFromCart(long productId);

    void createOrder(String address, String phone);
    
    Page<Order> getAssuredOrders(
            OrderSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Order__ ... queryPaths);
    
    Page<Order> getMyOrders(
            OrderSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Order__ ... queryPaths);
    
    Page<Order> getMyRepsonsibleOrders(
            OrderSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Order__ ... queryPaths);

    void assignDeliveryman(long orderId, long deliverymanId);

    void deliveryOrder(long orderId);

    void changeOrderAddress(long orderId, String address, String phone);
}
