package com.hsm.test;

import org.junit.Test; 
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.hsm.HelloWorld;

public class HelloWorldTest {

    @Test
    public void testResponse() {
        assertThat(new HelloWorld().hello(), equalTo("Hello World"));
    }

}
