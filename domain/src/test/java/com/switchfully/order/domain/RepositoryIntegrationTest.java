package com.switchfully.order.domain;

import com.switchfully.order.IntegrationTest;
import com.switchfully.order.domain.customers.Customer;
import com.switchfully.order.domain.customers.CustomerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.switchfully.order.domain.customers.CustomerTestBuilder.aCustomer;

@DataJpaTest
class RepositoryIntegrationTest extends IntegrationTest {

    @Autowired
    CustomerRepository repository;

    @Test
    void save() {
        Customer customerToSave = aCustomer().build();

        Customer savedCustomer = repository.save(customerToSave);

        Assertions.assertThat(savedCustomer.getId()).isNotNull();
        Assertions.assertThat(repository.findById(savedCustomer.getId()))
                .get()
                .isEqualToComparingFieldByField(savedCustomer);
    }

    @Test
    void update() {
        Customer customerToSave = aCustomer().withFirstname("Jo").withLastname("Jorissen").build();
        Customer savedCustomer = repository.save(customerToSave);


        Customer updatedCustomer = repository.save(aCustomer()
                .withId(savedCustomer.getId())
                .withFirstname("Joske")
                .withLastname("Jorissen")
                .build());

        Assertions.assertThat(updatedCustomer.getId()).isNotNull().isEqualTo(savedCustomer.getId());
        Assertions.assertThat(updatedCustomer.getFirstname()).isEqualTo("Joske");
        Assertions.assertThat(updatedCustomer.getLastname()).isEqualTo("Jorissen");
        Assertions.assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void get() {
        Customer savedCustomer = repository.save(aCustomer().build());

        Customer actualCustomer = repository.findById(savedCustomer.getId()).orElse(null);

        Assertions.assertThat(actualCustomer)
                .isEqualToComparingFieldByField(savedCustomer);
    }

    @Test
    void getAll() {
        Customer customerOne = repository.save(aCustomer().build());
        Customer customerTwo = repository.save(aCustomer().build());

        List<Customer> allCustomers = repository.findAll();

        Assertions.assertThat(allCustomers).containsExactly(customerOne, customerTwo);
    }

}
