package com.tianyoukeji.org.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.service.BaseService;

@Service
public class StateService extends BaseService<State> {

	final static Logger logger = LoggerFactory.getLogger(StateService.class);
	@Override
	public void init() {
	}

}