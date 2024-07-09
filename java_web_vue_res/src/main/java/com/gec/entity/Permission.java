package com.gec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.enums.EnabledStatus;
import lombok.Data;

@Data
@TableName(value = "permission")
public class Permission {
    private int id;

    private int pid;

    private String name;

    private String uri;

    @TableField("create_time")
    private String createTime;

    private int sort;

    @TableField("is_delete")
    private EnabledStatus is_delete;
}
