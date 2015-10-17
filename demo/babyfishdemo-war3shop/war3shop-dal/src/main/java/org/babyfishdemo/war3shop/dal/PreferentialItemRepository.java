package org.babyfishdemo.war3shop.dal;

import org.babyfishdemo.war3shop.entities.PreferentialItem;
import org.babyfishdemo.war3shop.entities.PreferentialItem__;
import org.babyfishdemo.war3shop.entities.Page;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface PreferentialItemRepository {

    Page<PreferentialItem> getPreferentialItemsByParent(
            long preferentialId, 
            int pageIndex, 
            int pageSize, 
            PreferentialItem__ ... queryPaths);
}
