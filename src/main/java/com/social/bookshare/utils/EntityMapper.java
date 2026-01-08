package com.social.bookshare.utils;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class EntityMapper {

	@PersistenceContext
    private EntityManager entityManager;
	
	public <T> T getReference(Class<T> entityClass, Long id) {
        if (id == null) 
        	return null;
        
        return entityManager.getReference(entityClass, id);
    }
}
