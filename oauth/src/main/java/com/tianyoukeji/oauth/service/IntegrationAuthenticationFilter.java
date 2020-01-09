package com.tianyoukeji.oauth.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.GenericFilterBean;

import com.utopia.tokensart.auth.service.authenticator.IntegrationAuthenticator;


/**
 * @author LIQIU
 * @date 2018-3-30
 **/
@Component
public class IntegrationAuthenticationFilter extends GenericFilterBean implements ApplicationContextAware {

    private static final String AUTH_TYPE_PARM_NAME = "auth_type";

    private static final String OAUTH_TOKEN_URL = "/oauth/token";

    private Collection<IntegrationAuthenticator> authenticators;

    private ApplicationContext applicationContext;

    private RequestMatcher requestMatcher;

    public IntegrationAuthenticationFilter(){
        this.requestMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher(OAUTH_TOKEN_URL, "GET"),
                new AntPathRequestMatcher(OAUTH_TOKEN_URL, "POST")
        );
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if(requestMatcher.matches(request)){
            //设置集成登录信息
            IntegrationAuthentication integrationAuthentication = new IntegrationAuthentication();
            integrationAuthentication.setAuthType(request.getParameter(AUTH_TYPE_PARM_NAME));
            integrationAuthentication.setClientId(getClientId());
            integrationAuthentication.setAuthParameters(request.getParameterMap());
            IntegrationAuthenticationContext.set(integrationAuthentication);
            try{
                //预处理
                this.prepare(integrationAuthentication);

                filterChain.doFilter(request,response);

                //后置处理
                this.complete(integrationAuthentication);
            }finally {
                IntegrationAuthenticationContext.clear();
            }
        }else{
            filterChain.doFilter(request,response);
        }
    }

    /**
     * 进行预处理
     * @param integrationAuthentication
     */
    private void prepare(IntegrationAuthentication integrationAuthentication) {

        //延迟加载认证器
        if(this.authenticators == null){
            synchronized (this){
                Map<String,IntegrationAuthenticator> integrationAuthenticatorMap = applicationContext.getBeansOfType(IntegrationAuthenticator.class);
                if(integrationAuthenticatorMap != null){
                    this.authenticators = integrationAuthenticatorMap.values();
                }
            }
        }

        if(this.authenticators == null){
            this.authenticators = new ArrayList<>();
        }

        for (IntegrationAuthenticator authenticator: authenticators) {
            if(authenticator.support(integrationAuthentication)){
                authenticator.prepare(integrationAuthentication);
            }
        }
    }

    /**
     * 后置处理
     * @param integrationAuthentication
     */
    private void complete(IntegrationAuthentication integrationAuthentication){
        for (IntegrationAuthenticator authenticator: authenticators) {
            if(authenticator.support(integrationAuthentication)){
                authenticator.complete(integrationAuthentication);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    
    private String getClientId() {
    	
    	final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        final String authorizationHeaderValue = request.getHeader("Authorization");
        final String base64AuthorizationHeader = Optional.ofNullable(authorizationHeaderValue).map(headerValue->headerValue.substring("Basic ".length())).orElse("");

        if(!StringUtils.isEmpty(base64AuthorizationHeader)){
            String decodedAuthorizationHeader = new String(Base64.getDecoder().decode(base64AuthorizationHeader), Charset.forName("UTF-8"));
            return decodedAuthorizationHeader.split(":")[0];
        }

        return "";
    }
}