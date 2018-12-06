package org.hy.common.ldap.annotation;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.Return;
import org.hy.common.StringHelp;
import org.hy.common.ldap.LDAP;





/**
 * LDAP条目配置翻译官。
 * 
 *  1. 将Java值对象翻译为LDAP条目
 *     1.1. Java类中的 @Ldap 注解将被翻译为本类。
 *     1.2. 再通过本类的 toEntry(...) 方法即可翻译成为 Apache LDAP API 中的条目对象。
 *     
 *  2. 将LDAP条目翻译为Java值对象的实例
 *     2.1. Java类中的 @Ldap 注解将被翻译为本类。
 *     2.2. 再通过本类的 toObject(...) 方法即可翻译 Apache LDAP API 中的条目对象为Java值对象。
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-14
 * @version     v1.0
 *              v2.0  2018-12-06  添加：支持同一属性多个属性值的LDAP特性。
 *                                     Java对象用List<Object>或Set<Object>或数组Object[]定义成员变量的类型，来支持多属性值的LDAP特性。
 *                                     当Java成员变量为String这样的简单时，LDAP中同一属性有多个属性值时，随机取一个给Java成员变量赋值。
 *                                     
 *                                     LDAP中的属性类型一般都是字符，而此类可以翻译为"条目配置翻译官"类指定的成员类型。
 */
public class LdapEntry
{
    
    /** Java值对象类的元类型。即有 @Ldap 注解的类 */
    private Class<?>          metaClass;
    
    /** 
     * LDAP中的"对象类ObjectClass"的名称组合成的ID。内部有自动排序过。区分大小写。
     * 
     * 格式为："对象名称1,对象名称2,...对象名称n"。如，"top,person"。 
     */
    private String            objectClassesID;

    /** 
     * LDAP中的"对象类ObjectClass"的名称。内部有自动排序过。
     * 
     * 格式为：List.get(index) = "对象名称" 。如，top、person等。
     */
    private List<String>       objectClasses;
    
    /** 获取DN值的getter方法 */
    private Method             dnGetMethod;
    
    /** 设置DN值的setter方法 */
    private Method             dnSetMethod;
    
    /**
     * LDAP中的"属性Attribute"的名称 与对应的 Java对象的getter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     */
    private Map<String ,Method>  elementsToLDAP;
    
    /**
     * LDAP中的"属性Attribute"的名称 与对应的 Java对象的setter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的setter()方法
     */
    private Map<String ,Method>  elementsToObject;

    
    
    public LdapEntry()
    {
        this.objectClasses    = new ArrayList<String>();
        this.elementsToLDAP   = new LinkedHashMap<String ,Method>();
        this.elementsToObject = new LinkedHashMap<String ,Method>();
    }
    
    
    
    /**
     * 有部分解释动作的构造器
     *
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_MetaClass      Java值对象类的元类型。即有 @Ldap 注解的类
     * @param i_ObjectClasses  LDAP中的"对象类ObjectClass"的名称。多个有逗号分隔。内部有自动排序过。
     */
    public LdapEntry(Class<?> i_MetaClass ,String i_ObjectClasses)
    {
        this();
        
        this.metaClass       = i_MetaClass;
        this.objectClassesID = StringHelp.replaceAll(i_ObjectClasses ,new String[]{" " ,"\r" ,"\n" ,"\t"} ,new String[]{""});
        String [] v_ObjectClasses = this.objectClassesID.split(",");
        
        for (int i=0; i<v_ObjectClasses.length; i++)
        {
            this.objectClasses.add(v_ObjectClasses[i]);
        }
        
        // 重新排序，生成对象ID
        this.objectClassesID = StringHelp.toString(Help.toSort(this.objectClasses) ,"" ,",");
    }
    
    
    
    /**
     * 获取DN值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_Values
     * @return
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     */
    public String getDNValue(Object i_Values) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if ( this.dnGetMethod != null )
        {
            Object v_Value = this.dnGetMethod.invoke(i_Values);
            if ( v_Value != null )
            {
                return v_Value.toString();
            }
        }
        
