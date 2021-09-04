package com.example.eblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eblog.entity.MUserMessage;
import com.example.eblog.service.*;
import com.example.eblog.shiro.AccountProfile;
import org.apache.shiro.SecurityUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;

public class BaseController {

    @Autowired
    HttpServletRequest req;

    @Autowired
    MPostService mPostService;

    @Autowired
    MCommentService mCommentService;


    @Autowired
    MUserService userService;

    @Autowired
    MUserMessageService messageService;

    @Autowired
    MUserCollectionService collectionService;


    @Autowired
    MCategoryService categoryService;

    @Autowired
    WsService wsService;

    @Autowired
    SearchService searchService;

    @Autowired
    AmqpTemplate amqpTemplate;


    public Page getPage() {
        //通过Get传参得到当前页面和每页大小
        //当前页面 页面大小
        int pn = ServletRequestUtils.getIntParameter(req, "pn", 1);
        int size = ServletRequestUtils.getIntParameter(req, "size", 2);
        return new Page(pn, size);
    }

    protected AccountProfile getProfile() {

        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

    protected Long getProfileId() {

        return getProfile().getId();
    }
}
