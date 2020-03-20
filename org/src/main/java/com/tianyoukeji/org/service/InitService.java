package com.tianyoukeji.org.service;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.entity.template.OrgTemplateRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;
import com.tianyoukeji.parent.entity.template.StateTemplateRepository;
import com.tianyoukeji.parent.service.StateMachineService;

@Service
public class InitService {

	@Autowired
	private StateTemplateRepository stateTemplateRepository;

	@Autowired
	private OrgRepository orgRepository;

	@Autowired
	private OrgTemplateRepository orgTemplateRepository;

	@Autowired
	private OrgTemplateService orgTemplateService;

	@Autowired
	private StateTemplateService stateTemplateService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private StateRepository stateRepository;

	/**
	 * 创建用户的状态机
	 */
	@PostConstruct
	public void init() {
		if (orgTemplateRepository.count() == 0) {
			com.tianyoukeji.org.service.OrgTemplateService.Builder builder = orgTemplateService.getBuilder()
					.code("platform").name("天邮科技总平台模板").department("部门1", "department1", null)
					.department("部门2", "department2", "department1")
					.department("部门2-2", "department2_2", "department2")
					.department("部门3", "department3", "department1")
					.role("平台超管", "platform_super", Terminal.ORG)
					.role("平台管理员", "platform_manager", Terminal.ORG)
					.role("平台员工", "platform_employee", Terminal.ORG)
					.menu("主页", "home", "/", 0, null).menu("第一页", "one", "/one", 1, "home")
					.menu("第二页", "two", "/two", 2, "home").menu("第一页二级页", "one_one", "/oneone", 3, "one");
			builder.getMenu("home").addRole("platform_manager").addRole("platform_super");
			builder.getMenu("one").addRole("platform_super");
			builder.getMenu("two").addRole("platform_manager");
			builder.getMenu("one_one").addRole("platform_super");
			builder.build();
		}

		if (stateTemplateRepository.count() == 0) {
			com.tianyoukeji.org.service.StateTemplateService.Builder builder = stateTemplateService.getBuilder();
			builder.entity("user")
					.state(10,"created", "初始状态", true, false, false, null, null, null, null, null, null, null)
					.state(20,"enabled", "有效状态", false, false, false, null, null, null, null, null, null, null)
					.state(30,"disabled", "禁止状态", false, false, false, null, null, null, null, null, null, null)
					.event(10,"enable", "有效", "enabled", null, "doEnable",Terminal.ORG)
					.event(20,"disable", "禁止", "disabled", null, "doDisable",Terminal.ORG)
					.event(30,"kick", "强制下线", null, null, "doKick",Terminal.ORG)
					.timer("speak", "说话定时器", "enabled", "doSpeak", null, 20);

			builder.getState("created").addEvent("enable").addEvent("disable");
			builder.getState("enabled").addEvent("disable").addEvent("kick");
			builder.getState("disabled").addEvent("enable");

			builder.getEvent("enable").addRole("platform_super").addRole("platform_manager");
			builder.getEvent("disable").addRole("platform_super").addRole("platform_manager");
			builder.getEvent("kick").addRole("platform_employee");
			builder.build();
		}

		// 创建用户的状态机states
		if (stateRepository.count() == 0) {
			stateTemplateService.entityStateDeploy("user");
			//初始化用户状态机
			StateMachineService.services.get("user").base_init();
		}

		// 创建第一个企业
		if (orgRepository.count() == 0) {
			Optional<User> findById = userRepository.findById(1L);
			User user = findById.get();
			// 根据platform模板给组织创建部门，角色，菜单
			Org org = orgTemplateService.orgTemplateDeploy("天邮平台"+ContextUtils.randomInt(10), user, "platform", "中华人民共和国", "浙江省", "杭州市");
		}
	}

}
