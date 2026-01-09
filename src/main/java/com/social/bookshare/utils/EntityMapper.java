package com.social.bookshare.utils;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class EntityMapper {

	@PersistenceContext
    private EntityManager entityManager;
	
	private static EntityMapper instance;
	
	@PostConstruct
    private void init() {
        instance = this;
    }
	
	public static <T> T getReference(Class<T> entityClass, Long id) {
        if (id == null || instance == null) 
        	return null;
        
        return instance.entityManager.getReference(entityClass, id);
    }
}
