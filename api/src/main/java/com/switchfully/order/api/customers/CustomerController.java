package com.switchfully.order.api.customers;

import com.switchfully.order.service.customers.CustomerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping(path = "/" + CustomerController.RESOURCE_NAME)
public class CustomerController {

    public static final String RESOURCE_NAME = "customers";

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @Inject
    public CustomerController(CustomerService customerService, CustomerMapper customerMapper) {
        this.customerService = customerService;
        this.customerMapper = customerMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CustomerDto createCustomer(@RequestBody CustomerDto customerDto) {
        return customerMapper.toDto(
                customerService.createCustomer(
                        customerMapper.toDomain(customerDto)));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CustomerDto> getAllCustomers() {
        return customerService.getAllCustomers().stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping(path="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CustomerDto getCustomer(@PathVariable String id) {
        return customerMapper.toDto(
                customerService.getCustomer(UUID.fromString(id)));
    }

}
