package com.tianyoukeji.oauth.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.liyang.jpa.restful.core.controller.DefaultExceptionHandler;
import com.liyang.jpa.restful.core.exception.Business503Exception;
import com.liyang.jpa.restful.core.exception.Timeout408Exception;
import com.liyang.jpa.restful.core.response.HTTPPostOkResponse;
import com.utopia.tokensart.auth.service.OauthUserService;
import com.utopia.tokensart.common.modules.base.models.UserMobile;
import com.utopia.tokensart.common.modules.base.repository.UserMobileRepository;
import com.utopia.tokensart.common.service.RedisService;
import com.utopia.tokensart.common.service.VerificationCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;

@RestController
public class RegisterController extends DefaultExceptionHandler {
    @Autowired
    private RedisService redisService;
    @Autowired
    private VerificationCode verificationCode;
    @Autowired
    private UserMobileRepository userMobileRepository;
    @Autowired
    private OauthUserService userService;

    @GetMapping(value = "/sendSMSCode", params = {"mobile!=", "callingCode!="})
//    @ApiOperation("发送手机短信验证码")
    public HTTPPostOkResponse sendSMSCode(@RequestParam("mobile") String mobile, @RequestParam(name = "callingCode") String callingCode) throws IOException {
        if (!redisService.setIfAbsent("sendSMSCode:limit", callingCode + mobile, "", 60)) {
            throw new Business503Exception(1, "请一分钟后重试", null);
        }

        verificationCode.sendMobileCode(callingCode, mobile);

        UserMobile userMobile = userMobileRepository.findByCallingCodeAndMobileAndIsCertifiedTrueAndStateTrue(callingCode, mobile);
        //是否注册过告诉前台
        if (userMobile != null) {
            return new HTTPPostOkResponse(userMobile.getUuid());
        }
        return new HTTPPostOkResponse();
    }

    @PostMapping("/resetPassword")
    public HTTPPostOkResponse resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto.getPassword(), dto.getCode(), dto.getMobile(), dto.getCallingCode());
        return new HTTPPostOkResponse();
    }


    //    @ApiModel("重置密码实体,email、mobile、mnemonic三选一")
    public static class ResetPasswordDTO {
        //        @ApiModelProperty("新的密码")
        @NotBlank
        @Length(min = 6,max = 18)
        private String password;
        //        @ApiModelProperty("验证码(email和mobile需填)")
        private String code;
        //        @ApiModelProperty("手机号")
        private String mobile;
        //        @ApiModelProperty("手机号国际区号(手机号需填)")
        private String callingCode;

        public String getCode() {
            return code;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getCallingCode() {
            return callingCode;
        }

        public void setCallingCode(String callingCode) {
            this.callingCode = callingCode;
        }
    }
}
