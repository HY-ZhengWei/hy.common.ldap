package org.hy.common.ldap.junit.dbtoldap;

import java.util.List;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.ldap.LDAP;
import org.hy.common.xml.XJava;
import org.hy.common.xml.plugins.AppInitConfig;
import org.junit.Test;





/**
 * 测试单元：从关系型数据库中拉取数据，并推入到LDAP中。 
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-06
 * @version     v1.0
 */
public class JU_DBToLDAP extends AppInitConfig
{
    
    private static boolean $Init = false;
    
    
    
    public JU_DBToLDAP()
    {
        this.init();
    }
    
    
    
    private synchronized void init()
    {
        if ( !$Init )
        {
            $Init = true;
            
            try
            {
                this.init("sys.DB.Config.xml");
                this.init("sys.LDAP.Config.xml");
                this.init("db.SQL.xml");
                this.init("org.hy.common.ldap.junit.dbtoldap");
            }
            catch (Exception exce)
            {
                System.out.println(exce.getMessage());
                exce.printStackTrace();
            }
        }
    }
    
    
    
    @Test
    public void test_DBToLDAP()
    {
        IUserDAO       v_UserDAO = (IUserDAO)XJava.getObject("UserDAO");
        List<UserInfo> v_Users   = v_UserDAO.queryAll();
        
        if ( Help.isNull(v_Users) )
        {
            System.err.println("未从关系型数据库中查询到用户");
            return;
        }
        
        Date v_BeginTime = new Date();
        System.out.println(v_BeginTime.getFullMilli() + "  从关系型数据库中查询到 " + v_Users.size() + " 位用户信息。");
        
        LDAP    v_LDAP    = (LDAP)XJava.getObject("LDAP");
        boolean v_Ret     = v_LDAP.addEntrys(v_Users);
        Date    v_EndTime = new Date();
        
        if ( v_Ret )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  添加成功。");
            System.out.println(Date.getNowTime().getFullMilli() + "  共添加 " + v_Users.size() + " 位用户信息。");
            System.out.println(Date.getNowTime().getFullMilli() + "  共用时 " + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        }
        else
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  添加异常.");
        }
    }
    
}
