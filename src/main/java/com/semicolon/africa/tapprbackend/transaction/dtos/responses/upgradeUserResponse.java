package com.semicolon.africa.tapprbackend.transaction.dtos.responses;

import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.enums.Role;

@Setter
@Getter
public class upgradeUserResponse {
    private String username;
    private Role role;
    private String message;

    public upgradeUserResponse(String userName, Role role, String userPromotedToAdminSuccessfully) {
        this.username = userName;
        this.role = role;
        this.message = userPromotedToAdminSuccessfully;
    }
}
