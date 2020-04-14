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
import com.tianyoukeji.org.controller.RoleController.AddRoleRequest;
import com.tianyoukeji.org.controller.RoleController.DeleteRoleRequest;
import com.tianyoukeji.org.controller.RoleController.UpdateRoleRequest;
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
public class RoleService extends BaseService<Role> {

	final static Logger logger = LoggerFactory.getLogger(RoleService.class);


	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private OrgRepository orgRepository;
	
	@Override
	public void init() {
	}
	
	/**
	 * 	为企业添加角色
	 * @param 
	 */
	@Transactional
	public HttpPostReturnUuid add(AddRoleRequest body) {
		
		Role role = new Role();
		Optional<Org> findById = orgRepository.findById(body.getOrg());
		if(!findById.isPresent()) {
			throw new BusinessException(1767, "企业不存在");
		}
		Org org = findById.get();
		role.setName(body.getName());
		role.setCode(body.getCode());
		role.setTerminal(body.getTerminal());
		role = roleRepository.save(role);
		Set<Role> roles = org.getRoles();
		if(roles==null) {
			roles = new HashSet<Role>();
		}
		roles.add(role);
		orgRepository.save(org);
		
		return new HttpPostReturnUuid(role.getUuid());
	}
	
	/**
	 * 	给企业删除角色
	 * @param body
	 * @return
	 */
	@Transactional
	public HttpPostReturnUuid delete(DeleteRoleRequest body) {
		
		Optional<Role> findById = roleRepository.findById(body.getUuid());
		if(!findById.isPresent()) {
			throw new BusinessException(1761, "角色不存在");
		}
		Role role = findById.get();
		if(role.getOrgs() == null || role.getOrgs().size() == 0){
			try {
				roleRepository.deleteById(body.getUuid());
			} catch (RuntimeException ex) {
				throw new BusinessException(1311, "请先删除所有的角色的关联");
			}
		}
		
		if(role.getOrgs().size() == 1) {
			Org org = role.getOrgs().iterator().next();
			org.setRoles(new HashSet<Role>());
			orgRepository.saveAndFlush(org);
			try {
				roleRepository.deleteById(body.getUuid());
			} catch (RuntimeException ex) {
				throw new BusinessException(1311, "请先删除所有的角色的关联");
			}
		}else {
			
			//todo 把企业内的role相应的关联检索一遍，如果发现存在，则抛出异常提醒手动删除
			
			
			
			//都删除干净以后，最后删除role和企业的关联
			Optional<Org> findFirst = role.getOrgs().stream().filter(o -> o.getUuid().equals(body.getOrg())).findFirst();
			if(findFirst.isPresent()) {
				Set<Role> roles = findFirst.get().getRoles();
				roles.removeIf(r -> r.getUuid().equals(body.getUuid()));
				orgRepository.save(findFirst.get());
			}
		}
	
		return new HttpPostReturnUuid(body.getUuid());
	}
	
	/**
	 * 	更新角色
	 * @param body
	 * @return
	 */
	@Transactional
	public HttpPostReturnUuid update(UpdateRoleRequest body) {
		
		Optional<Role> findById4 = roleRepository.findById(body.getUuid());
		if(!findById4.isPresent()) {
			throw new BusinessException(1391, "角色不存在");
		}
		Role role = findById4.get();
		Optional<Org> findById = orgRepository.findById(body.getOrg());
		if(!findById.isPresent()) {
			throw new BusinessException(1767, "企业不存在");
		}
		Org org = findById.get();
		role.setName(body.getName());
		role.setCode(body.getCode());
		role.setTerminal(body.getTerminal());
		Set<Role> roles = org.getRoles();
		if(roles==null) {
			roles = new HashSet<Role>();
		}
		roles.add(role);
		orgRepository.save(org);
		return new HttpPostReturnUuid(role.getUuid());
	}
	
}