package com.jly.querydsl.repository;

import com.jly.querydsl.bean.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lanyangji
 * @date 2019/1/17 20:16
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, QuerydslPredicateExecutor<Customer> {

    /**
     * 按照姓名查询部分属性
     *
     * @param lastName
     * @return
     */
    @Query("SELECT new Customer(c.lastName, c.email) FROM Customer c WHERE c.lastName = :lastName")
    Customer findByLastName(String lastName);


    /**
     * 根据姓名查询指定格式的生日
     *
     * @param lastName
     * @return
     */
    @Query("SELECT DATE_FORMAT(c.birth, '%Y-%m-%d') FROM Customer c WHERE c.lastName = :lastName")
    Object findBirthFormat(@Param("lastName") String lastName);

}
