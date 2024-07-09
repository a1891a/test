package com.gec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 后台角色和权限关系表
 */
@Data
@TableName(value = "user_role_permission_relation")
public class UserRolePermissionRelation {
    private int id;

    @TableField("role_id")
    private int roleId;

    @TableField("permission_id")
    private int permissionId;
}