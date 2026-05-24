package com.lobster.trade.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class MyBatisFixConfig implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] names = beanFactory.getBeanDefinitionNames();
        for (String name : names) {
            try {
                var def = beanFactory.getBeanDefinition(name);
                var attr = def.getAttribute("factoryBeanObjectType");
                if (attr instanceof String s) {
                    try {
                        Class<?> cls = Class.forName(s);
                        def.setAttribute("factoryBeanObjectType", cls);
                    } catch (ClassNotFoundException ignored) {}
                }
            } catch (Exception ignored) {}
        }
    }
}