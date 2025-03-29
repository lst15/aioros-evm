package com.aioros.aioros;

import com.aioros.aioros.implementations.AbstractClientContext;
import org.apache.commons.configuration2.ex.ConfigurationException;

//@SpringBootApplication
public class AiorosApplication {

	public static void main(String[] args) throws ConfigurationException {
		AbstractClientContext x = new AbstractClientContext();
		x.test();
//		SpringApplication.run(AiorosApplication.class, args);
	}

}
