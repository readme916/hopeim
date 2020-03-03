package com.tianyoukeji.parent.entity;

import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Role, Long> {

	Role findByCodeAndOrgIsNull(String code);
	Role findByCodeAndOrg(Org org);
}
