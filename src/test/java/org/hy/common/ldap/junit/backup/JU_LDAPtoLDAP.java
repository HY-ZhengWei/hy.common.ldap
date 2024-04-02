package org.hy.common.ldap.junit.backup;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.ldap.LDAP;
import org.hy.common.ldap.junit.dbtoldap.DSLdapUser;
import org.hy.common.ldap.junit.dbtoldap.UserInfo;
import org.hy.common.xml.XJava;
import org.hy.common.xml.log.Logger;
import org.hy.common.xml.plugins.AppInitConfig;
import org.junit.Test;





/**
 * 测试单元：备份LDAP01的数据到LDAP02服务上
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-29
 * @version     v1.0
 */
public class JU_LDAPtoLDAP extends AppInitConfig
{
    
    private static boolean $Init = false;
    
    private static final Logger $Logger = new Logger(JU_LDAPtoLDAP.class);
    
    
    
    public JU_LDAPtoLDAP()
    {
        this.init();
    }
    
    
    
    /**
     * 加载配置
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-12-29
     * @version     v1.0
     */
    private synchronized void init()
    {
        if ( !$Init )
        {
            $Init = true;
            
            try
            {
                this.init("sys.LDAP01.Config.xml");
                this.init("sys.LDAP02.Config.xml");
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
    public void test_Backup2023()
    {
        LDAP       v_LDAP01 = (LDAP)XJava.getObject("LDAP01");
        LDAP       v_LDAP02 = (LDAP)XJava.getObject("LDAP02");
        DSLdapUser v_User   = new DSLdapUser();
        
        List<DSLdapUser> v_Datas = new ArrayList<DSLdapUser>();
        
        String    v_UIDs   = StringHelp.replaceAll(StringHelp.replaceAll(XJava.getParam("UIDs").getValue() ,"\n" ,"") ,"\r" ,"");
        String [] v_UIDArr = v_UIDs.split(",");
        
        for (String v_UID : v_UIDArr)
        {
            v_User = (DSLdapUser)v_LDAP01.queryEntry("uid=" + v_UID + ",ou=users,dc=wwww,dc=com");
            
            if ( v_User != null )
            {
                v_Datas.add(v_User);
            }
        }
        
//        for (int x=50006; x<=52786; x++)
//        {
//            v_User = (DSLdapUser)v_LDAP01.queryEntry("uid=" + x + ",ou=users,dc=wwww,dc=com");
//
//            if ( v_User != null )
//            {
//                v_Datas.add(v_User);
//            }
//
//        }
        
        int              v_Count = 0;
        
        if ( !Help.isNull(v_Datas) )
        {
            //v_LDAP02.delEntryChildTree(v_User.getUserID());
            // v_Count = v_LDAP02.addEntrys(v_Datas);
            
            
            for (Object v_DataItem : v_Datas)
            {
                if ( v_DataItem instanceof DSLdapUser )
                {
                    v_LDAP02.delEntry(v_DataItem);
                    if ( v_LDAP02.addEntry(v_DataItem) )
                    {
                        v_Count++;
                    }
                }
                else
                {
                    $Logger.info(v_DataItem);
                }
            }
            
        }
        
        System.out.println("应备份 " + v_Datas.size() + " 条数据，实际成功备份 " + v_Count + " 条数据。");
    }
    
    
    
    /**
     * 备份数据
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_Backup()
    {
        LDAP       v_LDAP01 = (LDAP)XJava.getObject("LDAP01");
        LDAP       v_LDAP02 = (LDAP)XJava.getObject("LDAP02");
        DSLdapUser v_User   = new DSLdapUser();
        
        v_User.setUserID("ou=users,dc=wwww,dc=com");
        
        List<DSLdapUser> v_Datas = (List<DSLdapUser>)v_LDAP01.searchEntrys(v_User);
        int              v_Count = 0;
        
        if ( !Help.isNull(v_Datas) )
        {
            v_LDAP02.delEntryChildTree(v_User.getUserID());
            // v_Count = v_LDAP02.addEntrys(v_Datas);
            
            
            for (Object v_DataItem : v_Datas)
            {
                if ( v_DataItem instanceof DSLdapUser )
                {
                    if ( v_LDAP02.addEntry(v_DataItem) )
                    {
                        v_Count++;
                    }
                }
                else
                {
                    $Logger.info(v_DataItem);
                }
            }
            
        }
        
        System.out.println("应备份 " + v_Datas.size() + " 条数据，实际成功备份 " + v_Count + " 条数据。");
    }
    
    
    
    
    /**
     * 备份数据
     */
    @Test
    public void test_BackupWeiXin()
    {
        LDAP       v_LDAP01 = (LDAP)XJava.getObject("LDAP01");
        LDAP       v_LDAP02 = (LDAP)XJava.getObject("LDAP02");
        DSLdapUser v_User   = new DSLdapUser();
        
        v_User.setUserID("ou=weixin,dc=wwww,dc=com");
        
        List<?> v_Datas = v_LDAP01.searchEntrys(v_User);
        int     v_Count = 0;
        
        if ( !Help.isNull(v_Datas) )
        {
            v_LDAP02.delEntryChildTree(v_User.getUserID());
            v_Count = v_LDAP02.addEntrys(v_Datas);
            
        }
        
        System.out.println("应备份 " + v_Datas.size() + " 条数据，实际成功备份 " + v_Count + " 条数据。");
    }
    
    
    
    /**
     * 用备份数据恢复
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_Recovery()
    {
        LDAP     v_LDAP01   = (LDAP)XJava.getObject("LDAP01");
        LDAP     v_LDAP02   = (LDAP)XJava.getObject("LDAP02");
        UserInfo v_UserInfo = new UserInfo();
        
        v_UserInfo.setUserID("ou=users,dc=wwww,dc=com");
        
        List<UserInfo> v_Datas = (List<UserInfo>)v_LDAP02.searchEntrys(v_UserInfo);
        int            v_Count = 0;
        
        if ( !Help.isNull(v_Datas) )
        {
            for (UserInfo v_User : v_Datas)
            {
                v_LDAP01.delEntry(v_User);
                v_LDAP01.addEntry(v_User);
                
                v_Count++;
                System.out.println(v_Count + "\t" + v_User.getUserNo() + "\t" + v_User.getUserTrueName());
            }
        }
        
        System.out.println("应恢复 " + v_Datas.size() + " 条数据，实际成功恢复 " + v_Count + " 条数据。");
    }
    
}
