package org.entropy.blogcomment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.entropy.blogcomment.mapper")
public class BlogCommentApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogCommentApplication.class, args);
	}

}
