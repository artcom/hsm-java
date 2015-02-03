package com.hsm.test;

import org.testng.annotations.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.hsm.HelloWorld;

public class HelloWorldTest {

    @Test(description="hello world test")
    public void testResponse() {
        assertThat(new HelloWorld().hello(), equalTo("Hello World"));
    }

}
