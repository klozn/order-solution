package com.switchfully.order.api.customers;

import com.switchfully.order.ControllerIntegrationTest;
import com.switchfully.order.api.customers.addresses.AddressDto;
import com.switchfully.order.api.customers.emails.EmailDto;
import com.switchfully.order.api.customers.phonenumbers.PhoneNumberDto;
import com.switchfully.order.api.interceptors.ControllerExceptionHandler;
import com.switchfully.order.domain.customers.Customer;
import com.switchfully.order.domain.customers.CustomerRepository;
import com.switchfully.order.domain.customers.CustomerTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import javax.inject.Inject;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CustomerControllerIntegrationTest extends ControllerIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper customerMapper;

    @Test
    void createCustomer() {
        CustomerDto customerToCreate = new CustomerDto()
                .withFirstname("Bruce")
                .withLastname("Wayne")
                .withEmail(new EmailDto()
                        .withLocalPart("brucy")
                        .withDomain("bat.net")
                        .withComplete("brucy@bat.net"))
                .withPhoneNumber(new PhoneNumberDto()
                        .withNumber("485212121")
                        .withCountryCallingCode("+32"))
                .withAddress(new AddressDto()
                        .withStreetName("Secretstreet")
                        .withHouseNumber("841")
                        .withPostalCode("1238")
                        .withCountry("GothamCountry"));

        CustomerDto createdCustomer = new TestRestTemplate()
                .postForObject(format("http://localhost:%s/%s", getPort(), CustomerController.RESOURCE_NAME), customerToCreate, CustomerDto.class);

        assertCustomerIsEqualIgnoringId(customerToCreate, createdCustomer);
    }

    @Test
    void createCustomer_givenCustomerNotValidForCreationBecauseOfMissingFirstName_thenErrorObjectReturnedByControllerExceptionHandler() {
        CustomerDto customerToCreate = new CustomerDto()
                .withFirstname(null)
                .withLastname("Wayne")
                .withEmail(new EmailDto()
                        .withLocalPart("brucy")
                        .withDomain("bat.net")
                        .withComplete("brucy@bat.net"))
                .withPhoneNumber(new PhoneNumberDto()
                        .withNumber("485212121")
                        .withCountryCallingCode("+32"))
                .withAddress(new AddressDto()
                        .withStreetName("Secretstreet")
                        .withHouseNumber("841")
                        .withPostalCode("1238")
                        .withCountry("GothamCountry"));

        ControllerExceptionHandler.Error error = new TestRestTemplate()
                .postForObject(format("http://localhost:%s/%s", getPort(), CustomerController.RESOURCE_NAME), customerToCreate, ControllerExceptionHandler.Error.class);

        assertThat(error).isNotNull();
        assertThat(error.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(error.getUniqueErrorId()).isNotNull().isNotEmpty();
        assertThat(error.getMessage()).contains("Invalid Customer provided for creation. " +
                "Provided object: Customer{id=");
    }

    @Test
    void getAllCustomers() {
        customerRepository.save(CustomerTestBuilder.aCustomer().build());
        customerRepository.save(CustomerTestBuilder.aCustomer().build());
        customerRepository.save(CustomerTestBuilder.aCustomer().build());

        CustomerDto[] allCustomers = new TestRestTemplate()
                .getForObject(format("http://localhost:%s/%s", getPort(), CustomerController.RESOURCE_NAME), CustomerDto[].class);

        assertThat(allCustomers).hasSize(3);
    }

    @Test
    void getAllCustomers_assertResultIsCorrectlyReturned() {
        Customer customerInDb = customerRepository.save(CustomerTestBuilder.aCustomer().build());

        CustomerDto[] allCustomers = new TestRestTemplate()
                .getForObject(format("http://localhost:%s/%s", getPort(), CustomerController.RESOURCE_NAME), CustomerDto[].class);

        assertThat(allCustomers).hasSize(1);
        assertThat(allCustomers[0])
                .usingRecursiveComparison()
                    .isEqualTo(customerMapper.toDto(customerInDb));
    }

    @Test
    void getCustomer() {
        customerRepository.save(CustomerTestBuilder.aCustomer().build());
        Customer customerToFind = customerRepository.save(CustomerTestBuilder.aCustomer().build());
        customerRepository.save(CustomerTestBuilder.aCustomer().build());

        CustomerDto foundCustomer = new TestRestTemplate()
                .getForObject(format("http://localhost:%s/%s/%s", getPort(), CustomerController.RESOURCE_NAME, customerToFind.getId().toString()), CustomerDto.class);

        assertThat(foundCustomer)
                .usingRecursiveComparison()
                    .isEqualTo(customerMapper.toDto(customerToFind));
    }

    private void assertCustomerIsEqualIgnoringId(CustomerDto customerToCreate, CustomerDto createdCustomer) {
        assertThat(createdCustomer.getId()).isNotNull().isNotEmpty();
        assertThat(createdCustomer.getAddress()).isEqualToComparingFieldByField(customerToCreate.getAddress());
        assertThat(createdCustomer.getPhoneNumber()).isEqualToComparingFieldByField(customerToCreate.getPhoneNumber());
        assertThat(createdCustomer.getEmail()).isEqualToComparingFieldByField(customerToCreate.getEmail());
        assertThat(createdCustomer.getFirstname()).isEqualTo(customerToCreate.getFirstname());
        assertThat(createdCustomer.getLastname()).isEqualTo(customerToCreate.getLastname());
    }

}
