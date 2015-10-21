package org.babyfishdemo.foundation.combiner.event;

import java.util.EventListener;

/**
 * @author Tao Chen
 */
public interface PropertyChangedListener extends EventListener {

    void propertyChanged(PropertyChanagedEvent e);
}
