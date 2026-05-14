package com.mb.brokerageprovider.config;

import com.mb.brokerageprovider.integration_tests.api.controller.OrderControllerIntegrationTests;
import com.mb.brokerageprovider.integration_tests.api.controller.UserControllerIntegrationTests;
import com.mb.brokerageprovider.integration_tests.service.impl.OrderServiceImplIntegrationTest;
import com.mb.brokerageprovider.mapper.OrderMapperTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

@Slf4j
public class SpringBootTestClassOrderer implements ClassOrderer {

    private static int getOrder(ClassDescriptor classDescriptor) {
        if (classDescriptor.getDisplayName().equals(UserControllerIntegrationTests.class.getSimpleName())) {
            return 1;
        } else if (classDescriptor.getDisplayName().equals(OrderControllerIntegrationTests.class.getSimpleName())) {
            return 2;
        } else if (classDescriptor.getDisplayName().equals(OrderMapperTest.class.getSimpleName())) {
            return 3;
        } else if (classDescriptor.getDisplayName().equals(OrderServiceImplIntegrationTest.class.getSimpleName())) {
            return 4;
        } else {
            return 5;
        }
    }

    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(Comparator.comparingInt(SpringBootTestClassOrderer::getOrder));
    }
}
