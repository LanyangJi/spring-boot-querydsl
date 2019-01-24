package com.jly.querydsl.bean.dto;

import com.jly.querydsl.bean.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author lanyangji
 * @date 2019/1/20 17:44
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO implements Serializable {

    private Integer id;

    private String lastName;

    private String email;

    private Integer age;

    private Integer gender;

    public CustomerDTO(String lastName, String email) {
        this.lastName = lastName;
        this.email = email;
    }
}
