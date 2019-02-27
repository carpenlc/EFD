package mil.nga.efd.controllers;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;

/**
 * GenericHibernateDAO implements the basic CRUD operations for Hibernate/JPA. 
 * This class should be used as a base class for all Hibernate/JPA  
 * implementations of persistent entity DAOs. 
 * 
 * This class was re-written from it's original implementation to support 
 * Spring  and JPA (vice proprietary Hibernate).
 * 
 * @author L. Craig Carpenter
 */
@Repository
public abstract class GenericHibernateDAO<T, ID extends Serializable> 
		implements GenericDAO<T, ID> {

	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		GenericHibernateDAO.class);
    
    /**
     * Injected JPA EntityManager object used to actually obtain a database 
     * connection.
     */
    @PersistenceContext
    protected EntityManager em;
    
    /**
     * Entity class type
     */
    private Class<T> entityClass;

    /**
     * Default constructor extracting the superclass type.
     */
    @SuppressWarnings("unchecked")
	public GenericHibernateDAO() {
    	ParameterizedType genericSuperclass = 
    			(ParameterizedType)getClass().getGenericSuperclass();
        this.entityClass =  (Class<T>)genericSuperclass.getActualTypeArguments()[1];
    }
    
    /**
     * Alternate constructor allowing subclasses to specify the entity class
     * type on construction.
     * 
     * @param entityClass The Entity type.
     */
    public GenericHibernateDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Accessor method for the EntityClass type.
     * @return The EntityClass type.
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @SuppressWarnings("unchecked")
    public T findById(ID id, boolean lock) {
        T entity = null;
        if (lock) {
        	entity = em.find(getEntityClass(), id, LockModeType.READ);
        }
        else {
        	entity = em.find(getEntityClass(), id);
        }
        return entity;
    }
    
    /**
     * Get an entity.
     * 
     * The semantics of getById are different than findById in that find expects
     * an entity to be there and will throw an exception if the entity referred
     * to by {@code id} does not exit.  get returns null
     * @see com.solers.util.dao.GenericDAO#getById(java.io.Serializable)
     */
    //@SuppressWarnings("unchecked")
    //public T getById(ID id) {
    //    return (T) getSession().get(getPersistentClass(), id);
    //}

    
    public abstract List<T> findAll();

    //@SuppressWarnings("unchecked")
   // public List<T> findByExample(T exampleInstance, String[] excludeProperty) {
    //    Criteria crit = getSession().createCriteria(getPersistentClass());
    //    Example example = Example.create(exampleInstance);
    //    for (String exclude : excludeProperty) {
    //        example.excludeProperty(exclude);
    //    }
    //    crit.add(example);
    //    return crit.list();
    //}

    public T persist(T entity) {
    	if (em != null) {
    		em.persist(entity);
        }
        else {
        	throw new IllegalStateException("EntityManager was not injected.");
        }
        return entity;
    }

    @Override
    public void remove(T entity) {
    	if (em != null) {
        	em.remove(entity);
        }
        else {
        	throw new IllegalStateException("EntityManager was not injected.");
        }
    }
    
    public void removeById(Collection<ID> ids) {
        for (ID id : ids) {
            T entity = findById(id, false);
            if (entity != null) {
                remove(entity);
            }
        }
    }

    
    /**
     * Flush changes to the target data source.
     */
    public void flush() {
        if (em != null) {
        	em.flush();
        }
        else {
        	throw new IllegalStateException("EntityManager was not injected.");
        }
    }

    /**
     * Clear the <code>EntityManager<code> object.
     */
    public void clear() {
    	if (em != null) {
    		em.clear();
    	}
    	else {
    		LOGGER.error("Unable to clear the EntityManager.  The "
    				+ "EntityManager was not injected.");
    	}
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    //@SuppressWarnings("unchecked")
    //protected List<T> findByCriteria(Criterion... criterion) {
    //	em.getCriteriaBuilder().cr
    //    Criteria crit = getSession().createCriteria(getPersistentClass());
    //    for (Criterion c : criterion) {
    //        crit.add(c);
    //    }
        
    //    return crit.list();
    //}
    
    /**
     * This method is the reason that we kept the <code>GenericDAOImpl</code>
     * code.  This method runs the validation checks against any entity before 
     * it goes into the backing data store.
     * 
     * @param entity The entity to validate.
     */
    protected void validate(T entity) {
        InvalidValue [] errors = validator.getInvalidValues(entity);
        if (errors.length > 0) {
            ValidationException ex = new ValidationException();
            for (InvalidValue v : errors) {
                ex.addMessage(v.getMessage());
            }
            throw ex;
        }
    }

}
