package com.gec.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.enums.Gender;
import com.gec.enums.Role;
import com.gec.excel.convert.RoleConvert;
import lombok.Data;

@Data
@TableName(value = "menu")
public class Menu {
    private int id;

    @TableField("parent_id")
    private int parentId;

    @TableField("create_time")
    private String createTime;

    private String title;

    private int level;

    private int sort;

    private String name;

    private String icon;

    private int hidden;
}
