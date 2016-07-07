/**
 * 
 */
package com.springboot.conf;

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.NamingException;

import org.hornetq.api.jms.HornetQJMSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

@Configuration
public class JmsConfig {
	@Value("${jms.initial:org.jnp.interfaces.NamingContextFactory}")
	private String initial;
	@Value("${jms.url:jnp://localhost:1099}")
	private String url;
	@Value("${jms.pkgs:org.jboss.naming:org.jnp.interfaces}")
	private String pkgs;
	@Value("${jms.name:/ConnectionFactory}")
	private String name;

	@Autowired
	private JndiTemplate jndiTemplate;
	@Autowired
	@Qualifier("connectionFactory")
	private ConnectionFactory factory;
	@Autowired
	@Qualifier("cachedConnectionFactory")
	private CachingConnectionFactory cFactory;

	@Bean
	public JndiTemplate jndiTemplate() {
		Properties prop = new Properties();
		prop.put("java.naming.factory.initial", initial);
		prop.put("java.naming.provider.url", url);
		prop.put("java.naming.factory.url.pkgs", pkgs);
		JndiTemplate jndiTemplate = new JndiTemplate(prop);
		return jndiTemplate;
	}

	@Bean
	public ConnectionFactory connectionFactory()
			throws IllegalArgumentException, NamingException {
		JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
		bean.setJndiName(name);
		bean.setJndiTemplate(jndiTemplate);
		bean.afterPropertiesSet();
		ConnectionFactory factory = (ConnectionFactory) bean.getObject();
		return factory;
	}

	@Bean
	public CachingConnectionFactory cachedConnectionFactory() {
		CachingConnectionFactory cFactory = new CachingConnectionFactory();
		cFactory.setTargetConnectionFactory(factory);
		cFactory.setSessionCacheSize(20);
		cFactory.setReconnectOnException(true);
		return cFactory;
	}

	@Bean
	// @Qualifier("jmsTemplate")
	public JmsTemplate jmsTemplate() {
		JmsTemplate jmsTemplate = new JmsTemplate(cFactory);
		return jmsTemplate;
	}

	/**
	 * 统一的预处理队列
	 * 
	 * @return
	 */
	@Bean
	public Queue preDSQueue() {
		return HornetQJMSClient.createQueue("PreDSQueue");
	}

}

