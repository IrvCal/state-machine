package com.irv.statemachine.services;

import com.irv.statemachine.domain.Payment;
import com.irv.statemachine.domain.PaymentEvent;
import com.irv.statemachine.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService{
    Payment newPayment(Payment payment);
    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
}
