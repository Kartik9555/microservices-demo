package com.microservices.demo.elastic.query.service.security;

import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceRequestModel;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class QueryServicePermissionEvaluator implements PermissionEvaluator {

    private static final String SUPER_USER_ROLE = "APP_SUPER_USER_ROLE";
    private final HttpServletRequest request;

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasPermission(Authentication authentication, @Nullable Object targetDomainObject, Object permission) {
        if(isSuperUser()){
            return true;
        }
        if(targetDomainObject instanceof ElasticQueryServiceRequestModel) {
            return preAuthorize(authentication, ((ElasticQueryServiceRequestModel) targetDomainObject).getId(), permission);
        } else if (targetDomainObject == null || targetDomainObject instanceof ResponseEntity<?>) {
            if (targetDomainObject == null) {
                return true;
            }
            List<ElasticQueryServiceResponseModel> response = ((ResponseEntity<List<ElasticQueryServiceResponseModel>>) targetDomainObject).getBody();
            Objects.requireNonNull(response);
            return postAuthorize(authentication, response, permission);
        }
        return false;
    }

    private boolean postAuthorize(Authentication authentication, List<ElasticQueryServiceResponseModel> response, Object permission) {
        TwitterQueryUser user = (TwitterQueryUser) authentication.getPrincipal();
        for (ElasticQueryServiceResponseModel responseModel : response) {
            Objects.requireNonNull(user);
            Objects.requireNonNull(user.getPermissions());
            PermissionType userPermission = user.getPermissions().get(responseModel.getId());
            if(!hasPermission((String)permission, userPermission)) {
                return false;
            }
        }
        return true;
    }

    private boolean preAuthorize(Authentication authentication, String id, Object permission) {
        TwitterQueryUser user = (TwitterQueryUser) authentication.getPrincipal();
        Objects.requireNonNull(user);
        Objects.requireNonNull(user.getPermissions());
        PermissionType userPermission = user.getPermissions().get(id);
        return hasPermission((String)permission, userPermission);
    }

    private static boolean hasPermission(String permission, PermissionType userPermission) {
        return userPermission != null && permission.equals(userPermission.getType());
    }

    @Override
    public boolean hasPermission(Authentication authentication, @Nullable Serializable targetId, String targetType, Object permission) {
        if(isSuperUser()){
            return true;
        }

        if(targetId == null) {
            return false;
        }
        return preAuthorize(authentication, (String) targetId, permission);
    }

    private boolean isSuperUser() {
        return request.isUserInRole(SUPER_USER_ROLE);
    }
}
