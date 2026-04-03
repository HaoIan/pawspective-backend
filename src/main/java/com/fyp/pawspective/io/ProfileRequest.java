package com.fyp.pawspective.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileRequest {

    @NotBlank(message = "Name should not be empty")
    private String name;

    @Email(message = "Enter valid email address")
    @NotNull(message = "Email should not be empty")
    private String email;

    @Size(min = 6, message = "Password must contain at least 6 characters")
    private String password;

    @Pattern(regexp = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$", message = "Please enter a valid phone number")
    private String phone;

    private String role;
}
