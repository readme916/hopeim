package com.tianyoukeji.oauth.filter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class MobileSmsAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter{

    private boolean postOnly = true;
    
    public MobileSmsAuthenticationProcessingFilter() {
        super(new AntPathRequestMatcher("/smsLogin", "POST"));
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
        
        String mobile = request.getParameter("username");
        String sms = request.getParameter("password");
        
        if (mobile == null) {
            mobile = "";
        }
        if (sms == null) {
        	sms = "";
        }
        mobile = mobile.trim();
        sms = sms.trim();
        
        MobileSmsAuthenticationToken authRequest = new MobileSmsAuthenticationToken(mobile, sms);
        
        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);
        
        return this.getAuthenticationManager().authenticate(authRequest);
    }
    
    protected void setDetails(HttpServletRequest request,
            AbstractAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }
}
