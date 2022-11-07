package org.hy.common.ldap.junit.apacheDS;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.ldap.LDAP;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





/**
 * 测试：LDAP目录服务的操作类
 * 
 * 当时的测试环境为：ApacheDS-2.0.0.AM25-x86_64
 * 使用API为：Apache LDAP API 2.0.0.AM2。
 * 
 * @author      ZhengWei(HY)
 * @createDate  2018-12-06
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JU_LDAP_Person
{
    
    private static boolean $isInit = false;
    
    
    
    public JU_LDAP_Person() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(JU_LDAP_Person.class.getName());
        }
    }
    
    
    
    /**
     * 添加OU
     * 
     * @author      ZhengWei(HY)
     * @createDate  2021-08-17
     * @version     v1.0
     */
    @Test
    public void test_000_AddOU()
    {
        LDAP     v_LDAP = (LDAP)XJava.getObject("LDAP");
        OUObject v_OU   = new OUObject();
        
        v_OU.setId("ou=ou001,dc=wzyb,dc=com");
        v_OU.setDescription("说明");
        
        boolean v_Ret = v_LDAP.addEntry(v_OU);
        
        if ( v_Ret )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  添加成功.");
        }
        else
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  添加异常.");
        }
    }
    
    
    
    /**
     * 添加人员。测试前，先确保 ou=xx,dc=wzyb,dc=com 层次的树结构。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-06
     * @version     v1.0
     */
    @Test
    public void test_001_AddPerson()
    {
        LDAP          v_LDAP  = (LDAP)XJava.getObject("LDAP");
        InetOrgPerson v_User  = new InetOrgPerson();
        List<String>  v_Names = new ArrayList<String>();
        String []     v_Tels  = new String[2];
        Date   []     v_Times = new Date[2];
        
        v_Names.add("ZhengWei");
        v_Names.add("HY");
        
        v_Tels[0] = "13612345678";
        v_Tels[1] = "19912345678";
        
        v_Times[0] = new Date();
        v_Times[1] = new Date().getFirstTimeOfDay();
        
        v_User.setPersonNo("uid=003,ou=xx,dc=wzyb,dc=com");
        v_User.setName(    v_Names);
        v_User.setTimeList(Help.toList(v_Times));
        v_User.setTel(     v_Tels);
        v_User.setTimes(   v_Times);
        v_User.setDataList(v_User.getTimeList());
        v_User.setSurname( "ZhengWei");
        
        boolean v_Ret = v_LDAP.addEntry(v_User);
        
        if ( v_Ret )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  添加成功.");
        }
        else
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  添加异常.");
        }
    }
    
    
    
    /**
     * 按DN查询人员
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-06
     * @version     v1.0
     */
    @Test
    public void test_002_QueryPerson()
    {
        LDAP          v_LDAP = (LDAP)XJava.getObject("LDAP");
        InetOrgPerson v_User = (InetOrgPerson)v_LDAP.queryEntry("uid=003,ou=xx,dc=wzyb,dc=com");
        
        if ( v_User != null )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  查询成功." + v_User.toString());
        }
        else
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  查询异常.");
        }
    }
    
}
