package com.gec.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 *
 * @author R
 */
@Getter
@AllArgsConstructor
public enum EnabledStatus {
    Enabled(0, "启用"),
    Disabled(1, "不启用");
    /**
     * 数据库字段
     */
    @EnumValue // 标记数据库存的值是code
    private final Integer value;
    /**
     * 映射成结果
     */
    @JsonValue
    private final String desc;
    public Integer getValue() {
        return this.value;
    }

    public String getDesc() {
        return this.desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }

    //重载以兼容两个参数，传codeValue或者valueDesc都可
    public static EnabledStatus getEnum(String inputValue){
        for (EnabledStatus obj:EnabledStatus.values()
        ) {
            if (obj.desc.equals(inputValue) || String.valueOf(obj.value).equals(inputValue)){
                return obj;
            }
        }
        return null;
    }
//    public static EnabledStatus getEnum(Integer value){
//        for (EnabledStatus obj:EnabledStatus.values()
//        ) {
//            if (obj.value.equals(value)){
//                return obj;
//            }
//        }
//        return null;
//    }
}