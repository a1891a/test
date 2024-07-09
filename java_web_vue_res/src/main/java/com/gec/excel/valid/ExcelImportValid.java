package com.gec.excel.valid;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.gec.entity.Clazz;
import org.apache.commons.lang3.EnumUtils;
import org.apache.poi.ss.formula.functions.T;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import static java.lang.StrictMath.E;

public class ExcelImportValid {
    /**
     * Excel导入字段校验
     *
     * @param object 校验的JavaBean 其属性须有自定义注解
     */
    public static void valid(Object object) throws Exception {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            //设置可访问
            field.setAccessible(true);
            //属性的值
            Object fieldValue = null;
            //是否包含必填校验注解
            try {
                fieldValue = field.get(object);
                //原始类型的值如果是null就继续查枚举型
            } catch (IllegalAccessException e) {
                throw new Exception("导入参数检查失败");
            }
            boolean isExcelValid = field.isAnnotationPresent(ExcelValid.class);
            if (isExcelValid && Objects.isNull(fieldValue)) {
                throw new Exception(field.getAnnotation(ExcelValid.class).message());
            }
        }
    }
}
