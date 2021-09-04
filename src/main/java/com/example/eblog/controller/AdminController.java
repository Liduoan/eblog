package com.example.eblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eblog.Vo.PostVo;
import com.example.eblog.common.lang.Result;
import com.example.eblog.entity.MPost;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @description:
 * @author: Liduoan
 * @time: 2021/2/23
 */
@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController{

    /**
     * 超级管理员操作删除  置顶  加精
     * @param id
     * @param rank 0表示取消，1表示操作
     * @param field
     * @return
     */
    @ResponseBody
    @PostMapping("/jie-set")
    public Result jetSet(Long id, Integer rank, String field) {

        MPost post = mPostService.getById(id);
        Assert.notNull(post, "该帖子已被删除");

        if("delete".equals(field)) {
            mPostService.removeById(id);
            return Result.success();

        } else if("status".equals(field)) {
            post.setRecommend(rank == 1);

        }  else if("stick".equals(field)) {
            post.setLevel(rank);
        }
        mPostService.updateById(post);
        return Result.success();
    }

    @ResponseBody
    @PostMapping("/initEsData")
    public Result initEsData() {

        int size = 10000;
        Page page = new Page();
        page.setSize(size);

        long total = 0;

        for (int i = 1; i < 1000; i ++) {
            //分页是当前页 记录条数
            //在sql中就是 limit （当前页-1）*记录条数，记录条数
            page.setCurrent(i);

            IPage<PostVo> paging = mPostService.paging(page, null, null, null, null, null);

            //从数据库中查出数据  把这些存入es
            int num = searchService.initEsData(paging.getRecords());

            total += num;
            //每次把10000条数据存入es
            // 当一页查不出10000条的时候，说明是最后一页了
            if(paging.getRecords().size() < size) {
                break;
            }
        }

        return Result.success("ES索引初始化成功，共 " + total + " 条记录！", null);
    }

}
