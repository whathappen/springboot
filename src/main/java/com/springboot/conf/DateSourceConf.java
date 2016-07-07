package com.springboot.conf;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class DateSourceConf {
	
	@Value("${jdbc.type:Mysql}")
	private String type;
	
	@Bean
	@ConfigurationProperties(prefix = "jdbc")
    public DataSource toDataSource(){
    	return DataSourceBuilder.create().build();
    }
	
	@Autowired
	@Qualifier("toDataSource")
	private DataSource toDataSource;
	
	@Bean(destroyMethod = "shutdown",name="ThreadPool")
    public ThreadPoolExecutor ThreadPool() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        return executor;
    }
	
	@Bean
    public SqlSessionFactory toSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(toDataSource);
        bean.setConfigLocation(new ClassPathResource(String.format("DSMybatisConfig%s.xml", type)));
        return bean.getObject();
    }
}
