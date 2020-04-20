package com.tianyoukeji.org.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tianyoukeji.org.controller.DepartmentController.AddDepartmentRequest;
import com.tianyoukeji.org.controller.DepartmentController.DeleteDepartmentRequest;
import com.tianyoukeji.org.controller.DepartmentController.UpdateDepartmentRequest;
import com.tianyoukeji.org.controller.MenuController.AddMenuRequest;
import com.tianyoukeji.org.controller.MenuController.DeleteMenuRequest;
import com.tianyoukeji.org.controller.MenuController.UpdateMenuRequest;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.entity.Department;
import com.tianyoukeji.parent.entity.DepartmentRepository;
import com.tianyoukeji.parent.entity.Menu;
import com.tianyoukeji.parent.entity.MenuRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.service.BaseService;
import com.tianyoukeji.parent.service.TIMService;

@Service
public class DepartmentService extends BaseService<Department> {

	final static Logger logger = LoggerFactory.getLogger(DepartmentService.class);


	@Autowired
	private DepartmentRepository departmentRepository;
	
	@Autowired
	private OrgRepository orgRepository;
	
	
	@Autowired
	private TIMService timService;

	
	@Override
	public void init() {
	}
	
	/**
	 * 	添加部门
	 * @param 
	 */
	@Transactional
	public HttpPostReturnUuid add(AddDepartmentRequest body) {
		
		Department department = new Department();
		
		Optional<Org> findById = orgRepository.findById(body.getOrg());
		if(!findById.isPresent()) {
			throw new BusinessException(1767, "企业不存在");
		}
		Org org = findById.get();
		department.setOrg(org);
		department.setName(body.getName());
		department.setCode(body.getCode());
		Long parentId = body.getParent();
		if(parentId!=null) {
			Optional<Department> findById2 = departmentRepository.findById(parentId);
			if(!findById2.isPresent()) {
				throw new BusinessException(1767, "父部门不存在");
			}
			department.setParent(findById2.get());
		}
		department = departmentRepository.save(department);
		timService.getOrCreateEntityQun("department",department.getUuid(), "部门群", null, null);
		return new HttpPostReturnUuid(department.getUuid());
	}
	
	/**
	 * 删除部门
	 * @param body
	 * @return
	 */
	@Transactional
	public HttpPostReturnUuid delete(DeleteDepartmentRequest body) {
		try {
			departmentRepository.deleteById(body.getUuid());
		} catch (RuntimeException ex) {
			throw new BusinessException(1371, "请先删除下级部门和移除部门下的员工");
		}
		return new HttpPostReturnUuid(body.getUuid());
	}
	
	/**
	 * 更新部门
	 * @param body
	 * @return
	 */
	@Transactional
	public HttpPostReturnUuid update(UpdateDepartmentRequest body) {
		
		Optional<Department> findById4 = departmentRepository.findById(body.getUuid());
		if(!findById4.isPresent()) {
			throw new BusinessException(1391, "部门不存在");
		}
		Department department = findById4.get();
		Optional<Org> findById = orgRepository.findById(body.getOrg());
		if(!findById.isPresent()) {
			throw new BusinessException(1767, "企业不存在");
		}
		Org org = findById.get();
		department.setOrg(org);
		department.setName(body.getName());
		department.setCode(body.getCode());
		
		Long parentId = body.getParent();
		if(parentId!=null) {
			Optional<Department> findById2 = departmentRepository.findById(parentId);
			if(!findById2.isPresent()) {
				throw new BusinessException(1767, "父部门不存在");
			}
			department.setParent(findById2.get());
		}else {
			department.setParent(null);
		}
		department = departmentRepository.save(department);
		return new HttpPostReturnUuid(department.getUuid());
	}
	
}