package com.jly.querydsl.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author lanyangji
 * @date 2019/1/17 19:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "querydsl_customer")
public class Customer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "last_name", unique = true)
    private String lastName;

    private String email;

    private Integer age;

    @Column(length = 1)
    private Integer gender;

    @Column(columnDefinition = "datetime default NOW()")
    private Date birth;

    public Customer(String lastName, String email) {
        this.lastName = lastName;
        this.email = email;
    }
}
