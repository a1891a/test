package com.gec.entity.reqData;

import com.gec.entity.User;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 这个是前端传过来的参数类
 * @param <T>
 */
@Data
public class ReqData<T> {
    /**
     * 用户
     */
    private User user;
    /**
     * 页数 和 页数大小
     */
    private String currentPage;
    private String pageSize;
    /**
     * 前端查询对象，内部键值和数据库的键值一致
     */
    private Map<String,Object> queryObject;
    /**
     * 前端更新对象
     */
    private Map<String,Object>  updateObject;
    /**
     * 前端点评对象
     */
    private Map<String,Object> commentsObject;
    /**
     * 前端新增对象，内部键值和实体类的键值一致
     */
    private T  insertObject;
    /**
     * 前端删除集合
     */
    private List<String> deleteCode;
    /**
     * 前端点评集合
     */
    private List<String> approvalCode;
    /**
     * 前端导出集合
     */
    private List<String> exportCode;
    /**
     * 以上数据可能需要的参数
     */
    private String updatePassword;
    private String userCode;
    private String userType;
    private String empCode;
    private String empName;
    private String deptCode;
    private String clazzCode;
    private String stuCode;
    private String proCode;
    private String modCode;
    private String stadCode;
    private String tcCode;
    private String tcAmount;
}
