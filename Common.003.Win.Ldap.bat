

cd .\bin


rd /s/q .\org\hy\common\ldap\junit


jar cvfm hy.common.ldap.jar MANIFEST.MF META-INF org

copy hy.common.ldap.jar ..
del /q hy.common.ldap.jar
cd ..





cd .\src
jar cvfm hy.common.ldap-sources.jar MANIFEST.MF META-INF org 
copy hy.common.ldap-sources.jar ..
del /q hy.common.ldap-sources.jar
cd ..
