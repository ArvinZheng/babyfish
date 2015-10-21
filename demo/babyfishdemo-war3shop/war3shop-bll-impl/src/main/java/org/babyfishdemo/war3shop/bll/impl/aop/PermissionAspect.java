package org.babyfishdemo.war3shop.bll.impl.aop;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.babyfish.collection.ArrayList;
import org.babyfish.util.Joins;
import org.babyfishdemo.war3shop.bll.AuthorizationException;
import org.babyfishdemo.war3shop.bll.AuthorizationService;
import org.babyfishdemo.war3shop.bll.Constants;
import org.springframework.stereotype.Component;

/**
 * @author Tao Chen
 */
@Aspect
@Component
public class PermissionAspect {
    
    @Resource
    private AuthorizationService authorizationService;

    @Pointcut(
            value = "execution(public * org.babyfishdemo.war3shop.bll.impl.*.*(..)) && " +
                    "@annotation(requirePrivilege)",
            argNames = "requirePrivilege"
    )
    public void underRequirePrivilege(RequirePermission requirePrivilege) {}
    
    @Before(
            value = "underRequirePrivilege(requirePrivilege)",
            argNames = "requirePrivilege")
    public void beforeServiceMethod(JoinPoint jp, RequirePermission requirePermission) {
        if (!this.authorizationService.hasPermissions(requirePermission.value())) {
            boolean beAccountManager = false;
            boolean beCustomer = false;
            Collection<String> administratorPermissions = new ArrayList<>(requirePermission.value().length);
            for (String permission : requirePermission.value()) {
                if (Constants.ACCOUNT_MANAGER_PERMISSION.equals(permission)) {
                    beAccountManager = true;
                } if (Constants.CUSTOMER_PERMISSION.equals(permission)) {
                    beCustomer = true;
                } else {
                    administratorPermissions.add(permission);
                }
            }
            List<String> actions = new ArrayList<>();
            if (beAccountManager) {
                actions.add("login as account manager");
            }
            if (beCustomer) {
                actions.add("login as customer");
            }
            if (!administratorPermissions.isEmpty()) {
                if (administratorPermissions.size() == 1) {
                    actions.add(
                            "login as administrator with the privilege \"" +
                            administratorPermissions.iterator().next() +
                            "\"");
                } else {
                    actions.add(
                            "login as administrator with any privilege of \"" +
                            Joins.join(administratorPermissions) +
                            "\""
                    );
                }
            }
            throw new AuthorizationException(
                    "The current operation \""
                    + jp.getSignature()
                    + "\" is forbidden, you must " +
                    Joins.join(actions, " or ")
            );
        }
    }
}
