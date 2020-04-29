package com.tianyoukeji.org.controller;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.tianyoukeji.org.service.StateTemplateService;
import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.template.OrgTemplate;
import com.tianyoukeji.parent.entity.template.OrgTemplateRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplate;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class TestController extends DefaultHandler{

	@Autowired
	private UserService userService;
	
	
	@Autowired
	private StateTemplateService stateTemplateService;

	@Autowired
	private OrgTemplateRepository orgTemplateRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private OrgRepository orgRepository;
	
	@GetMapping(path="/test/test")
	public Object test1() {
		return getHostIP();
	}
	
	
	private String getHostIP(){

        Enumeration<NetworkInterface> allNetInterfaces = null;
        String resultIP=null;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        InetAddress ip = null;
        while (allNetInterfaces.hasMoreElements())
        {
        NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
        System.out.println(netInterface.getName());
        Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
        while (addresses.hasMoreElements())
        {
        ip = (InetAddress) addresses.nextElement();
        if (ip != null && ip instanceof Inet4Address)
        { 
           if(resultIP==null)
            resultIP= ip.getHostAddress();  
           System.out.println("本机地址是："+ip.getHostAddress());
        
        } 
        }
        }
          return resultIP;
         
    }
	
	@GetMapping(path = "/test/enable")
	public void enable(Authentication authentication) {
		
		List<String> currentUserExecutableEvent = userService.currentUserExecutableEvent(1l);
		System.out.println(currentUserExecutableEvent);
		userService.dispatchEvent(1l, "enable",null);
		User findById = userService.findById(1l);
		State state = findById.getState();
		System.out.println(state.toString());
	}
	@GetMapping(path = "/test/forbid")
	public void forbid(Authentication authentication) {
		userService.dispatchEvent(1l, "forbid",null);
//		User findById = userService.findById(5l);
//		State state = findById.getState();
//		System.out.println(userService.stateExecutableEvent(state));
//		System.out.println(userService.currentUserStateExecutableEvent(state));
//		return null;
	}
	@GetMapping(path = "/test/status")
	public Object test(Authentication authentication) {
		return SmartQuery.fetchList("event","fields=*&group=entity");
	}
//	@GetMapping(path = "/test/state")
//	public Object state(Authentication authentication) {
//		return stateService.fetchList("fields=events,events.roles,events.target,firstTarget,thenTarget,lastTarget,timers,*&entity=user&sort=sort,asc");
//	}
}
