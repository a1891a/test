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
public enum Importance {
    Low(0, "低"),
    Medium(1, "中"),
    High(2, "高");
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
    public static Importance getEnum(String inputValue){
        for (Importance obj:Importance.values()
        ) {
            if (obj.desc.equals(inputValue) || String.valueOf(obj.value).equals(inputValue)){
                return obj;
            }
        }
        return null;
    }
//    public static Importance getEnum(Integer value){
//        for (Importance obj:Importance.values()
//        ) {
//            if (obj.value.equals(value)){
//                return obj;
//            }
//        }
//        return null;
//    }
}