package com.ezjava.builder;

import com.ezjava.bean.Constans;
import com.ezjava.bean.FieldInfo;
import com.ezjava.bean.TableInfo;
import com.ezjava.utils.DateUtils;
import com.ezjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BuildService {
    private static final Logger logger = LoggerFactory.getLogger(BuildService.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constans.PATH_SERVICE);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String className = tableInfo.getBeanName() + "Service";
        File poFile = new File(folder, className + ".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out, "UTF-8");
            bw = new BufferedWriter(outw);

            bw.write("package " + Constans.PACKAGE_SERVICE + ";");
            bw.newLine();
            bw.newLine();

//            if (tableInfo.getHaveDate() || tableInfo.getHaveDateTime()) {
//                bw.write(Constans.BEAN_DATE_FORMAT_CLASS);
//                bw.newLine();
//                bw.write(Constans.BEAN_DATE_UNFORMAT_CLASS);
//                bw.newLine();
//                bw.write("import " + Constans.PACKAGE_UTILS + ".DateUtils;");
//                bw.newLine();
//                bw.write("import " + Constans.PACKAGE_ENUMS + ".DateTimePatternEnum;");
//                bw.newLine();
//                bw.write("import java.util.Date;");
//                bw.newLine();
//            }

            bw.write("import " + Constans.PACKAGE_VO + ".PaginationResultVO;");
            bw.newLine();
            bw.write("import " + Constans.PACKAGE_PO + "." + tableInfo.getBeanName() + ";");
            bw.newLine();
            bw.write("import " + Constans.PACKAGE_QUERY + "." + tableInfo.getBeanParamName() + ";");
            bw.newLine();
            bw.newLine();
            bw.write("import java.util.List;");
            bw.newLine();

            BuildComment.createClassComment(bw, tableInfo.getComment() + "Service");
            bw.write("public interface " + className + " {");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "根据条件查询列表");
            bw.write("\tList<" + tableInfo.getBeanName() + "> findListByParam(" + tableInfo.getBeanParamName() + " query);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "根据条件查询数量");
            bw.write("\tInteger findCountByParam(" + tableInfo.getBeanParamName() + " query);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "分页查询");
            bw.write("\tPaginationResultVO<" + tableInfo.getBeanName() + "> findListByPage(" + tableInfo.getBeanParamName() + " query);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "新增");
            bw.write("\tInteger add(" + tableInfo.getBeanName() + " bean);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "批量新增");
            bw.write("\tInteger addBatch(List<" + tableInfo.getBeanName() + "> listBean);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "批量新增或更新");
            bw.write("\tInteger addOrUpdateBatch(List<" + tableInfo.getBeanName() + "> listBean);");
            bw.newLine();
            bw.newLine();

            for (Map.Entry<String, List<FieldInfo>> entry : tableInfo.getKeyIndexMap().entrySet()) {
                List<FieldInfo> keyFieldInfoList = entry.getValue();

                Integer index = 0;
                StringBuilder methodName = new StringBuilder();
                StringBuilder methodParams = new StringBuilder();
                for (FieldInfo fieldInfo : keyFieldInfoList) {
                    index++;
                    methodName.append(StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName()));
                    if (index < keyFieldInfoList.size()) {
                        methodName.append("And");
                    }

                    methodParams.append(fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName());
                    if (index < keyFieldInfoList.size()) {
                        methodParams.append(", ");
                    }
                }

                BuildComment.createFieldComment(bw, "根据" + methodName + "查询数据");
                bw.write("\t" + tableInfo.getBeanName() + " get" + tableInfo.getBeanName() + "By" + methodName + "(" + methodParams + ");");
                bw.newLine();
                bw.newLine();

                BuildComment.createFieldComment(bw, "根据" + methodName + "更新数据");
                bw.write("\tInteger update" + tableInfo.getBeanName() + "By" + methodName + "(" + tableInfo.getBeanName() + " bean, " + methodParams + ");");
                bw.newLine();
                bw.newLine();

                BuildComment.createFieldComment(bw, "根据" + methodName + "删除数据");
                bw.write("\tInteger delete" + tableInfo.getBeanName() + "By" + methodName + "(" + methodParams + ");");
                bw.newLine();
                bw.newLine();
            }

            bw.write("}");
            bw.newLine();

            bw.flush();
        } catch (Exception e) {
            logger.error("创建Service失败", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outw != null) {
                try {
                    outw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
