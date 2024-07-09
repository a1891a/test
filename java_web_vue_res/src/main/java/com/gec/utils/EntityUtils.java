package com.gec.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.entity.User;
import com.gec.entity.reqData.ReqData;
import com.gec.enums.ApprovalStatus;
import com.gec.enums.Gender;
import com.gec.enums.Importance;
import com.gec.enums.Role;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.gec.enums.EnabledStatus.Disabled;
import static com.gec.enums.EnabledStatus.Enabled;

public class

EntityUtils {
    /**
     * updateObject中进行转换
     *
     * @param updateObject
     */
    public static void enumUpdateObject(Map<String, Object> updateObject) {
        for (String key : updateObject.keySet()) {
            switch (key) {
                case "isApproval":
                    ApprovalStatus approvalStatus = ApprovalStatus.getEnum(updateObject.get(key).toString());
                    updateObject.replace("isApproval", approvalStatus);
                    break;
                case "importance":
                    Importance importance = Importance.getEnum(updateObject.get(key).toString());
                    updateObject.replace("importance", importance);
                    break;
                case "gender":
                    Gender gender = Gender.getEnum(updateObject.get(key).toString());
                    updateObject.put("gender", gender);
                    break;
                case "role":
                    Role role = Role.getEnum(updateObject.get(key).toString());
                    updateObject.replace("role", role);
                    break;
            }
        }
    }

    /**
     * queryObject中进行转换
     *
     * @param queryObject
     */
    public static void enumQueryObject(Map<String, Object> queryObject) {
        for (String key : queryObject.keySet()) {
            if (!queryObject.get(key).equals("") && queryObject.get(key) != null) {
                switch (key) {
                    case "is_approval":
                        ApprovalStatus approvalStatus = ApprovalStatus.getEnum(queryObject.get(key).toString());
                        queryObject.put("is_approval", approvalStatus.getValue());
                        break;
                    case "importance":
                        Importance importance = Importance.getEnum(queryObject.get(key).toString());
                        queryObject.put("importance", importance.getValue());
                        break;
                    case "gender":
                        Gender gender = Gender.getEnum(queryObject.get(key).toString());
                        queryObject.put("gender", gender.getValue());
                        break;
                    case "role":
                        Role role = Role.getEnum(queryObject.get(key).toString());
                        queryObject.put("role", role.getValue());
                        break;
                }
            }
        }
    }

    /**
     * 获取deleteCode
     */
    public static <T> List<String> getUserDeleteCode(ReqData<T> reqData, IService<User> iService) {
        List<String> deleteCode = reqData.getDeleteCode();
        List<User> userList = iService.list();
        List<String> needDeleteCode = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            for (int j = 0; j < deleteCode.size(); j++) {
                if (userList.get(i).getTargetCode().equals(deleteCode.get(j))) {
                    needDeleteCode.add(userList.get(i).getUserCode());
                }
            }
        }
        return needDeleteCode;
    }

    /**
     * 获取查询Wrapper
     *
     * @keyMap是额外条件
     */
    public static <T> QueryWrapper getQueryWrapper(ReqData<T> reqData) {
        //获取查询条件数组
        Map<String, Object> queryObject = reqData.getQueryObject();
        enumQueryObject(queryObject);
        //设置查询条件
        QueryWrapper<T> queryWrapper = new QueryWrapper();
        //查询没删除的
        queryWrapper.eq("is_delete", Enabled);
        for (String key : queryObject.keySet()) {
            //如果查询的键值的值为空的，那么就说明不查询
            if (queryObject.get(key) != "" && queryObject.get(key) != null) {
                //集合代表and查询
                if (queryObject.get(key) instanceof ArrayList) {
                    List<String> list = (List<String>) queryObject.get(key);
                    queryWrapper.and(wrapper -> {
                        for (int i = 0; i < list.size(); i++) {
                            if (i == list.size() - 1) {
                                wrapper.like(key, list.get(i));
                            } else {
                                wrapper.like(key, list.get(i)).or();
                            }
                        }
                    });
                } else {
                    queryWrapper.like(key, queryObject.get(key));
                }
            }
        }
        return queryWrapper;
    }

    public static <T> QueryWrapper getQueryWrapper(ReqData<T> reqData, Map<String, Object> keyMap) {
        //获取查询条件数组
        Map<String, Object> queryObject = reqData.getQueryObject();
        enumQueryObject(queryObject);
        //设置查询条件
        QueryWrapper<T> queryWrapper = new QueryWrapper();
        //keyMap为额外属性
        for (String key : keyMap.keySet()) {
            if (keyMap.get(key) != null && keyMap.get(key) != "") {
                queryWrapper.eq(key, keyMap.get(key));
            }
        }
        for (String key : queryObject.keySet()) {
            //如果查询的键值的值为空的，那么就说明不查询
            if (queryObject.get(key) != "" && queryObject.get(key) != null) {
                //集合代表and查询
                if (queryObject.get(key) instanceof ArrayList) {
                    List<String> list = (List<String>) queryObject.get(key);
                    queryWrapper.and(wrapper -> {
                        for (int i = 0; i < list.size(); i++) {
                            if (i == list.size() - 1) {
                                wrapper.like(key, list.get(i));
                            } else {
                                wrapper.like(key, list.get(i)).or();
                            }
                        }
                    });
                } else {
                    queryWrapper.like(key, queryObject.get(key));
                }
            }
        }
        return queryWrapper;
    }

    /**
     * 获取更新Wrapper
     */
    public static <T> UpdateWrapper getUpdateWrapper(ReqData<T> reqData) {
        Map<String, Object> updateObject = reqData.getUpdateObject();
        enumUpdateObject(updateObject);
        //更新前置条件
        UpdateWrapper<T> updateWrapper = new UpdateWrapper();
        for (String key : updateObject.keySet()) {
            //将键值改为跟数据库一致的键值
            String keyName = key;
            for (int i = 0; i < key.length(); i++) {
                if (Character.isUpperCase(key.charAt(i))) {
                    keyName = key.substring(0, i) + "_" + Character.toLowerCase(key.charAt(i)) + key.substring(i + 1);
                    break;
                }
            }
            updateWrapper.set(keyName, updateObject.get(key));
        }
        return updateWrapper;
    }

    /**
     * 获取点评Wrapper
     */
    public static <T> UpdateWrapper getCommentsWrapper(ReqData<T> reqData) {
        Map<String, Object> updateObject = reqData.getCommentsObject();
        //更新前置条件
        UpdateWrapper<T> updateWrapper = new UpdateWrapper();
        for (String key : updateObject.keySet()) {
            //将键值改为跟数据库一致的键值
            String keyName = key;
            for (int i = 0; i < key.length(); i++) {
                if (Character.isUpperCase(key.charAt(i))) {
                    keyName = key.substring(0, i) + "_" + Character.toLowerCase(key.charAt(i)) + key.substring(i + 1);
                    break;
                }
            }
            updateWrapper.set(keyName, updateObject.get(key));
        }
        return updateWrapper;
    }

    /**
     * @获取删除Wrapper
     * @keyMap为额外条件
     */
    public static <T> UpdateWrapper getDeleteWrapper(ReqData<T> reqData, String key) {
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        //逻辑删除
        updateWrapper.set("is_delete", Disabled);
        List<String> CodeList = reqData.getDeleteCode();
        for (int i = 0; i < CodeList.size(); i++) {
            updateWrapper.eq(key, CodeList.get(i)).or();
        }
        return updateWrapper;
    }

    public static <T> UpdateWrapper getDeleteWrapper(ReqData<T> reqData, String key, Map<String, Object> keyMap) {
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        //逻辑删除
        updateWrapper.set("is_delete", Disabled);
        //额外条件
        for (String keys : keyMap.keySet()) {
            if (keyMap.get(keys) != null && keyMap.get(keys) != "") {
                updateWrapper.eq(keys, keyMap.get(keys));
            }
        }
        List<String> CodeList = reqData.getDeleteCode();
        for (int i = 0; i < CodeList.size(); i++) {
            updateWrapper.eq(key, CodeList.get(i)).or();
        }
        return updateWrapper;
    }

    /**
     * @获取审核Wrapper
     * @keyMap为额外更新
     */
    public static <T> UpdateWrapper getApprovalWrapper(ReqData<T> reqData, String key) {
        UpdateWrapper<T> updateWrapper = new UpdateWrapper();
        //审核通过
        updateWrapper.set("is_approval", "0");
        List<String> CodeList = reqData.getApprovalCode();
        for (int i = 0; i < CodeList.size(); i++) {
            updateWrapper.eq(key, CodeList.get(i)).or();
        }
        return updateWrapper;
    }

    public static <T> UpdateWrapper getApprovalWrapper(ReqData<T> reqData, String key, Map<String, Object> keyMap) {
        UpdateWrapper<T> updateWrapper = new UpdateWrapper();
        //额外更新
        for (String keys : keyMap.keySet()) {
            if (keyMap.get(keys) != null && keyMap.get(keys) != "") {
                updateWrapper.set(keys, keyMap.get(keys));
            }
        }
        updateWrapper.set("is_approval", "0");
        List<String> CodeList = reqData.getApprovalCode();
        for (int i = 0; i < CodeList.size(); i++) {
            updateWrapper.eq(key, CodeList.get(i)).or();
        }
        return updateWrapper;
    }

    /**
     * 获取总页数
     *
     * @keyMap为自定义条件
     */
    public static <T> int getListRecordByQuery(IService<T> iService, ReqData<T> reqData) {
        return iService.count(getQueryWrapper(reqData));
    }

    public static <T> int getListRecordByQuery(IService<T> iService, ReqData<T> reqData, Map<String, Object> keyMap) {
        return iService.count(getQueryWrapper(reqData, keyMap));
    }

    /**
     * 分页查询
     *
     * @keyMap为自定义条件
     */
    public static <T> List<T> getListByPage(IService<T> iService, ReqData<T> reqData) {
        //从前端获取查询需要的数据
        int currentPage = Integer.parseInt(reqData.getCurrentPage());
        int pageSize = Integer.parseInt(reqData.getPageSize());
        QueryWrapper<T> queryWrapper = getQueryWrapper(reqData);
        queryWrapper.last("limit " + (currentPage - 1) * pageSize + "," + pageSize);
        return iService.list(queryWrapper);
    }

    public static <T> List<T> getListByPage(IService<T> iService, ReqData<T> reqData, Map<String, Object> keyMap) {
        //从前端获取查询需要的数据
        int currentPage = Integer.parseInt(reqData.getCurrentPage());
        int pageSize = Integer.parseInt(reqData.getPageSize());
        QueryWrapper<T> queryWrapper = getQueryWrapper(reqData);
        for (String key : keyMap.keySet()) {
            if (keyMap.get(key) != null && keyMap.get(key) != "") {
                queryWrapper.eq(key, keyMap.get(key));
            }
        }
        queryWrapper.last("limit " + (currentPage - 1) * pageSize + "," + pageSize);
        return iService.list(queryWrapper);
    }


    /**
     * 获取动态添加的对象
     */

    public static <T> Object getInsertEntity(Class<T> myClass, Map<String, Object> insertObject) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        //获取指定的构造器：
        Constructor con = myClass.getDeclaredConstructor();
        //保证该构造器是可以访问的
        con.setAccessible(true);
        //创建实体
        Object entity = con.newInstance();
        //获取该实体的所有方法
        Method[] declaredMethods = myClass.getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++) {
            //因为是添加，所以是set方法
            if (declaredMethods[i].getName().substring(0, 3).equals("set")) {
                //获取方法名称
                String methodName = declaredMethods[i].getName();
                //获取传入参数的类型
                Class<?>[] parameterTypes = declaredMethods[i].getParameterTypes();
                //寻找该方法需要的数据
                for (String key : insertObject.keySet()) {
                    //如果键值相同就说明是要添加的
                    //首字母大写的驼峰命名
                    //如 setProName   ProName
                    if (key.equals(declaredMethods[i].getName().substring(3))) {
                        Object value = insertObject.get(key);
                        //执行方法
                        entity.getClass().getDeclaredMethod(methodName, parameterTypes).invoke(entity, new Object[]{value});
                        break;
                    }
                }
            }
        }
        return entity;
    }
}
