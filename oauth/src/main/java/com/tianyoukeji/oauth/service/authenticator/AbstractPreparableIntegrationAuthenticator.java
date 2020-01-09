package com.tianyoukeji.oauth.service.authenticator;

import com.utopia.tokensart.auth.service.IntegrationAuthentication;
import com.utopia.tokensart.common.modules.base.models.User;

/**
 * @author LIQIU
 * @date 2018-4-4
 **/
public abstract class AbstractPreparableIntegrationAuthenticator implements IntegrationAuthenticator{

    @Override
    public abstract User authenticate(IntegrationAuthentication integrationAuthentication);

    @Override
    public abstract void prepare(IntegrationAuthentication integrationAuthentication);

    @Override
    public abstract boolean support(IntegrationAuthentication integrationAuthentication);

    @Override
    public void complete(IntegrationAuthentication integrationAuthentication) {

    }
}
