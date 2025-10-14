package com.ecommerce.orderprocessing.user.repository;

import com.ecommerce.orderprocessing.user.domain.entity.User;
import com.ecommerce.orderprocessing.common.AbstractContainerBaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;


import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_shouldReturnUser() {
        // Given
        User user = new User("Test", "User", "test@test.com", "password");
        entityManager.persistAndFlush(user);

        // When
        User found = userRepository.findByEmail("test@test.com").orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void existsByEmail_shouldReturnTrue() {
        // Given
        User user = new User("Test", "User", "test@test.com", "password");
        entityManager.persistAndFlush(user);

        // When
        boolean exists = userRepository.existsByEmail("test@test.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void findByIsActiveTrue_shouldReturnActiveUsers() {
        User inactiveUser = new User("Inactive", "User", "inactive@test.com", "password");
        inactiveUser.setIsActive(false);
        entityManager.persistAndFlush(inactiveUser);

        // When
        List<User> activeUsers = userRepository.findByIsActiveTrue();

        // Then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo(inactiveUser.getEmail());
    }

    /*
    @Test
    void findByRole_shouldReturnCustomersWithRole() {
        // Given
        User adminUser = new User("Admin User", "admin@test.com", "password");
        adminUser.setRole(UserRole.ADMIN);
        entityManager.persistAndFlush(adminUser);

        User regularUser = new User("Regular User", "user@test.com", "password");
        regularUser.setRole(UserRole.CUSTOMER);
        entityManager.persistAndFlush(regularUser);

        // When
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);

        // Then
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getEmail()).isEqualTo(adminUser.getEmail());
    }
    */

    @Test
    void findUsersCreatedBetween_shouldReturnUsers() {
        // Given
        User user1 = new User("User", "1", "user1@test.com", "password");
        user1.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(user1);

        User user2 = new User("User", "2", "user2@test.com", "password");
        user2.setCreatedAt(LocalDateTime.now().plusDays(1));
        entityManager.persistAndFlush(user2);

        // When
        List<User> users = userRepository.findUsersCreatedBetween(LocalDateTime.now().minusDays(2), LocalDateTime.now());

        // Then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo(user1.getEmail());
    }

    @Test
    void countByIsActiveTrue_shouldReturnCountOfActiveUsers() {
        // Given
        User activeUser = new User("Active", "User", "active@test.com", "password");
        activeUser.setIsActive(true);
        entityManager.persistAndFlush(activeUser);

        User inactiveUser = new User("Inactive", "User", "inactive@test.com", "password");
        inactiveUser.setIsActive(false);
        entityManager.persistAndFlush(inactiveUser);

        // When
        long count = userRepository.countByIsActiveTrue();

        // Then
        assertThat(count).isEqualTo(1);
    }
}
