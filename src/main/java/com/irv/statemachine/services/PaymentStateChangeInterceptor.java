package com.irv.statemachine.services;

import com.irv.statemachine.domain.Payment;
import com.irv.statemachine.domain.PaymentEvent;
import com.irv.statemachine.domain.PaymentState;
import com.irv.statemachine.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository repository;

    @Override
    public void postStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
                                Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine,
                                StateMachine<PaymentState, PaymentEvent> rootStateMachine) {
        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER,-1L)))
                    .ifPresent(paymentId->{
                        Payment payment = repository.getById(paymentId);
                        payment.setPaymentState(state.getId());
                        repository.save(payment);
                    });
        });
    }
}
