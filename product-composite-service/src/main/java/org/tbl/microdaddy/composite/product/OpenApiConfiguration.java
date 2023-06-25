package org.tbl.microdaddy.composite.product;


import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.OAUTH2;

@SecurityScheme(
        name = "security_auth",
        type = OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
                        tokenUrl = "${springdoc.oAuthFlow.tokenUrl}",
                        scopes = {
                                @OAuthScope(name = "product:read", description = "read scope"),
                                @OAuthScope(name = "product:write", description = "write scope")
                        }
                )))
public class OpenApiConfiguration {  }
