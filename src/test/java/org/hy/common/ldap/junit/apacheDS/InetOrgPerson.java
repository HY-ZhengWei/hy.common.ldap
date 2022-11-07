package org.hy.common.ldap.junit.apacheDS;

import java.util.List;

import org.hy.common.Date;
import org.hy.common.ldap.annotation.Ldap;
import org.hy.common.ldap.annotation.LdapType;
import org.hy.common.xml.SerializableDef;





/**
 * ApacheDS服务的用户信息类。
 * 
 * 测试"条目翻译官"
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-06
 * @version     v1.0
 */
/** 定义ObjectClass。多类名个用英文逗号分隔。 */
@Ldap("inetOrgPerson ,organizationalPerson ,person ,top")
public class InetOrgPerson extends SerializableDef
{
    
    private static final long serialVersionUID = 8402644581084788601L;

    /** 定义DN */
    @Ldap(type=LdapType.DN)
    private String personNo;
    
    /** 当 LdapType.DN personNo="uid=xx,dc=example,dc=com" 时，在查询时，uid将自动赋值。在写入时，也不用特意赋值 */
    @Ldap("uid")
    private String       uid;
    
    /** 支持多个属性值的LDAP特性：字符类型的集合 */
    @Ldap("cn")
    private List<String> name;
    
    /** 支持多个属性值的LDAP特性：日期类型的集合，LDAP目录数据库中的属性值可以是字符串类型的 */
    @Ldap("description")
    private List<Date>   timeList;
    
    /** 支持多个属性值的LDAP特性：万能元素类型的集合 */
    @Ldap("localityName")
    private List<?>      dataList;
    
    /** 支持多个属性值的LDAP特性：字符类型的数组 */
    @Ldap("telephoneNumber")
    private String []    tel;
    
    /** 支持多个属性值的LDAP特性：日期类型的数组，LDAP目录数据库中的属性值可以是字符串类型的 */
    @Ldap("gn")
    private Date   []    times;
    
    @Ldap("sn")
    private String       surname;

    
    
    /**
     * 获取：定义DN
     */
    public String getPersonNo()
    {
        return personNo;
    }

    
    /**
     * 设置：定义DN
     * 
     * @param personNo 
     */
    public void setPersonNo(String personNo)
    {
        this.personNo = personNo;
    }

    
    /**
     * 获取：当 LdapType.DN personNo="uid=xx,dc=example,dc=com" 时，在查询时，uid将自动赋值。在写入时，也不用特意赋值
     */
    public String getUid()
    {
        return uid;
    }

    
    /**
     * 设置：当 LdapType.DN personNo="uid=xx,dc=example,dc=com" 时，在查询时，uid将自动赋值。在写入时，也不用特意赋值
     * 
     * @param uid 
     */
    public void setUid(String uid)
    {
        this.uid = uid;
    }

    
    /**
     * 获取：支持多个属性值的LDAP特性：字符类型的集合
     */
    public List<String> getName()
    {
        return name;
    }

    
    /**
     * 设置：支持多个属性值的LDAP特性：字符类型的集合
     * 
     * @param name 
     */
    public void setName(List<String> name)
    {
        this.name = name;
    }

    
    /**
     * 获取：支持多个属性值的LDAP特性：日期类型的集合，LDAP目录数据库中的属性值可以是字符串类型的
     */
    public List<Date> getTimeList()
    {
        return timeList;
    }

    
    /**
     * 设置：支持多个属性值的LDAP特性：日期类型的集合，LDAP目录数据库中的属性值可以是字符串类型的
     * 
     * @param timeList 
     */
    public void setTimeList(List<Date> timeList)
    {
        this.timeList = timeList;
    }

    
    /**
     * 获取：支持多个属性值的LDAP特性：万能元素类型的集合
     */
    public List<?> getDataList()
    {
        return dataList;
    }

    
    /**
     * 设置：支持多个属性值的LDAP特性：万能元素类型的集合
     * 
     * @param dataList 
     */
    public void setDataList(List<?> dataList)
    {
        this.dataList = dataList;
    }

    
    /**
     * 获取：支持多个属性值的LDAP特性：字符类型的数组
     */
    public String [] getTel()
    {
        return tel;
    }

    
    /**
     * 设置：支持多个属性值的LDAP特性：字符类型的数组
     * 
     * @param tel 
     */
    public void setTel(String [] tel)
    {
        this.tel = tel;
    }

    
    /**
     * 获取：支持多个属性值的LDAP特性：日期类型的数组，LDAP目录数据库中的属性值可以是字符串类型的
     */
    public Date [] getTimes()
    {
        return times;
    }

    
    /**
     * 设置：支持多个属性值的LDAP特性：日期类型的数组，LDAP目录数据库中的属性值可以是字符串类型的
     * 
     * @param times 
     */
    public void setTimes(Date [] times)
    {
        this.times = times;
    }

    
    /**
     * 获取：
     */
    public String getSurname()
    {
        return surname;
    }

    
    /**
     * 设置：
     * 
     * @param surname 
     */
    public void setSurname(String surname)
    {
        this.surname = surname;
    }
    
}
