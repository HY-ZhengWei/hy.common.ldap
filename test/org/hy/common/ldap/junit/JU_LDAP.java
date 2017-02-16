package org.hy.common.ldap.junit;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.hy.common.Date;
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
        v_User.setName(    "ZhengWei");
        v_User.setPassword("1234567890");
        
        boolean v_Ret = v_LDAP.addEntry("ou=ZhengWei,dc=maxcrc,dc=com" ,v_User);
        
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
    public void test_003_Del()
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
    public void test_004_Query()
    {
        LDAP           v_LDAP   = (LDAP)XJava.getObject("LDAP");
        LdapConnection v_Conn   = null;
        EntryCursor    v_Cursor = null;
        try
        {
            v_Conn   = v_LDAP.getConnection();
            v_Cursor = v_Conn.search(new Dn("dc=maxcrc,dc=com") ,"(objectclass=*)" ,SearchScope.SUBTREE);
            
            while ( v_Cursor.next() )
            {
                Entry v_Entry = v_Cursor.get();
                
                if ( v_Entry != null )
                {
                    System.out.println(v_Entry);
                }
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            LDAP.closeCursor(    v_Cursor);
            LDAP.closeConnection(v_Conn);
        }
    }
    
}
