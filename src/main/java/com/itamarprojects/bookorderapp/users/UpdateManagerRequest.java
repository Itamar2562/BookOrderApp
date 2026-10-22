package com.itamarprojects.bookorderapp.users;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class UpdateManagerRequest
{
    @NotNull
    private UUID managerId;

    public UpdateManagerRequest(UUID managerId){
        this.managerId = managerId;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public void setManagerId(UUID managerId) {
        this.managerId = managerId;
    }
    @Override
    public String toString(){
        return this.managerId.toString();
    }

}
