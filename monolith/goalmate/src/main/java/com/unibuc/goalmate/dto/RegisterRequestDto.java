package com.unibuc.goalmate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    @NotNull(message = "Email must not be null.")
    @Size(min = 1, message = "Email must be at least 1 character long.")
    @Pattern(regexp= "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$",
            message="Format email invalid.")
    private String email;

    @NotNull(message = "Username must not be null.")
    @Size(min = 1, message = "Username must be at least 1 character long.")
    private String username;

    @NotNull(message = "Password must not be null.")
    @Size(min = 1, message = "Password must be at least 1 character long.")
    @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message="The password must include at least one uppercase letter, " +
                    "one lowercase letter, one special character, one number, and be at least 8 characters long.")
    private String password;
}
