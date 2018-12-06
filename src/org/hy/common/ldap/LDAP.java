package org.hy.common.ldap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.cursor.Cursor;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.AddRequestImpl;
import org.apache.directory.api.ldap.model.message.AddResponse;
import org.apache.directory.api.ldap.model.message.DeleteRequest;
import org.apache.directory.api.ldap.model.message.DeleteRequestImpl;
import org.apache.directory.api.ldap.model.message.DeleteResponse;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.message.ModifyResponse;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.controls.ManageDsaITImpl;
import org.apache.directory.api.ldap.model.message.controls.OpaqueControl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.ldap.annotation.LdapAnnotation;
import org.hy.common.ldap.annotation.LdapEntry;
import org.hy.common.xml.XJava;





/**
 * LDAP目录服务的操作类。基于 Apache LDAP API。
 * 
 * 可用于OpenLDAP目录服务的访问操作。
 * 
 * API能通用的原因是：LDAP都是基于X.500标准，目前存在众多版本的LDAP，而最常见的则是V2和V3两个版本，它们分别于1995年和1997年首次发布。
 * 
 * 
 * 主导思想：通过 @Ldap 注解十分方便的实现Java写入、读取LDAP目录服务中的数据，同时不破坏、不改造Java程序原有的数据结构。
 * 
 *   特点1：使用LDAP连接池操作LDAP目录服务。
 *   特点2：内部自动获取连接，自动释放连接，无须外界干预。
 *   特点3：可用XML配置文件配置
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-13
 * @version     v1.0
 */
public class LDAP
{
    
    public static final String $ControlOID  = "1.2.840.113556.1.4.805";
    
