package com.project.ecommerce.components;

import com.project.ecommerce.models.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if( authentication != null && authentication.getPrincipal() instanceof User user){
            if(!user.isActive()){
                return null;
            }
                return (User) authentication.getPrincipal();
        }
        return null;
    }
}
