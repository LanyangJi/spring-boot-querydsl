package com.jly.querydsl.repository;

import com.jly.querydsl.bean.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * @author lanyangji
 * @date 2019/1/17 20:22
 */
public interface OrderRepository extends JpaRepository<Order, Integer>, QuerydslPredicateExecutor<Order> {
}
