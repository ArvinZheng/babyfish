package org.babyfishdemo.war3shop.web.captcha;

import java.util.List;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Nulls;
import org.springframework.beans.factory.FactoryBean;

import com.github.bingoohuang.patchca.filter.FilterFactory;
import com.github.bingoohuang.patchca.filter.predefined.CurvesRippleFilterFactory;
import com.github.bingoohuang.patchca.filter.predefined.DiffuseRippleFilterFactory;
import com.github.bingoohuang.patchca.filter.predefined.DoubleRippleFilterFactory;
import com.github.bingoohuang.patchca.filter.predefined.MarbleRippleFilterFactory;
import com.github.bingoohuang.patchca.filter.predefined.RandomFilterFactory;
import com.github.bingoohuang.patchca.filter.predefined.WobbleRippleFilterFactory;

/**
 * @author Tao Chen
 */
public class RandomFilterFactoryFactoryBean implements FactoryBean<FilterFactory>{
    
    private static final List<FilterFactory> DEFAULT_FILTER_FACTORIES;

    private List<FilterFactory> filterFactories;

    public void setFilterFactories(List<FilterFactory> filterFactories) {
        if (filterFactories == null) {
            this.filterFactories = null;
        } else {
            List<FilterFactory> nonNullFilterFactories = new ArrayList<>(filterFactories.size());
            for (FilterFactory filterFactory : filterFactories) {
                if (filterFactory != null) {
                    nonNullFilterFactories.add(filterFactory);
                }
            }
            this.filterFactories = nonNullFilterFactories;
        }
    }

    @Override
    public FilterFactory getObject() throws Exception {
        List<FilterFactory> filterFactories = this.filterFactories;
        if (Nulls.isNullOrEmpty(filterFactories)) {
            filterFactories = DEFAULT_FILTER_FACTORIES;
        }
        RandomFilterFactory randomFilterFactory = new RandomFilterFactory();
        for (FilterFactory filterFactory : filterFactories) {
            randomFilterFactory.addFilterFactory(filterFactory);
        }
        return randomFilterFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return FilterFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
    
    static {
        List<FilterFactory> defaultFilterFactories = new ArrayList<>();
        defaultFilterFactories.add(new CurvesRippleFilterFactory());
        defaultFilterFactories.add(new MarbleRippleFilterFactory());
        defaultFilterFactories.add(new DoubleRippleFilterFactory());
        defaultFilterFactories.add(new WobbleRippleFilterFactory());
        defaultFilterFactories.add(new DiffuseRippleFilterFactory());
        DEFAULT_FILTER_FACTORIES = MACollections.unmodifiable(defaultFilterFactories);
    }
}
