start mvn deploy:deploy-file -Dfile=hy.common.ldap.jar                              -DpomFile=./src/META-INF/maven/org/hy/common/ldap/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:1481/repository/thirdparty
start mvn deploy:deploy-file -Dfile=hy.common.ldap-sources.jar -Dclassifier=sources -DpomFile=./src/META-INF/maven/org/hy/common/ldap/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:1481/repository/thirdparty
