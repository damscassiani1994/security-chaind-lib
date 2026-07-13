package co.com.scl.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAuthModel {
    private String username;
    private String password;
    private boolean active;
}