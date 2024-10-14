package com.project.model;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CourseDAO {
	private JdbcTemplate jdbcTemplate;
	private String sql;

	public CourseDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public CourseDO getCourseScore(int course_id) {
		this.sql="select * from final_course where course_id=?";
		return this.jdbcTemplate.queryForObject(sql, new CourseRowMapper(),course_id);
	}
	
	public List<CourseReg> getCourseReg(int course_id) {
		this.sql="select m_name, m_dept, fm.member_id from final_member fm inner join final_course_register fcr on fm.member_id=fcr.member_id where course_id=? and c_regstatus=0";
		return this.jdbcTemplate.query(sql, new RowMapper<CourseReg>() {
			@Override
			public CourseReg mapRow(ResultSet rs, int rownum) throws SQLException{
				CourseReg courseReg = new CourseReg();
				courseReg.setM_name(rs.getString("m_name"));
				courseReg.setM_dept(rs.getString("m_dept"));
				courseReg.setMember_id(rs.getInt("member_id"));
				return courseReg;
			}
		},course_id);
	}
	
	public List<CourseReg> searchMemberReg(int course_id, String searchType, String searchText){
		if(searchType.equals("name")) {
			this.sql = "select m_name, m_dept, fm.member_id from final_member fm inner join final_course_register fcr on fm.member_id=fcr.member_id where course_id=? and c_regstatus=0 and m_name like '%"+ searchText +"%'";	
		}else {
			this.sql = " select m_name, m_dept, fm.member_id from final_member fm inner join final_course_register fcr on fm.member_id=fcr.member_id where course_id=? and c_regstatus=0 and m_dept like '%"+ searchText +"%'";	
		}
		
		return this.jdbcTemplate.query(this.sql, new RowMapper<CourseReg>() {
			@Override
			public CourseReg mapRow(ResultSet rs, int rownum) throws SQLException{
				CourseReg courseReg = new CourseReg();
				courseReg.setM_name(rs.getString("m_name"));
				courseReg.setM_dept(rs.getString("m_dept"));
				courseReg.setMember_id(rs.getInt("member_id"));
				
				return courseReg;
			}
		},course_id);
	}
	
	public void approveCourseReg(int course_id, int member_id) {
		this.sql="update final_course_register set c_regstatus=1 where course_id=? and member_id=?";
		this.jdbcTemplate.update(sql, course_id, member_id);
	}
	
	public void rejectCourseReg(int course_id, int member_id) {
		this.sql="delete from final_course_register where course_id=? and member_id=?";
		this.jdbcTemplate.update(sql, course_id, member_id);
	}
	
	public void insertStudent(int course_id, int member_id) {
		this.sql="insert into final_course_student (student_id, course_id, member_id) values (seq_student_id.nextval, ?, ?)";
		this.jdbcTemplate.update(sql, course_id, member_id);
	}


}
