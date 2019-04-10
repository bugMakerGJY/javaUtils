package utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateTestDataUtil {

    private static Connection c;

    /**
     * @Description: 加载JDBC驱动
     * @author: gjy
     * @Date: 2019/04/10 15:29
     */
    private static boolean loadJDBC() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @Description: 设置数据库连接信息
     * @author: gjy
     * @Date: 2019/04/10 15:29
     */
    public static void setConnection(String url, String user, String password) {
        try {
            loadJDBC();
            c = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description: 首字母转小写  用途--将大写的类名首字母转成驼峰
     * @author: gjy
     * @Date: 2019/04/10 15:29
     */
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    /**
     * @Description: 驼峰转下划线
     * @author: gjy
     * @Date: 2019/04/10 15:29
     */
    public static String HumpToUnderline(String para) {
        StringBuilder sb = new StringBuilder(para);
        int temp = 0;//定位
        if (!para.contains("_")) {
            for (int i = 0; i < para.length(); i++) {
                if (Character.isUpperCase(para.charAt(i))) {
                    sb.insert(i + temp, "_");
                    temp += 1;
                }
            }
        }
        return sb.toString().toLowerCase();
    }

    /**
     * @Description: 获取sql中特定占位符的个数
     * @author: gjy
     * @Date: 2019/04/10 15:29
     */
    public static int count(String sql, String key) {
        int fromIndex = 0;
        int c = 0;
        while (true) {
            int index = sql.indexOf(key, fromIndex);
            if (-1 != index) {
                fromIndex = index + 1;
                c++;
            } else {
                break;
            }
        }
        return c;
    }

    /**
     * @param clazz 表示测试数据对应的实体类
     * @param start 表示测试数据后标的起始值    ex.   start=1   则testdata1
     * @param end   表示测试数据后标的结束值    ex.   end=10   则testdata10
     * @Description 创建测试数据
     * @author gjy
     * @Date 2019/04/10 15:29
     */
    public static <T> void createTestData(Class<T> clazz, int start, int end) {
        if (null == c) {
            System.out.println("get connection fail,please set connection first! ");
        } else if (!BeanUtil.isBean(clazz)) {
            System.out.println("parametertype is not bean! ");
        } else {
            try {
                Statement s = c.createStatement();
                String sqlFormat = createSqlFormat(clazz);
                int c = count(sqlFormat, "%d");
                for (int i = start; i <= end; i++) {
                    List<Integer> ins = new ArrayList<>();
                    for (int j = 1; j <= c; j++) {
                        ins.add(i);
                    }
                    String sql = String.format(sqlFormat, ins.toArray());
                    s.execute(sql);

                    System.out.println(sql);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @Description: 创建sql语句格式
     * @author: gjy
     * @Date: 2019/04/10 15:29
     */
    public static <T> String createSqlFormat(Class<T> clazz) {
        Field[] field = clazz.getDeclaredFields();
        StringBuffer sqlFormat1 = new StringBuffer("insert into ");
        StringBuffer sqlFormat2 = new StringBuffer(") values (");
        sqlFormat1.append(HumpToUnderline(toLowerCaseFirstOne(clazz.getSimpleName())) + " (");
        Map<String, Object> keyValue = new LinkedHashMap<>();
        for (Field f : field) {
            if (f.getGenericType() == java.lang.Integer.class || f.getGenericType() == java.lang.Long.class) {
                keyValue.put(CreateTestDataUtil.HumpToUnderline(f.getName()) + ",", "%d,");
            } else if (f.getGenericType() == java.lang.String.class) {
                keyValue.put(CreateTestDataUtil.HumpToUnderline(f.getName()) + ",", "'" + CreateTestDataUtil.HumpToUnderline(f.getName()) + "%d',");
            } else if (f.getGenericType() == java.util.Date.class) {
                keyValue.put(CreateTestDataUtil.HumpToUnderline(f.getName()) + ",", "'" + DateUtil.formatDateTime(DateUtil.date(System.currentTimeMillis()))+"',");
            } else {
                keyValue.put(CreateTestDataUtil.HumpToUnderline(f.getName()) + "id" + ",", 1 + ",");
            }

        }

        for (Map.Entry<String, Object> entry : keyValue.entrySet()) {
            sqlFormat1.append(HumpToUnderline(entry.getKey()));
            sqlFormat2.append(HumpToUnderline(String.valueOf(entry.getValue())));
        }
        return sqlFormat1.deleteCharAt(sqlFormat1.length() - 1).append(sqlFormat2.deleteCharAt(sqlFormat2.length() - 1)) + ")";
    }
}
