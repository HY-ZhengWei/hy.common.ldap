package org.hy.common.ldap.junit.apacheDS;

import org.hy.common.ldap.annotation.Ldap;
import org.hy.common.ldap.annotation.LdapType;





/**
 * ApacheDS服务的OU信息类。
 * 
 * 测试"条目翻译官"
 *
 * @author      ZhengWei(HY)
 * @createDate  2021-08-17
 * @version     v1.0
 */
@Ldap("organizationalUnit ,top")
public class OUObject
{
    
    @Ldap(type=LdapType.DN)
    private String id;
    
    @Ldap("ou")
    private String ou;
    
    @Ldap("description")
    private String description;

    
    
    public String getOu()
    {
        return ou;
    }

    
    public void setOu(String ou)
    {
        this.ou = ou;
    }

    
    public String getDescription()
    {
        return description;
    }

    
    public void setDescription(String description)
    {
        this.description = description;
    }

    
    public String getId()
    {
        return id;
    }

    
    public void setId(String id)
    {
        this.id = id;
    }
    
}
