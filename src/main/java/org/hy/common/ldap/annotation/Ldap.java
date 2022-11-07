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
     * LDAP中的"对象类ObjectClass"的名称。多个有逗号分隔。先后顺序不影响”条目配置翻译官“。区分大小写。
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
     * LDAP中的属性的类型。
     *   类型1. 普通属性（默认值）
     *   类型2. DN标识。 @Ldap(type=LdapType.DN) 注解在Java类中只允许定义一次。
     * 
     * 使用在：Java类的属性上，或Java类的setter、getter方法上。
     * 
     * 编码形式如：@Ldap(type=LdapType.Attribute)
     *           @Ldap(type=LdapType.DN)
     *           @Ldap(type=LdapType.DN ,name="entryDN")   当type=LdapType.DN的同时，name=为RDN的名称。
     *           @Ldap(type=LdapType.DN ,value="entryDN")  当type=LdapType.DN的同时，value=为RDN的名称。
     *           
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @return
     */
    public LdapType type() default LdapType.Attribute;
    
    
    
    /**
     * LDAP中的"属性Attribute"的名称。区分大小写。
     * 
     * 使用在：Java类的属性上，或Java类的setter、getter方法上。
     * 
     * 编码形式如：      @Ldap(name="ou")
     * 也可简写为：      @Ldap("ou")
     * 也可采用方法名称： @Ldap
     * 
     * 当type=LdapType.DN的同时，name=为RDN的名称。
     * 当type=LdapType.DN的同时，value=为RDN的名称。
     * 
     * 注意：当LDAP数据库中的属性名givenName简写为gn时，这里应当配置成givenName。
     *      配置成gn简写的名称，可以写入，但不能读取成功。LDAP按全称保存了。 
     *      
     *      但commonName的简写cn是可以用的。
     *      也就说，当用简写写入到LDAP中时，LDAP没有自动改成全称保存时，
     *      也是按简写保存的，就可以用简写，否则不建义用简写的属性名。
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
