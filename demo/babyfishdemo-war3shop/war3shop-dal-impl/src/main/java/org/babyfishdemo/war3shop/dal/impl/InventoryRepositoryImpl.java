package org.babyfishdemo.war3shop.dal.impl;

import org.babyfishdemo.war3shop.dal.InventoryRepository;
import org.babyfishdemo.war3shop.entities.Inventory;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class InventoryRepositoryImpl extends AbstractRepositoryImpl<Inventory, Long> implements InventoryRepository {

}
