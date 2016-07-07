package com.springboot.SendAndStorage;

import java.util.LinkedHashMap;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.JmsUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.springboot.pojo.User;

@Service
public class SendHQ {

	Logger logger = LoggerFactory.getLogger(SendHQ.class);
	
	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier("preDSQueue")
	private Queue preDSQueue;

	// @Scheduled(fixedDelay = 1000L)
	// @PostConstruct
	public String sendHQ(List<Object> pojoList) {
		final String msgTxt = JSON.toJSONString(pojoList,
				SerializerFeature.BrowserCompatible,
				SerializerFeature.WriteClassName,
				SerializerFeature.DisableCircularReferenceDetect);
		jmsTemplate.send(preDSQueue, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(msgTxt);
			}
		});
		return msgTxt.toString();
	}
	
}
