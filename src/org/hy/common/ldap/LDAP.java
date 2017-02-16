package org.hy.common.ldap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.cursor.Cursor;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
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
import org.hy.common.Help;
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
     * 用于：将Java值对象翻译为LDAP条目
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
        return ((Map<Class<?> ,LdapEntry>)XJava.getObject(LdapAnnotation.$LdapEntryClasses)).get(i_Class);
    }
    
    
    
    /**
     * 获取XJava对象池中的"条目翻译官"。
     * 
     * 用于：将LDAP条目翻译为Java值对象的实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @param i_ObjectClassesID  在定义Java类名称前用 @Ldap 注解的LDAP ObjectClass名称组成的ID。
     * @return
     */
    @SuppressWarnings("unchecked")
    public static LdapEntry getLdapEntry(String i_ObjectClassesID)
    {
        return ((Map<String ,LdapEntry>)XJava.getObject(LdapAnnotation.$LdapEntryClassIDs)).get(i_ObjectClassesID);
    }
    
    
    
    /**
     * 获取XJava对象池中的"条目翻译官"。
     * 
     * 用于：将LDAP条目翻译为Java值对象的实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @param i_Entry  条目。将解释条目中的ObjectClass，并组合成ID，再获取"条目翻译官"。
     * @return
     */
    public static LdapEntry getLdapEntry(Entry i_Entry)
    {
        if ( i_Entry == null )
        {
            return null;
        }
        
        Attribute          v_Attribute = i_Entry.get("objectclass");
        Iterator<Value<?>> v_Iter      = v_Attribute.iterator();
        StringBuilder      v_Buffer    = new StringBuilder();
        
        while (v_Iter.hasNext())
        {
            Value<?> v_Value = v_Iter.next();
            
            v_Buffer.append(",").append(v_Value.getString());
        }
        
        String v_ObjectClassesID = v_Buffer.toString();
        if ( Help.isNull(v_ObjectClassesID) )
        {
            return null;
        }
        else
        {
            return getLdapEntry(v_ObjectClassesID.substring(1));
        }
    }
    
    
    
    /**
     * 查询条目。只返回条目本身。即只返回一条记录(树目录的一个节点)。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN           条目标识
     * @return
     */
    public Object queryEntry(String i_DN)
    {
        List<Object> v_Ret = this.queryEntrys(i_DN ,SearchScope.OBJECT);
        
        if ( Help.isNull(v_Ret) )
        {
            return null;
        }
        else
        {
            return v_Ret.get(0);
        }
    }
    
    
    
    /**
     * 查询条目。返回直接隶属于i_DN的子条目，不返回子子条目。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN           条目标识
     * @return
     */
    public List<Object> queryEntryChilds(String i_DN)
    {
        return this.queryEntrys(i_DN ,SearchScope.ONELEVEL);
    }
    
    
    
    /**
     * 查询条目。返回直接或间接隶属于i_DN的子条目及子子条目。
     * 
     * 即将i_DN下面的树结构上的所有条目都返回。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN           条目标识
     * @return
     */
    public List<Object> queryEntryTrees(String i_DN)
    {
        return this.queryEntrys(i_DN ,SearchScope.SUBTREE);
    }
    
    
    
    /**
     * 查询条目。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN           条目标识
     * @param i_SearchScope  查询范围
     *                       搜索范围01：SearchScope.OBJECT    返回输入给定DN，如果它存在的话。
     *                       搜索范围02：SearchScope.ONELEVEL  返回低于目前DN的所有子元素，不包括当前DN，也不包括与当前DN无直接关系的DN，即树目录深度为1。
     *                       搜索范围03：SearchScope.SUBTREE   返回所有元素从给出的DN，包括与DN相关的元素，无论树的深度。
     * @return
     */
    private List<Object> queryEntrys(String i_DN ,SearchScope i_SearchScope)
    {
        LdapConnection v_Conn   = null;
        EntryCursor    v_Cursor = null;
        List<Object>   v_Ret    = new ArrayList<Object>();
        
        try
        {
            v_Conn   = this.getConnection();
            v_Cursor = v_Conn.search(new Dn(i_DN) ,"(objectclass=*)" ,i_SearchScope);
            
            while ( v_Cursor.next() )
            {
                Entry     v_Entry     = v_Cursor.get();
                LdapEntry v_LdapEntry = getLdapEntry(v_Entry);
                
                if ( v_LdapEntry != null )
                {
                    v_Ret.add(v_LdapEntry.toObject(v_Entry));
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
     * 只用于用 @Ldap 注解的Java对象。
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
     * Apache LDAP 主要提供三种添加条目的方法。当前用：同步的、服务器有返回响应的。
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
     * 判断条目是否存在
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @param i_DN  条目标识
     * @return
     */
    public boolean isExists(String i_DN)
    {
        LdapConnection v_Conn = null;
        boolean        v_Ret  = false;
        
        try
        {
            v_Conn = this.getConnection();
            v_Ret  = v_Conn.exists(i_DN);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            closeConnection(v_Conn);
        }
        
        return v_Ret;
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
