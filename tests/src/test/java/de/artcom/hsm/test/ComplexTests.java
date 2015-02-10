package de.artcom.hsm.test;

import org.junit.Before;
import org.junit.Test;

import de.artcom.hsm.Parallel;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.Sub;

public class ComplexTests {

    private State a1;
    private State a2;
    private State a3;
    private Sub a;

    private State b1;
    private Sub b2;
    private State b21;
    private State b22;
    private Sub b;

    private State c11;
    private State c12;
    private State c21;
    private State c22;
    private StateMachine c1Machine;
    private StateMachine c2Machine;
    private Parallel c;

    private StateMachine stateMachine;

    @Before
    public void setUpTest() {
        a1 = new State("a1");
        a2 = new State("a2");
        a3 = new State("a3");
        a = new Sub("a", a1, a2 , a3);

        b1 = new State("b1");
        b21 = new State("b21");
        b22 = new State("b22");
        b2 = new Sub("b2", b21, b22);
        b = new Sub("b", b1, b2);

        c11 = new State("c11");
        c12 = new State("c12");
        c21 = new State("c21");
        c22 = new State("c22");
        c1Machine = new StateMachine(c11, c12);
        c2Machine = new StateMachine(c21, c22);
        c = new Parallel("c", c1Machine, c2Machine);

        stateMachine = new StateMachine(a, b, c);
    }

    @Test
    public void complexStateMachineTest1() {
        //when:
        stateMachine.init();
        stateMachine.teardown();
        //then: no exceptions
    }
}
