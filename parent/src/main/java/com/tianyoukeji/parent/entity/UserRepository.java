package com.tianyoukeji.parent.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

	User findByUserinfoMobile(String mobile);
	User findByUnionId(String unionId);
	
	List<User> findByUuidIn(List<Long> ids);
}