    /** Entry.get(...) 方法的参数是不区分大小写的，但为了屏蔽歧义和方便固定义此常量 */
    public static final String $ObjectClass = "objectClass";
    
    
    
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
    public void closeConnection(LdapConnection i_Conn)
    {
        if ( i_Conn != null )
        {
            try
            {
                this.connPool.releaseConnection(i_Conn);
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
     * 判定LDAP操作是否成功
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_Response
     * @return
     */
    public static boolean isSuccess(ResultResponse i_Response)
    {
        if ( i_Response != null && ResultCodeEnum.SUCCESS.equals(i_Response.getLdapResult().getResultCode()) )
        {
            return true;
        }
        else
        {
            System.out.println(i_Response);
            return false;
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
        LdapEntry v_LdapEntry = ((Map<String ,LdapEntry>)XJava.getObject(LdapAnnotation.$LdapEntryClassIDs)).get(i_ObjectClassesID);
        
        if ( v_LdapEntry == null )
        {
            System.err.println("LDAP.getLdapEntry('" + i_ObjectClassesID + "') is not find Java Object(LdapEntry).");
        }
        
        return v_LdapEntry;
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
        
        Attribute       v_Attribute     = i_Entry.get(LDAP.$ObjectClass);
        Iterator<Value> v_Iter          = v_Attribute.iterator();
        List<String>    v_ObjectClasses = new ArrayList<String>();
        
        while (v_Iter.hasNext())
        {
            Value v_Value = v_Iter.next();
            
            // 1.0.0版本中用的是v_Value.getString()
            v_ObjectClasses.add(v_Value.getValue());
        }
        
        Help.toSort(v_ObjectClasses);
        
        String v_ObjectClassesID = StringHelp.toString(v_ObjectClasses ,"" ,",");
        if ( Help.isNull(v_ObjectClassesID) )
        {
            return null;
        }
        else
        {
            return getLdapEntry(v_ObjectClassesID);
        }
    }
    
    
    
    /**
     * 查询条目。只返回条目本身。即只返回一条记录(树目录的一个节点)。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_Values
     * @return
     */
    public Object queryEntry(Object i_Values)
    {
        List<?> v_Ret = this.queryEntrys(i_Values ,SearchScope.OBJECT);
        
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
        List<?> v_Ret = this.queryEntrys(i_DN ,SearchScope.OBJECT);
        
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
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_Values
     * @return
     */
    public List<?> queryEntryChilds(Object i_Values)
    {
        return this.queryEntrys(i_Values ,SearchScope.ONELEVEL);
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
    public List<?> queryEntryChilds(String i_DN)
    {
        return this.queryEntrys(i_DN ,SearchScope.ONELEVEL);
    }
    
    
    
    /**
     * 查询所有子条目。返回直接或间接隶属于i_DN的子条目及子子条目。
     * 
     * 即将i_DN下面的树结构上的所有条目都返回。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_Values
     * @return
     */
    public List<?> queryEntryTrees(Object i_Values)
    {
        return this.queryEntrys(i_Values ,SearchScope.SUBTREE);
    }
    
    
    
    /**
     * 查询所有子条目。返回直接或间接隶属于i_DN的子条目及子子条目。
     * 
     * 即将i_DN下面的树结构上的所有条目都返回。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN           条目标识
     * @return
     */
    public List<?> queryEntryTrees(String i_DN)
    {
        return this.queryEntrys(i_DN ,SearchScope.SUBTREE);
    }
    
    
    
    /**
     * 查询条目。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_DN           条目标识
     * @param i_SearchScope  查询范围
     *                       搜索范围01：SearchScope.OBJECT    返回输入给定DN，如果它存在的话。
     *                       搜索范围02：SearchScope.ONELEVEL  返回低于目前DN的所有子元素，不包括当前DN，也不包括与当前DN无直接关系的DN，即树目录深度为1。
     *                       搜索范围03：SearchScope.SUBTREE   返回所有元素从给出的DN，包括与DN相关的元素，无论树的深度。
     * @return
     */
    private List<?> queryEntrys(Object i_Values ,SearchScope i_SearchScope)
    {
        LdapEntry v_LdapEntry = getLdapEntry(i_Values.getClass());
        
        if ( v_LdapEntry == null )
        {
            return new ArrayList<Object>();
        }
        
        try
        {
            return this.queryEntrys(v_LdapEntry.getDNValue(i_Values) ,i_SearchScope);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return new ArrayList<Object>();
    }
    
    
    
    /**
     * 查询条目。
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
    private List<?> queryEntrys(String i_DN ,SearchScope i_SearchScope)
    {
        LdapConnection v_Conn   = null;
        EntryCursor    v_Cursor = null;
        List<Object>   v_Ret    = new ArrayList<Object>();
        
        try
        {
            v_Conn   = this.getConnection();
            v_Cursor = v_Conn.search(new Dn(i_DN) ,"(" + LDAP.$ObjectClass + "=*)" ,i_SearchScope);
            
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
            this.closeConnection(v_Conn);
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 批量添加条目。i_ValuesMap集合中的每个元素可以是不同类型的，对应不同类型的LDAP类。
     * 
     * 注1：有顺序的添加。这样可以实现先添加父条目，再添加子条目的功能。
     * 注2：没有事务机构，这不是LDAP的长项。不要指望LDAP可以作到。
     *      某个元素执行异常后，前面的不回滚，其后的不再执行添加。
     * 注3：批量添加时只占用一个连接。
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_ValuesMap
     * @return
     */
    public boolean addEntrys(List<?> i_ValuesMap)
    {
        if ( Help.isNull(i_ValuesMap) )
        {
            return false;
        }
        
        boolean     v_Ret    = false;
        List<Entry> v_Entrys = new ArrayList<Entry>();
        
        try
        {
            for (Object v_Values : i_ValuesMap)
            {
                if ( v_Values == null )
                {
                    continue;
                }
                
                LdapEntry v_LdapEntry = getLdapEntry(v_Values.getClass());
                if ( v_LdapEntry == null )
                {
                    return v_Ret;
                }
                
                Entry v_Entry = v_LdapEntry.toEntry(v_Values);
                if ( v_Entry == null )
                {
                    return v_Ret;
                }
                v_Entrys.add(v_Entry);
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
            return v_Ret;
        }
        
        
        LdapConnection v_Conn = null;
        
        try
        {
            v_Ret  = true;
            v_Conn = this.getConnection();
            
            for (Entry v_Entry : v_Entrys)
            {
                AddRequest  v_AddRequest = new AddRequestImpl();
                AddResponse v_Response   = null;
                
                v_AddRequest.setEntry(  v_Entry);
                v_AddRequest.addControl(new ManageDsaITImpl());
                
                v_Response = v_Conn.add(v_AddRequest);
                
                if ( !LDAP.isSuccess(v_Response) )
                {
                    v_Ret = false;
                    break;
                }
            }
        }
        catch (Exception exce)
        {
            v_Ret = false;
            exce.printStackTrace();
        }
        finally
        {
            this.closeConnection(v_Conn);
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
     * @param i_Values
     * @return
     */
    public boolean addEntry(Object i_Values)
    {
        LdapEntry v_LdapEntry = getLdapEntry(i_Values.getClass());
        
        if ( v_LdapEntry == null )
        {
            return false;
        }
        
        Entry v_Entry = null;
        
        try
        {
            v_Entry = v_LdapEntry.toEntry(i_Values);
            
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
            v_AddRequest.setEntry(  i_Entry);
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
            this.closeConnection(v_Conn);
        }
        
        return LDAP.isSuccess(v_Response);
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
            this.closeConnection(v_Conn);
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 批量删除条目。i_DNs集合中的每个元素可对应不同类型的LDAP类。
     * 
     * 注1：有顺序的删除。这样可以实现先删除子条目，再添加父条目的功能。
     * 注2：没有事务机构，这不是LDAP的长项。不要指望LDAP可以作到。
     *      某个元素执行异常后，前面的不回滚，其后的不再执行删除。
     * 注3：批量删除时只占用一个连接。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DNs
     * @return
     */
    public boolean delEntrys(List<String> i_DNs)
    {
        boolean        v_Ret  = false;
        LdapConnection v_Conn = null;
        
        if ( Help.isNull(i_DNs) )
        {
            return v_Ret;
        }
        
        try
        {
            v_Ret = true;
            v_Conn = this.getConnection();
            
            for (String v_DN : i_DNs)
            {
                if ( Help.isNull(v_DN) )
                {
                    continue;
                }
                
                DeleteRequest  v_Request  = new DeleteRequestImpl();
                DeleteResponse v_Response = null;
                
                v_Request.setName(new Dn(v_DN));
                
                v_Response = v_Conn.delete(v_Request);
                
                if ( !LDAP.isSuccess(v_Response) )
                {
                    v_Ret = false;
                    break;
                }
            }
        }
        catch (Exception exce)
        {
            v_Ret = false;
            exce.printStackTrace();
        }
        finally
        {
            this.closeConnection(v_Conn);
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
            this.closeConnection(v_Conn);
        }
        
        return LDAP.isSuccess(v_Response);
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
        
        return LDAP.isSuccess(v_Response);
    }
    
    
    
    /**
     * 对条目的添加多个属性。
     * 
     *   1. 自动识别要添加的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_NewValues
     * @return
     */
    public boolean addAttributes(Object i_NewValues)
    {
        return modifyEntry(i_NewValues ,true ,false ,false);
    }
    
    
    
    /**
     * 条目添加属性
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN              DN标识
     * @param i_AttributeName   属性名称
     * @param i_AttributeValue  属性值(可为多个)
     * @return
     */
    public boolean addAttribute(String i_DN ,String i_AttributeName ,String ... i_AttributeValue)
    {
        return this.modifyEntry(ModificationOperation.ADD_ATTRIBUTE ,i_DN ,i_AttributeName ,i_AttributeValue);
    }
    
    
    
    /**
     * 修改条目的多个属性值。
     * 
     *   1. 自动识别要修改的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_NewValues
     * @return
     */
    public boolean modifyAttributes(Object i_NewValues)
    {
        return modifyEntry(i_NewValues ,false ,true ,false);
    }
    
    
    
    /**
     * 修改条目的属性值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN              DN标识
     * @param i_AttributeName   属性名称
     * @param i_AttributeValue  属性值(可为多个)
     * @return
     */
    public boolean modifyAttribute(String i_DN ,String i_AttributeName ,String ... i_AttributeValue)
    {
        return this.modifyEntry(ModificationOperation.REPLACE_ATTRIBUTE ,i_DN ,i_AttributeName ,i_AttributeValue);
    }
    
    
    
    /**
     * 删除条目的多个属性。
     * 
     *   1. 自动识别要删除的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_NewValues
     * @return
     */
    public boolean delAttributes(Object i_NewValues)
    {
        return modifyEntry(i_NewValues ,false ,false ,true);
    }
    
    
    
    /**
     * 删除条目的属性
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_DN              DN标识
     * @param i_AttributeName   属性名称
     * @return
     */
    public boolean delAttribute(String i_DN ,String i_AttributeName)
    {
        return this.modifyEntry(ModificationOperation.REMOVE_ATTRIBUTE ,i_DN ,i_AttributeName ,"");
    }
    
    
    
    /**
     * 修改条目的多个属性。
     * 
     *   1. 自动识别要添加的多个属性
     *   2. 自动识别要修改的多个属性
     *   3. 自动识别要删除的多个属性
     * 
     * 只用于用 @Ldap 注解的Java对象。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_NewValues
     * @param i_IsAdd      当LDAP中没有时，是否新增LDAP属性
     * @param i_IsUpdate   当新旧不同时，是否修改LDAP中对应的属性
     * @param i_IsDel      当Java属性值为null时，是否删除LDAP中对应的属性
     * @return
     */
    public boolean modifyEntry(Object i_NewValues ,boolean i_IsAdd ,boolean i_IsUpdate ,boolean i_IsDel)
    {
        LdapEntry v_LdapEntry = getLdapEntry(i_NewValues.getClass());
        
        if ( v_LdapEntry == null )
        {
            return false;
        }
        
        LdapConnection v_Conn     = null;
        ModifyRequest  v_Request  = null;
        ModifyResponse v_Response = null;
        
        try
        {
            v_Request = v_LdapEntry.toModify(this.queryEntry(v_LdapEntry.getDNValue(i_NewValues)) ,i_NewValues ,i_IsAdd ,i_IsUpdate ,i_IsDel);
            if ( v_Request == null )
            {
                return false;
            }
            
            v_Conn     = this.getConnection();
            v_Response = v_Conn.modify(v_Request);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            this.closeConnection(v_Conn);
        }
        
        return LDAP.isSuccess(v_Response);
    }

    
    
    /**
     * 修改条目(准确是修改条目的属性)。可包括如下操作。
     *   1. ModificationOperation.ADD_ATTRIBUTE：    添加属性  
     *   2. ModificationOperation.REMOVE_ATTRIBUTE： 删除属性
     *   3. ModificationOperation.REPLACE_ATTRIBUTE：替换属性值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-16
     * @version     v1.0
     *
     * @param i_Operation       操作类型
     * @param i_DN              DN标识
     * @param i_AttributeName   属性名称
     * @param i_AttributeValue  属性值(可为多个)
     * @return
     */
    private boolean modifyEntry(ModificationOperation i_Operation ,String i_DN ,String i_AttributeName ,String ... i_AttributeValue)
    {
        LdapConnection v_Conn     = null;
        ModifyRequest  v_Request  = new ModifyRequestImpl();
        ModifyResponse v_Response = null;
        
        try
        {
            v_Request.setName(new Dn(i_DN));
            v_Request.addModification(new DefaultModification(i_Operation ,i_AttributeName ,i_AttributeValue));
            
            v_Conn = this.getConnection();
            v_Response = v_Conn.modify(v_Request);
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally
        {
            this.closeConnection(v_Conn);
        }
        
        return LDAP.isSuccess(v_Response);
    }
    
}
