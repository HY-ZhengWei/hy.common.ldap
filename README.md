# hy.common.ldap


LDAP目录服务的操作类库。基于 Apache LDAP API。

可用于OpenLDAP目录服务的访问操作。


主导思想：通过 @Ldap 注解十分方便的实现Java写入、读取LDAP目录服务中的数据，同时不破坏、不改造Java程序原有的数据结构。

特点1：使用LDAP连接池操作LDAP目录服务。

特点2：内部自动获取连接，自动释放连接，无须外界干预。

特点3：可用XML配置文件配置