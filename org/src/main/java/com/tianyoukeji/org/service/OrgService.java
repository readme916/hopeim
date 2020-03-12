package com.tianyoukeji.org.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.service.BaseService;
import com.tianyoukeji.parent.service.TIMService;

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
	private UserRepository userRepository;

	@Autowired
	private TIMService timService;
	
	/**
	 * 创建默认企业
	 */
	@Override
	public void init() {
	}
	
	public void addUser(String unionId) {
		User findByUnionId = userRepository.findByUnionId(unionId);
		if(findByUnionId==null) {
			throw new BusinessException(1766, "用户不存在");
		}
		if(findByUnionId.getOrg()!=null) {
			throw new BusinessException(1767, "用户已有企业");
		}
		findByUnionId.setOrg(getCurrentOrg());
		userRepository.save(findByUnionId);
		timService.addOrgFriends(findByUnionId.getUserinfo().getMobile(), getCurrentOrg());
	}
	
	public void deleteUser(String unionId) {
		User findByUnionId = userRepository.findByUnionId(unionId);
		if(findByUnionId==null) {
			throw new BusinessException(1716, "用户不存在");
		}
		if(findByUnionId.getOrg()==null) {
			throw new BusinessException(1769, "用户没有加入企业");
		}
		if(!findByUnionId.getOrg().getUuid().equals(getCurrentOrg().getUuid())) {
			throw new BusinessException(1787, "无权删除企业用户");
		}
		findByUnionId.setOrg(null);
		userRepository.save(findByUnionId);
		timService.deleteOrgFriends(findByUnionId.getUserinfo().getMobile(), getCurrentOrg());
	}
}