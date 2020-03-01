package com.tianyoukeji.parent.entity;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;


public interface TimerRepository extends JpaRepository<Timer, Long> {
	Set<Timer> findByOrgUuidAndEntityAndSource(Long uuid ,String entity, State state);
	Set<Timer> findByOrgIsNullAndEntityAndSource(String entity, State state);
}
