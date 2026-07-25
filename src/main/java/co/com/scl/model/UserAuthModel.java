package co.com.scl.model;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
@Builder
public class UserAuthModel {
    private String username;
    private String password;
    private boolean active;
    @Builder.Default
    private Set<String> roles = Collections.emptySet();
}