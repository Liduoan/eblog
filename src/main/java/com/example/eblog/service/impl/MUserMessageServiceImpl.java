package com.example.eblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eblog.Vo.UserMessageVo;
import com.example.eblog.entity.MUserMessage;
import com.example.eblog.mapper.MUserMessageMapper;
import com.example.eblog.service.MUserMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 公众号：java思维导图
 * @since 2021-02-16
 */
@Service
public class MUserMessageServiceImpl extends ServiceImpl<MUserMessageMapper, MUserMessage> implements MUserMessageService {

    @Autowired
    private MUserMessageMapper mUserMessageMapper;

    @Override
    public IPage<UserMessageVo> paging(Page page, QueryWrapper<MUserMessage> mUserMessageQueryWrapper) {
        return mUserMessageMapper.selectMessages(page, mUserMessageQueryWrapper);
    }

    @Override
    public void updateToReaded(List<Long> ids) {
        if(ids.isEmpty())
            return;
        mUserMessageMapper.updateToReaded(new QueryWrapper<MUserMessage>()
                .in("id", ids)
        );
    }
}
