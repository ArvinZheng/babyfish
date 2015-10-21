package org.babyfishdemo.war3shop.dal.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.babyfishdemo.war3shop.dal.VariableRepository;
import org.babyfishdemo.war3shop.entities.Variable;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class VariableRepositoryImpl 
extends AbstractRepositoryImpl<Variable, String> 
implements VariableRepository {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Variable getVariableByName(String variableName) {
        return this.em.find(Variable.class, variableName);
    }
}
