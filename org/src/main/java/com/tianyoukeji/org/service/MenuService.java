package com.tianyoukeji.org.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tianyoukeji.org.controller.MenuController.AddMenuRequest;
import com.tianyoukeji.org.controller.MenuController.DeleteMenuRequest;
import com.tianyoukeji.org.controller.MenuController.UpdateMenuRequest;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.entity.Menu;
import com.tianyoukeji.parent.entity.MenuRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.service.BaseService;

@Service
public class MenuService extends BaseService<Menu> {

	final static Logger logger = LoggerFactory.getLogger(MenuService.class);


	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private OrgRepository orgRepository;
	
	@Autowired
	private MenuRepository menuRepository;
	
	@Override
	public void init() {
	}
	
	/**
	 * 	添加菜单
	 * @param 
	 */
	@Transactional
	public HttpPostReturnUuid add(AddMenuRequest body) {
		
		Menu menu = new Menu();
		
		Optional<Org> findById = orgRepository.findById(body.getOrg());
		if(!findById.isPresent()) {
			throw new BusinessException(1767, "企业不存在");
		}
		Org org = findById.get();
		menu.setOrg(org);
		menu.setName(body.getName());
		menu.setCode(body.getCode());
		menu.setIconUrl(body.getIconUrl());
		menu.setUrl(body.getUrl());
		menu.setSort(body.getSort());
		
		Long parentId = body.getParent();
		if(parentId!=null) {
			Optional<Menu> findById2 = menuRepository.findById(parentId);
			if(!findById2.isPresent()) {
				throw new BusinessException(1767, "父菜单不存在");
			}
			menu.setParent(findById2.get());
		}
		Set<Long> roles = body.getRoles();
		if(roles!=null) {
			HashSet<Role> hashSet = new HashSet<Role>();
			for (Long role : roles) {
				Optional<Role> findById3 = roleRepository.findById(role);
				if(findById.isPresent()) {
					hashSet.add(findById3.get());
				}
			}
			menu.setRoles(hashSet);
		}
		menu = menuRepository.save(menu);
		return new HttpPostReturnUuid(menu.getUuid());
	}
	
	/**
	 * 删除菜单
	 * @param body
	 * @return
	 */
	@Transactional
	public HttpPostReturnUuid delete(DeleteMenuRequest body) {
		try {
			menuRepository.deleteById(body.getUuid());
		} catch (RuntimeException ex) {
			throw new BusinessException(1371, "请先删除或改变下级的菜单和角色的关联");
		}
		return new HttpPostReturnUuid(body.getUuid());
	}
	
	/**
	 * 更新菜单
	 * @param body
	 * @return
	 */
	@Transactional
	public HttpPostReturnUuid update(UpdateMenuRequest body) {
		
		Optional<Menu> findById4 = menuRepository.findById(body.getUuid());
		if(!findById4.isPresent()) {
			throw new BusinessException(1391, "菜单不存在");
		}
		Menu menu = findById4.get();
		Optional<Org> findById = orgRepository.findById(body.getOrg());
		if(!findById.isPresent()) {
			throw new BusinessException(1767, "企业不存在");
		}
		Org org = findById.get();
		menu.setOrg(org);
		menu.setName(body.getName());
		menu.setCode(body.getCode());
		menu.setIconUrl(body.getIconUrl());
		menu.setUrl(body.getUrl());
		menu.setSort(body.getSort());
		
		Long parentId = body.getParent();
		if(parentId!=null) {
			Optional<Menu> findById2 = menuRepository.findById(parentId);
			if(!findById2.isPresent()) {
				throw new BusinessException(1767, "父菜单不存在");
			}
			menu.setParent(findById2.get());
		}else {
			menu.setParent(null);
		}
		Set<Long> roles = body.getRoles();
		if(roles!=null) {
			HashSet<Role> hashSet = new HashSet<Role>();
			for (Long role : roles) {
				Optional<Role> findById3 = roleRepository.findById(role);
				if(findById.isPresent()) {
					hashSet.add(findById3.get());
				}
			}
			menu.setRoles(hashSet);
		}
		menu = menuRepository.save(menu);
		return new HttpPostReturnUuid(menu.getUuid());
	}
	
}