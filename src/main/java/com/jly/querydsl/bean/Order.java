package com.jly.querydsl.bean;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author lanyangji
 * @date 2019/1/17 19:53
 */
@Data
@Accessors(chain = true)
@Entity
@Table(name = "querydsl_order")
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Integer customerId;
}
