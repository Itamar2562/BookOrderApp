package com.itamarprojects.bookorderapp.users;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum UserRole
{
    WORKER (
            Set.of(
                    Permissions.WORKER_CREATE,
                    Permissions.WORKER_DELETE,
                    Permissions.WORKER_UPDATE,
                    Permissions.WORKER_READ)
    ),
    MANAGER (
            Set.of(
                    Permissions.MANAGER_CREATE,
                    Permissions.MANAGER_DELETE,
                    Permissions.MANAGER_UPDATE,
                    Permissions.MANAGER_READ
            )
    );



    private final Set<Permissions> permissions;

    UserRole(Set<Permissions> permissions)
    {
        this.permissions = permissions;
    }

    Set<Permissions> getPermissions(){
        return this.permissions;
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }



}
