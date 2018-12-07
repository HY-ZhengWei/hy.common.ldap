package org.hy.common.ldap.junit.dbtoldap;

import java.util.List;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.ldap.LDAP;
import org.hy.common.ldap.objectclasses.LDAPNode;
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
    
    
    
    /**
     * 加载配置
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-06
     * @version     v1.0
     */
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
                this.init("org.hy.common.ldap.bean");
                this.init("org.hy.common.ldap.junit.dbtoldap");
            }
            catch (Exception exce)
            {
                System.out.println(exce.getMessage());
                exce.printStackTrace();
            }
        }
    }
    
    
    
    /**
     * 从关系型数据库中拉取数据，并推入到LDAP中。 
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-06
     * @version     v1.0
     */
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
        System.out.println(Date.getNowTime().getFullMilli() + "  从关系型数据库中查询到 " + v_Users.size() + " 位用户信息。");
        
        
        LDAP     v_LDAP          = (LDAP)XJava.getObject("LDAP");
        String   v_LDAPNodeDN    = "ou=users,dc=wzyb,dc=com";
        LDAPNode v_UsersLDAPNode = (LDAPNode)v_LDAP.queryEntry(v_LDAPNodeDN);
        
        // 如果父节点不存在，先创建父节点，之后再在父节点下创建用户数据。
        if ( v_UsersLDAPNode == null )
        {
            v_UsersLDAPNode = new LDAPNode();
            
            v_UsersLDAPNode.setId(v_LDAPNodeDN);
            v_UsersLDAPNode.setDescription("用户信息分类");
            
            boolean v_Ret = v_LDAP.addEntry(v_UsersLDAPNode);
            if ( !v_Ret )
            {
                System.err.println("在LDAP服务上创建父节点异常，请查检并确保分区Partition是存在的。");
                return;
            }
            else
            {
                System.out.println(Date.getNowTime().getFullMilli() + "  创建父节点成功。");
            }
        }
        
        create(v_Users);
    }
    
    
    
    /**
     * 用另外多个数据库中的同步更新用户信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-06
     * @version     v1.0
     *
     */
    @Test
    public void test_UnionToLDAP()
    {
        IUserDAO       v_UserDAO = (IUserDAO)XJava.getObject("UserDAO");
        List<UserInfo> v_Users   = null;
        
        v_Users = v_UserDAO.queryUnionA();
        if ( Help.isNull(v_Users) )
        {
            System.err.println("未从关系型数据库中查询到用户");
            return;
        }
        System.out.println(Date.getNowTime().getFullMilli() + "  从关系型数据库中查询到 " + v_Users.size() + " 位用户信息。");
        // 添加不存的用户，已存在的不添加
        create(v_Users);
        addAttr(v_Users);
        
        
        v_Users = v_UserDAO.queryUnionB01();  // 登陆名登陆
        if ( Help.isNull(v_Users) )
        {
            System.err.println("未从关系型数据库中查询到用户");
            return;
        }
        System.out.println(Date.getNowTime().getFullMilli() + "  从关系型数据库中查询到 " + v_Users.size() + " 位用户信息。");
        // 添加不存的用户，已存在的不添加
        create(v_Users);
        addAttr(v_Users);
        
        
        v_Users = v_UserDAO.queryUnionB02();  // 工号登陆
        if ( Help.isNull(v_Users) )
        {
            System.err.println("未从关系型数据库中查询到用户");
            return;
        }
        System.out.println(Date.getNowTime().getFullMilli() + "  从关系型数据库中查询到 " + v_Users.size() + " 位用户信息。");
        addAttr(v_Users);
    }
    
    
    
    /**
     * 用另外多个数据库中的同步更新用户信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-07
     * @version     v1.0
     *
     */
    @Test
    public void test_UnionB02ToLDAP()
    {
        IUserDAO       v_UserDAO = (IUserDAO)XJava.getObject("UserDAO");
        List<UserInfo> v_Users   = null;
        
        v_Users = v_UserDAO.queryUnionB02();  // 工号登陆
        if ( Help.isNull(v_Users) )
        {
            System.err.println("未从关系型数据库中查询到用户");
            return;
        }
        System.out.println(Date.getNowTime().getFullMilli() + "  从关系型数据库中查询到 " + v_Users.size() + " 位用户信息。");
        addAttr(v_Users);
    }
    
    
    
    /**
     * 添加条目
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-07
     * @version     v1.0
     *
     * @param i_Users
     */
    private void create(List<UserInfo> i_Users)
    {
        LDAP v_LDAP      = (LDAP)XJava.getObject("LDAP");
        Date v_BeginTime = new Date();
        int  v_AddCount  = v_LDAP.addEntrys(i_Users);
        Date v_EndTime   = new Date();
        
        if ( v_AddCount > 0 )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  添加成功。");
            System.out.println(Date.getNowTime().getFullMilli() + "  共添加 " + v_AddCount + " 位用户信息。");
            System.out.println(Date.getNowTime().getFullMilli() + "  共用时 " + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        }
        else if ( v_AddCount == 0 )
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  未添加任何用户.");
        }
        else
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  添加异常.");
        }
    }
    
    
    
    /**
     * 添加条目属性
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-07
     * @version     v1.0
     *
     * @param i_Users
     */
    private void addAttr(List<UserInfo> i_Users)
    {
        LDAP v_LDAP          = (LDAP)XJava.getObject("LDAP");
        Date v_BeginTime     = new Date();
        int  v_ModEntryCount = v_LDAP.modifyEntrys(i_Users ,true ,false ,false);  // 只添加属性，不删除属性
        Date v_EndTime       = new Date();
        
        if ( v_ModEntryCount > 0 )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  合并成功。");
            System.out.println(Date.getNowTime().getFullMilli() + "  共合并 " + v_ModEntryCount + " 位用户信息。");
            System.out.println(Date.getNowTime().getFullMilli() + "  共用时 " + Date.toTimeLen(v_EndTime.differ(v_BeginTime)));
        }
        else if ( v_ModEntryCount == 0 )
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  未合并任何数据.");
        }
        else
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  合并异常.");
        }
    }
    
    
    
    @Test
    public void test_ModUser()
    {
        LDAP     v_LDAP     = (LDAP)XJava.getObject("LDAP");
        UserInfo v_UserInfo = new UserInfo();
        
        v_UserInfo.setUserID("uid=52140,ou=users,dc=wzyb,dc=com");
        v_UserInfo.setLoginName("admin-zw");
        v_UserInfo.setLoginPwd("123456");
        v_UserInfo.setLoginName("mes-zw");
        v_UserInfo.setLoginPwd("654321");
        v_UserInfo.setLastTime(new Date());
        
        int v_ModAttrCount = v_LDAP.modifyEntry(v_UserInfo ,true ,true ,false);
        if ( v_ModAttrCount > 0 )
        {
            System.out.println(Date.getNowTime().getFullMilli() + "  修改了 " + v_ModAttrCount + " 个属性.");
        }
        else if ( v_ModAttrCount == 0 )
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  未修改任何属性.");
        }
        else
        {
            System.err.println(Date.getNowTime().getFullMilli() + "  修改异常.");
        }
    }
    
}
