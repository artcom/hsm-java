package de.artcom.hsm.test;

import org.junit.Test;
import org.mockito.InOrder;

import de.artcom.hsm.Action;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.Sub;
import de.artcom.hsm.TransitionKind;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class SubStateMachineTest {

    @Test
    public void canCreateSubStateMachine() {
        //given:
        State loud = new State("loud");
        State quiet = new State("quiet");
        Sub on = new Sub("on", new StateMachine(quiet, loud));

        //when:
        StateMachine sm = new StateMachine(on);
        sm.init();

        //then: no exception
    }

    @Test
    public void canTransitionSubStates() {
        //given:
        Action onEnterLoud = mock(Action.class);
        Action onEnterQuiet = mock(Action.class);
        Action onEnterOn = mock(Action.class);
        Action onEnterOff = mock(Action.class);
        State loud = new State("loud")
                .onEnter(onEnterLoud);
        State quiet = new State("quiet")
                .onEnter(onEnterQuiet);

        quiet.addHandler("volume_up", loud, TransitionKind.External);
        loud.addHandler("volume_down", quiet, TransitionKind.External);

        Sub on = new Sub("on", new StateMachine(quiet, loud))
                .onEnter(onEnterOn);

        State off = new State("off")
                .onEnter(onEnterOff);

        on.addHandler("switched_off", off, TransitionKind.External);
        off.addHandler("switched_on", on, TransitionKind.External);

        StateMachine sm = new StateMachine(off, on);
        sm.init();

        //when:
        sm.handleEvent("switched_on");
        sm.handleEvent("volume_up");
        sm.handleEvent("switched_off");
        sm.handleEvent("switched_on");

        //then:
        InOrder inOrder = inOrder(onEnterOff, onEnterOn, onEnterQuiet, onEnterLoud);
        inOrder.verify(onEnterOff).run();
        inOrder.verify(onEnterOn).run();
        inOrder.verify(onEnterQuiet).run();
        inOrder.verify(onEnterLoud).run();
        inOrder.verify(onEnterOff).run();
        inOrder.verify(onEnterOn).run();
        inOrder.verify(onEnterQuiet).run();
    }

}
