package com.example.eblog.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.eblog.entity.MCategory;
import com.example.eblog.service.MCategoryService;
import com.example.eblog.service.MPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.List;

@Component
public class ContextStartup implements ApplicationRunner, ServletContextAware {

    @Autowired
    MCategoryService mCategoryService;

    ServletContext servletContext;

    @Autowired
    MPostService mpostService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        /**
         * 这里的查询语句是Mybatis plus 的
         * QueryWrapper 继承自 AbstractWrapper
         * 自身的内部属性 entity 也用于生成 where 条件
         * 及 LambdaQueryWrapper, 可以通过 new QueryWrapper().lambda() 方法获取
         * */
        List<MCategory> categories =
                mCategoryService.list(new QueryWrapper<MCategory>()
                .eq("status", 0));
        servletContext.setAttribute("categorys", categories);
        mpostService.initWeekPoke();
//        System.out.println(categories);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {

        this.servletContext = servletContext;
    }
}
