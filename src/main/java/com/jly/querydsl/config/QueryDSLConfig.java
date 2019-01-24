package com.jly.querydsl.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

/**
 * @author lanyangji
 * @date 2019/1/17 20:03
 */
@Configuration
public class QueryDSLConfig {

    /**
     * 第一种使用QueryDSL需要注入此bean
     *
     * @param entityManager
     * @return
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

}
