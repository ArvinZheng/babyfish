package org.babyfishdemo.war3shop.dal;

import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.PurchasingItem;
import org.babyfishdemo.war3shop.entities.PurchasingItem__;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface PurchasingItemRepository {

    Page<PurchasingItem> getPurchasingItemsByPurchasingId(
            long purchasingId,
            int pageIndex,
            int pageSize,
            PurchasingItem__ ... queryPaths);
}
