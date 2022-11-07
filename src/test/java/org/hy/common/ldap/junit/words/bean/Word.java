package org.hy.common.ldap.junit.words.bean;

import org.hy.common.Date;
import org.hy.common.ldap.annotation.Ldap;
import org.hy.common.ldap.annotation.LdapType;
import org.hy.common.xml.SerializableDef;





/**
 * 测试Model类 
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-17
 * @version     v1.0
 */
@Ldap("word ,top")
public class Word extends SerializableDef
{

    private static final long serialVersionUID = 4089899117849514994L;
    
    /** 定义DN */
    @Ldap(type=LdapType.DN ,name="entryDB")
    private String id;
    
    @Ldap
    private String en;
    
    @Ldap
    private String noun;
    
    @Ldap
    private String v;
    
    @Ldap
    private String vt;
    
    @Ldap
    private String vi;
    
    @Ldap
    private Date   createTime;

    
    
    public String getId()
    {
        return id;
    }

    
    public void setId(String id)
    {
        this.id = id;
    }

    
    public String getEn()
    {
        return en;
    }

    
    public void setEn(String en)
    {
        this.en = en;
    }


    public String getNoun()
    {
        return noun;
    }

    
    public void setNoun(String noun)
    {
        this.noun = noun;
    }

    
    public String getV()
    {
        return v;
    }

    
    public void setV(String v)
    {
        this.v = v;
    }

    
    public String getVt()
    {
        return vt;
    }

    
    public void setVt(String vt)
    {
        this.vt = vt;
    }

    
    public String getVi()
    {
        return vi;
    }

    
    public void setVi(String vi)
    {
        this.vi = vi;
    }

    
    public Date getCreateTime()
    {
        return createTime;
    }

    
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
    
}
