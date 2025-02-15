package com.ezjava.builder;

import com.ezjava.bean.Constans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BuildBase {
    private static Logger logger = LoggerFactory.getLogger(BuildBase.class);

    public static void execute() {
        List<String> headerInfoList = new ArrayList<>();

        //生成日期枚举
        headerInfoList.add("package " + Constans.PACKAGE_ENUMS);
        build(headerInfoList, "DateTimePatternEnum", Constans.PATH_ENUMS);

        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_UTILS);
        build(headerInfoList, "DateUtils", Constans.PATH_UTILS);

        //生成baseMapper
        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_MAPPERS);
        build(headerInfoList, "BaseMapper", Constans.PATH_MAPPERS);

        //生成pageSize枚举
        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_ENUMS);
        build(headerInfoList, "PageSize", Constans.PATH_ENUMS);

        //生成simplePage枚举
        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_QUERY);
        headerInfoList.add("import " + Constans.PACKAGE_ENUMS + ".PageSize;");
        build(headerInfoList, "SimplePage", Constans.PATH_QUERY);

        //生成baseQuery枚举
        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_QUERY);
        build(headerInfoList, "BaseQuery", Constans.PATH_QUERY);

        //生成PaginationResultVO枚举
        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_VO);
        build(headerInfoList, "PaginationResultVO", Constans.PATH_VO);

        //生成BusinessException枚举
        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_EXCEPTION);
        headerInfoList.add("import " + Constans.PACKAGE_ENUMS + ".ResponseCodeEnum;");
        build(headerInfoList, "BusinessException", Constans.PATH_EXCEPTION);

        //生成ResultCodeEnum枚举
        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_ENUMS);
        build(headerInfoList, "ResponseCodeEnum", Constans.PATH_ENUMS);

        //生成BaseController枚举
        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_CONTROLLER);
        headerInfoList.add("import " + Constans.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headerInfoList.add("import " + Constans.PACKAGE_VO + ".ResponseVo");
        build(headerInfoList, "ABaseController", Constans.PATH_CONTROLLER);

        //生成ResponseVo
        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_VO);
        build(headerInfoList, "ResponseVo", Constans.PATH_VO);

        //生成AGlobalExceptionHandlerController枚举
        headerInfoList.clear();
        headerInfoList.add("package " + Constans.PACKAGE_CONTROLLER);
        headerInfoList.add("import " + Constans.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headerInfoList.add("import " + Constans.PACKAGE_VO + ".ResponseVo");
        headerInfoList.add("import " + Constans.PACKAGE_EXCEPTION + ".BusinessException");
        build(headerInfoList, "AGlobalExceptionHandlerController", Constans.PATH_CONTROLLER);
    }

    private static void build(List<String> headerInfoList, String fileName, String outPutPath) {
        File folder = new File(outPutPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File javaFile = new File(outPutPath, fileName + ".java");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        InputStream in = null;
        InputStreamReader inr = null;
        BufferedReader bf = null;
        try {
            out = new FileOutputStream(javaFile);
            outw = new OutputStreamWriter(out, "UTF-8");
            bw = new BufferedWriter(outw);

            String templatePath = BuildBase.class.getClassLoader().getResource("template/" + fileName + ".txt").getPath();
            in = new FileInputStream(templatePath);
            inr = new InputStreamReader(in, "UTF-8");
            bf = new BufferedReader(inr);

            for (String head : headerInfoList) {
                bw.write(head + ";");
                bw.newLine();
                bw.newLine();

            }

            String lineInfo = null;
            while ((lineInfo = bf.readLine()) != null) {
                bw.write(lineInfo);
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            logger.error("生成基础类：{}失败", fileName, e);
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inr != null) {
                try {
                    inr.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }


            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (outw != null) {
                try {
                    outw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
