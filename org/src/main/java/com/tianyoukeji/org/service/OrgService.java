package com.tianyoukeji.org.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.Department;
import com.tianyoukeji.parent.entity.DepartmentRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.entity.User.Position;
import com.tianyoukeji.parent.service.BaseService;
import com.tianyoukeji.parent.service.TIMService;

@Service
public class OrgService extends BaseService<Org> {

	final static Logger logger = LoggerFactory.getLogger(OrgService.class);


	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TIMService timService;
	
	@Override
	public void init() {
	}
	
	/**
	 * 	企业添加员工
	 * @param unionId
	 */
	@Transactional
	public void addUser(String unionId) {
		User findByUnionId = userRepository.findByUnionId(unionId);
		if(findByUnionId==null) {
			throw new BusinessException(1766, "用户不存在");
		}
		if(findByUnionId.getOrg()!=null) {
			throw new BusinessException(1767, "用户已有企业");
		}
		findByUnionId.setOrg(getCurrentOrg());
		
		Role findByCode = roleRepository.findByCode("platform_employee");
		findByUnionId.setRole(findByCode);
		userRepository.save(findByUnionId);
		timService.addOrgFriends(findByUnionId.getUserinfo().getMobile(), getCurrentOrg());
	}
	
	/**
	 * 	企业删除员工
	 * @param unionId
	 */
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
		
		Department department = findByUnionId.getDepartment();
		if(department.getGroupId()!=null) {
			timService.quitQun(findByUnionId.getUserinfo().getMobile(), department.getGroupId());
		}
		Role findByCode = roleRepository.findByCode("user");
		findByUnionId.setRole(findByCode);
		findByUnionId.setOrg(null);
		findByUnionId.setDepartment(null);
		userRepository.save(findByUnionId);
		timService.deleteOrgFriends(findByUnionId.getUserinfo().getMobile(), getCurrentOrg());
		
	}
	
	/**
	 * 	企业分配员工部门
	 * @param 
	 */
	public void locateUserDepartment(Long uuid, Long departmentId) {
		Optional<User> findById = userRepository.findById(uuid);
		if(!findById.isPresent()) {
			throw new BusinessException(1716, "用户不存在");
		}
		
		User user = findById.get();
		if(user.getOrg()==null) {
			throw new BusinessException(1769, "用户没有加入企业");
		}
		if(!user.getOrg().getUuid().equals(getCurrentOrg().getUuid())) {
			throw new BusinessException(1787, "无权分配企业用户");
		}
		Optional<Department> departmentOptional = departmentRepository.findById(departmentId);
		if(!departmentOptional.isPresent()) {
			throw new BusinessException(1712, "部门不存在");
		}
		if(!departmentOptional.get().getOrg().getUuid().equals(getCurrentOrg().getUuid())) {
			throw new BusinessException(1789, "部门不属于自己企业");
		}
		
		Department department = user.getDepartment();
		if(department.getGroupId()!=null) {
			timService.quitQun(findById.get().getUserinfo().getMobile(), department.getGroupId());
		}
		
		user.setDepartment(departmentOptional.get());
		userRepository.save(user);
		if(departmentOptional.get().getGroupId()!=null) {
			timService.joinQun(findById.get().getUserinfo().getMobile(), departmentOptional.get().getGroupId());
		}
		timService.updateUser(findById.get().getUserinfo().getMobile(), null, null, null, null, null, departmentId);
	}
	
	/**
	 * 	企业分配部门主管
	 * @param 
	 */
	public void locateManagerDepartment(Long mangerId, Long departmentId) {
		Optional<User> findById = userRepository.findById(mangerId);
		if(!findById.isPresent()) {
			throw new BusinessException(1716, "用户不存在");
		}
		
		User user = findById.get();
		if(user.getOrg()==null) {
			throw new BusinessException(1769, "用户没有加入企业");
		}
		if(!user.getOrg().getUuid().equals(getCurrentOrg().getUuid())) {
			throw new BusinessException(1787, "无权分配企业用户");
		}
		Optional<Department> departmentOptional = departmentRepository.findById(departmentId);
		if(!departmentOptional.isPresent()) {
			throw new BusinessException(1712, "部门不存在");
		}
		if(!departmentOptional.get().getOrg().getUuid().equals(getCurrentOrg().getUuid())) {
			throw new BusinessException(1789, "部门不属于自己企业");
		}
		Department department = departmentOptional.get();
		if(department.getManager()!=null) {
			User manager = department.getManager();
			//如果没有变化，直接返回
			if(manager.getUuid().equals(mangerId)) {
				return;
			}
			//部门主管，可能管理多个部门，所以判断是否完全辞去主管职位
			List<Department> findByManager = departmentRepository.findByManager(manager);
			if(findByManager.size()==1) {
				manager.setPosition(null);
			}
			userRepository.save(manager);
		}
		
		department.setManager(user);
		departmentRepository.save(department);
		user.setPosition(Position.MANAGER);
		userRepository.save(user);

	}
	/**
	 * 	企业分配员工角色
	 * @param 
	 */
	public void locateUserRole(Long uuid, Long roleId) {
		Optional<User> findById = userRepository.findById(uuid);
		if(!findById.isPresent()) {
			throw new BusinessException(1716, "用户不存在");
		}
		
		User user = findById.get();
		if(user.getOrg()==null) {
			throw new BusinessException(1769, "用户没有加入企业");
		}
		if(!user.getOrg().getUuid().equals(getCurrentOrg().getUuid())) {
			throw new BusinessException(1787, "无权分配企业用户");
		}
		Optional<Role> roleOptional = roleRepository.findById(roleId);
		if(!roleOptional.isPresent()) {
			throw new BusinessException(1712, "角色不存在");
		}
		if(!user.getOrg().getRoles().stream().anyMatch(r -> r.getUuid().equals(roleId))) {
			throw new BusinessException(1713, "角色不属于公司");
		}
		user.setRole(roleOptional.get());
		userRepository.save(user);
		timService.updateUser(findById.get().getUserinfo().getMobile(), null, null, null, null, roleId, null);
	}
	
}