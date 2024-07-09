package com.gec.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.UserRoleMenuRelationDao;
import com.gec.entity.UserRoleMenuRelation;
import com.gec.service.UserRoleMenuRelationService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleMenuRelationServiceImpl extends ServiceImpl<UserRoleMenuRelationDao, UserRoleMenuRelation> implements UserRoleMenuRelationService {
}
