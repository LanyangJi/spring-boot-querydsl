package com.jly.querydsl;

import com.jly.querydsl.bean.Customer;
import com.jly.querydsl.bean.QCustomer;
import com.jly.querydsl.bean.QOrder;
import com.jly.querydsl.bean.dto.CustomerDTO;
import com.jly.querydsl.repository.CustomerRepository;
import com.jly.querydsl.repository.OrderRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootQuerydslApplicationTests {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    // 初始化先插入一些数据
    @Test
    public void init() {
        Customer customer = new Customer();
        customer.setLastName("姬岚洋").setEmail("jly@qq.com").setAge(23).setGender(1);

        Customer customer1 = new Customer();
        customer1.setLastName("王海涛").setEmail("wht@qq.com").setGender(0).setAge(23);

        customerRepository.saveAll(Lists.newArrayList(customer, customer1));
    }

    /**
     * 在Spring环境下，我们可以通过两种风格来使用QueryDSL。
     * 一种是使用JPAQueryFactory的原生QueryDSL风格，
     * <p>
     * 另一种是基于Spring Data提供的QueryDslPredicateExecutor<T>的Spring-data风格。
     * 使用QueryDslPredicateExecutor<T>可以简化一些代码，使得查询更加优雅。
     * 而JPAQueryFactory的优势则体现在其功能的强大，支持更复杂的查询业务。甚至可以用来进行更新和删除操作。
     */

    // 方法一，使用 com.querydsl.jpa.impl.JPAQueryFactory
    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Transactional
    @Test
    public void testUpdate() {
        QCustomer customer = QCustomer.customer;
        jpaQueryFactory.update(customer).set(customer.email, "hah@qq.com").where(customer.id.eq(1)).execute();
    }

    @Transactional
    @Test
    public void testDelete() {
        QOrder order = QOrder.order;
        jpaQueryFactory.delete(order).where(order.name.like("%xs%")).execute();
    }

    @Test
    public void testQuery_Select_and_Fetch() {
        // 1. 查询字段
        QCustomer customer = QCustomer.customer;
        List<String> nameList = jpaQueryFactory.select(customer.lastName).from(customer).fetch();
        System.out.println(nameList);

        // 2. 查询实体
        List<Customer> customerList = jpaQueryFactory.selectFrom(customer).fetch();
        System.out.println(customerList);

        // 3. 获取首个查询结果
        Customer c1 = jpaQueryFactory.selectFrom(customer).fetchFirst();
        System.out.println(c1);

        // 4. 去重查询
        List<Tuple> fetch = jpaQueryFactory.selectDistinct(customer.lastName, customer.email).from(customer).fetch();
        fetch.stream().forEach(
                tuple -> {
                    System.out.println(tuple.get(0, String.class));
                    System.out.println(tuple.get(1, String.class));
                }
        );

        // 5. 查询一个
        //  获取唯一查询结果-fetchOne()
        //  当fetchOne()根据查询条件从数据库中查询到多条匹配数据时，会抛`NonUniqueResultException`。
//        Customer customer1 = jpaQueryFactory.selectFrom(customer).fetchOne();
//        System.out.println(customer1);

        // 6. 查询结果并封装在DTO中
        QOrder order = QOrder.order;
        List<CustomerDTO> customerDTOList = jpaQueryFactory
                .select(Projections.constructor(CustomerDTO.class, customer.lastName, customer.email))
                .from(customer)
                .leftJoin(order)
                .on(customer.id.intValue().eq(order.id.intValue()))
                .fetch();

        customerDTOList.stream().forEach(
                System.out::println
        );
    }

    @Test
    public void testWhere() {
        QCustomer customer = QCustomer.customer;
        List<Customer> list = jpaQueryFactory.selectFrom(customer)
                .where(customer.lastName.like("%岚%").and(customer.age.between(20, 40)).or(customer.gender.eq(1))).fetch();
        System.out.println(list);

        System.out.println("-----------------------------------------------------");

        // 拼接查询条件
        Customer c = new Customer();
        c.setLastName("岚").setAge(null).setGender(1);

        // 使用QueryDSL提供的BooleanBuilder来进行查询条件管理
        BooleanBuilder builder = new BooleanBuilder();
        if (!StringUtils.isEmpty(c.getLastName())) {
            builder.and(customer.lastName.like("%" + c.getLastName() + "%"));
        }
        if (c.getAge() != null) {
            builder.and(customer.age.gt(c.getAge()));
        }
        if (c.getGender() != null) {
            builder.or(customer.gender.eq(c.getGender()));
        }

        List<Customer> fetch = jpaQueryFactory.selectFrom(customer).where(builder).fetch();
        System.out.println(fetch);

        System.out.println("-----------------------------------------------------");

        // 使用 BooleanBuilder使用构造复杂查询条件
        BooleanBuilder b1 = new BooleanBuilder();
        b1.and(customer.lastName.like("%岚%"));

        BooleanBuilder b2 = new BooleanBuilder();
        b2.or(customer.gender.eq(1));
        b2.or(customer.age.between(20, 40));

        b1.or(b2);
        List<Customer> fetch1 = jpaQueryFactory.selectFrom(customer).where(b1).fetch();
        System.out.println(fetch1);

    }

    @Test
    public void testJoinQuery() {
        QCustomer customer = QCustomer.customer;
        QOrder order = QOrder.order;
        List<Customer> fetch = jpaQueryFactory.selectFrom(customer)
                .leftJoin(order)
                .on(customer.id.intValue().eq(order.customerId.intValue()))
                .where(customer.lastName.like("%岚%"))
                .fetch();
        System.out.println(fetch);
    }

    /**
     * MySql聚合函数
     */
    @Test
    public void testMySqlFunction() {
        // avg()
        QCustomer customer = QCustomer.customer;
        Double aDouble = jpaQueryFactory.select(customer.age.avg()).from(customer).fetchOne();
        System.out.println(aDouble);

        // concat()
        List<String> fetch = jpaQueryFactory.select(customer.lastName.concat(customer.email)).from(customer).fetch();
        System.out.println(fetch);

        // date_format()
        /**
         * 当用到DATE_FORMAT这类QueryDSL似乎没有提供支持的Mysql函数时，
         *      我们可以手动拼一个String表达式。这样就可以无缝使用Mysql中的函数了。
         */
        List<String> fetch1 = jpaQueryFactory.select(Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", customer.birth))
                .from(customer)
                .fetch();
        System.out.println(fetch1);
    }

    @Test
    public void testSubQuery() {
        QCustomer customer = QCustomer.customer;
        QOrder order = QOrder.order;

        List<String> list = jpaQueryFactory.select(order.name)
                .from(order)
                .where(order.customerId.intValue().eq(
                        JPAExpressions.select(customer.id)
                                .from(customer)
                                .where(customer.lastName.eq("姬岚洋"))
                ))
                .fetch();
        System.out.println(list);
    }

    @Test
    public void testOrderBy() {
        QCustomer customer = QCustomer.customer;
        List<Customer> fetch = jpaQueryFactory.selectFrom(customer).orderBy(customer.id.desc()).fetch();
        System.out.println(fetch);
    }

    /**
     * 写法一和二都会发出两条sql进行查询，一条查询count，一条查询具体数据。
     * 写法二的getTotal()等价于写法一的fetchCount。
     * 无论是哪种写法，在查询count的时候，orderBy、limit、offset这三个都不会被执行。可以大胆使用。
     */
    @Test
    public void testPage() {
        QCustomer customer = QCustomer.customer;
        // 写法一
        JPAQuery<Customer> customerJPAQuery = jpaQueryFactory.selectFrom(customer).orderBy(customer.id.desc());
        // fetchCount()的时候上面的orderBy不会被执行, offset表示的是从该参数指定的行位置的下一行开始
        long l = customerJPAQuery.fetchCount();
        System.out.println(l);
        List<Customer> fetch = customerJPAQuery.offset(0).limit(5).fetch();
        System.out.println(fetch);

        // 写法二
        QueryResults<Customer> results = jpaQueryFactory.selectFrom(customer).orderBy(customer.id.desc()).offset(1).limit(1).fetchResults();
        List<Customer> list = results.getResults();
        System.out.println("total: " + results.getTotal());
        System.out.println("limit: " + results.getLimit());
        System.out.println("offset: " + results.getOffset());
        System.out.println(list);
    }

    /**
     * QueryDSL并没有对Mysql的所有函数提供支持，好在它给我们提供了Template特性。我们可以使用Template来实现各种QueryDSL未直接支持的语法。
     * 不过Template好用归好用，但也有其局限性。
     * 例如当我们需要用到复杂的正则表达式匹配的时候，就有些捉襟见肘了。这是由于Template中使用了{}来作为占位符，而正则表达式中也可能使用了{}，因而会产生冲突。
     */
    @Test
    public void testTemplate() {
        QCustomer customer = QCustomer.customer;
        // 使用 booleanTemplate 充当 where 子句或者 where子句的一部分
        List<Customer> fetch = jpaQueryFactory
                .selectFrom(customer)
                .where(Expressions.booleanTemplate("{0} LIKE '%岚%'", customer.lastName))
                .fetch();
        System.out.println(fetch);
        // 如果上面的用法需要用到多个占位符
        List<Customer> fetch1 = jpaQueryFactory
                .selectFrom(customer)
                .where(Expressions.booleanTemplate("{0} LIKE '%岚%' OR {1} = 1", customer.lastName, customer.gender))
                .fetch();
        System.out.println(fetch1);


        // 使用stringTemplate充当查询语句的一部分
        List<String> fetch2 = jpaQueryFactory.select(Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", customer.birth))
                .from(customer).fetch();
        System.out.println(fetch2);
        // 使用 stringTemplate作为where子句的一部分
        List<Integer> fetch3 = jpaQueryFactory.select(customer.id)
                .from(customer)
                .where(Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%s')", customer.birth).eq("2019-01-21"))
                .fetch();
        System.out.println(fetch3);
    }


    /**
     * 使用 QueryDslPredicateExecutor接口继承方式
     */
    @Test
    public void testSimpleQuery() {
        QCustomer customer = QCustomer.customer;
        Iterable<Customer> all = customerRepository.findAll(customer.lastName.like("%岚%"));
        System.out.println(all);
    }

    /**
     * 使用更优雅的BooleanBuilder 来进行条件分支管理
     */
    @Test
    public void testQueryCondition() {
        QCustomer customer = QCustomer.customer;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(customer.lastName.like("%岚%"));
        builder.or(customer.gender.eq(1));

        List<Customer> all = (List<Customer>) customerRepository.findAll(builder);
        System.out.println(all);
    }

    /**
     * findOne
     *      findOne，顾名思义，从数据库中查出一条数据。没有重载方法。
     *      和JPAQuery的fetchOne()一样，当根据查询条件从数据库中查询到多条匹配数据时，
     *      会抛NonUniqueResultException。使用的时候需要慎重。
     *
     * findAll
     *      findAll是从数据库中查出匹配的所有数据。提供了以下几个重载方法。
     *
     *      findAll(Predicate predicate)
     *       findAll(OrderSpecifier<?>... orders)
     *      findAll(Predicate predicate,OrderSpecifier<?>... orders)
     *      findAll(Predicate predicate,Sort sort)
     *
     *      第一个重载方法是不带排序的，第二个重载方法是只带QueryDSL提供的OrderSpecifier方式实现排序而不带查询条件的，
     *      而第三个方法则是既有条件又有排序的。
     */
    @Test
    public void testFindAllSort() {
        QCustomer customer = QCustomer.customer;
        // 第一种使用querydsl提供的排序方式
        OrderSpecifier<Integer> orderSpecifier = new OrderSpecifier<>(Order.DESC, customer.id);
        Iterable<Customer> all = customerRepository.findAll(orderSpecifier);
        System.out.println(all);

        // 第二种使用spring data封装的排序方式
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        List<Customer> all1 = customerRepository.findAll(sort);
        System.out.println(all1);
    }

    @Test
    public void testCustomQuery() {
        Customer customer = customerRepository.findByLastName("姬岚洋");
        System.out.println(customer);

    }

    @Test
    public void testJpqlMysqlFunction() {
        Object birthFormat = customerRepository.findBirthFormat("姬岚洋");
        System.out.println(birthFormat);
    }

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatasource() throws SQLException {
        System.out.println(dataSource.getConnection());
    }
}

