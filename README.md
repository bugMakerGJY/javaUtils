javaUtils 
====  
创建测试数据工具--CreateTestDataUtil
-------  
>功能  
>>根据Bean向表中插入测试数据  
		>示例  
```Java
//设置数据库连接
CreateTestDataUtil.setConnection("jdbc:mysql://xx.xx.xx.xxx:3306/xxx?useUnicode=true&characterEncoding=utf-8&useSSL=false","root", "password");
//根据Bean向表中插入30条测试数据
CreateTestDataUtil.createTestData(Bean.class,1,30);
```
