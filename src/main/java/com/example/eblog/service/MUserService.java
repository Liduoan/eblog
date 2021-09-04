package com.example.eblog.service;

import com.example.eblog.common.lang.Result;
import com.example.eblog.entity.MUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.eblog.shiro.AccountProfile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 公众号：java思维导图
 * @since 2021-02-16
 */
public interface MUserService extends IService<MUser> {

    Result register(MUser user);

    AccountProfile login(String username, String password);
}
