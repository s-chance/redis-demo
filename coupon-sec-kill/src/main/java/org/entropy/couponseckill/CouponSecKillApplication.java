package org.entropy.couponseckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.entropy.couponseckill.mapper")
public class CouponSecKillApplication {

	public static void main(String[] args) {
		SpringApplication.run(CouponSecKillApplication.class, args);
	}

}
