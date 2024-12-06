package com.elice.homealone.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

@SpringBootTest
public class ApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testAllBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Registered Spring Beans:");
        Arrays.stream(beanNames).forEach(System.out::println);
    }
}