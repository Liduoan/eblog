package com.example.eblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eblog.Vo.CommentVo;
import com.example.eblog.entity.MComment;
import com.example.eblog.mapper.MCommentMapper;
import com.example.eblog.service.MCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 公众号：java思维导图
 * @since 2021-02-16
 */
@Service
public class MCommentServiceImpl extends ServiceImpl<MCommentMapper, MComment> implements MCommentService {

    @Autowired
    private MCommentMapper mCommentMapper;

    @Override
    public IPage<CommentVo> paing(Page page, Long postId, Long userId, String order) {

        //连接两个表 然后通过post_id
        return mCommentMapper.selectComments(page, new QueryWrapper<MComment>()
                .eq(postId != null, "post_id", postId)
                .eq(userId != null, "user_id", userId)
                .orderByDesc(order != null, order)
        );
    }
}
