package org.hy.common.ldap.junit;

import org.hy.common.ldap.annotation.Ldap;
import org.hy.common.ldap.annotation.LdapType;
import org.hy.common.xml.SerializableDef;





/**
 * OpenLDAP的用户信息类。
 * 
 * 测试"条目翻译官"
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-14
 * @version     v1.0@Ldap("top ,organizationalUnit")
 */
/** 定义ObjectClass。多类名个用英文逗号分隔。 */

public class User extends SerializableDef
{
    
    private static final long serialVersionUID = -7490271838098991229L;
    
    
    
    /** 定义DN */
    @Ldap(type=LdapType.DN)
    private String id;

    /** 定义属性ou */
    @Ldap(name="ou")
    private String name;
    
    /** 定义属性userPassword */
    @Ldap("userPassword")
    private String password;
    
    /** 定义属性street */
    @Ldap("street")
    private String address;

    
    
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }
    
}
