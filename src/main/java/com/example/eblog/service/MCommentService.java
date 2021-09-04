package com.example.eblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eblog.Vo.CommentVo;
import com.example.eblog.entity.MComment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 公众号：java思维导图
 * @since 2021-02-16
 */
public interface MCommentService extends IService<MComment> {

    IPage<CommentVo> paing(Page page, Long postId, Long userId, String order);

}
