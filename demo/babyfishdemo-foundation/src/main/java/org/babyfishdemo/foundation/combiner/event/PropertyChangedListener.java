package org.babyfishdemo.foundation.combiner.event;

import java.util.EventListener;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface PropertyChangedListener extends EventListener {

    void propertyChanged(PropertyChanagedEvent e);
}
