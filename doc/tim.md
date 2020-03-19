腾讯TIM
-------------


* [腾讯TIM完整协议](https://cloud.tencent.com/document/product/269/1498)

* 腾讯TIM在本系统中，具有重要的地位和作用，完成了多终端的交互和展示的基础工作

* 作为基础组件，无缝的嵌入我们前后台系统中，和谐统一

* TIM Service 位于  com.tianyoukeji.parent.service.TIMService ，完成后台与腾讯的交互功能

* TIM两个最重要的功能应用
1. 建立群
	**根据业务需求，建立各类实体的群，绑定到对应id的实体上，自动拉入相关人员，完成业务的交流和记录，例如订单，维修单，部门等**
2. 自定义消息格式
	**根据业务需求，为各类实体的信息建立独特的消息格式，让自定义消息的格式的业务信息，在用户之间流转和操作**


```java

public interface TIMServiceInterface {

	/**
	 * 生成用户签名
	 * 
	 * @param username
	 * @return
	 */
	String genSig(String username);

	/**
	 * 注册app用户到tim
	 * 
	 * @param username
	 * @return
	 */
	TIMResponse registerUser(String username, String nick, String faceUrl);

	/**
	 * 更新用户资料，不需要跟新则输入null
	 * 
	 * @param username
	 * @return
	 */
	TIMResponse updateUser(String username, String nick, Gender gender, String faceUrl, String selfSignature,
			Long roleId, Long departmentId);

	/**
	 * tim用户踢下线并失效
	 * 
	 * @param username
	 * @return
	 */
	TIMResponse kickUser(Long uuid);

	/**
	 * 用户的登录状态
	 * 
	 * @param username
	 * @return
	 */
	HashMap<String, Boolean> loginQuery(List<String> mobiles);

	/**
	 * 批量发送普通消息
	 * 
	 * @param username
	 * @return
	 */
	TIMResponse batchSendMessage(List<Long> ids, TIMMsgElement element);

	/**
	 * 批量发送推送消息
	 * 
	 * @param username
	 * @return
	 */
	TIMResponse batchSendMessageOffline(List<Long> ids, TIMMsgElement element, String offlineTitle, String offlineDesc);

	/**
	 * 加入企业时候，自动把其他员工导入自己联系人
	 * 
	 * @param username
	 * @return
	 */
	TIMResponse addOrgFriends(String username, Org org);

	/**
	 * 退出企业时候，自动把其他员工从自己联系人删除
	 * 
	 * @param username
	 * @return
	 */
	TIMResponse deleteOrgFriends(String username, Org org);

	/**
	 * 添加好友，普通好友分类
	 * 
	 * @param username
	 * @return
	 */
	TIMResponse addFriend(String username, User user);

	/**
	 * 删除普通好友
	 * 
	 * @param username
	 * @return
	 */
	TIMResponse deleteFriend(String username, User user);

	/**
	 * 创建一个基于entity的群，把相关的人员拉入，已经存在这个实体群则直接返回
	 * 
	 * @param username
	 * @return
	 */
	TIMResponse getOrCreateEntityQun(String entity, Long id, String name, String faceUrl, List<String> users);

	/**
	 * 加入群
	 * 
	 * @param
	 * @return
	 */
	TIMResponse joinQun(String username, String groupId);

	/**
	 * 退出群
	 * 
	 * @param
	 * @return
	 */
	TIMResponse quitQun(String username, String groupId);

	/**
	 * 发送群普通消息
	 * 
	 * @param
	 * @return
	 */
	TIMResponse sendQunMessage(String groupId, TIMMsgElement element);

	/**
	 * 发送群普通消息，带推送
	 * 
	 * @param
	 * @return
	 */
	TIMResponse sendQunMessageOffline(String groupId, TIMMsgElement element, String offlineTitle, String offlineDesc);

	/**
	 * 发送群系统通知
	 * 
	 * @param
	 * @return
	 */
	TIMResponse sendQunNotice(String groupId, String content);

}
```