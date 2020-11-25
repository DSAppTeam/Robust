package com.meituan.robust.autopatch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mivanzhang on 17/1/19.
 *
 * Reading mapping from mapping.txt,which is generated by ProGuard
 */

public class ReadMapping {
    private static ReadMapping instance;

    private Map<String, ClassMapping> usedInModifiedClassMappingInfo = new HashMap<String, ClassMapping>();

    public static ReadMapping getInstance() {
        if (instance == null) {
            instance = new ReadMapping();
        }
        return instance;
    }

    public static void init() {
        instance = new ReadMapping();
    }

    private ReadMapping() {

    }

    /***
     * read all class mapping info
     */
    public void initMappingInfo() {
        //查找mapping文件
        InputStream is = null;
        boolean needBacktrace = true;
        String line;
        try {
            is = new FileInputStream(Config.mappingFilePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 1024);
            // 读取一行，存储于字符串列表中
            line = reader.readLine().trim();
            while (line != null) {
                line = line.trim();
                if (!needBacktrace) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                }
                needBacktrace = false;
                if (line.indexOf("->") > 0 && line.indexOf(":") == line.length() - 1) {
                    ClassMapping classMapping = new ClassMapping();
                    classMapping.setClassName(line.substring(0, line.indexOf("->") - 1).trim());
                    classMapping.setValueName(line.split("->")[1].substring(0, line.split("->")[1].length() - 1).trim());
                    line = reader.readLine();
                    while (line != null) {
                        line = line.trim();
                        if (line.endsWith(":")) {
                            needBacktrace = true;
                            break;
                        }
                        String[] lineinfo = line.split(" ");
                        if (lineinfo.length != 4) {
                            throw new RuntimeException("mapping line info is error  " + line);
                        }
                        if (lineinfo[1].contains("(") && lineinfo[1].contains(")")) {
                            //methods need return type
                            classMapping.getMemberMapping().put(getMethodSigureWithReturnTypeInMapping(lineinfo[0].trim(), lineinfo[1].trim()), lineinfo[3].trim());
                        } else {
                            //fields
                            classMapping.getMemberMapping().put(lineinfo[1].trim(), lineinfo[3].trim());
                        }
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        line = line.trim();
                    }
                    usedInModifiedClassMappingInfo.put(classMapping.getClassName(), classMapping);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ClassMapping getClassMapping(String classname) {
        return usedInModifiedClassMappingInfo.get(classname);
    }

    public void setClassMapping(String classname, ClassMapping classMapping) {
        usedInModifiedClassMappingInfo.put(classname, classMapping);
    }

    public ClassMapping getClassMappingOrDefault(String classname) {
        ClassMapping defaultClassMapping = new ClassMapping();
        if (!Config.supportProGuard) {
            defaultClassMapping.setValueName(classname);
        }
        return usedInModifiedClassMappingInfo.getOrDefault(classname, defaultClassMapping);
    }

    /***
     * @param returnTypeWithNumber
     * @param methodSignure
     * @return returnType+" "+methodSignure,just one blank
     */

    public String getMethodSigureWithReturnTypeInMapping(String returnTypeWithNumber, String methodSignure) {
        //初步观察mapping文件，使用":"来截取返回值，还可以通过寻找第一个字符，
        if (methodSignure.contains(":")) {
            //兼容R8
            return getMethodSignureWithReturnType(returnTypeWithNumber.substring(returnTypeWithNumber.lastIndexOf(":") + 1), methodSignure.substring(0, methodSignure.indexOf(":")));
        }
        return getMethodSignureWithReturnType(returnTypeWithNumber.substring(returnTypeWithNumber.lastIndexOf(":") + 1), methodSignure);
    }

    public String getMethodSignureWithReturnType(String returnType, String methodSignure) {
        //只有一个空格
        return returnType + " " + methodSignure;
    }
}