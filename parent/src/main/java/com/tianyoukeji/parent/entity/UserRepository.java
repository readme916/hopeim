package com.tianyoukeji.parent.entity;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

	User findByUserinfoMobile(String mobile);
}
