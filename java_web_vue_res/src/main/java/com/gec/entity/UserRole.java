package com.gec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.enums.EnabledStatus;
import lombok.Data;

@Data
@TableName(value="user_role")
public class UserRole {
    private int id;
    private String name;
    private String description;
    @TableField("admin_count")
    private int adminCount;
    @TableField("create_time")
    private String createTime;
    @TableField("is_delete")
    private EnabledStatus isDelete;
    private int sort;
}
