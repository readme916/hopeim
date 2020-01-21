package com.tianyoukeji.parent.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface StateRepository extends JpaRepository<State, Long> {

	List<State> findByEntity(String entityName);
}
