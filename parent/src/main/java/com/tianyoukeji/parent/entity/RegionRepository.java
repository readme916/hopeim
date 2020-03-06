package com.tianyoukeji.parent.entity;

import org.springframework.data.jpa.repository.JpaRepository;


public interface RegionRepository extends JpaRepository<Region, Long> {

	Region findByFullname(String fullname);
}
