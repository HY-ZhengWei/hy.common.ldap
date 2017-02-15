package org.hy.common.ldap;

import java.util.Map;

import org.apache.directory.api.ldap.model.cursor.Cursor;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
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
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaITImpl;
import org.apache.directory.api.ldap.model.message.controls.OpaqueControl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
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
    
    public static final String $ControlOID = "1.2.840.113556.1.4.805";
    
    /** LDAP的连接池 */
    private LdapConnectionPool connPool;
    
    
    
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
     * @return      返回对象的具体实现类是：org.apache.directory.ldap.client.api.LdapNetworkConnection
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
    
    
    
    public Object getEntry(String i_DN ,Class<?> i_MetaClass)
    {
        LdapConnection v_Conn      = null;
        LdapEntry      v_LdapEntry = getLdapEntry(i_MetaClass);
        EntryCursor    v_Cursor    = null;
        Object         v_Ret       = null;
        
        if ( v_LdapEntry == null )
        {
            return null;
        }
        
        try
        {
            v_Conn   = this.getConnection();
            v_Cursor = v_Conn.search(new Dn(i_DN) ,"(objectclass=*)" ,SearchScope.OBJECT);
            
            while ( v_Cursor.next() )
            {
                Entry v_Entry = v_Cursor.get();
                if ( v_Entry != null )
                {
                    v_Ret = v_LdapEntry.toObject(v_Entry);
                    break;
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
        
        return v_Ret;
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
     * @param i_DN  条目标识
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
    
    
    
    /**
     * 删除条目树（递归删除条目）
     * 
     * LdapNetworkConnection类中是一个deleteTree()方法的，但没有返回值，真不知道开发者是怎么考虑的。
     * 看过它的源码，发现它是通过delete()方法实现的，只是添加一个控制对象。所以本方法实现思路相同，如下。
     * 
     * @see org.apache.directory.ldap.client.api.LdapNetworkConnection.deleteTree()
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @param i_DN  条目标识
     * @return
     */
    public boolean delEntryTree(String i_DN)
    {
        LdapConnection v_Conn     = null;
        DeleteRequest  v_Request  = new DeleteRequestImpl();
        DeleteResponse v_Response = null;
        
        try
        {
            v_Request.setName(new Dn(i_DN));
            
            v_Request.addControl(new OpaqueControl($ControlOID));
            v_Conn = (LdapNetworkConnection)this.getConnection();
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
