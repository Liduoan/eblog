package com.example.eblog.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eblog.Vo.PostVo;
import com.example.eblog.entity.MPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 公众号：java思维导图
 * @since 2021-02-16
 */
public interface MPostMapper extends BaseMapper<MPost> {

    //Mybatis-plus会使用page来帮助我们分页处理
    IPage<PostVo> selectPost(Page page, @Param(Constants.WRAPPER)QueryWrapper wrapper);

    PostVo selectOnePost(@Param(Constants.WRAPPER)QueryWrapper<MPost> eq);
}
