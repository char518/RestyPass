package df.open.restypass.spring;

import df.open.restypass.spring.proxy.RestyProxyRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


/**
 * 启用RestyPass
 * Created by darrenfu on 17-6-19.
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RestyProxyRegister.class)
public @interface EnableRestyPass {

    /**
     * Value string.
     *
     * @return the string
     */
    String value() default "";

//    Class<? extends RestyPassFactory> factory() default DefaultRestyPassFactory.class;

    /**
     * Base packages string [ ].
     *
     * @return the string [ ]
     */
    String[] basePackages() default {};

    /**
     * Base package classes class [ ].
     *
     * @return the class [ ]
     */
    Class<?>[] basePackageClasses() default {};

}
