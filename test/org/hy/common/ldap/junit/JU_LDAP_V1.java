package org.hy.common.ldap.junit;

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
public class JU_LDAP_V1
{
    private static boolean $isInit = false;
    
    
    
    public JU_LDAP_V1() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(JU_LDAP_V1.class.getName());
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
    public void test_002_Adds()
    {
        LDAP         v_LDAP   = (LDAP)XJava.getObject("LDAP");
        User         v_User01 = new User();
        User         v_User02 = new User();
        List<Object> v_Values = new ArrayList<Object>();
        
        v_User01.setId(      "ou=Batch01,dc=maxcrc,dc=com");
        v_User01.setName(    "Batch01");
        v_User01.setPassword("1234567890");
        
        v_User02.setId(      "ou=Batch02,dc=maxcrc,dc=com");
        v_User02.setName(    "Batch02");
        v_User02.setPassword("0987654321");
        
        v_Values.add(v_User01);
        v_Values.add(v_User02);
        
        int v_AddCount = v_LDAP.addEntrys(v_Values);
        
        if ( v_AddCount > 0 )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  批量添加成功，共添加 " + v_AddCount + " 个条目.");
        }
        else if ( v_AddCount == 0 )
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  未添加任何条目.");
        }
        else
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  批量添加异常.");
        }
    }
    
    
    
    @Test
    public void test_003_QueryEntry()
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
    
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void test_004_QueryEntryChilds()
    {
        LDAP       v_LDAP   = (LDAP)XJava.getObject("LDAP");
        List<User> v_Entrys = (List<User>)v_LDAP.queryEntryChilds("dc=maxcrc,dc=com");
        
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
    
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void test_005_QueryEntryTrees()
    {
        LDAP       v_LDAP   = (LDAP)XJava.getObject("LDAP");
        List<User> v_Entrys = (List<User>)v_LDAP.queryEntryTrees("dc=maxcrc,dc=com");
        
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
    public void test_012_modifyAttribute()
    {
        LDAP v_LDAP         = (LDAP)XJava.getObject("LDAP");
        User v_User         = new User();
        int  v_ModAttrCount = 0;
        
        v_User.setId(      "ou=ZhengWei,dc=maxcrc,dc=com");
        v_User.setName(    "ZhengWei");
        v_User.setPassword("改密码：ABC");
        v_User.setAddress( "新属性：西安");
        
        
        v_ModAttrCount = v_LDAP.addAttributes(v_User);     // 自动识别要添加的多个属性
        if ( v_ModAttrCount > 0 )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  添加属性成功.");
        }
        else if ( v_ModAttrCount == 0 )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  未添加任何属性.");
        }
        else
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  添加属性异常.");
        }
        
        
        v_ModAttrCount = v_LDAP.modifyAttributes(v_User);  // 自动识别要修改的多个属性
        if ( v_ModAttrCount > 0 )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  修改属性成功，共修改 " + v_ModAttrCount + " 个属性.");
        }
        else if ( v_ModAttrCount == 0 )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  未修改任何属性.");
        }
        else
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  修改属性异常.");
        }
        
        
        v_User.setAddress(null);                  // 置Null即为删除
        v_ModAttrCount = v_LDAP.delAttributes(v_User);     // 自动识别要删除的多个属性
        if ( v_ModAttrCount > 0 )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  删除属性成功，共删除 " + v_ModAttrCount + " 个属性.");
        }
        else if ( v_ModAttrCount == 0 )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  未删除任何属性.");
        }
        else
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  删除属性异常.");
        }
    }
    
    
    
    @Test
    public void test_091_Del()
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
    
    
    
    @Test
    public void test_092_Dels()
    {
        LDAP         v_LDAP = (LDAP)XJava.getObject("LDAP");
        List<String> v_DNs  = new ArrayList<String>();  
        
        v_DNs.add("ou=Batch01,dc=maxcrc,dc=com");
        v_DNs.add("ou=Batch02,dc=maxcrc,dc=com");
        
        boolean v_Ret = v_LDAP.delEntrys(v_DNs);
        
        if ( v_Ret )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  批量删除成功.");
        }
        else
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  批量删除异常.");
        }
    }
    
}
