package com.hsm.test;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.hsm.HelloWorld;

public class HelloWorldTest {

    @Test
    public void testResponse() {
        assertThat(new HelloWorld().hello(), equalTo("Hello World"));
    }

}
