package com.springboot.aerospike.test;


import java.security.Policy;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;

public class AeroSpikeTest {
	
	private AerospikeClient client = null;
	
	@Before
	public void setUp(){
		AerospikeClient client = new AerospikeClient("192.168.188.128", 3000);
		this.client = client;
	}
	
	@Test
	public void asWriteTest(){
		// Initialize policy.
		WritePolicy policy = new WritePolicy();
		// Write single value.
		policy.expiration = 100;
		policy.timeout = 50;  // 50 millisecond timeout.
		for(int i = 0 ; i < 10 ; i ++){
			Key key = new Key("bsfit", "myset", "mykey");
			Bin bin = new Bin("mybin"+i, i);
			client.put(policy, key, bin);
		}		
	}
	
	@Test
	public void asDeleteByBin(){
		WritePolicy policy = new WritePolicy();
		Key key = new Key("bsfit", "myset", "mykey");
		Bin bin1 = Bin.asNull("mybin1"); // Set bin value to null to drop bin.
		client.put(policy, key, bin1);
	}
	
	@Test
	public void asDeleteByKey(){
		Key key = new Key("bsfit", "myset", "mykey");
		client.delete(null, key);
	}
	
	@Test
	public void asRead(){
		Key key = new Key("bsfit", "myset", "mykey");
		Record record = client.get(null, key);
		Iterator<?> it = record.bins.entrySet().iterator();
		while(it.hasNext()){
			Entry<?, ?> entry = (Entry<?, ?>) it.next();
			System.out.println("key:"+entry.getKey()+"----value:"+entry.getValue());
		}

	}

}
