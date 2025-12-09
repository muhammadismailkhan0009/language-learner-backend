package com.myriadcode.languagelearner.common.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

@Configuration
public class JpaConfig implements BeanPostProcessor {

    private final Environment environment;

    public JpaConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LocalContainerEntityManagerFactoryBean emf) {
            System.out.println("🎯 Found EntityManagerFactory bean: " + beanName);

            // Create and add post processor
            PersistenceUnitPostProcessor postProcessor = pui -> {
                System.out.println("✅ BeanPostProcessor approach - PostProcessor called!");
                System.out.println("📦 Auto-detected entities: " + pui.getManagedClassNames().size());

                var classNames = pui.getManagedClassNames();
                if (classNames != null) {
                    classNames.removeIf(className -> {
                        try {
                            Class<?> clazz = Class.forName(className);
                            var profile = clazz.getAnnotation(Profile.class);
                            return profile != null && !environment.acceptsProfiles(profile.value());
                        } catch (Exception e) {
                            return false;
                        }
                    });
                }
            };

            emf.setPersistenceUnitPostProcessors(postProcessor);
        }
        return bean;
    }
}

