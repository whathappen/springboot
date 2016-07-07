package com.springboot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.springboot.SendAndStorage.SendHQ;
import com.springboot.SendAndStorage.StorageDB;
import com.springboot.pojo.User;
@Controller
public class SimpleController {
	
	@Autowired
	private SendHQ shq;
	
	@Autowired
	private StorageDB sdb;
	
	@RequestMapping(value="/",method = RequestMethod.GET)
	public ModelAndView hello(){
		return new ModelAndView("hello");
		
	}
	
	@ResponseBody
	@RequestMapping(value="/test",method = RequestMethod.POST)
	public ModelAndView test(User user){
		System.out.println(user.getUserName()+user.getUserPassword());
		return new ModelAndView("redirect:/");
		
	}
	
	@ResponseBody
	@RequestMapping(value="/sendHQ",method = RequestMethod.GET,produces = "application/json; charset=utf-8")
	public Object preSendHQ(){	
		int poolSize = 4;
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager();
        connMgr.setMaxTotal(poolSize + 1);
        connMgr.setDefaultMaxPerRoute(poolSize);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connMgr).build();
        RestTemplate template = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
        FastJsonHttpMessageConverter fastjson = new FastJsonHttpMessageConverter();
        fastjson.setFeatures(SerializerFeature.WriteClassName, SerializerFeature.BrowserCompatible, SerializerFeature.DisableCircularReferenceDetect);
        converters.add(fastjson);
        template.setMessageConverters(converters);
        String url = "http://localhost:8088/send";
        List<Object> pojoList = new ArrayList<Object>();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Random rd = new Random();
		for (int i = 0; i < 10; i++) {
			User user = new User();
			user.setUserName("test-00" + i);
			user.setAge(rd.nextInt(60 - 18) + 18);
			user.setUserPassword("password" + rd.nextInt());
			user.setUserAddr("浙江省杭州市");
			pojoList.add((Object) user);
		}
		return JSONObject.toJSONString(template.postForObject(url, pojoList, List.class),true);
	}
	
	@ResponseBody
	@RequestMapping(value="/send",method = RequestMethod.GET)
	public Object realSendHQ(@RequestBody List<Object> pojoList){	
		return JSON.parse(shq.sendHQ(pojoList));
	}
	
	@ResponseBody
	@RequestMapping(value="/sendtest",method = RequestMethod.POST)
	public Map<String,Object> sendhq(User user){
		List<Object> pojoList = new ArrayList<Object>();
		pojoList.add(user);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("message", decodeUnicode(shq.sendHQ(pojoList)));
		return map;
	}
	
	@ResponseBody
	@RequestMapping(value="/storageDB",method = RequestMethod.POST)
	public void StorageDB(){	
		sdb.receiveHQ();
	}
	
	public static String decodeUnicode(String theString) {  
        char aChar;  
        int len = theString.length();  
        StringBuffer outBuffer = new StringBuffer(len);  
        for (int x = 0; x < len;) {  
            aChar = theString.charAt(x++);  
            if (aChar == '\\') {  
                aChar = theString.charAt(x++);  
                if (aChar == 'u') {  
                    // Read the xxxx  
                    int value = 0;  
                    for (int i = 0; i < 4; i++) {  
                        aChar = theString.charAt(x++);  
                        switch (aChar) {  
                        case '0':  
                        case '1':  
                        case '2':  
                        case '3':  
                        case '4':  
                        case '5':  
                        case '6':  
                        case '7':  
                        case '8':  
                        case '9':  
                            value = (value << 4) + aChar - '0';  
                            break;  
                        case 'a':  
                        case 'b':  
                        case 'c':  
                        case 'd':  
                        case 'e':  
                        case 'f':  
                            value = (value << 4) + 10 + aChar - 'a';  
                            break;  
                        case 'A':  
                        case 'B':  
                        case 'C':  
                        case 'D':  
                        case 'E':  
                        case 'F':  
                            value = (value << 4) + 10 + aChar - 'A';  
                            break;  
                        default:  
                            throw new IllegalArgumentException(  
                                    "Malformed   \\uxxxx   encoding.");  
                        }  
      
                    }  
                    outBuffer.append((char) value);  
                } else {  
                    if (aChar == 't')  
                        aChar = '\t';  
                    else if (aChar == 'r')  
                        aChar = '\r';  
                    else if (aChar == 'n')  
                        aChar = '\n';  
                    else if (aChar == 'f')  
                        aChar = '\f';  
                    outBuffer.append(aChar);  
                }  
            } else  
                outBuffer.append(aChar);  
        }  
        return outBuffer.toString();  
    }

}
