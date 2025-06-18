package com.example.goal_service.feign;

import com.example.goal_service.dto.GoalMateUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", configuration = FeignClientConfig.class)
public interface AuthClient {

    @GetMapping("/admin/getUser/{email}")
    GoalMateUserDto getUserByEmail(@PathVariable String email);

    @GetMapping("/admin/check")
    boolean isCurrentUserAdmin();
}

