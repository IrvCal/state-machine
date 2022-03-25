package com.irv.statemachine.services;

import com.irv.statemachine.domain.Payment;
import com.irv.statemachine.domain.PaymentEvent;
import com.irv.statemachine.domain.PaymentState;
import com.irv.statemachine.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    private static final String PAYMENT_ID_HEADER = "payment_id";
    @Autowired
    private PaymentRepository repository;
    private final StateMachineFactory<PaymentState,PaymentEvent> factory;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setPaymentState(PaymentState.NEW);
        return repository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = buid(paymentId);

        sendEvent(paymentId,sm,PaymentEvent.PRE_AUTHORIZE);

        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = buid(paymentId);

        sendEvent(paymentId,sm,PaymentEvent.AUTHORIZATION_APPROVED);

        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = buid(paymentId);

        sendEvent(paymentId,sm,PaymentEvent.AUTHORIZATION_DECLINE);

        return null;
    }

    private void sendEvent(Long paymentId,StateMachine<PaymentState,PaymentEvent> sm,PaymentEvent event){
        Message message = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER,paymentId)
                .build();

        sm.sendEvent(message);
    }

    private StateMachine<PaymentState,PaymentEvent> buid(Long paymentId){
        Payment payment = repository.getById(paymentId);
        StateMachine<PaymentState,PaymentEvent> sm = factory.getStateMachine(Long.toString(payment.getId()));

        sm.stop();

        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.resetStateMachine(
                    new DefaultStateMachineContext<>(payment.getPaymentState(),null,null,null));
        });
        sm.start();
        return sm;
    }
}
