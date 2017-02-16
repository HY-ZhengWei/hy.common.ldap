package org.hy.common.ldap.junit;

import org.hy.common.ldap.annotation.Ldap;
import org.hy.common.ldap.annotation.LdapType;
import org.hy.common.xml.SerializableDef;





/**
 * 用户信息类。
 * 
 * 可测试"条目翻译官"
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-14
 * @version     v1.0
 */
@Ldap("top ,organizationalUnit")
public class User extends SerializableDef
{
    
    private static final long serialVersionUID = -7490271838098991229L;
    
    
    
    @Ldap(type=LdapType.DN)
    private String id;

    @Ldap(name="ou")
    private String name;
    
    @Ldap("userPassword")
    private String password;

    
    
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
    
}
