package com.project.config;

import com.project.model.*;
//import com.project.model.JWT.JwtUtil;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class BeanConfig {
	@Bean
	public DataSource dataSource() {
		DataSource ds = new DataSource();

		ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		ds.setUrl("jdbc:oracle:thin:@localhost:1521:xe");
		ds.setUsername("scott");
		ds.setPassword("tiger");
		ds.setInitialSize(5);
		ds.setMinIdle(5);
		ds.setMaxIdle(10);
		ds.setMinEvictableIdleTimeMillis(1000 * 60 * 3);
		ds.setTimeBetweenEvictionRunsMillis(1000 * 10);

		return ds;
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
	
	@Bean
	public MemberDAO memberDao(DataSource dataSource) {
		return new MemberDAO(dataSource);
	}

	@Bean
	public AttendanceDAO attendanceDAO(DataSource dataSource){
		return new AttendanceDAO(dataSource);
	}
	
	@Bean
	public CourseDAO courseDAO(DataSource dataSource){
		return new CourseDAO(dataSource);
	}
	
	@Bean
	public PostDAO postDAO(DataSource dataSource){
		return new PostDAO(dataSource);
	}
	
//	@Bean
//	public JwtUtil jwtUtil() {
//		return new JwtUtil();
//	}
}
