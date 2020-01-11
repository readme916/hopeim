package com.tianyoukeji.oauth.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tianyoukeji.parent.common.AvatarUtils;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.Userinfo;
import com.tianyoukeji.parent.entity.UserinfoRepository;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.service.BaseService;

import java.util.*;

import javax.annotation.PostConstruct;

@Service
public class OauthUserService extends BaseService<User>{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserinfoRepository userInfoRepository;

    @PostConstruct
    private void init() {
    	if(this.count()==0) {
    		register("admin","admin");
    	}
    }
    

    
    
    /**
     * 简单注册
     * @param username
     * @param password
     * @return
     */
    @Transactional
    public User register(String username, String password) {
        if (userRepository.findByUserinfoMobile(username) != null) {
            throw new BusinessException(1000, "用户名已存在");
        }
        User user = new User();
        user.setEnabled(true);
        save(user);
        
        //根据id生成用户名
        user.setNickname("用户"+gen(user.getUuid()));
        Userinfo userInfo = new Userinfo();
        userInfo.setMobile(username);
        userInfo.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password));
        userInfoRepository.save(userInfo);

        user.setUserinfo(userInfo);
        user.setHeadimgurl(AvatarUtils.generatorUserAvatar(username));
        userRepository.save(user);
        return user;
    }



    /**
     * 随机字符串
     */
    private static final char[] CHARS = new char[]
            {'6', 'j', 'a', 'y', '3', 'q', 'b', 'c', 'x', '5', 'e', 'f', 's', '7', 'g', 'h', 'v', 'i', '9', 'w', 'k', '4', 'm', 'n', 'd', 'p', 'r', 't', 'u', '2', '8', 'z'};

    private final static int CHARS_LENGTH = 32;
    /**
     * 邀请码长度
     */
    private final static int CODE_LENGTH = 6;

    /**
     * 随机数据
     */
    private final static long SLAT = 3396552L;

    /**
     * PRIME1 与 CHARS 的长度 L互质，可保证 ( id * PRIME1) % L 在 [0,L)上均匀分布
     */
    private final static int PRIME1 = 3;

    /**
     * PRIME2 与 CODE_LENGTH 互质，可保证 ( index * PRIME2) % CODE_LENGTH  在 [0，CODE_LENGTH）上均匀分布
     */
    private final static int PRIME2 = 11;

    /**
     * 生成邀请码
     *
     * @param id 唯一的id主键
     * @return code
     */
    private String gen(Long id) {
        //补位
        id = id * PRIME1 + SLAT;
        //将 id 转换成32进制的值
        long[] b = new long[CODE_LENGTH];
        //32进制数
        b[0] = id;
        for (int i = 0; i < CODE_LENGTH - 1; i++) {
            b[i + 1] = b[i] / CHARS_LENGTH;
            //按位扩散
            b[i] = (b[i] + i * b[0]) % CHARS_LENGTH;
        }
        b[5] = (b[0] + b[1] + b[2] + b[3] + b[4]) * PRIME1 % CHARS_LENGTH;

        //进行混淆
        long[] codeIndexArray = new long[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            codeIndexArray[i] = b[i * PRIME2 % CODE_LENGTH];
        }

        StringBuilder buffer = new StringBuilder();
        Arrays.stream(codeIndexArray).boxed().map(Long::intValue).map(t -> CHARS[t]).forEach(buffer::append);
        return buffer.toString();
    }
}
