package com.tianyoukeji.parent.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.statemachine.state.PseudoStateContext.PseudoAction;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.tencentyun.TLSSigAPIv2;
import com.tianyoukeji.parent.common.AvatarUtils;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.entity.base.IQunEntity;
import com.tianyoukeji.parent.service.TIMService.TIMMsgElement;
import com.tianyoukeji.parent.service.TIMService.TIMResponse;

@Service
public class TIMService {
	final static Logger logger = LoggerFactory.getLogger(TIMService.class);

	@Value("${SDKAppID}")
	private String SDKAppID;

	@Value("${SDKAPPSecret}")
	private String SDKAPPSecret;

	private TLSSigAPIv2 api;

	private String adminSig;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private UserRepository userRepository;

	@PostConstruct
	private void init() {
		api = new TLSSigAPIv2(Long.valueOf(SDKAppID), SDKAPPSecret);
		adminSig = api.genSig("administrator", 500 * 86400);
	}

	/**
	 * 生成用户签名
	 * 
	 * @param username
	 * @return
	 */
	public String genSig(String username) {
		return api.genSig(username, 500 * 86400);
	}

	/**
	 * 注册app用户到tim
	 * 
	 * @param username
	 * @return
	 */
	public TIMResponse registerUser(String username, String nick, String faceUrl) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/im_open_login_svc/account_import?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("From_Account", username);
		hashMap.put("Nick", nick);
		hashMap.put("FaceUrl", faceUrl);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * 更新用户资料，不需要跟新则输入null
	 * 
	 * @param username
	 * @return
	 */
	public TIMResponse updateUser(String username, String nick, Gender gender, String faceUrl, String selfSignature,
			Integer roleId) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/profile/portrait_set?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("Identifier", username);
		ArrayList<ProfileItem> items = new ArrayList<ProfileItem>();
		if (nick != null) {
			items.add(new ProfileItem("Tag_Profile_IM_Nick", nick));
		}
		if (gender != null) {
			items.add(new ProfileItem("Tag_Profile_IM_Gender", gender));
		}
		if (selfSignature != null) {
			items.add(new ProfileItem("Tag_Profile_IM_SelfSignature", gender));
		}
		if (faceUrl != null) {
			items.add(new ProfileItem("Tag_Profile_IM_Image", faceUrl));
		}
		if (roleId != null) {
			items.add(new ProfileItem("Tag_Profile_IM_Role", roleId));
		}
		hashMap.put("ProfileItem", items);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * tim用户踢下线并失效
	 * 
	 * @param username
	 * @return
	 */
	public TIMResponse kickUser(Long uuid) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/im_open_login_svc/kick?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		HashMap<String, String> hashMap = new HashMap<String, String>();
		Optional<User> findById = userRepository.findById(uuid);
		if (!findById.isPresent()) {
			throw new BusinessException(1237, "用户不存在");
		}
		hashMap.put("Identifier", findById.get().getUserinfo().getMobile());
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * 用户的登录状态
	 * 
	 * @param username
	 * @return
	 */
	public HashMap<String, Boolean> loginQuery(List<String> mobiles) {

		if (mobiles == null || mobiles.isEmpty()) {
			return null;
		}

		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/openim/querystate?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("To_Account", mobiles);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);

