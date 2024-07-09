package com.gec.utils;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.entity.Analysis;
import com.gec.entity.User;
import com.gec.service.AnalysisService;
import com.gec.service.UserService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {
    public static <T> void getExportExcel(HttpServletResponse response, IService<T> iService, Class<T> tClass, QueryWrapper<T> queryWrapper) throws Exception {
        List<T> tList = iService.list(queryWrapper);
        String fileName = "导出excel";
        ServletOutputStream servletOutputStream = servletOutputStream(fileName, response);
        // DownloadData 是实体类，sheet 里面是 sheet 名称，doWrite 里面放要写入的数据，类型为 List<DownloadData>
        EasyExcel.write(servletOutputStream, tClass).autoCloseStream(Boolean.FALSE).sheet("导出").doWrite(tList);
        servletOutputStream.flush();
        // 关闭流
        servletOutputStream.close();
    }

    public static void getAnalysisExportExcel(HttpServletResponse response, AnalysisService analysisService, UserService userService, QueryWrapper<Analysis> queryWrapper) throws Exception {
        List<Analysis> analysisList = analysisService.list(queryWrapper);
        //获取提交用户
        QueryWrapper<User> userQueryWrapper = new QueryWrapper();
        for (int i = 0; i < analysisList.size(); i++) {
            queryWrapper.eq("user_code", analysisList.get(i).getOperator()).or();
        }
        List<User> userList = userService.list(userQueryWrapper);
        //设置提交人名称
        for (int i = 0; i < analysisList.size(); i++) {
            for (int j = 0; j < userList.size(); j++) {
                if (analysisList.get(i).getOperator().equals(userList.get(j).getUserCode())) {
                    analysisList.get(i).setOperatorName(userList.get(j).getUserName());
                }
            }
        }
        String fileName = "导出excel";
        ServletOutputStream servletOutputStream = servletOutputStream(fileName, response);
        // DownloadData 是实体类，sheet 里面是 sheet 名称，doWrite 里面放要写入的数据，类型为 List<DownloadData>
        EasyExcel.write(servletOutputStream, Analysis.class).autoCloseStream(Boolean.FALSE).sheet("导出").doWrite(analysisList);
        servletOutputStream.flush();
        // 关闭流
        servletOutputStream.close();
    }

    public static <T> void getModuleExcel(HttpServletResponse response, Class<T> tClass) throws Exception {
        List<T> tList = new ArrayList<T>();
        String fileName = "模板excel";
        ServletOutputStream servletOutputStream = servletOutputStream(fileName, response);
        // DownloadData 是实体类，sheet 里面是 sheet 名称，doWrite 里面放要写入的数据，类型为 List<DownloadData>
        EasyExcel.write(servletOutputStream, tClass).autoCloseStream(Boolean.FALSE).sheet("模板").doWrite(tList);
        servletOutputStream.flush();
        // 关闭流
        servletOutputStream.close();
    }

    private static ServletOutputStream servletOutputStream(String fileName, HttpServletResponse response) throws Exception {
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.addHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            response.addHeader("Access-Control-Expose-Headers", "Content-disposition");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "no-store");
            response.addHeader("Cache-Control", "max-age=0");
            return response.getOutputStream();
        } catch (IOException e) {
            throw new Exception("导出excel表格失败!", e);
        }
    }
}
