package de.artcom.hsm.test;

import de.artcom.hsm.*;
import de.artcom.hsm.test.helper.Logger;
import groovy.util.logging.Log;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class CustomLogStateMachineTest {



    @Test
    public void getAllActiveStates() {
        // given:
        State a11 = new State("a11");
        State a22 = new State("a22");
        State a33 = new State("a33");
        Parallel a1 = new Parallel("a1", new StateMachine(a11), new StateMachine(a22, a33));
        Sub a = new Sub("a", a1);
        StateMachine sm = new StateMachine(a);
        sm.setLogger(new Logger());
        sm.init();

        // when:
        List<State> allActiveStates = sm.getAllActiveStates();

        // then:
        assertThat(allActiveStates, hasItems(a, a1, a11, a22));
        assertThat(allActiveStates, not(hasItems(a33)));
    }

}
