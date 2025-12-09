package com.myriadcode.languagelearner.user_management.application.endpoints.user_info;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.user_management.application.endpoints.ApiRequest;
import com.myriadcode.languagelearner.user_management.application.endpoints.ApiResponse;
import com.myriadcode.languagelearner.user_management.application.endpoints.user_info.request.UserInfoRequest;
import com.myriadcode.languagelearner.user_management.application.services.UserRegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth/user")
@Validated
public class UserAuthController {

    @Autowired
    private UserRegistrationService userRegistration;

    @PostMapping("register")
    public ResponseEntity<ApiResponse<UserId>> registerUser(@Valid @RequestBody ApiRequest<UserInfoRequest> userInfoToRegister) {
        var id = userRegistration.registerUser(userInfoToRegister.payload());
        return ResponseEntity.ok(new ApiResponse<>(id));
    }


    public record AccountEmail(String email) {
    }

    public record AuthInformation(String authHeaderToken, String installId, String email) {
    }
}
