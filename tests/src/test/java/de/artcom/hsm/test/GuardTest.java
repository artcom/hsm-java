package de.artcom.hsm.test;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import de.artcom.hsm.Action;
import de.artcom.hsm.Guard;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.TransitionKind;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class GuardTest {

    @Test
    public void testFirstGuard() {
        // given:
        Action enterA2 = mock(Action.class);
        Action enterA3 = mock(Action.class);

        State a1 = new State("a1");
        State a2 = new State("a2").onEnter(enterA2);
        State a3 = new State("a3").onEnter(enterA3);

        a1.addHandler("T1", a2, TransitionKind.External, new Guard() {
            @Override
            public boolean evaluate(Map<String, Object> payload) {
                Boolean foo = (Boolean) payload.get("foo");
                return foo;
            }
        }).addHandler("T1", a3, TransitionKind.External, new Guard() {
            @Override
            public boolean evaluate(Map<String, Object> payload) {
                Boolean foo = (Boolean) payload.get("foo");
                return !foo;
            }
        });

        StateMachine sm = new StateMachine(a1, a2, a3);
        sm.init();

        //when:
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("foo", true);
        sm.handleEvent("T1", payload);

        //then:
        verify(enterA2).run();
        verifyZeroInteractions(enterA3);
    }

    @Test
    public void testSecondGuard() {
        // given:
        Action enterA2 = mock(Action.class);
        Action enterA3 = mock(Action.class);

        State a1 = new State("a1");
        State a2 = new State("a2").onEnter(enterA2);
        State a3 = new State("a3").onEnter(enterA3);

        a1.addHandler("T1", a2, TransitionKind.External, new Guard() {
            @Override
            public boolean evaluate(Map<String, Object> payload) {
                Boolean foo = (Boolean) payload.get("foo");
                return foo;
            }
        }).addHandler("T1", a3, TransitionKind.External, new Guard() {
            @Override
            public boolean evaluate(Map<String, Object> payload) {
                Boolean foo = (Boolean) payload.get("foo");
                return !foo;
            }
        });

        StateMachine sm = new StateMachine(a1, a2, a3);
        sm.init();

        //when:
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("foo", false);
        sm.handleEvent("T1", payload);

        //then:
        verify(enterA3).run();
        verifyZeroInteractions(enterA2);
    }

    @Test
    public void createHandlerWithActionAndGuard() {
        // given:
        Action a1Action = mock(Action.class);
        State a = new State("a");
        a.addHandler("T1", a, TransitionKind.Internal, a1Action, new Guard() {
            @Override
            public boolean evaluate(Map<String, Object> payload) {
                return payload.containsKey("foo");
            }
        });
        StateMachine sm = new StateMachine(a);
        sm.init();

        // when:
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("foo", "bar");
        sm.handleEvent("T1", payload);

        // then:
        verify(a1Action).run();
    }

}