        return null;
    }
    
    
    
    /**
     * 将Java值对象翻译为LDAP条目
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *              v2.0  2018-12-06  添加：支持同一属性多个属性值的LDAP特性。
     *                                     Java对象用List<Object>或Set<Object>或数组Object[]定义成员变量的类型，来支持多属性值的LDAP特性。
     *                                     当Java成员变量为String这样的简单类型时，LDAP中同一属性有多个属性值时，随机取一个给Java成员变量赋值。
     *
     * @param i_Values  Java值对象
     * @return
     * @throws LdapException
     */
    public Entry toEntry(Object i_Values) throws LdapException
    {
        DefaultEntry v_Entry = new DefaultEntry();
        String       v_DN    = null;
        
        // 设置LDAP的ObjectClass
        for (int v_Index=0; v_Index<this.objectClasses.size(); v_Index++)
        {
            v_Entry.add(LDAP.$ObjectClass ,this.objectClasses.get(v_Index));
        }
        
        // 设置LDAP的DN
        try
        {
            v_DN = this.getDNValue(i_Values);
            if ( !Help.isNull(v_DN) )
            {
                v_Entry.setDn(v_DN);
            }
        }
        catch (Exception exce)
        {
            System.out.println(Date.getNowTime().getFull() + " LDAP DN get method(" + this.dnGetMethod.getName() + ") value is error.");
            exce.printStackTrace();
            // 不返回，允许DN未设置的情况出现
        }
        
        // 设置LDAP的属性
        for (Map.Entry<String ,Method> v_Item : this.elementsToLDAP.entrySet())
        {
            try
            {
                Object v_Value = v_Item.getValue().invoke(i_Values);
                if ( v_Value != null )
                {
                    if ( v_Value instanceof List )
                    {
                        List<?>  v_VList = (List<?>)v_Value;
                        if ( !Help.isNull(v_VList) )
                        {
                            int       v_Size     = v_VList.size();
                            String [] v_ValueArr = new String[v_Size];
                            
                            for (int i=0; i<v_Size; i++)
                            {
                                Object v_VLItem = v_VList.get(i);
                                if ( v_VLItem == null )
                                {
                                    v_ValueArr[i] = "";
                                }
                                else
                                {
                                    v_ValueArr[i] = v_VLItem.toString();
                                }
                            }
                        
                            v_Entry.add(v_Item.getKey() ,v_ValueArr);
                        }
                    }
                    else if ( v_Value instanceof Set )
                    {
                        Set<?> v_VSet = (Set<?>)v_Value; 
                        if ( !Help.isNull(v_VSet) )
                        {
                            int         v_Size     = v_VSet.size();
                            String []   v_ValueArr = new String[v_Size];
                            Iterator<?> v_Iter     = v_VSet.iterator();
                            
                            for (int i=0; i<v_Size && v_Iter.hasNext(); i++)
                            {
                                Object v_VLItem = v_Iter.next();
                                if ( v_VLItem == null )
                                {
                                    v_ValueArr[i] = "";
                                }
                                else
                                {
                                    v_ValueArr[i] = v_VLItem.toString();
                                }
                            }
                            
                            v_Entry.add(v_Item.getKey() ,v_ValueArr);
                        }
                    }
                    else if ( v_Value instanceof Object [] )
                    {
                        Object [] v_VArray = (Object [])v_Value;
                        if ( !Help.isNull(v_VArray) )
                        {
                            int       v_Size     = v_VArray.length;
                            String [] v_ValueArr = new String[v_Size];
                            
                            for (int i=0; i<v_Size; i++)
                            {
                                Object v_VLItem = v_VArray[i];
                                if ( v_VLItem == null )
                                {
                                    v_ValueArr[i] = "";
                                }
                                else
                                {
                                    v_ValueArr[i] = v_VLItem.toString();
                                }
                            }
                            
                            v_Entry.add(v_Item.getKey() ,v_ValueArr);
                        }
                    }
                    else
                    {
                        if ( !Help.isNull(v_Value.toString()) )
                        {
                            v_Entry.add(v_Item.getKey() ,v_Value.toString());
                        }
                    }
                }
            }
            catch (Exception exce)
            {
                System.out.println(Date.getNowTime().getFull() + " LDAP Attribute name(" + v_Item.getKey() + ") get method(" + v_Item.getValue().getName() + ") value is error.");
                exce.printStackTrace();
            }
        }
        
        return v_Entry;
    }
    
    
    
    /**
     * 将LDAP条目翻译为Java值对象的实例
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *              v2.0  2018-12-06  添加：支持同一属性多个属性值的LDAP特性。
     *                                     Java对象用List<Object>或Set<Object>或数组Object[]定义成员变量的类型，来支持多属性值的LDAP特性。
     *                                     当Java成员变量为String这样的简单时，LDAP中同一属性有多个属性值时，随机取一个给Java成员变量赋值。
     *
     * @param i_Entry   条目对象
     * @return
     */
    public Object toObject(Entry i_Entry)
    {
        Object v_Ret = this.newObject();
        
        if ( v_Ret == null )
        {
            return v_Ret;
        }
        
        // 设置Java的DN
        if ( this.dnSetMethod != null )
        {
            try
            {
                Object v_MethodParam = Help.toObject(this.dnSetMethod.getParameterTypes()[0] ,i_Entry.getDn().toString());
                this.dnSetMethod.invoke(v_Ret ,v_MethodParam);
            }
            catch (Exception exce)
            {
                System.out.println(Date.getNowTime().getFull() + " LDAP DN set method(" + this.dnSetMethod.getName() + ") value is error.");
                exce.printStackTrace();
            }
        }
        
        // 设置Java的属性
        for (Map.Entry<String ,Method> v_Item : this.elementsToObject.entrySet())
        {
            try
            {
                Attribute v_Attribute = i_Entry.get(v_Item.getKey());
                if ( v_Attribute != null )
                {
                    Object v_MPValue = null;
                    
                    if ( MethodReflect.isExtendImplement(v_Item.getValue().getParameterTypes()[0] ,List.class) )
                    {
                        Class<?>        v_ListItemClass = MethodReflect.getGenerics(v_Item.getValue() ,0 ,0);
                        List<Object>    v_Values        = new ArrayList<Object>();
                        Iterator<Value> v_Iter          = v_Attribute.iterator();
                        
                        v_ListItemClass = Help.NVL(v_ListItemClass ,String.class);
                        while (v_Iter.hasNext())
                        {
                            Value v_Value = v_Iter.next();
                            
                            v_Values.add(Help.toObject(v_ListItemClass ,v_Value.getValue()));
                        }
                        
                        v_MPValue = v_Values;
                    }
                    else if ( MethodReflect.isExtendImplement(v_Item.getValue().getParameterTypes()[0] ,Set.class) )
                    {
                        Class<?>        v_SetItemClass = MethodReflect.getGenerics(v_Item.getValue() ,0 ,0);
                        Set<Object>     v_Values       = new HashSet<Object>();
                        Iterator<Value> v_Iter         = v_Attribute.iterator();
                        
                        v_SetItemClass = Help.NVL(v_SetItemClass ,String.class);
                        while (v_Iter.hasNext())
                        {
                            Value v_Value = v_Iter.next();
                            
                            v_Values.add(Help.toObject(v_SetItemClass ,v_Value.getValue()));
                        }
                        
                        v_MPValue = v_Values;
                    }
                    else if ( v_Item.getValue().getParameterTypes()[0].isArray() )
                    {
                        Class<?>        v_ArrayItemClass = v_Item.getValue().getParameterTypes()[0].getComponentType();
                        Iterator<Value> v_Iter           = v_Attribute.iterator();
                        int             v_Index          = 0;
                        
                        v_ArrayItemClass = Help.NVL(v_ArrayItemClass ,String.class);
                        v_MPValue = Array.newInstance(v_ArrayItemClass ,v_Attribute.size());
                        
                        while (v_Iter.hasNext())
                        {
                            Value v_Value = v_Iter.next();
                            
                            Array.set(v_MPValue ,v_Index++ ,Help.toObject(v_ArrayItemClass ,v_Value.getValue()));
                        }
                    }
                    else
                    {
                        Value v_Value = v_Attribute.get();
                        if ( v_Value != null )
                        {
                            // 1.0.0版本中用的v_Value.getString()
                            v_MPValue = Help.toObject(v_Item.getValue().getParameterTypes()[0] ,v_Value.getValue());
                        }
                        else
                        {
                            v_MPValue = "";
                        }
                    }
                    
                    v_Item.getValue().invoke(v_Ret ,v_MPValue);
                }
            }
            catch (Exception exce)
            {
                System.out.println(Date.getNowTime().getFull() + " LDAP Attribute name(" + v_Item.getKey() + ") set method(" + v_Item.getValue().getName() + ") value is error.");
                exce.printStackTrace();
            }
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 用于修改条目属性前，先查询LDAP服务中的条目。再与Java新值对比后得出要执行修改动作。
     * 
     * 可自动对比出，哪些是要添加的属性；
     * 可自动对比出，哪些是要修改的属性；
     * 可自动对比出，哪些是要删除的属性；
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_OldEntry   LDAP服务中的旧值
     * @param i_NewValues  Java对象中的新值
     * @param i_IsAdd      当LDAP中没有时，是否新增LDAP属性
     * @param i_IsUpdate   当新旧不同时，是否修改LDAP中对应的属性
     * @param i_IsDel      当Java属性值为null时，是否删除LDAP中对应的属性
     * @return
     */
    public ModifyRequest toModify(Entry i_OldEntry ,Object i_NewValues 
                                 ,boolean i_IsAdd
                                 ,boolean i_IsUpdate
                                 ,boolean i_IsDel)
    {
        ModifyRequest v_Request = new ModifyRequestImpl();
        String        v_DN      = null;
        int           v_MCount  = 0;
        
        // 设置LDAP的DN
        try
        {
            v_DN = this.getDNValue(i_NewValues);
            if ( !Help.isNull(v_DN) )
            {
                v_Request.setName(new Dn(v_DN));
            }
        }
        catch (Exception exce)
        {
            System.out.println(Date.getNowTime().getFull() + " LDAP DN get method(" + this.dnGetMethod.getName() + ") value is error.");
            exce.printStackTrace();
            // 不返回，允许DN未设置的情况出现
        }
        
        // 设置LDAP的属性
        for (Map.Entry<String ,Method> v_Item : this.elementsToLDAP.entrySet())
        {
            try
            {
                Object v_NewValue = v_Item.getValue().invoke(i_NewValues);
                if ( v_NewValue != null )
                {
                    if ( i_IsAdd || i_IsUpdate )
                    {
                        Attribute v_Attribute = i_OldEntry.get(v_Item.getKey());
                        if ( v_Attribute != null )
                        {
                            if ( i_IsUpdate )
                            {
                                Value v_OldValue = v_Attribute.get();
                                if ( v_OldValue != null )
                                {
                                    // 1.0.0 用的是 v_OldValue.getString();
                                    if ( !v_NewValue.toString().equals(v_OldValue.getValue()) )
                                    {
                                        // 修改属性值
                                        v_Request.addModification(new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE ,v_Item.getKey() ,v_NewValue.toString()));
                                        v_MCount++;
                                    }
                                }
                                else
                                {
                                    // 修改属性值
                                    v_Request.addModification(new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE ,v_Item.getKey() ,v_NewValue.toString()));
                                    v_MCount++;
                                }
                            }
                        }
                        else if ( i_IsAdd )
                        {
                            // 添加属性
                            v_Request.addModification(new DefaultModification(ModificationOperation.ADD_ATTRIBUTE ,v_Item.getKey() ,v_NewValue.toString()));
                            v_MCount++;
                        }
                    }
                }
                // 当Java属性值为null时，删除LDAP中对应的属性
                else if ( i_IsDel )
                {
                    Attribute v_Attribute = i_OldEntry.get(v_Item.getKey());
                    if ( v_Attribute != null )
                    {
                        // 删除属性
                        v_Request.addModification(new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE ,v_Item.getKey()));
                        v_MCount++;
                    }
                }
            }
            catch (Exception exce)
            {
                System.out.println(Date.getNowTime().getFull() + " LDAP Attribute name(" + v_Item.getKey() + ") get method(" + v_Item.getValue().getName() + ") value is error.");
                exce.printStackTrace();
            }
        }
        
        return v_MCount >= 1 ? v_Request : null;
    }
    
    
    
    /**
     * 用于修改条目属性前，先查询LDAP服务中的条目。再与Java新值对比后得出要执行修改动作。
     * 
     * 可自动对比出，哪些是要添加的属性；
     * 可自动对比出，哪些是要修改的属性；
     * 可自动对比出，哪些是要删除的属性；
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-17
     * @version     v1.0
     *
     * @param i_OldValues  LDAP服务中的旧值
     * @param i_NewValues  Java对象中的新值
     * @param i_IsAdd      当LDAP中没有时，是否新增LDAP属性
     * @param i_IsUpdate   当新旧不同时，是否修改LDAP中对应的属性
     * @param i_IsDel      当Java属性值为null时，是否删除LDAP中对应的属性
     * @return
     */
    public Return<ModifyRequest> toModify(Object i_OldValues ,Object i_NewValues 
                                 ,boolean i_IsAdd
                                 ,boolean i_IsUpdate
                                 ,boolean i_IsDel)
    {
        Return<ModifyRequest> v_Ret = new Return<ModifyRequest>();
        v_Ret.set(true).setParamInt(0);
        
        if ( i_OldValues == null )
        {
            return v_Ret;
        }
        
        ModifyRequest v_Request = new ModifyRequestImpl();
        String        v_DN      = null;
        int           v_MCount  = 0;
        
        v_Request.getResultResponse().getLdapResult().setResultCode(ResultCodeEnum.SUCCESS);
        
        // 设置LDAP的DN
        try
        {
            v_DN = this.getDNValue(i_NewValues);
            if ( !Help.isNull(v_DN) )
            {
                v_Request.setName(new Dn(v_DN));
            }
        }
        catch (Exception exce)
        {
            System.out.println(Date.getNowTime().getFull() + " LDAP DN get method(" + this.dnGetMethod.getName() + ") value is error.");
            exce.printStackTrace();
            // 不返回，允许DN未设置的情况出现
        }
        
        // 设置LDAP的属性
        for (Map.Entry<String ,Method> v_Item : this.elementsToLDAP.entrySet())
        {
            try
            {
                Object v_NewValue = v_Item.getValue().invoke(i_NewValues);
                if ( v_NewValue != null )
                {
                    if ( i_IsAdd || i_IsUpdate )
                    {
                        Object v_OldValue = v_Item.getValue().invoke(i_OldValues);
                        if ( v_OldValue != null )
                        {
                            if ( i_IsUpdate && !v_NewValue.equals(v_OldValue) )
                            {
                                // 修改属性值
                                v_Request.addModification(new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE ,v_Item.getKey() ,v_NewValue.toString()));
                                v_MCount++;
                            }
                        }
                        else if ( i_IsAdd )
                        {
                            // 添加属性
                            v_Request.addModification(new DefaultModification(ModificationOperation.ADD_ATTRIBUTE ,v_Item.getKey() ,v_NewValue.toString()));
                            v_MCount++;
                        }
                    }
                }
                // 当Java属性值为null时，删除LDAP中对应的属性
                else if ( i_IsDel )
                {
                    Object v_OldValue = v_Item.getValue().invoke(i_OldValues);
                    if ( v_OldValue != null )
                    {
                        // 删除属性
                        v_Request.addModification(new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE ,v_Item.getKey()));
                        v_MCount++;
                    }
                }
            }
            catch (Exception exce)
            {
                System.out.println(Date.getNowTime().getFull() + " LDAP Attribute name(" + v_Item.getKey() + ") get method(" + v_Item.getValue().getName() + ") value is error.");
                exce.printStackTrace();
            }
        }
        
        v_Ret.setParamObj(v_Request);
        v_Ret.setParamInt(v_MCount);
        v_Ret.set(v_MCount >= 1);
        
        return v_Ret;
    }
    
    
    
    /**
     * 添加元素
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Name
     * @param i_GetMethod
     * @param i_SetMethod
     */
    public void putElement(String i_Name ,Method i_GetMethod ,Method i_SetMethod)
    {
        if ( i_GetMethod != null )
        {
            this.elementsToLDAP  .put(i_Name ,i_GetMethod);
        }
        
        if ( i_SetMethod != null )
        {
            this.elementsToObject.put(i_Name ,i_SetMethod);
        }
    }
    
    
    
    /**
     * 构造Java值对象类
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-15
     * @version     v1.0
     *
     * @return
     */
    private Object newObject()
    {
        try
        {
            return this.metaClass.newInstance();
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        
        return null;
    }


    
    /**
     * 获取：Java值对象类的元类型。即有 @Ldap 注解的类
     */
    public Class<?> getMetaClass()
    {
        return metaClass;
    }


    
    /**
     * 设置：Java值对象类的元类型。即有 @Ldap 注解的类
     * 
     * @param metaClass 
     */
    public void setMetaClass(Class<?> metaClass)
    {
        this.metaClass = metaClass;
    }


    
    /**
     * 获取：LDAP中的"对象类ObjectClass"的名称组合成的ID。
     * 
     * 格式为："对象名称1,对象名称2,...对象名称n"。如，"top,person"。
     */
    public String getObjectClassesID()
    {
        return objectClassesID;
    }



    /**
     * 设置：LDAP中的"对象类ObjectClass"的名称组合成的ID。
     * 
     * 格式为："对象名称1,对象名称2,...对象名称n"。如，"top,person"。
     * 
     * @param objectClassesID 
     */
    public void setObjectClassesID(String objectClassesID)
    {
        this.objectClassesID = objectClassesID;
    }



    /**
     * 获取：LDAP中的"对象类ObjectClass"的名称。
     * 
     * 格式为：List.get(index) = "对象名称" 。如，top、person等。
     */
    public List<String> getObjectClasses()
    {
        return objectClasses;
    }


    
    /**
     * 设置：LDAP中的"对象类ObjectClass"的名称。
     * 
     * 格式为：List.get(index) = "对象名称" 。如，top、person等。
     * 
     * @param objectClasses 
     */
    public void setObjectClasses(List<String> objectClasses)
    {
        this.objectClasses = objectClasses;
    }

    
    
    /**
     * 获取：获取DN值的getter方法
     */
    public Method getDnGetMethod()
    {
        return dnGetMethod;
    }


    
    /**
     * 设置：获取DN值的getter方法
     * 
     * @param dnGetMethod 
     */
    public void setDnGetMethod(Method dnGetMethod)
    {
        this.dnGetMethod = dnGetMethod;
    }


    
    /**
     * 获取：设置DN值的setter方法
     */
    public Method getDnSetMethod()
    {
        return dnSetMethod;
    }


    
    /**
     * 设置：设置DN值的setter方法
     * 
     * @param dnSetMethod 
     */
    public void setDnSetMethod(Method dnSetMethod)
    {
        this.dnSetMethod = dnSetMethod;
    }



    /**
     * 获取：LDAP中的"属性Attribute"的名称 与对应的 Java对象的getter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     */
    public Map<String ,Method> getElementsToLDAP()
    {
        return elementsToLDAP;
    }


    
    /**
     * 设置：LDAP中的"属性Attribute"的名称 与对应的 Java对象的getter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     * 
     * @param elementsToLDAP 
     */
    public void setElementsToLDAP(Map<String ,Method> elementsToLDAP)
    {
        this.elementsToLDAP = elementsToLDAP;
    }


    
    /**
     * 获取：LDAP中的"属性Attribute"的名称 与对应的 Java对象的setter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的setter()方法
     */
    public Map<String ,Method> getElementsToObject()
    {
        return elementsToObject;
    }


    
    /**
     * 设置：LDAP中的"属性Attribute"的名称 与对应的 Java对象的setter()方法
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的setter()方法
     * 
     * @param elementsToObject 
     */
    public void setElementsToObject(Map<String ,Method> elementsToObject)
    {
        this.elementsToObject = elementsToObject;
    }
    
}
