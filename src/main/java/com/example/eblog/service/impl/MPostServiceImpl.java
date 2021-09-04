package com.example.eblog.service.impl;



import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eblog.Vo.PostVo;
import com.example.eblog.entity.MPost;
import com.example.eblog.mapper.MPostMapper;
import com.example.eblog.service.MPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.eblog.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
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
public class MPostServiceImpl extends ServiceImpl<MPostMapper, MPost> implements MPostService {

    //使持久层进来
    //这和我以前使用的通用Mapper不一样
    @Autowired
    private MPostMapper mPostMapper;


    @Autowired
    private RedisUtil redisUtil;

    @Override
    public IPage<PostVo> paging(Page page, Long categoryId, Long userId, Integer level, Boolean recommend, String order) {
        if(level==null){
            level = -1;
        }
        //相当于Where语句
        QueryWrapper wrapper = new QueryWrapper<MPost>()
                .eq(categoryId != null,  "category_id", categoryId)
                .eq(userId != null,  "user_id", userId)
                .eq(level == 0,  "level", 0)
                .gt(level > 0,  "level", 0)
                .orderByDesc(order != null, order);
        //page用来分页
        return mPostMapper.selectPost(page,wrapper);
    }

    @Override
    public PostVo selectOnePost(QueryWrapper<MPost> eq) {

        return mPostMapper.selectOnePost(eq);
    }

    /**
    * 功能描述: 用于记录前一周的评论排行Rank
    * @Date: 2021/2/18
    * @Author: Liduoan
    * @Param:
      * @param
    * @return: void
    */

    @Override
    public void initWeekPoke() {

        // 获取7天的发表的文章
        // https://apidoc.gitee.com/loolly/hutool/
        // 这里是进行了日期的偏移处理
        List<MPost> posts = this.list(new QueryWrapper<MPost>()
                .ge("created", DateUtil.offsetDay(new Date(), -7)) // 11号
                .select("id, title, user_id, comment_count, view_count, created")
        );

        // 初始化文章的总评论量
        // 对每篇文章进行处理
        for (MPost post : posts) {
            String key = "day:rank:" + DateUtil.format(post.getCreated(), DatePattern.PURE_DATE_FORMAT);

            redisUtil.zSet(key, post.getId(), post.getCommentCount());

            // 7天后自动过期(15号发表，7-（18-15）=4)
            // 相差的天数时长
            long between = DateUtil.between(new Date(), post.getCreated(), DateUnit.DAY);
            //还有多少天就要把这个缓存删了
            long expireTime = (7 - between) * 24 * 60 * 60; // 有效时间

            redisUtil.expire(key, expireTime);

            //开始直接放入rank:post:值
            // 缓存文章的一些基本信息（id，标题，评论数量，作者）
            this.hashCachePostIdAndTitle(post, expireTime);
        }

        // 做并集
        this.zunionAndStoreLast7DayForWeekRank();
    }

    /**
    * 功能描述:  当评论增加或者减少的时候修改redis的数据
    * @Date: 2021/2/19
    * @Author: Liduoan
    * @Param:
      * @param postId
     * @param isIncr
    * @return: void
    */

    @Override
    public void incrCommentCountAndUnionForWeekRank(long postId, boolean isIncr) {

        //这里有个bug
        //如果没有这个键，那么该如何
        //是当天进行评论的，那么修改的应该是当天的数据
        String  currentKey = "day:rank:" + DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);
        redisUtil.zIncrementScore(currentKey, postId, isIncr? 1: -1);

        //这是Mp-service的crud接口
        MPost post = this.getById(postId);
        // 使得这个自动过期
        // 7天后自动过期(15号发表，7-（18-15）=4)
        long between = DateUtil.between(new Date(), post.getCreated(), DateUnit.DAY);
        long expireTime = (7 - between) * 24 * 60 * 60; // 有效时间

        // 缓存这篇文章的基本信息
        this.hashCachePostIdAndTitle(post, expireTime);

        // 重新做并集
        this.zunionAndStoreLast7DayForWeekRank();
    }

    /**
    * 功能描述: 这里通过访问一次增加一次浏览量
    * @Date: 2021/2/19
    * @Author: Liduoan
    * @Param:
      * @param vo
    * @return: void
    */

    @Override
    public void putViewCount(PostVo vo) {
        //每一次浏览的时候，我们就写下这个浏览量的redis键值对
        String key = "rank:post:" + vo.getId();


        // 1、从缓存中获取viewcount
        Integer viewCount = (Integer) redisUtil.hget(key, "post:viewCount");

        // 可能在那个文章热议那边就已经有了
        // 2、如果没有，就先从实体里面获取，再加一
        if(viewCount != null) {
            vo.setViewCount(viewCount + 1);
        } else {
            vo.setViewCount(vo.getViewCount() + 1);
        }

        // 3、同步到缓存里面
        redisUtil.hset(key, "post:viewCount", vo.getViewCount());


    }


    /**
    * 功能描述: 缓存对应的文章信息
    * @Date: 2021/2/18
    * @Author: Liduoan
    * @Param:
      * @param post
     * @param expireTime
    * @return: void
    */


    private void hashCachePostIdAndTitle(MPost post, long expireTime) {
        String key = "rank:post:" + post.getId();
        boolean hasKey = redisUtil.hasKey(key);
        if(!hasKey) {

            redisUtil.hset(key, "post:id", post.getId(), expireTime);
            redisUtil.hset(key, "post:title", post.getTitle(), expireTime);
            //这里的评论数量是从数据库中获取的
//            redisUtil.hset(key, "post:commentCount", post.getCommentCount(), expireTime);
            redisUtil.hset(key, "post:viewCount", post.getViewCount(), expireTime);
        }

    }

    private void zunionAndStoreLast7DayForWeekRank() {
        String  currentKey = "day:rank:" + DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);

        String destKey = "week:rank";
        List<String> otherKeys = new ArrayList<>();
        for(int i=-6; i < 0; i++) {
            String temp = "day:rank:" +
                    DateUtil.format(DateUtil.offsetDay(new Date(), i), DatePattern.PURE_DATE_FORMAT);
            //把每一天的都放进去
            otherKeys.add(temp);
        }

        redisUtil.zUnionAndStore(currentKey, otherKeys, destKey);
    }
}
