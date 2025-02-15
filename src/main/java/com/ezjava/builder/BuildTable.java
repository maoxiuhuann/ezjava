package com.ezjava.builder;

import com.ezjava.bean.Constans;
import com.ezjava.bean.FieldInfo;
import com.ezjava.bean.TableInfo;
import com.ezjava.utils.PropertiesUtils;
import com.ezjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BuildTable {

    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);
    private static Connection conn = null;

    private static final String SQL_SHOW_TABLE_STATUS = "SHOW TABLE STATUS";

    private static final String SQL_SHOW_TABLE_FIELDS = "SHOW FULL FIELDS FROM %s";

    private static final String SQL_SHOW_TABLE_INDEX = "SHOW INDEX FROM %s";


    //连接数据库
    static {
        String driverName = PropertiesUtils.getString("db.driver.name");
        String url = PropertiesUtils.getString("db.url");
        String user = PropertiesUtils.getString("db.username");
        String password = PropertiesUtils.getString("db.password");
        try {
            Class.forName(driverName);//加载并注册指定的数据库驱动类。
            conn = DriverManager.getConnection(url, user, password);//使用指定的数据库 URL、用户名和密码创建与数据库的连接。
        } catch (Exception e) {
            logger.error("数据库连接失败", e);
        }
    }

    /**
     * 读取表信息
     */
    public static List<TableInfo> getTables() {
        PreparedStatement ps = null;//PreparedStatement 是 Statement 的子接口，它表示一个预编译的 SQL 语句
        ResultSet tableResult = null;//ResultSet 是用于表示数据库查询结果的对象，它封装了查询结果，并提供了一系列方法来遍历和操作查询结果中的数据。

        List<TableInfo> tableInfoList = new ArrayList<>();
        try {
            ps = conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            tableResult = ps.executeQuery();
            while (tableResult.next()) {//遍历查询结果，获取表名 name 和表注释 comment。

                String tableName = tableResult.getString("name");
                String comment = tableResult.getString("comment");

                String beanName = tableName;

                if (Constans.IGNORE_TABLE_PREFIX) {//配置是否忽略表名前缀
                    // 返回从 beginIndex 到 endIndex（不包括）的子字符串。如果只指定 beginIndex，则返回从该位置到字符串末尾的部分。
                    beanName = tableName.substring(beanName.indexOf("_") + 1);
                }
                beanName = processField(beanName, true);//BeanName 通常用于生成与表对应的实体类

                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName);
                tableInfo.setBeanName(beanName);
                tableInfo.setComment(comment);
                tableInfo.setBeanParamName(beanName + Constans.SUFFIX_BEAN_QUERY);//BeanParamName 通常用于生成查询类或参数封装类

                readFieldInfo(tableInfo);

                getKeyIndexInfo(tableInfo);

                tableInfoList.add(tableInfo);
            }
        } catch (Exception e) {
            logger.error("查询表信息失败", e);
        } finally {
            if (tableResult != null) {
                try {
                    tableResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return tableInfoList;
    }

    /**
     * 读取表字段信息
     */
    private static void readFieldInfo(TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        List<FieldInfo> fieldInfoList = new ArrayList<>();

        List<FieldInfo> fieldExtendList1 = new ArrayList<>();
        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_FIELDS, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();

            Boolean haveDateTime = false;
            Boolean haveDate = false;
            Boolean haveBigDecimal = false;
            while (fieldResult.next()) {
                String field = fieldResult.getString("field");//数据库中的字段名
                String type = fieldResult.getString("type");//数据库中的字段类型
                String extra = fieldResult.getString("extra");//数据库中的附加信息
                String comment = fieldResult.getString("comment");//数据库中的字段注释

                // 处理字段类型，去除括号中的长度信息，如int(11)-> int
                if (type.indexOf("(") > 0) {
                    type = type.substring(0, type.indexOf("("));
                }
                String propertyName = processField(field, false);

                FieldInfo fieldInfo = new FieldInfo();
                fieldInfoList.add(fieldInfo);


                fieldInfo.setFieldName(field);
                fieldInfo.setComment(comment);
                fieldInfo.setSqlType(type);
                fieldInfo.setAutoIncrement("auto_increment".equalsIgnoreCase(extra) ? true : false);
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setJavaType(processJavaType(type));

                if (ArrayUtils.contains(Constans.SQL_DATE_TIME_TYPES, type)) {
                    haveDateTime = true;
                }
                if (ArrayUtils.contains(Constans.SQL_DATE_TYPES, type)) {
                    haveDate = true;
                }
                if (ArrayUtils.contains(Constans.SQL_DECIMAL_TYPES, type)) {
                    haveBigDecimal = true;
                }

                if (ArrayUtils.contains(Constans.SQL_STRING_TYPES,type)){

                    FieldInfo fuzzyField = new FieldInfo();
                    fuzzyField.setJavaType(fieldInfo.getJavaType());
                    fuzzyField.setPropertyName(propertyName+Constans.SUFFIX_BEAN_QUERY_FUZZY);
                    fuzzyField.setFieldName(fieldInfo.getFieldName());
                    fuzzyField.setSqlType(type);
                    fieldExtendList1.add(fuzzyField);
                }

                if(ArrayUtils.contains(Constans.SQL_DATE_TIME_TYPES,type) || ArrayUtils.contains(Constans.SQL_DATE_TYPES,type)){
                    FieldInfo timeStartField = new FieldInfo();
                    timeStartField.setJavaType("String");
                    timeStartField.setPropertyName(propertyName + Constans.SUFFIX_BEAN_QUERY_TIME_START);
                    timeStartField.setFieldName(fieldInfo.getFieldName());
                    timeStartField.setSqlType(type);
                    fieldExtendList1.add(timeStartField);

                    FieldInfo timeEndField = new FieldInfo();
                    timeEndField.setJavaType("String");
                    timeEndField.setPropertyName(propertyName + Constans.SUFFIX_BEAN_QUERY_TIME_END);
                    timeEndField.setFieldName(fieldInfo.getFieldName());
                    timeEndField.setSqlType(type);
                    fieldExtendList1.add(timeEndField);
                }
            }
            // 处理tableInfo中fieldInfo以外的扩展字段信息
            tableInfo.setHaveDate(haveDate);
            tableInfo.setHaveDateTime(haveDateTime);
            tableInfo.setHaveBigDecimal(haveBigDecimal);
            tableInfo.setFieldList(fieldInfoList);
            tableInfo.setFieldExtendList(fieldExtendList1);
        } catch (Exception e) {
            logger.error("查询表信息失败", e);
        } finally {
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 读取表索引信息
     * @param tableInfo
     * @return
     */
    private static List<FieldInfo> getKeyIndexInfo(TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        List<FieldInfo> fieldInfoList = new ArrayList<>();
        try {

            Map<String, FieldInfo> tempMap = new HashMap<>();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                tempMap.put(fieldInfo.getFieldName(), fieldInfo);
            }

            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_INDEX, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();
            while (fieldResult.next()) {
                String keyName = fieldResult.getString("key_name");
                Integer nonUnique = fieldResult.getInt("non_unique");
                String columnName = fieldResult.getString("column_name");

                if (nonUnique == 1) {
                    continue;
                }
                List<FieldInfo> keyFieldList = tableInfo.getKeyIndexMap().get(keyName);
                if (null == keyFieldList) {
                    keyFieldList = new ArrayList();
                    tableInfo.getKeyIndexMap().put(keyName, keyFieldList);
                }
                /*for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                    if (fieldInfo.getFieldName().equals(columnName)) {
                        keyFieldList.add(fieldInfo);
                    }
                }*/
                keyFieldList.add(tempMap.get(columnName));
            }
        } catch (Exception e) {
            logger.error("查询索引信息失败", e);
        } finally {
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return fieldInfoList;
    }

    /**
     * 处理字段名，将下划线分隔的字段名转换为驼峰式命名
     * @param field
     * @param upperCaseFirstLetter
     * @return
     */
    private static String processField(String field, Boolean upperCaseFirstLetter) {
        StringBuffer sb = new StringBuffer();
        String[] fields = field.split("_");
        sb.append(upperCaseFirstLetter ? StringUtils.upperCaseFirstLetter(fields[0]) : fields[0]);
        for (int i = 1, len = fields.length; i < len; i++) {
            sb.append(StringUtils.upperCaseFirstLetter(fields[i]));
        }
        return sb.toString();
    }

    /**
     * 将数据库表中的类型转换为Java类型
     * @param type
     * @return
     */
    private static String processJavaType(String type) {
        if (ArrayUtils.contains(Constans.SQL_INTEGER_TYPES, type)) {
            return "Integer";
        } else if (ArrayUtils.contains(Constans.SQL_LONG_TYPES, type)) {
            return "Long";
        } else if (ArrayUtils.contains(Constans.SQL_STRING_TYPES, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constans.SQL_DATE_TIME_TYPES, type) || ArrayUtils.contains(Constans.SQL_DATE_TYPES, type)) {
            return "Date";
        } else if (ArrayUtils.contains(Constans.SQL_DECIMAL_TYPES, type)) {
            return "BigDecimal";
        } else {
            return "无法识别的类型";
        }
    }
}
