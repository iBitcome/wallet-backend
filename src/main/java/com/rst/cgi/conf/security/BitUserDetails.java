package com.rst.cgi.conf.security;

import com.rst.cgi.data.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 用户基本信息封装
 * @author huangxiaolin
 * @date 2018-05-15 14:52
 */
public class BitUserDetails implements UserDetails {

    private final UserEntity user;

    public BitUserDetails(UserEntity user) {
        this.user = user;
    }

    public UserEntity getUser() {
        return this.user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        //账号没冻结
        if (user.getIsFrozen() == 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BitUserDetails) {
            if (user.getId().equals(((BitUserDetails) obj).getUser().getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return user.getId().hashCode();
    }
}
