package com.tianyoukeji.org.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.service.BaseService;

@Service
public class OrgService extends BaseService<Org> {

	final static Logger logger = LoggerFactory.getLogger(OrgService.class);

	@Autowired
	private OrgTemplateService orgTemplateService;

	@Autowired
	private StateTemplateService stateTemplateService;

	@Autowired
	private OrgRepository orgRepository;

	@Autowired
	private UserService userService;

	/**
	 * 创建默认企业
	 */
	@Override
	public void init() {
	}

}