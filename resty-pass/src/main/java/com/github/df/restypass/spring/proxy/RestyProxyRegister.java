package com.github.df.restypass.spring.proxy;

import com.github.df.restypass.annotation.RestyService;
import com.github.df.restypass.command.RestyCommandContext;
import com.github.df.restypass.spring.EnableRestyPass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 说明: RestyService代理类注册器
 *
 * @author darren-fu
 * @contact 13914793391
 * @date 2016 /11/22
 */
public class RestyProxyRegister implements ImportBeanDefinitionRegistrar,
        ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware {

    private static final Logger log = LoggerFactory.getLogger(RestyProxyRegister.class);
    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;
    private Environment environment;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        try {
            RestyCommandContext commandContext = RestyCommandContext.getInstance();
//            Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(EnableRestyPass.class.getName());

            // bean搜索器
            ClassPathScanningCandidateComponentProvider scanner = getScanner();
            Set<String> basePackages = getBasePackages(importingClassMetadata);

            for (String basePackage : basePackages) {
                // 搜索符合条件的bean
                Set<BeanDefinition> components = scanner.findCandidateComponents(basePackage);

                for (BeanDefinition component : components) {
                    if (component instanceof ScannedGenericBeanDefinition) {

                        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RestyProxyBeanFactory.class);
                        beanDefinitionBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                        Class beanClz = Class.forName(component.getBeanClassName());
                        // 分析类元数据，并存储到RestyCommandContext中
                        commandContext.initContextForService(beanClz);

                        beanDefinitionBuilder.addPropertyValue("type", beanClz);
                        beanDefinitionBuilder.addPropertyValue("restyCommandContext", commandContext);
                        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
                        beanDefinition.setPrimary(true);
                        // 注册bean
                        registry.registerBeanDefinition(component.getBeanClassName(), beanDefinition);
                        log.info("生成代理类:{}", component.getBeanClassName());

                    }
                }
            }
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RestyCommandContext.class);
            beanDefinitionBuilder.setFactoryMethod("getInstance");
            beanDefinitionBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            registry.registerBeanDefinition(RestyCommandContext.class.getName(), beanDefinitionBuilder.getBeanDefinition());
        } catch (ClassNotFoundException e) {
            log.error("class not found", e);
        }
    }

    /**
     * Gets scanner.
     *
     * @return the scanner
     */
    protected ClassPathScanningCandidateComponentProvider getScanner() {
        ClassPathScanningCandidateComponentProvider scan = new CustomClassPathScanningCandidateComponentProvider(false);
        scan.addIncludeFilter((metadataReader, metadataReaderFactory) -> metadataReader.getClassMetadata().isInterface()
                && metadataReader.getAnnotationMetadata().hasAnnotation(RestyService.class.getName())
                && isProfileMatch(metadataReader.getAnnotationMetadata())
        );
        return scan;
    }


    /**
     * Gets base packages.
     *
     * @param importingClassMetadata the importing class metadata
     * @return the base packages
     */
    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableRestyPass.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        if (StringUtils.hasText((String) attributes.get("value"))) {
            basePackages.add((String) attributes.get("value"));
        }

        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    private static class CustomClassPathScanningCandidateComponentProvider extends ClassPathScanningCandidateComponentProvider {

        /**
         * Instantiates a new Custom class path scanning candidate component provider.
         *
         * @param useDefaultFilters the use default filters
         */
        CustomClassPathScanningCandidateComponentProvider(boolean useDefaultFilters) {
            super(useDefaultFilters);
        }

        @Override
        protected boolean isCandidateComponent(
                AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
        }
    }

    private boolean isProfileMatch(AnnotatedTypeMetadata metadata) {
        if (environment != null) {
            MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(Profile.class.getName());
            if (attrs != null) {
                for (Object value : attrs.get("value")) {
                    if (environment.acceptsProfiles(((String[]) value))) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

}
