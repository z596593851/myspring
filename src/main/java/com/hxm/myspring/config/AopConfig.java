package com.hxm.myspring.config;

import com.hxm.myspring.aop.LogAspects;
import com.hxm.myspring.aop.MathCalculator;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

//@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScan("com.hxm")
@Configuration

public class AopConfig {

    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource getDateSource() {
        return DataSourceBuilder.create().build();
    }


    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory testSqlSessionFactory(@Qualifier("dataSource") DataSource datasource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(datasource);
        //开启驼峰规则
        bean.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
        return bean.getObject();
    }
    @Bean("sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate testsqlsessiontemplate(
            @Qualifier("sqlSessionFactory") SqlSessionFactory sessionfactory) {
        return new SqlSessionTemplate(sessionfactory);
    }

    @Bean
    public MyAdvisor myAdvisor(){
        MyAdvisor myAdvisor=new MyAdvisor();
        myAdvisor.setAdvice(myInterceptor());
        return myAdvisor;
    }

    @Bean
    public MyInterceptor myInterceptor(){
        return new MyInterceptor();
    }

    @Bean
    public MyAttributeSource myAttributeSource(){
        return new MyAttributeSource();
    }

    @Bean
    public MyPointCut myPointCut(){
        return new MyPointCut();
    }

}
