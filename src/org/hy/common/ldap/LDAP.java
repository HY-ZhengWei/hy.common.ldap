package org.hy.common.ldap;

import java.util.Map;

import org.apache.directory.api.ldap.model.cursor.Cursor;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.AddRequestImpl;
import org.apache.directory.api.ldap.model.message.AddResponse;
import org.apache.directory.api.ldap.model.message.DeleteRequest;
import org.apache.directory.api.ldap.model.message.DeleteRequestImpl;
import org.apache.directory.api.ldap.model.message.DeleteResponse;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaITImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.hy.common.ldap.annotation.LdapAnnotation;
import org.hy.common.ldap.annotation.LdapEntry;
import org.hy.common.xml.XJava;





/**
 * LDAP目录服务的操作类。基于 apache LDAP API。
 * 
 * 可用于OpenLDAP目录服务的访问操作。
 * 
 * API能通用的原因是：LDAP都是基于X.500标准，目前存在众多版本的LDAP，而最常见的则是V2和V3两个版本，它们分别于1995年和1997年首次发布。
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-13
 * @version     v1.0
 */
public class LDAP
{
    
    /** LDAP的连接池 */
    LdapConnectionPool connPool;
    
    
    
    public LDAP(LdapConnectionPool i_ConnPool)
    {
        this.connPool = i_ConnPool;
        LdapAnnotation.parser();
    }
    
    
    
    /**
     * 获取连接池中一个有效连接
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-13
     * @version     v1.0
     *
     * @return
     * @throws LdapException
     */
    public LdapConnection getConnection() throws LdapException
    {
        return this.connPool.getConnection();
    }
    
    
    
    /**
     * 释放或关闭连接
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Conn
     */
    public static void closeConnection(LdapConnection i_Conn)
    {
        if ( i_Conn != null )
        {
            try
            {
                if ( !i_Conn.isConnected() )
                {
                    i_Conn.close();
                }
            }
            catch (Exception exce)
            {
                // Nothing.
            }
            i_Conn = null;
        }
    }
    
    
    
    /**
     * 关闭游标
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Cursor
     */
    public static void closeCursor(Cursor<?> i_Cursor)
    {
        if ( i_Cursor != null )
        {
            try
            {
                if ( !i_Cursor.isClosed() )
                {
                    i_Cursor.close();
                }
            }
            catch (Exception exce)
            {
                // Nothing.
            }
            i_Cursor = null;
        }
    }
    
    
    
    /**
     * 获取XJava对象池中的"条目翻译官"。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Class  有 @Ldap 注解的Java元类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static LdapEntry getLdapEntry(Class<?> i_Class)
    {
        return ((Map<Class<?> ,LdapEntry>)XJava.getObject(LdapAnnotation.$LdapClassesXID)).get(i_Class);
    }
    
    
    
    /**
     * 添加条目。
     * 
     * 只针对用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_DN
     * @param i_Elements
     * @return
     */
    public boolean addEntry(String i_DN ,Object i_Elements)
    {
        LdapEntry v_LdapEntry = getLdapEntry(i_Elements.getClass());
        
        if ( v_LdapEntry == null )
        {
            return false;
        }
        
        Entry v_Entry = null;
        
        try
        {
            v_Entry = v_LdapEntry.toEntry(i_Elements);
            
            v_Entry.setDn(i_DN);
            
            return this.addEntry(v_Entry);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return false;
    }
    
    
    
    /**
     * 添加条目
     * 
     * apache LDAP 主要提供三种添加条目的方法。当前用：同步的、服务器有返回响应的。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_DN        条目标识
     * @param i_Elements  条目属性
     * @return            添加成功返回true。
     */
    public boolean addEntry(String i_DN ,Object ... i_Elements)
    {
        Entry v_Entry = null;
        
        try
        {
            v_Entry = new DefaultEntry(i_DN ,i_Elements);
            
            return this.addEntry(v_Entry);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return false;
    }
    
    
    
    /**
     * 添加条目
     * 
     * Apache LDAP 主要提供三种添加条目的方法。当前用：同步的、服务器有返回响应的。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     * 
     * @param i_Entry
     * @return            添加成功返回true。
     */
    public boolean addEntry(Entry i_Entry)
    {
        LdapConnection v_Conn       = null;
        AddRequest     v_AddRequest = new AddRequestImpl();
        AddResponse    v_Response   = null;
        
        try
        {
            v_AddRequest.setEntry( i_Entry);
            v_AddRequest.addControl(new ManageDsaITImpl());
            
            v_Conn = this.getConnection();
            v_Response = v_Conn.add(v_AddRequest);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            closeConnection(v_Conn);
        }
        
        
        if ( v_Response != null && ResultCodeEnum.SUCCESS.equals(v_Response.getLdapResult().getResultCode()) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    
    /**
     * 删除条目
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @param i_DN
     * @return
     */
    public boolean delEntry(String i_DN)
    {
        LdapConnection v_Conn     = null;
        DeleteRequest  v_Request  = new DeleteRequestImpl();
        DeleteResponse v_Response = null;
        
        try
        {
            v_Request.setName(new Dn(i_DN));
            
            v_Conn = this.getConnection();
            v_Response = v_Conn.delete(v_Request);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            closeConnection(v_Conn);
        }
        
        
        if ( v_Response != null && ResultCodeEnum.SUCCESS.equals(v_Response.getLdapResult().getResultCode()) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
}
