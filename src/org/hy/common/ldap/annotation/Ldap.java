package org.hy.common.ldap.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;





/**
 * Ldap的注解接口
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2017-02-14
 */
@Documented
@Target({ElementType.TYPE ,ElementType.METHOD ,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ldap
{
    
    /**
     * LDAP中的"对象类ObjectClass"的名称。多个有逗号分隔。有先后有顺序。
     * 
     * 使用在：Java类的定义上
     * 
     * 编码形式如：@Ldap(objectClass="top ,person")
     * 也可简写为：@Ldap("top ,person")
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @return
     */
    public String objectClass() default "";
    
    
    
    /**
     * LDAP中的"属性Attribute"的名称。
     * 
     * 使用在：Java类的属性上，或Java类的setter、getter方法上。
     * 
     * 编码形式如：      @Ldap(name="ou")
     * 也可简写为：      @Ldap("ou")
     * 也可采用方法名称： @Ldap
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @return
     */
    public String name() default "";
    
    
    
    /**
     * 1. 当用在Java类的定义上时，与 Ldap.objectClass 同义。
     * 2. 当用在Java类的属性上时，与 Ldap.name        同义。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @return
     */
    public String value() default "";
    
}
