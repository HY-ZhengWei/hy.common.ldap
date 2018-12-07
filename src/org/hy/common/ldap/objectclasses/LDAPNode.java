package org.hy.common.ldap.objectclasses;

import org.hy.common.ldap.annotation.Ldap;
import org.hy.common.ldap.annotation.LdapType;
import org.hy.common.xml.SerializableDef;





/**
 * 简单的LDAP目录节点对象。因比较常用，所以在此统一定义一下。 
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-06
 * @version     v1.0
 */
@Ldap("organizationalUnit ,top")
public class LDAPNode extends SerializableDef
{

    private static final long serialVersionUID = -6841866580745371107L;
    
    /** 定义DN。RDN必须是：ou=xxx */
    @Ldap(type=LdapType.DN)
    private String id;
    
    /** 目录节点名称 */
    @Ldap("ou")
    private String name;
    
    /** 目录节点的本地名称 */
    @Ldap("localityName")
    private String localityName;
    
    /** 描述   */
    @Ldap("description")
    private String description;
    
    
    
    /**
     * 获取：定义DN。RDN必须是：ou=xxx
     */
    public String getId()
    {
        return id;
    }


    /**
     * 设置：定义DN。RDN必须是：ou=xxx
     * 
     * @param id 
     */
    public void setId(String id)
    {
        this.id = id;
    }


    /**
     * 获取：目录节点名称
     */
    public String getName()
    {
        return name;
    }

    
    /**
     * 设置：目录节点名称
     * 
     * @param name 
     */
    public void setName(String name)
    {
        this.name = name;
    }

    
    /**
     * 获取：目录节点的本地名称
     */
    public String getLocalityName()
    {
        return localityName;
    }

    
    /**
     * 设置：目录节点的本地名称
     * 
     * @param localityName 
     */
    public void setLocalityName(String localityName)
    {
        this.localityName = localityName;
    }


    /**
     * 获取：描述
     */
    public String getDescription()
    {
        return description;
    }

    
    /**
     * 设置：描述
     * 
     * @param description 
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
}
