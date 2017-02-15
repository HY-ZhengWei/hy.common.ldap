package org.hy.common.ldap.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.hy.common.Date;
import org.hy.common.StringHelp;





/**
 * LDAP条目配置翻译官。
 * 
 *  1. Java类中的@Ldap注解将被翻译为本类。
 *  2. 再通过本类的 toEntry(...) 方法即可翻译成为 Apache LDAP API 中的条目对象。
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-02-14
 * @version     v1.0
 */
public class LdapEntry
{

    /** 
     * LDAP中的"对象类ObjectClass"的名称。
     * 
     * 格式为：List.get(index) = "对象名称" 。如，top、person等。
     */
    private List<String>       objectClasses;
    
    
    
    /**
     * LDAP中的"属性Attribute"的名称 与对应的 Java属性对象
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     */
    private Map<String ,Method>  elements;

    
    
    public LdapEntry()
    {
        this.objectClasses = new ArrayList<String>();
        this.elements      = new LinkedHashMap<String ,Method>();
    }
    
    
    
    /**
     * 有部分解释动作的构造器
     *
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_ObjectClasses  LDAP中的"对象类ObjectClass"的名称。多个有逗号分隔。有先后有顺序。
     */
    public LdapEntry(String i_ObjectClasses)
    {
        this();
        
        String [] v_ObjectClasses = StringHelp.replaceAll(i_ObjectClasses ,new String[]{" " ,"\r" ,"\t"} ,new String[]{""}).split(",");
        
        for (int i=0; i<v_ObjectClasses.length; i++)
        {
            this.objectClasses.add(v_ObjectClasses[i]);
        }
    }
    
    
    
    /**
     * 添加元素
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Name
     * @param i_Method
     */
    public void putElement(String i_Name ,Method i_Method)
    {
        this.elements.put(i_Name ,i_Method);
    }
    
    
    
    /**
     * 将本类转为Apache LDAP API 中的条目对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-02-14
     * @version     v1.0
     *
     * @param i_Values  Java值对象
     * @return
     * @throws LdapException
     */
    public Entry toEntry(Object i_Values) throws LdapException
    {
        DefaultEntry v_Entry = new DefaultEntry();
        
        for (int v_Index=0; v_Index<this.objectClasses.size(); v_Index++)
        {
            v_Entry.add("objectClass" ,this.objectClasses.get(v_Index));
        }
        
        for (Map.Entry<String ,Method> v_Item : this.elements.entrySet())
        {
            try
            {
                Object v_Value = v_Item.getValue().invoke(i_Values);
                if ( v_Value != null )
                {
                    v_Entry.add(v_Item.getKey() ,v_Value.toString());
                }
            }
            catch (Exception exce)
            {
                System.out.println(Date.getNowTime().getFull() + " LDAP Attribute name(" + v_Item.getKey() + ") call method(" + v_Item.getValue().getName() + ") is error.");
                exce.printStackTrace();
            }
        }
        
        return v_Entry;
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
     * 获取：LDAP中的"属性Attribute"的名称 与对应的 Java属性对象
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     */
    public Map<String ,Method> getElements()
    {
        return elements;
    }


    
    /**
     * 设置：LDAP中的"属性Attribute"的名称 与对应的 Java属性对象
     * 
     * Map.key   = "属性名称"。 如，o、ou等。
     * Map.value = Java属性对象的getter()方法
     * 
     * @param elements 
     */
    public void setElements(Map<String ,Method> elements)
    {
        this.elements = elements;
    }
    
}
