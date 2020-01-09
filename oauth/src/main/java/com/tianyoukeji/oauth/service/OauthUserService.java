package com.tianyoukeji.oauth.service;

import com.liyang.jpa.restful.core.exception.Business503Exception;
import com.utopia.tokensart.common.modules.base.init.GlobalType;
import com.utopia.tokensart.common.modules.base.models.*;
import com.utopia.tokensart.common.modules.base.repository.*;
import com.utopia.tokensart.common.service.*;
import com.utopia.tokensart.common.utils.AvatarUtils;
import com.utopia.tokensart.common.utils.Region;
import com.utopia.tokensart.common.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class OauthUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMobileRepository userMobileRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MnemonicService mnemonicService;
    @Autowired
    private VerificationCode verificationCode;
    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private RegionService regionService;
    @Autowired
    private UserAccountService accountService;
    @Autowired
    private UserLevelRepository userLevelRepository;
    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserLevelService userLevelService;
    @Autowired
    private UserService userService;

    @Transactional
    public User loginRegister(String mobile, String smsCode, String callingCode, String siteUuid, String referrerCode) {
        User user = userRepository.findFirstByUserMobilesCallingCodeAndUserMobilesMobileAndUserMobilesIsCertifiedAndUserMobilesState(callingCode, mobile, true, true);
        if (user != null) {
            //这说明是权限问题
            return null;
        }
        Optional<Site> siteOptional = siteRepository.findById(siteUuid);
        if (!siteOptional.isPresent()) {
            throw new Business503Exception(1, "子站没有找到", null);
        }

        Token fiatToken = tokenRepository.findByUniqueCode(siteOptional.get().getCurrencyCode());
        user = register(UUID.randomUUID().toString(), smsCode, fiatToken);
        addMobile(mobile, smsCode, callingCode, user);

        //引荐人加两积分
        if (!StringUtils.isEmpty(referrerCode)) {
            userService.firstUseReferrerCode(user, referrerCode);
        }


        return user;

    }

    @Transactional
    public User register(String username, String password, Token fiatToken) {

        if (userRepository.findByUsername(username) != null) {
            throw new Business503Exception(2000, "用户名已存在", null);
        }

        Region region = regionService.getRegion();
        Role roleRegister = roleRepository.findByRoleCode(GlobalType.RoleCode.REGISTER);
        User user = new User();
        user.setFirstLogin(true);
        user.setUsername(username);
        user.setDefaultFiatToken(fiatToken);

        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password));
        userInfo.setMnemonic(String.join(" ", mnemonicService.getMnemonic()));
        userInfoRepository.saveAndFlush(userInfo);

        user.setUserInfo(userInfo);
        user.setEnabled(true);
        user.setRoles(Collections.singleton(roleRegister));

        if (region != null) {
            user.setCountry(region.getCountry());
            user.setProvine(region.getProvine());
            user.setCity(region.getCity());
            user.setDistrict(region.getDistrict());
        }

        user.setAvatarUrl(AvatarUtils.generatorUserAvatar(username));


        //默认的用户级别
        UserLevel normalLevel = userLevelRepository.findByLevel(GlobalType.UserLevel.NORMAL);
        user.setUserLevel(normalLevel);

        //唯一的推荐码
        String referrerCode = null;
        for (int i = 0; i < 3; i++) {
            referrerCode = gen(SnowflakeIdWorker.getInstance().nextId());
            Integer count = userRepository.countAllByReferrerCode(referrerCode);
            if (count > 0) {
                referrerCode = null;
                continue;
            }
            break;
        }

        user.setReferrerCode(referrerCode);

        userRepository.save(user);

        accountService.generateBasicAccountByOrgOrUser(null, user);

        return user;

    }

    @Transactional
    public UserMobile addMobile(String mobile, String code, String callingCode, User user) {
        if (!verificationCode.validMobileCode(callingCode, mobile, code)) {
            throw new Business503Exception(2001, "短信验证码错误", null);
        }

        if (userMobileRepository.findByCallingCodeAndMobileAndIsCertifiedTrueAndStateTrue(callingCode,
                mobile) != null) {
            throw new Business503Exception(2009, "手机号已被占用", null);
        }

        // 把其它手机状态改为不可用
        userMobileRepository.updateStateFalseByUser(user);

        UserMobile userMobile = new UserMobile();
        userMobile.setUser(user);
        userMobile.setCallingCode(callingCode);
        userMobile.setIsCertified(true);
        userMobile.setState(true);
        userMobile.setMobile(mobile);
        return userMobileRepository.save(userMobile);
    }


    public void resetPassword(String password, String code, String mobile, String callingCode) {

        User user = null;

        if (!verificationCode.validMobileCode(callingCode, mobile, code)) {
            throw new Business503Exception(2001, "短信验证码错误", null);
        }
        UserMobile userMobile = userMobileRepository
                .findByCallingCodeAndMobileAndIsCertifiedTrueAndStateTrue(callingCode, mobile);
        user = userMobile != null ? userMobile.getUser() : null;


        if (user == null) {
            throw new Business503Exception(2007, "密码重置失败", null);
        }

        user.getUserInfo().setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password));
        userInfoRepository.save(user.getUserInfo());
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
