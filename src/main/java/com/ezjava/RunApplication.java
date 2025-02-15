package com.ezjava;

import com.ezjava.bean.TableInfo;
import com.ezjava.builder.*;
import com.ezjava.utils.PropertiesUtils;

import java.util.List;

public class RunApplication {
    public static void main(String[] args) {
        System.out.println("开始生成代码");

        PropertiesUtils propertiesUtils = new PropertiesUtils();

        List<TableInfo> tableInfoList = BuildTable.getTables();

        BuildBase.execute();

        for (TableInfo tableInfo : tableInfoList) {
            BuildPo.execute(tableInfo);

            BuildQuery.execute(tableInfo);

            BuildMapper.execute(tableInfo);

            BuildMapperXml.execute(tableInfo);

            BuildService.execute(tableInfo);

            BuildServiceImpl.execute(tableInfo);

            BuildController.execute(tableInfo);
        }

        System.out.println("生成代码成功，代码在" + propertiesUtils.getString("path.base"));
    }
}
