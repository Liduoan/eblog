package com.example.eblog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.eblog.Vo.PostVo;
import org.omg.CORBA.BAD_CONTEXT;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController extends BaseController{

    //开局直接使用Mapping 锁定在index上
    @RequestMapping({"", "/", "index"})
    public String index() {

        //因为主页上有两栏显示,所以不能直接使用post模板来帮助
        // 1分页信息 2分类 3用户 4置顶  5精选 6排序
        IPage<PostVo> results = mPostService.paging(getPage(), null, null, null, null, "created");

        req.setAttribute("pageData",results);
        //这个是为了高亮显示
        req.setAttribute("currentCategoryId",0);
//        System.out.println(results);

        return "index";
    }

    @RequestMapping("/search")
    public String search(String q) {

        IPage pageData = searchService.search(getPage(), q);

        req.setAttribute("q", q);
        req.setAttribute("pageData", pageData);
        return "search";
    }


}
