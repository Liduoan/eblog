package com.example.eblog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eblog.Vo.UserMessageVo;
import com.example.eblog.entity.MUserMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 公众号：java思维导图
 * @since 2021-02-16
 */
public interface MUserMessageService extends IService<MUserMessage> {

    IPage<UserMessageVo> paging(Page page, QueryWrapper<MUserMessage> mUserMessageQueryWrapper);

    void updateToReaded(List<Long> ids);
}
