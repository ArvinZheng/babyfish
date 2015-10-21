package org.babyfishdemo.foundation.typedi18n.wrong;

import org.babyfish.lang.Arguments;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class Wrong {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private Wrong() {}

    public static int dayOfWeek(String name) {
        switch (Arguments.mustNotBeEmpty("name", Arguments.mustNotBeNull("name", name)).toLowerCase()) {
        case "sunday":
            return 0;
        case "monday":
            return 1;
        case "tuesday":
            return 2;
        case "wednesday":
            return 3;
        case "thursday":
            return 4;
        case "friday":
            return 5;
        case "saturday":
            return 6;
        }
        throw new IllegalArgumentException(LAZY_RESOURCE.get().unknownName(name));
    }
    
    private interface Resource {
        String unknownName(String name);
    }
}
