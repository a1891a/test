package com.gec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 后台角色和菜单关系表
 */
@Data
@TableName(value = "user_role_menu_relation")
public class UserRoleMenuRelation {
    private int id;

    @TableField("role_id")
    private int roleId;

    @TableField("menu_id")
    private int menuId;
}
