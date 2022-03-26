package com.irv.statemachine.services;

import com.irv.statemachine.domain.Payment;
import com.irv.statemachine.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService service;
    @Autowired
    PaymentRepository repository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .amount(new BigDecimal("12.99"))
                .build();
    }

    @Transactional
    @Test
    void preAuth() {

        Payment savedPayment = service.newPayment(payment);
        System.out.println("Debe ser NEW: "+savedPayment.getPaymentState());
        service.preAuth(savedPayment.getId());
        Payment preAuthPayment = repository.getById(savedPayment.getId());
        System.out.println("Debe ser PRE_AUTH: "+preAuthPayment.getPaymentState());

    }
}