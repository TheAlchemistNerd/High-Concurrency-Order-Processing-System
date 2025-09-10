package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.domain.enumeration.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void findByEmail_shouldReturnCustomer() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        // When
        Customer found = customerRepository.findByEmail("test@test.com").orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    void existsByEmail_shouldReturnTrue() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        // When
        boolean exists = customerRepository.existsByEmail("test@test.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void findByIsActiveTrue_shouldReturnActiveCustomers() {
        // Given
        Customer activeCustomer = new Customer("Active Customer", "active@test.com", "password");
        activeCustomer.setIsActive(true);
        entityManager.persistAndFlush(activeCustomer);

        Customer inactiveCustomer = new Customer("Inactive Customer", "inactive@test.com", "password");
        inactiveCustomer.setIsActive(false);
        entityManager.persistAndFlush(inactiveCustomer);

        // When
        List<Customer> activeCustomers = customerRepository.findByIsActiveTrue();

        // Then
        assertThat(activeCustomers).hasSize(1);
        assertThat(activeCustomers.get(0).getEmail()).isEqualTo(activeCustomer.getEmail());
    }

    @Test
    void findByRole_shouldReturnCustomersWithRole() {
        // Given
        Customer adminCustomer = new Customer("Admin Customer", "admin@test.com", "password");
        adminCustomer.setRole(UserRole.ADMIN);
        entityManager.persistAndFlush(adminCustomer);

        Customer regularCustomer = new Customer("Regular Customer", "customer@test.com", "password");
        regularCustomer.setRole(UserRole.CUSTOMER);
        entityManager.persistAndFlush(regularCustomer);

        // When
        List<Customer> admins = customerRepository.findByRole(UserRole.ADMIN);

        // Then
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getEmail()).isEqualTo(adminCustomer.getEmail());
    }

    @Test
    void findCustomersCreatedBetween_shouldReturnCustomers() {
        // Given
        Customer customer1 = new Customer("Customer 1", "customer1@test.com", "password");
        customer1.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(customer1);

        Customer customer2 = new Customer("Customer 2", "customer2@test.com", "password");
        customer2.setCreatedAt(LocalDateTime.now().plusDays(1));
        entityManager.persistAndFlush(customer2);

        // When
        List<Customer> customers = customerRepository.findCustomersCreatedBetween(LocalDateTime.now().minusDays(2), LocalDateTime.now());

        // Then
        assertThat(customers).hasSize(1);
        assertThat(customers.get(0).getEmail()).isEqualTo(customer1.getEmail());
    }

    @Test
    void countByIsActiveTrue_shouldReturnCountOfActiveCustomers() {
        // Given
        Customer activeCustomer = new Customer("Active Customer", "active@test.com", "password");
        activeCustomer.setIsActive(true);
        entityManager.persistAndFlush(activeCustomer);

        Customer inactiveCustomer = new Customer("Inactive Customer", "inactive@test.com", "password");
        inactiveCustomer.setIsActive(false);
        entityManager.persistAndFlush(inactiveCustomer);

        // When
        long count = customerRepository.countByIsActiveTrue();

        // Then
        assertThat(count).isEqualTo(1);
    }
}
