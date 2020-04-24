package com.tianyoukeji.parent.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

	Department findByCodeAndOrg(String code , Org org);
	List<Department> findByManager(User manager);
}
