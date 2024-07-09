package com.gec.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.UserRolePermissionRelationDao;
import com.gec.entity.UserRoleMenuRelation;
import com.gec.entity.UserRolePermissionRelation;
import com.gec.service.UserRolePermissionRelationService;
import org.springframework.stereotype.Service;

@Service
public class UserRolePermissionRelationServiceImpl extends ServiceImpl<UserRolePermissionRelationDao, UserRolePermissionRelation> implements UserRolePermissionRelationService {
}
