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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final StateMachineFactory<PaymentState,PaymentEvent> factory;
    private final PaymentStateChangeInterceptor interceptor;
    @Autowired
    private PaymentRepository repository;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setPaymentState(PaymentState.NEW);
        return repository.save(payment);
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId,sm,PaymentEvent.PRE_AUTHORIZE_APPROVED);

        return sm;
    }
    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId,sm,PaymentEvent.AUTHORIZATION_APPROVED);

        return sm;
    }
    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId,sm,PaymentEvent.AUTHORIZATION_DECLINE);

        return sm;
    }

    /**
     * Construye un mensaje con la informacion
     * de un pago
     * @param paymentId
     * @param sm
     * @param event
     */
    private void sendEvent(Long paymentId,StateMachine<PaymentState,PaymentEvent> sm,PaymentEvent event){
        Message message = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER,paymentId)
                .build();

        sm.sendEvent(message);
    }

    /**
     * Consulta el estado de un pago en la db
     * y lo actualiza para volverlo a subir a
     * db (lo actualiza)
     * @param paymentId pago
     * @return el sm actualizado
     */
    private StateMachine<PaymentState,PaymentEvent> build(Long paymentId){
        Payment payment = repository.getById(paymentId);
        StateMachine<PaymentState,PaymentEvent> sm = factory.getStateMachine(Long.toString(payment.getId()));

        sm.stop();

        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(interceptor);
            sma.resetStateMachine(
                    new DefaultStateMachineContext<>(payment.getPaymentState(),null,null,null));
        });
        sm.start();
        return sm;
    }
}
