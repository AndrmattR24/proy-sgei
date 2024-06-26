package group5.idat.project_sgei.dto;

import lombok.Data;

@Data
public class RegisterUserDTO {

    private String fullName;
    private String username;
    private String email;
    private String password;
    private String rePassword;
}