		HashMap<String, Boolean> response = new HashMap<String, Boolean>();
		if (postForObject.getActionStatus().equals(ActionStatus.OK)) {
			List result = (List) postForObject.getQueryResult();
			for (Object o : result) {
				Map m = (Map) o;
				if (m.get("State").toString().equals("Online")) {
					response.put(m.get("To_Account").toString(), true);
				} else {
					response.put(m.get("To_Account").toString(), false);
				}
			}
			return response;
		} else {
			return null;
		}
	}

	/**
	 * 批量发送普通消息
	 * 
	 * @param username
	 * @return
	 */
	public TIMResponse batchSendMessage(List<Long> ids, TIMMsgElement element) {

		List<User> findByUuidIn = userRepository.findByUuidIn(ids);
		if (findByUuidIn == null || findByUuidIn.isEmpty()) {
			return null;
		}
		List<String> mobiles = findByUuidIn.stream().map(u -> u.getUserinfo().getMobile()).collect(Collectors.toList());

		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/openim/batchsendmsg?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("SyncOtherMachine", 2);
		hashMap.put("To_Account", mobiles);
		hashMap.put("MsgRandom", nextInt);
		List elems = new ArrayList<TIMMsgElement>();
		elems.add(element);
		hashMap.put("MsgBody", elems);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * 批量发送推送消息
	 * 
	 * @param username
	 * @return
	 */
	public TIMResponse batchSendMessageOffline(List<Long> ids, TIMMsgElement element, String offlineTitle,
			String offlineDesc) {
		List<User> findByUuidIn = userRepository.findByUuidIn(ids);
		if (findByUuidIn == null || findByUuidIn.isEmpty()) {
			return null;
		}
		List<String> mobiles = findByUuidIn.stream().map(u -> u.getUserinfo().getMobile()).collect(Collectors.toList());

		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/openim/batchsendmsg?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("SyncOtherMachine", 2);
		hashMap.put("To_Account", mobiles);
		hashMap.put("MsgRandom", nextInt);
		List elems = new ArrayList<TIMMsgElement>();
		elems.add(element);
		hashMap.put("MsgBody", elems);
		HashMap offlineInfo = new HashMap<String, Object>();
		offlineInfo.put("PushFlag", 0);
		offlineInfo.put("Title", offlineTitle);
		offlineInfo.put("Desc", offlineDesc);
		hashMap.put("OfflinePushInfo", offlineInfo);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * 加入企业时候，自动把其他员工导入自己联系人
	 * 
	 * @param username
	 * @return
	 */
	public TIMResponse addOrgFriends(String username, Org org) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/sns/friend_add?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";

		Set<User> users = org.getUsers();
		if (users == null || users.isEmpty()) {
			return null;
		}

		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("From_Account", username);
		hashMap.put("AddType", "Add_Type_Both");
		hashMap.put("ForceAddFlags", 1);
		ArrayList<Map<String, String>> friends = new ArrayList<Map<String, String>>();
		for (User user : users) {
			HashMap<String, String> u = new HashMap<String, String>();
			u.put("To_Account", user.getUserinfo().getMobile());
			u.put("GroupName", "org");
			u.put("AddSource", "AddSource_Type_org");
			friends.add(u);
		}
		hashMap.put("AddFriendItem", friends);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * 退出企业时候，自动把其他员工从自己联系人删除
	 * 
	 * @param username
	 * @return
	 */
	public TIMResponse deleteOrgFriends(String username, Org org) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/sns/friend_delete?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";

		Set<User> users = org.getUsers();
		if (users == null || users.isEmpty()) {
			return null;
		}

		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("From_Account", username);
		hashMap.put("DeleteType", "Delete_Type_Both");
		ArrayList<String> friends = new ArrayList<String>();
		for (User user : users) {
			friends.add(user.getUserinfo().getMobile());
		}
		hashMap.put("To_Account", friends);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * 添加好友，普通好友分类
	 * 
	 * @param username
	 * @return
	 */
	public TIMResponse addFriend(String username, User user) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/sns/friend_add?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		if (user == null) {
			return null;
		}
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("From_Account", username);
		hashMap.put("AddType", "Add_Type_Both");
		ArrayList<Map<String, String>> friends = new ArrayList<Map<String, String>>();
		HashMap<String, String> u = new HashMap<String, String>();
		u.put("To_Account", user.getUserinfo().getMobile());
		u.put("GroupName", "user");
		u.put("AddSource", "AddSource_Type_user");
		friends.add(u);
		hashMap.put("AddFriendItem", friends);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * 删除普通好友
	 * 
	 * @param username
	 * @return
	 */
	public TIMResponse deleteFriend(String username, User user) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/sns/friend_delete?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";

		if (user == null) {
			return null;
		}
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("From_Account", username);
		hashMap.put("DeleteType", "Delete_Type_Both");
		ArrayList<String> friends = new ArrayList<String>();
		friends.add(user.getUserinfo().getMobile());
		hashMap.put("To_Account", friends);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * 创建一个基于entity的群，把相关的人员拉入，已经存在这个实体群则直接返回
	 * 
	 * @param username
	 * @return
	 */
	public TIMResponse getOrCreateEntityQun(String entity, Long id, String name, String faceUrl, List<String> users) {

		if (!entityInstanceOf(entity, IQunEntity.class)) {
			throw new BusinessException(1395, "实体类型不符合IQun");
		}
		JpaRepository jpaRepository = SmartQuery.getStructure(entity).getJpaRepository();
		Optional findById = jpaRepository.findById(id);
		if (!findById.isPresent()) {
			throw new BusinessException(1481, "实体不存在" + id);
		}
		IQunEntity qunEntity = (IQunEntity) findById.get();
		String groupId = qunEntity.getGroupId();
		if (StringUtils.hasText(groupId)) {
			TIMResponse timResponse = new TIMResponse();
			timResponse.setActionStatus(ActionStatus.OK);
			timResponse.setErrorCode(0);
			timResponse.setGroupId(groupId);
			return timResponse;
		}
//		if(users == null || users.isEmpty()) {
//			throw new BusinessException(1482, "不能创建无人群组");
//		}
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/group_open_http_svc/create_group?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";

		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("Type", "Private");
		hashMap.put("Name", name);
		hashMap.put("Introduction", entity + "/" + id);
		if (faceUrl == null) {
			faceUrl = AvatarUtils.generatorGroupAvatar(entity + "/" + id);
		}
		hashMap.put("FaceUrl", faceUrl);
		if (users != null) {
			ArrayList<HashMap<String, String>> us = new ArrayList<HashMap<String, String>>();
			for (String uName : users) {
				HashMap<String, String> hashMapUser = new HashMap<String, String>();
				hashMapUser.put("Member_Account", uName);
				us.add(hashMapUser);
			}
			hashMap.put("MemberList", us);
		}

		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		if (postForObject.getActionStatus().equals(ActionStatus.OK)) {
			
			qunEntity.setGroupId(postForObject.getGroupId());
			jpaRepository.save(qunEntity);
			
			TIMMsgElement timMsgElement = new TIMMsgElement();
			timMsgElement.setMsgType(MessageType.TIMTextElem);
			TIMTextContent timTextContent = new TIMTextContent();
			timTextContent.setText("Hello Everyone!");
			timMsgElement.setMsgContent(timTextContent);
			sendQunMessage(postForObject.getGroupId(), timMsgElement);
		}

		return postForObject;
	}

	/**
	 * 加入群
	 * 
	 * @param
	 * @return
	 */
	public TIMResponse joinEntityQun(String username, String groupId) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/group_open_http_svc/add_group_member?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("GroupId", groupId);

		ArrayList<HashMap<String, String>> us = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> hashMapUser = new HashMap<String, String>();
		hashMapUser.put("Member_Account", username);
		us.add(hashMapUser);
		hashMap.put("MemberList", us);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}
	
	/**
	 * 退出群
	 * 
	 * @param
	 * @return
	 */
	public TIMResponse quitEntityQun(String username, String groupId) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/group_open_http_svc/delete_group_member?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("GroupId", groupId);

		ArrayList<String>us = new ArrayList<String>();
		us.add(username);
		hashMap.put("MemberToDel_Account", us);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}
	/**
	 * 发送群普通消息
	 * 
	 * @param
	 * @return
	 */
	public TIMResponse sendQunMessage(String groupId, TIMMsgElement element) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/group_open_http_svc/send_group_msg?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("GroupId", groupId);
		hashMap.put("Random", nextInt);
		List elems = new ArrayList<TIMMsgElement>();
		elems.add(element);
		hashMap.put("MsgBody", elems);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * 发送群普通消息，带推送
	 * 
	 * @param
	 * @return
	 */
	public TIMResponse sendQunMessageOffline(String groupId, TIMMsgElement element, String offlineTitle,
			String offlineDesc) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/group_open_http_svc/send_group_msg?sdkappid=" + SDKAppID
				+ "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt + "&contenttype=json";
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("GroupId", groupId);
		hashMap.put("Random", nextInt);
		List elems = new ArrayList<TIMMsgElement>();
		elems.add(element);
		hashMap.put("MsgBody", elems);
		HashMap offlineInfo = new HashMap<String, Object>();
		offlineInfo.put("PushFlag", 0);
		offlineInfo.put("Title", offlineTitle);
		offlineInfo.put("Desc", offlineDesc);
		hashMap.put("OfflinePushInfo", offlineInfo);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	/**
	 * 发送群系统通知
	 * 
	 * @param
	 * @return
	 */
	public TIMResponse sendQunNotice(String groupId, String content) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/group_open_http_svc/send_group_system_notification?sdkappid="
				+ SDKAppID + "&identifier=administrator&usersig=" + adminSig + "&random=" + nextInt
				+ "&contenttype=json";
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("GroupId", groupId);
		hashMap.put("Content", content);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}

	private boolean entityInstanceOf(String entity, Class<?> clz) {
		EntityStructure structure = SmartQuery.getStructure(entity);
		Class<?> entityClass = structure.getEntityClass();
		return clz.isAssignableFrom(entityClass);
	}

	public static class TIMResponse {
		@JsonProperty("ActionStatus")
		private ActionStatus actionStatus;
		@JsonProperty("ErrorInfo")
		private String errorInfo = null;
		@JsonProperty("ErrorCode")
		private Integer errorCode = 0;
		@JsonProperty("MsgKey")
		private String msgKey = null;

		@JsonProperty("MsgTime")
		private Long msgTime = null;

		@JsonProperty("MsgSeq")
		private Long msgSeq = null;

		@JsonProperty("ErrorList")
		private Object errorList = null;

		@JsonProperty("ErrorDisplay")
		private String errorDisplay = null;

		@JsonProperty("ResultItem")
		private Object resultItem = null;

		@JsonProperty("Fail_Account")
		private Object failAccount = null;

		@JsonProperty("GroupId")
		private String groupId = null;

		@JsonProperty("QueryResult")
		private Object queryResult;

		public Object getQueryResult() {
			return queryResult;
		}

		public void setQueryResult(Object queryResult) {
			this.queryResult = queryResult;
		}

		public Long getMsgTime() {
			return msgTime;
		}

		public void setMsgTime(Long msgTime) {
			this.msgTime = msgTime;
		}

		public Long getMsgSeq() {
			return msgSeq;
		}

		public void setMsgSeq(Long msgSeq) {
			this.msgSeq = msgSeq;
		}

		public String getGroupId() {
			return groupId;
		}

		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}

		public Object getResultItem() {
			return resultItem;
		}

		public void setResultItem(Object resultItem) {
			this.resultItem = resultItem;
		}

		public Object getFailAccount() {
			return failAccount;
		}

		public void setFailAccount(Object failAccount) {
			this.failAccount = failAccount;
		}

		public String getErrorDisplay() {
			return errorDisplay;
		}

		public void setErrorDisplay(String errorDisplay) {
			this.errorDisplay = errorDisplay;
		}

		public String getMsgKey() {
			return msgKey;
		}

		public void setMsgKey(String msgKey) {
			this.msgKey = msgKey;
		}

		public Object getErrorList() {
			return errorList;
		}

		public void setErrorList(Object errorList) {
			this.errorList = errorList;
		}

		public ActionStatus getActionStatus() {
			return actionStatus;
		}

		public void setActionStatus(ActionStatus actionStatus) {
			this.actionStatus = actionStatus;
		}

		public String getErrorInfo() {
			return errorInfo;
		}

		public void setErrorInfo(String errorInfo) {
			this.errorInfo = errorInfo;
		}

		public Integer getErrorCode() {
			return errorCode;
		}

		public void setErrorCode(Integer errorCode) {
			this.errorCode = errorCode;
		}

	}

	public static class TIMMsgElement {

		@JsonProperty("MsgType")
		MessageType msgType;
		@JsonProperty("MsgContent")
		TIMContent msgContent;

		public MessageType getMsgType() {
			return msgType;
		}

		public void setMsgType(MessageType msgType) {
			this.msgType = msgType;
		}

		public TIMContent getMsgContent() {
			return msgContent;
		}

		public void setMsgContent(TIMContent msgContent) {
			this.msgContent = msgContent;
		}

	}

	public static class TIMTextContent implements TIMContent {
		@JsonProperty("Text")
		String text;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

	}

	public static class TIMLocationContent implements TIMContent {
		@JsonProperty("Desc")
		String desc;
		@JsonProperty("Latitude")
		Double latitude;
		@JsonProperty("Longitude")
		Double longitude;

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public Double getLatitude() {
			return latitude;
		}

		public void setLatitude(Double latitude) {
			this.latitude = latitude;
		}

		public Double getLongitude() {
			return longitude;
		}

		public void setLongitude(Double longitude) {
			this.longitude = longitude;
		}

	}

	public static class TIMCustomContent implements TIMContent {
		/**
		 * 内部传输数据enityt Json字符串 {"name":"kaka","id":2222}
		 */
		@JsonProperty("Data")
		Object data;

		/**
		 * 显示的文本，例如： 用户{{name}}的名片
		 */
		@JsonProperty("Desc")
		String desc;

		/**
		 * 一般使用entity来标识这是个什么实体 ，来作为扩展传过去
		 */
		@JsonProperty("Ext")
		String ext;

		@JsonProperty("Sound")
		String sound;

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public String getExt() {
			return ext;
		}

		public void setExt(String ext) {
			this.ext = ext;
		}

		public String getSound() {
			return sound;
		}

		public void setSound(String sound) {
			this.sound = sound;
		}

	}

	public static class ProfileItem {
		@JsonProperty("Tag")
		String tag;
		@JsonProperty("Value")
		Object value;

		public ProfileItem(String tag, Object value) {
			super();
			this.tag = tag;
			this.value = value;
		}

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}

	public interface TIMContent {

	}

	public enum Gender {
		Gender_Type_Unknown, Gender_Type_Female, Gender_Type_Male
	}

	public enum ActionStatus {
		OK, FAIL
	}

	public enum MessageType {
		TIMTextElem, TIMLocationElem, TIMCustomElem
	}
}
