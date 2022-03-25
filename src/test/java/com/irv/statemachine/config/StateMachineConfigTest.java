package com.irv.statemachine.config;

import com.irv.statemachine.domain.PaymentEvent;
import com.irv.statemachine.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StateMachineConfigTest {
    
    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewStateMachine() {
        StateMachine<PaymentState,PaymentEvent> sm = factory.getStateMachine(UUID.randomUUID());
        sm.start();
        getState(sm);
        sm.sendEvent(PaymentEvent.PRE_AUTHORIZE);
        getState(sm);
        sm.sendEvent(PaymentEvent.PRE_AUTHORIZE_APPROVED);
        getState(sm);
        sm.sendEvent(PaymentEvent.AUTHORIZATION_DECLINE);//se ignora esta linea y creo que es porque este comportamiento no se definio
        getState(sm);
    }
    void getState(StateMachine<PaymentState,PaymentEvent> sm){
        System.out.println(sm.getState().toString());
    }
}