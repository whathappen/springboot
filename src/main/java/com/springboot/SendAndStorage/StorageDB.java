package com.springboot.SendAndStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsUtils;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.springboot.pojo.User;

@Service
public class StorageDB {
	Logger logger = LoggerFactory.getLogger(StorageDB.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier("ThreadPool")
	private ThreadPoolExecutor executor;

	@Autowired
	@Qualifier("toSqlSessionFactory")
	private SqlSessionFactory toFactory;

	@Autowired
	@Qualifier("preDSQueue")
	private Queue preDSQueue;

	@Value("${publish.db:true}")
	private boolean isInsert;
	protected ScheduledExecutorService scheduledService = Executors
			.newScheduledThreadPool(1);


	@SuppressWarnings("unchecked")
	// @Scheduled(fixedDelay = 1000L)
	// @PostConstruct
	public void receiveHQ() {
		if (isInsert) {

			Connection conToClose = null;
			Session sessionToClose = null;
			MessageConsumer consumerToClose = null;
			try {
				Connection conToUse = jmsTemplate.getConnectionFactory()
						.createConnection();
				conToClose = conToUse;
				conToUse.start();

				Session sessionToUse = conToUse.createSession(true,
						Session.SESSION_TRANSACTED);
				sessionToClose = sessionToUse;

				MessageConsumer consumerToUse = sessionToUse
						.createConsumer(preDSQueue);
				consumerToClose = consumerToUse;
				long beg = System.currentTimeMillis();
				Message message = null;
				List<Object> objs;
				List<Object> list = new ArrayList<Object>();
				do {
					message = consumerToUse.receive(100);
					if (message != null) {
						objs = (List<Object>) JSON
								.parse(((TextMessage) message).getText());
						list.addAll(objs);
					}
				} while ((message != null));
				long end = System.currentTimeMillis();
				if (list.size() > 0) {
					logger.info("receive object size : [{}], latency:[{}]ms",
							list.size(), end - beg);
					doTask(list);
					sessionToUse.commit();
				}
			} catch (JMSException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				JmsUtils.closeMessageConsumer(consumerToClose);
				JmsUtils.closeSession(sessionToClose);
				ConnectionFactoryUtils.releaseConnection(conToClose,
						jmsTemplate.getConnectionFactory(), true);
			}
		}
	}
	
	public void doTask(List<Object> list) throws InterruptedException {
		if (list == null || list.isEmpty()) {
			return;
		}
		List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
		List<User> users = new ArrayList<User>();
		for (Object object : list) {
			if (object instanceof User)
				users.add((User) object);
		}
		if (users.size() > 0) {
			tasks.add(new UserReceiveTask(toFactory, isInsert, users));
		}
		executor.invokeAll(tasks);
	}
}
