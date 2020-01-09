package com.tianyoukeji.oauth.service.authenticator;

import com.liyang.jpa.restful.core.exception.Business503Exception;
import com.utopia.tokensart.auth.service.IntegrationAuthentication;
import com.utopia.tokensart.auth.service.OauthUserService;
import com.utopia.tokensart.common.modules.base.init.GlobalType;
import com.utopia.tokensart.common.modules.base.models.User;
import com.utopia.tokensart.common.modules.base.repository.UserRepository;
import com.utopia.tokensart.common.service.VerificationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 集成验证码认证
 *
 * @author LIQIU
 * @date 2018-3-31
 **/
@Component
public class MobileSmsIntegrationAuthenticator extends UsernamePasswordAuthenticator {

    private final static String VERIFICATION_CODE_AUTH_TYPE = "sms";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCode verificationCode;

    @Autowired
    private OauthUserService userService;

    @Override
    public User authenticate(IntegrationAuthentication integrationAuthentication) {
        String clientId = integrationAuthentication.getClientId();
        GlobalType.ClientGroup group = null;
        User findByUsername = null;

        String callingCode = integrationAuthentication.getAuthParameter("callingCode");
        if (callingCode == null) {
            System.out.println("需要callingCode参数");
            return null;
        }
        String mobile = integrationAuthentication.getAuthParameter("mobile");
        if (mobile == null) {
            System.out.println("需要mobile参数");
            return null;
        }
        String smsCode = integrationAuthentication.getAuthParameter("password");
        String siteUuid = integrationAuthentication.getAuthParameter("site_uuid");
        String referrerCode = integrationAuthentication.getAuthParameter("referrer_code");
        if ("user-web".equals(clientId) || "user-app".equals(clientId)) {
            group = GlobalType.ClientGroup.USER;
            findByUsername = userRepository.findFirstByUserMobilesCallingCodeAndUserMobilesMobileAndUserMobilesIsCertifiedAndUserMobilesStateAndRolesClientGroup(callingCode, mobile, true, true, group);
            if (findByUsername == null) {
                findByUsername = userService.loginRegister(mobile, smsCode, callingCode, siteUuid, referrerCode);
            }
        } else if ("org-web".equals(clientId) || "org-app".equals(clientId)) {
            findByUsername = userRepository.findFirstByUserMobilesCallingCodeAndUserMobilesMobileAndUserMobilesIsCertifiedAndUserMobilesStateAndOrgStaffsNotNull(callingCode, mobile, true, true);
            if (findByUsername == null) {
                findByUsername = userService.loginRegister(mobile, smsCode, callingCode, siteUuid, referrerCode);
            }
        } else if ("platform-web".equals(clientId) || "platform-app".equals(clientId)) {
            group = GlobalType.ClientGroup.PLATFORM;
            findByUsername = userRepository.findFirstByUserMobilesCallingCodeAndUserMobilesMobileAndUserMobilesIsCertifiedAndUserMobilesStateAndRolesClientGroup(callingCode, mobile, true, true, group);
        }
        return findByUsername;
    }

    @Override
    public boolean support(IntegrationAuthentication integrationAuthentication) {
        return VERIFICATION_CODE_AUTH_TYPE.equals(integrationAuthentication.getAuthType());
    }


    @Override
    public void prepare(IntegrationAuthentication integrationAuthentication) {

        if (integrationAuthentication.getAuthParameter("password") == null) {
            throw new Business503Exception(2071, "验证码必填", null);
        }

        String mobile = integrationAuthentication.getAuthParameter("mobile");
        String callingCode = integrationAuthentication.getAuthParameter("callingCode");
        boolean result = verificationCode.validMobileCode(callingCode, mobile, integrationAuthentication.getAuthParameter("password"));
        if (!result) {
            throw new Business503Exception(2073, "验证码无效", null);
        }

    }

}
