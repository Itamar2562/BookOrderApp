package com.itamarprojects.bookorderapp.users;

public  enum Permissions
{
    MANAGER_CREATE("manager:create"),
    MANAGER_DELETE("manager:delete"),
    MANAGER_UPDATE("manager:update"),
    MANAGER_READ("manager:read"),

    WORKER_CREATE("worker:create"),
    WORKER_READ("worker:read"),
    WORKER_UPDATE("worker:update"),
    WORKER_DELETE("worker:delete");

    Permissions(String permission){
        this.permission = permission;
    }

    private final String permission;

    public String getPermission()
    {
        return this.permission;
    }
}
