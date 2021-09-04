package com.example.eblog.schedules;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.eblog.entity.MPost;
import com.example.eblog.service.MPostService;
import com.example.eblog.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ViewCountSyncTask {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MPostService postService;

    /**
    * 功能描述:这里是为了存入数据库 做定时任务
    * @Date: 2021/2/19
    * @Author: Liduoan
    * @Param:
      * @param
    * @return: void
    */

    @Scheduled(cron = "0/5 * * * * *") //每分钟同步
    public void task() {

        Set<String> keys = redisTemplate.keys("rank:post:*");

        //更新的是浏览量
        //也就是rank:post的浏览量
        List<String> ids = new ArrayList<>();
        for (String key : keys) {
            //如果这个key里面有浏览量键值对 那么记下这个文章的id
            if(redisUtil.hHasKey(key, "post:viewCount")){
                ids.add(key.substring("rank:post:".length()));
            }
        }

        if(ids.isEmpty()) return;

        // 需要更新阅读量的文章
        List<MPost> posts = postService.list(new QueryWrapper<MPost>().in("id", ids));

        posts.stream().forEach((post) ->{
            //获取文章的东西
            Integer viewCount = (Integer) redisUtil.hget("rank:post:" + post.getId(), "post:viewCount");
            post.setViewCount(viewCount);
        });

        if(posts.isEmpty()) return;

        boolean isSucc = postService.updateBatchById(posts);

        //为了防止又得重复的提交 删除那些文章的记录
        if(isSucc) {
            ids.stream().forEach((id) -> {
                redisUtil.hdel("rank:post:" + id, "post:viewCount");
                System.out.println(id + "---------------------->同步成功");
            });
        }
    }

}
