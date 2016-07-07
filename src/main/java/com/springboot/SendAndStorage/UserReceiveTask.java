package com.springboot.SendAndStorage;

import java.util.List;
import java.util.concurrent.Callable;

import javax.jms.Queue;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import com.springboot.mapper.UserMapper;
import com.springboot.pojo.User;

public class UserReceiveTask implements Callable<Integer> {

	Logger logger = LoggerFactory.getLogger(UserReceiveTask.class);

	private SqlSessionFactory toFactory;
	private boolean isInsert;
	private List<User> objList;


	public UserReceiveTask(SqlSessionFactory toFactory, boolean isInsert,
			List<User> objList) {
		super();
		this.toFactory = toFactory;
		this.isInsert = isInsert;
		this.objList = objList;
	}
	@Override
	public Integer call() throws Exception {
		int tCount = 0;
		tCount = doLoader(objList);
		return tCount;
	}

	private int doLoader(List<User> users) {
		if (isInsert) {
			saveUsers(users);
		}
		int cc = users.size();
		return cc;
	}

	public void saveUsers(List<User> users) {
		logger.info("正在往User表插入{}条数据o>.<o", users.size());
		if (users != null && users.size() > 0) {
			SqlSession sqlSession = null;
			UserMapper mapper = null;
			try {
				sqlSession = toFactory.openSession(ExecutorType.BATCH, false);
				mapper = sqlSession.getMapper(UserMapper.class);
				for (User user : users) {
					if (user != null) {
						mapper.insertSelective(user);
					}
				}
				sqlSession.commit();
				sqlSession.clearCache();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (sqlSession != null)
					sqlSession.close();
				logger.info("数据插入完毕！");
			}
		}
	}

}
