package com.jly.querydsl.controller;

import com.jly.querydsl.bean.QCustomer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lanyangji
 * @date 2019/1/23 15:32
 */
@RestController
public class MainController {

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @GetMapping("/names")
    public List<String> names() {
        return jpaQueryFactory.select(QCustomer.customer.lastName)
                .from(QCustomer.customer)
                .where(QCustomer.customer.age.gt(18))
                .orderBy(QCustomer.customer.id.desc())
                .fetch();
    }


}
