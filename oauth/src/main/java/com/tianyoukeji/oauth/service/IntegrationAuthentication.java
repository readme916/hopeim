package com.tianyoukeji.oauth.service;

import java.util.HashMap;
import java.util.Map;

public class IntegrationAuthentication {
	private String authType;
	private String clientId;
    private Map<String,String[]> authParameters = new HashMap<>();

    public String getAuthParameter(String paramter){
        String[] values = this.authParameters.get(paramter);
        if(values != null && values.length > 0){
            return values[0];
        }
        return null;
    }

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public Map<String, String[]> getAuthParameters() {
		return authParameters;
	}

	public void setAuthParameters(Map<String, String[]> authParameters) {
		this.authParameters = authParameters;
	}
    
}
