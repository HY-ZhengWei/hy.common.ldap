package org.hy.common.ldap.junit;

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
 * 当时的测试环境为：OpenLDAP 2.4.42
 * 使用API为：Apache LDAP API 未使用OpenLDAP的 JLDAP 库。
 * 
 * API能通用的原因是：LDAP都是基于X.500标准，目前存在众多版本的LDAP，而最常见的则是V2和V3两个版本，它们分别于1995年和1997年首次发布。
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-13
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_LDAP
{
    private static boolean $isInit = false;
    
    
    
    public JU_LDAP() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(JU_LDAP.class.getName());
        }
    }
    
    
    
    @Test
    public void test_001_Add()
    {
        LDAP v_LDAP = (LDAP)XJava.getObject("LDAP");
        
        User v_User = new User();
        v_User.setId(      "ou=ZhengWei,dc=maxcrc,dc=com");
        v_User.setName(    "ZhengWei");
        v_User.setPassword("1234567890");
        
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
    
    
    
    @Test
    public void test_002_QueryEntry()
    {
        LDAP v_LDAP = (LDAP)XJava.getObject("LDAP");
        User v_User = (User)v_LDAP.queryEntry("ou=ZhengWei,dc=maxcrc,dc=com");
        
        if ( v_User != null )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  查询成功." + v_User.toString());
        }
        else
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  查询异常.");
        }
    }
    
    
    
    @Test
    public void test_003_QueryEntryChilds()
    {
        LDAP         v_LDAP   = (LDAP)XJava.getObject("LDAP");
        List<Object> v_Entrys = v_LDAP.queryEntryChilds("dc=maxcrc,dc=com");
        
        if ( !Help.isNull(v_Entrys) )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  查询成功.");
            Help.print(v_Entrys);
        }
        else
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  查询异常.");
        }
    }
    
    
    
    @Test
    public void test_004_QueryEntryTrees()
    {
        LDAP         v_LDAP   = (LDAP)XJava.getObject("LDAP");
        List<Object> v_Entrys = v_LDAP.queryEntryTrees("dc=maxcrc,dc=com");
        
        if ( !Help.isNull(v_Entrys) )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  查询成功.");
            Help.print(v_Entrys);
        }
        else
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  查询异常.");
        }
    }
    
    
    
    @Test
    public void test_011_IsExists()
    {
        LDAP    v_LDAP     = (LDAP)XJava.getObject("LDAP");
        boolean v_IsExists = v_LDAP.isExists("ou=ZhengWei,dc=maxcrc,dc=com");
        
        if ( v_IsExists )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  DN存在.");
        }
        else
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  DN不存在.");
        }
    }
    
    
    
    @Test
    public void test_012_Del()
    {
        LDAP v_LDAP = (LDAP)XJava.getObject("LDAP");
        
        boolean v_Ret = v_LDAP.delEntry("ou=ZhengWei,dc=maxcrc,dc=com");
        
        if ( v_Ret )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  删除成功.");
        }
        else
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  删除异常.");
        }
    }
    
}
