package com.example.eblog.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.eblog.Vo.CommentVo;
import com.example.eblog.Vo.PostVo;
import com.example.eblog.common.lang.Result;
import com.example.eblog.config.RabbitConfig;
import com.example.eblog.entity.*;
import com.example.eblog.search.mq.PostMqIndexMessage;
import com.example.eblog.util.ValidationUtil;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
public class PostController extends BaseController{

    /***
     * \\d*好像是那个表达式
     */
    @GetMapping("/category/{id:\\d*}")
    public String category(@PathVariable(name = "id") Long id) {
        //高亮显示
        req.setAttribute("currentCategoryId",id);
        // 防止没有pn直接进来
        // 参数是在分页机制那里出来的
        int pn = ServletRequestUtils.getIntParameter(req, "pn", 1);
        //这里纯粹就是显示记录 那么和上文的index显示的应该是差不多的
        req.setAttribute("pn", pn);
        return "post/category";
    }

    //文章展示
    @GetMapping("/post/{id:\\d*}")
    public String detail(@PathVariable(name = "id") Long id) {

        //在这里嵌入条件，得到文章的具体信息
        //我认为不使用同一函数来获取是因为返回值不同
        PostVo vo = mPostService.selectOnePost(new QueryWrapper<MPost>().eq("p.id", id));
        Assert.notNull(vo, "文章已被删除");

        // 得到文章的相应评论
        // 1分页，2文章id，3用户id，排序
        // 这里只是使用了文章的id值 但是为了通用所以才增加了用户ID
        IPage<CommentVo> results = mCommentService.paing(getPage(), vo.getId(), null, "created");

        //高亮显示
        req.setAttribute("currentCategoryId",id);

        req.setAttribute("post",vo);
        req.setAttribute("pageData", results);

        //阅读量刷新机制
        mPostService.putViewCount(vo);

        return "post/detail";
    }


    /**
     * 判断用户是否收藏了文章
     * @param pid
     * @return
     */
    @ResponseBody
    @PostMapping("/collection/find/")
    public Result collectionFind(Long pid) {
        int count = collectionService.count(new QueryWrapper<MUserCollection>()
                .eq("user_id", getProfileId())
                .eq("post_id", pid)
        );
        //count>0  => true
        return Result.success(MapUtil.of("collection", count > 0 ));
    }

    /**
    * 功能描述: 这个是用于添加收藏的
    * @Date: 2021/2/23
    * @Author: Liduoan
    * @Param:
      * @param pid
    * @return: com.example.eblog.common.lang.Result
    */

    @ResponseBody
    @PostMapping("/collection/add/")
    public Result collectionAdd(Long pid) {

        MPost post = mPostService.getById(pid);

        Assert.isTrue(post != null, "改帖子已被删除");

        int count = collectionService.count(new QueryWrapper<MUserCollection>()
                .eq("user_id", getProfileId())
                .eq("post_id", pid)
        );
        if(count > 0) {
            return Result.fail("你已经收藏");
        }

        MUserCollection collection = new MUserCollection();
        collection.setUserId(getProfileId());
        collection.setPostId(pid);
        collection.setCreated(new Date());
        collection.setModified(new Date());

        collection.setPostUserId(post.getUserId());

        collectionService.save(collection);
        return Result.success();
    }

    @ResponseBody
    @PostMapping("/collection/remove/")
    public Result collectionRemove(Long pid) {
        MPost post = mPostService.getById(pid);
        Assert.isTrue(post != null, "改帖子已被删除");

        collectionService.remove(new QueryWrapper<MUserCollection>()
                .eq("user_id", getProfileId())
                .eq("post_id", pid));

        return Result.success();
    }



    /**
    * 功能描述: 编辑文章
    * @Date: 2021/2/23
    * @Author: Liduoan
    * @Param:
      * @param
    * @return: java.lang.String
    */

    @GetMapping("/post/edit")
    public String edit(){

        String id = req.getParameter("id");

        if(!StringUtils.isEmpty(id)) {
            MPost post = mPostService.getById(id);
            Assert.isTrue(post != null, "改帖子已被删除");
            Assert.isTrue(post.getUserId().longValue() == getProfileId().longValue(), "没权限操作此文章");
            req.setAttribute("post", post);
        }

        req.setAttribute("categories", categoryService.list());
        return "/post/edit";
    }


    /**
    * 功能描述: 发布文章
    * @Date: 2021/2/23
    * @Author: Liduoan
    * @Param:
      * @param post
    * @return: com.example.eblog.common.lang.Result
    */

