package com.hsm.test;

import com.hsm.HelloWorld;
import spock.lang.Specification;

class HelloWorldSpec extends Specification {

    def "test the hello response"() {
        when:
          HelloWorld hello = new HelloWorld();
        then:
          hello.hello() == "Hello World";
    }

    def "test the hello response junit like"() {
        expect:
          new HelloWorld().hello() == "Hello World";
    }

    def "test the uncovered method"() {
        expect:
          new HelloWorld().uncoveredMethod() == "uncoveredMethod";
    }

}  
