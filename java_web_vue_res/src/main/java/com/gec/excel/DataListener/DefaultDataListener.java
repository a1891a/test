package com.gec.excel.DataListener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.common.CodeRule;
import com.gec.common.Common;
import com.gec.entity.*;
import com.gec.entity.Module;
import com.gec.entity.reqData.ReqData;
import com.gec.excel.valid.ExcelImportValid;
import com.gec.utils.CodeUtils;
import com.gec.utils.DateUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gec.enums.EnabledStatus.Enabled;

@Slf4j
public class DefaultDataListener<T> extends AnalysisEventListener<T> {
    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;

    //传入正确的表头进行校验
    private Map<Integer, String> head;
    //表格内主要的名字不能相同
    private Map<Integer, String> names = new HashMap<>();
    //要调用的service
    private IService<T> service;
    List<T> entityList = new ArrayList();
    //数据包
    private ReqData<T> reqData;
    //当前操作的用户
    private User sessionUser;
    public DefaultDataListener(IService<T> service, Map<Integer, String> head,User sessionUser) {
        this.service = service;
        this.head = head;
        this.sessionUser = sessionUser;
    }

    public DefaultDataListener(IService<T> service, Map<Integer, String> head, ReqData<T> reqData,User sessionUser) {
        this.service = service;
        this.head = head;
        this.reqData = reqData;
        this.sessionUser = sessionUser;
    }

    /**
     * 表头校验
     *
     * @param headMap
     * @param context
     */
    @SneakyThrows
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        if (headMap.size() <= 0) {
            throw new Exception("你上传的excel表格格式不对!请不要修改表头!");
        }
        for (Integer key : headMap.keySet()) {
            if (!head.containsValue(headMap.get(key))) {
                throw new Exception("你上传的excel表格格式不对!请不要修改表头!");
            }
        }
    }

    //读取数据会执行这方法
    @SneakyThrows
    @Override
    public void invoke(T t, AnalysisContext analysisContext) {
        //数据校验
        try {
            ExcelImportValid.valid(t);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        T entity = convertEntity(t);
        if (entity != null) {
            entityList.add(entity);
        }
//        if (entityList.size() >= BATCH_COUNT) {
//            saveData();
//            entityList.clear();
//        }
    }

    private T convertEntity(T t) {
        int codeCount = service.count() + 1 + entityList.size();
        if (t.getClass() == Dept.class) {
            Dept dept = (Dept) t;
            //表格内名字相同的直接只处理第一个
            if (names.size() > 0 && names.containsValue(dept.getDeptName())) {
                return null;
            }
            //不能有相同名字的部门
            QueryWrapper<T> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("dept_name", dept.getDeptName());
            Dept one = (Dept) service.getOne(queryWrapper);
            if (one != null) {
                return null;
            }
            names.put(entityList.size(), dept.getDeptName());
            //查询最新的那条数据的id
            String deptCode = CodeRule.DeptCodeRule + CodeUtils.getCode(codeCount, 5);
            dept.setDeptCode(deptCode);
            //设置日期
            dept.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
            dept.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
            //设置操作员
            dept.setOperator(sessionUser.getUserCode());
            //设置上级部门
            dept.setParentCode("0");
            //是否启用
            dept.setIsDelete(Enabled);
            return (T) dept;
        } else if (t.getClass() == Clazz.class) {
            Clazz clazz = (Clazz) t;
            //表格内名字相同的直接只处理第一个
            if (names.size() > 0 && names.containsValue(clazz.getClazzName())) {
                return null;
            }
            QueryWrapper<T> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("clazz_name", clazz.getClazzName());
            Clazz one = (Clazz) service.getOne(queryWrapper);
            if (one != null) {
                return null;
            }
            names.put(entityList.size(), clazz.getClazzName());
            //查询最新的那条数据的id
            String clazzCode = CodeRule.ClazzCodeRule + CodeUtils.getCode(codeCount, 5);
            clazz.setClazzCode(clazzCode);
            //设置日期
            clazz.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
            clazz.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
            //设置操作员
            clazz.setOperator(sessionUser.getUserCode());
            //是否启用
            clazz.setIsDelete(Enabled);
            return (T) clazz;
        } else if (t.getClass() == Project.class) {
            Project project = (Project) t;
            //表格内名字相同的直接只处理第一个
            if (names.size() > 0 && names.containsValue(project.getProName())) {
                return null;
            }
            //项目名不能一致
            QueryWrapper<T> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("pro_name", project.getProName());
            Project one = (Project) service.getOne(queryWrapper);
            if (one != null) {
                return null;
            }
            //查询最新的那条数据的id
            String proCode = CodeRule.ProjectCodeRule + CodeUtils.getCode(codeCount, 5);
            project.setProCode(proCode);
            //设置日期
            project.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
            project.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
            //设置操作员
            project.setOperator(sessionUser.getUserCode());
            //是否启用
            project.setIsDelete(Enabled);
            return (T) project;
        } else if (t.getClass() == Module.class) {
            Module module = (Module) t;
            //表格内名字相同的直接只处理第一个
            if (names.size() > 0 && names.containsValue(module.getModName())) {
                return null;
            }
            //模块名不能一致
            QueryWrapper<T> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("mod_name", module.getModName());
            Module one = (Module) service.getOne(queryWrapper);
            if (one != null) {
                return null;
            }
            //查询最新的那条数据的id
            String modCode = CodeRule.ModuleCodeRule + CodeUtils.getCode(codeCount, 5);
            module.setModCode(modCode);
            module.setProCode(reqData.getProCode());
            //设置日期
            module.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
            module.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
            //设置操作员
            module.setOperator(sessionUser.getUserCode());
            //是否启用
            module.setIsDelete(Enabled);
            return (T) module;
        } else if (t.getClass() == TcStandard.class) {
            TcStandard stad = (TcStandard) t;
            //查询最新的那条数据的id
            String stadCode = CodeRule.TcStandardRule + CodeUtils.getCode(codeCount, 5);
            stad.setStadCode(stadCode);
            stad.setModCode(reqData.getModCode());
            //设置日期
            stad.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
            stad.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
            //设置操作员
            stad.setOperator(sessionUser.getUserCode());
            //是否启用
            stad.setIsDelete(Enabled);
            return (T) stad;
        }
        return null;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
    }

    private void saveData() {
        service.saveBatch(entityList);
    }
}
