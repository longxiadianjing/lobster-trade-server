package com.lobster.trade.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

@Component
public class FixFactoryBeanTypePostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        fixStringFactoryBeanObjectTypes(registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        fixStringFactoryBeanObjectTypes(beanFactory);
    }

    private void fixStringFactoryBeanObjectTypes(Object registry) {
        String[] names;
        if (registry instanceof BeanDefinitionRegistry bdr) {
            names = bdr.getBeanDefinitionNames();
        } else if (registry instanceof ConfigurableListableBeanFactory bdf) {
            names = bdf.getBeanDefinitionNames();
        } else {
            return;
        }

        for (String name : names) {
            try {
                BeanDefinition def;
                if (registry instanceof BeanDefinitionRegistry) {
                    def = ((BeanDefinitionRegistry) registry).getBeanDefinition(name);
                } else {
                    def = ((ConfigurableListableBeanFactory) registry).getBeanDefinition(name);
                }

                Object attr = def.getAttribute("factoryBeanObjectType");
                if (attr instanceof String s && !s.isEmpty()) {
                    try {
                        Class<?> cls = Class.forName(s, false, Thread.currentThread().getContextClassLoader());
                        def.setAttribute("factoryBeanObjectType", cls);
                        System.out.println("[FIX] Fixed factoryBeanObjectType for: " + name + " -> " + cls.getName());
                    } catch (ClassNotFoundException e) {
                        // Not a valid class name - leave as string
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE; // Run first
    }
}