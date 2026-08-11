package com.learningsp.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String profileImage;
}
