package org.babyfishdemo.war3shop.dal;

import org.babyfishdemo.war3shop.entities.Variable;

/**
 * @author Tao Chen
 */
public interface VariableRepository extends AbstractRepository<Variable, String> {

    Variable getVariableByName(String variableName);
}
