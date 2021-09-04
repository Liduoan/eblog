package com.example.eblog.template;

import com.example.eblog.common.templates.DirectiveHandler;
import com.example.eblog.common.templates.TemplateDirective;
import com.example.eblog.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 本周热议
 */
@Component
public class HotsTemplate extends TemplateDirective {

    @Autowired
    RedisUtil redisUtil;

    @Override
    public String getName() {
        return "hots";
    }

    @Override
    public void execute(DirectiveHandler handler) throws Exception {
        String weekRankKey = "week:rank";

        Set<ZSetOperations.TypedTuple> typedTuples = redisUtil.getZSetRank(weekRankKey, 0, 6);

        List<Map> hotPosts = new ArrayList<>();

        for (ZSetOperations.TypedTuple typedTuple : typedTuples) {
            Map<String, Object> map = new HashMap<>();
            //week:rank里面是 redisUtil.zSet(key, post.getId(), post.getCommentCount());
            // id commentCount
            Object value = typedTuple.getValue(); // post的id
            String postKey = "rank:post:" + value;

            map.put("id", value);
            map.put("title", redisUtil.hget(postKey, "post:title"));
            //渲染的时候，这个评论数量是从redis中获取的
            //那么数据库和redis中的评论数量是不一致的
            map.put("commentCount", typedTuple.getScore());//是可以得到有序集合的score

            hotPosts.add(map);
        }

        handler.put(RESULTS, hotPosts).render();

    }
}
