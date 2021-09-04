package com.example.eblog.template;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eblog.Vo.PostVo;
import com.example.eblog.common.templates.DirectiveHandler;
import com.example.eblog.common.templates.TemplateDirective;
import com.example.eblog.service.MPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostsTemplate extends TemplateDirective {

    //编写Template的插件

    @Autowired
    MPostService postService;

    @Override
    public String getName() {
        return "posts";
    }

    @Override
    public void execute(DirectiveHandler handler) throws Exception {
        //这里的参数是在ftl中输入的
        Integer level = handler.getInteger("level");
        Long categoryId = handler.getLong("categoryId");

        Integer pn = handler.getInteger("pn", 1);
        Integer size = handler.getInteger("size", 2);

        //通用查询文章记录
        IPage<PostVo> page = postService.paging(new Page(pn, size), categoryId, null, level, null, "created");
        //这里把sql返回的数据放入RESULTS
        handler.put(RESULTS, page).render();
    }
}
