package co.com.scl.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserAuthModelTest {

    @Test
    void builderSetsAllProvidedFields() {
        UserAuthModel user = UserAuthModel.builder()
            .username("john.doe")
            .password("secret")
            .active(true)
            .roles(Set.of("ADMIN"))
            .build();

        assertThat(user.getUsername()).isEqualTo("john.doe");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).containsExactly("ADMIN");
    }

    @Test
    void builderDefaultsRolesToEmptySetWhenNotProvided() {
        UserAuthModel user = UserAuthModel.builder()
            .username("john.doe")
            .password("secret")
            .active(false)
            .build();

        assertThat(user.getRoles()).isNotNull().isEmpty();
    }

    @Test
    void settersUpdateFieldValues() {
        UserAuthModel user = UserAuthModel.builder().build();

        user.setUsername("jane.doe");
        user.setPassword("changed");
        user.setActive(true);
        user.setRoles(Set.of("USER"));

        assertThat(user.getUsername()).isEqualTo("jane.doe");
        assertThat(user.getPassword()).isEqualTo("changed");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).containsExactly("USER");
    }

    @Test
    void equalsAndHashCodeAreBasedOnFieldValues() {
        UserAuthModel first = UserAuthModel.builder()
            .username("john.doe").password("secret").active(true).roles(Set.of("ADMIN")).build();
        UserAuthModel second = UserAuthModel.builder()
            .username("john.doe").password("secret").active(true).roles(Set.of("ADMIN")).build();

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}