    @ResponseBody
    @PostMapping("/post/submit")
    public Result submit(MPost post) {

        ValidationUtil.ValidResult validResult = ValidationUtil.validateBean(post);

        if(validResult.hasErrors()) {
            return Result.fail(validResult.getErrors());
        }


        //看是发表新文章还是更改旧文章
        if(post.getId() == null) {
            post.setUserId(getProfileId());

            post.setModified(new Date());
            post.setCreated(new Date());
            post.setEditMode(null);
            //这里是置顶和精帖
            post.setCommentCount(0);
            post.setLevel(0);
            post.setRecommend(false);
            post.setViewCount(0);
            post.setVoteDown(0);
            post.setVoteUp(0);
            mPostService.save(post);

        } else {
            MPost tempPost = mPostService.getById(post.getId());
            Assert.isTrue(tempPost.getUserId().longValue() == getProfileId().longValue(), "无权限编辑此文章！");

            tempPost.setTitle(post.getTitle());
            tempPost.setContent(post.getContent());
            tempPost.setCategoryId(post.getCategoryId());
            mPostService.updateById(tempPost);
        }

        // 通知消息给mq，告知更新或添加
        amqpTemplate.convertAndSend(RabbitConfig.es_exchage, RabbitConfig.es_bind_key,
                new PostMqIndexMessage(post.getId(), PostMqIndexMessage.CREATE_OR_UPDATE));

        return Result.success().action("/post/" + post.getId());
    }


    @ResponseBody
    @Transactional
    @PostMapping("/post/delete")
    public Result delete(Long id) {

        MPost post = mPostService.getById(id);
        Assert.notNull(post, "该帖子已被删除");
        Assert.isTrue(post.getUserId().longValue() == getProfileId().longValue(), "无权限删除此文章！");

        mPostService.removeById(id);

        // 删除相关消息、收藏等
        messageService.removeByMap(MapUtil.of("post_id", id));
        collectionService.removeByMap(MapUtil.of("post_id", id));

        amqpTemplate.convertAndSend(RabbitConfig.es_exchage, RabbitConfig.es_bind_key,
                new PostMqIndexMessage(post.getId(), PostMqIndexMessage.REMOVE));

        return Result.success().action("/user/index");
    }


    @ResponseBody
    @Transactional
    @PostMapping("/post/reply/")
    public Result reply(Long jid, String content) {

        Assert.notNull(jid, "找不到对应的文章");
        Assert.hasLength(content, "评论内容不能为空");

        MPost post = mPostService.getById(jid);
        Assert.isTrue(post != null, "该文章已被删除");

        MComment comment = new MComment();
        comment.setPostId(jid);
        comment.setContent(content);
        comment.setUserId(getProfileId());
        comment.setCreated(new Date());
        comment.setModified(new Date());
        comment.setLevel(0);
        comment.setVoteDown(0);
        comment.setVoteUp(0);
        mCommentService.save(comment);

        // 评论数量加一
        post.setCommentCount(post.getCommentCount() + 1);
        mPostService.updateById(post);

        // 本周热议数量加一
        mPostService.incrCommentCountAndUnionForWeekRank(post.getId(), true);

        // 通知作者，有人评论了你的文章
        // 作者自己评论自己文章，不需要通知
        if(comment.getUserId() != post.getUserId()) {
            MUserMessage message = new MUserMessage();
            message.setPostId(jid);
            message.setCommentId(comment.getId());
            message.setFromUserId(getProfileId());
            message.setToUserId(post.getUserId());
            message.setType(1);
            message.setContent(content);
            message.setCreated(new Date());
            message.setStatus(0);
            messageService.save(message);

//             即时通知作者（websocket）
            wsService.sendMessCountToUser(message.getToUserId());
        }

        // 通知被@的人，有人回复了你的文章
        if(content.startsWith("@")) {
            String username = content.substring(1, content.indexOf(" "));
            System.out.println(username);

            MUser user = userService.getOne(new QueryWrapper<MUser>().eq("username", username));
            if(user != null) {
                MUserMessage message = new MUserMessage();
                message.setPostId(jid);
                message.setCommentId(comment.getId());
                message.setFromUserId(getProfileId());
                message.setToUserId(user.getId());
                message.setType(2);
                message.setContent(content);
                message.setCreated(new Date());
                message.setStatus(0);
                messageService.save(message);

                // 即时通知被@的用户
            }
        }
//
        return Result.success().action("/post/" + post.getId());
    }

}
