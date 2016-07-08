package com.springboot.conf;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

@Configuration
@EnableConfigurationProperties
public class AeroSpikeConfig {

	@Value("${aerospike.host:192.168.188.128}")
	private String host;

	@Value("${aerospike.port:3000}")
	private int port;

	@Bean
	@Primary
	public AerospikeClient asClient() {

		AerospikeClient client = new AerospikeClient(host, port);
		return client;
	}

	@Bean
	public KryoPool kryoPool() {
		KryoFactory factory = new KryoFactory() {
			public Kryo create() {
				Kryo kryo = new Kryo();
				return kryo;
			}
		};
		KryoPool pool = new KryoPool.Builder(factory).softReferences().build();
		return pool;
	}
}
