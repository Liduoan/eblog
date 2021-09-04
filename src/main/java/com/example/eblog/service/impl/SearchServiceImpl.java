package com.example.eblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.eblog.Vo.PostVo;
import com.example.eblog.entity.MPost;
import com.example.eblog.search.model.PostDocment;
import com.example.eblog.search.mq.PostMqIndexMessage;
import com.example.eblog.search.repository.PostRepository;
import com.example.eblog.service.MPostService;
import com.example.eblog.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    PostRepository postRepository;


    // 这里为了转实体而映入的  那么需要注入Bean
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    MPostService postService;

    @Override
    public IPage search(Page page, String keyword) {
        // 分页信息 mybatis plus的page 转成 jpa的page
        Long current = page.getCurrent() - 1;
        Long size = page.getSize();
        Pageable pageable = PageRequest.of(current.intValue(), size.intValue());

        //核心代码这两行 但是怎么转换两种page？
        // 搜索es得到pageData
        MultiMatchQueryBuilder multiMatchQueryBuilder =
                QueryBuilders.multiMatchQuery(keyword,
                "title", "authorName", "categoryName");

        org.springframework.data.domain.Page<PostDocment> docments
                = postRepository.search(multiMatchQueryBuilder, pageable);

        // 结果信息 jpa的pageData转成mybatis plus的pageData
        IPage pageData = new Page(page.getCurrent(), page.getSize(), docments.getTotalElements());
        pageData.setRecords(docments.getContent());
        return pageData;
    }

    @Override
    public int initEsData(List<PostVo> records) {
        if(records == null || records.isEmpty()) {
            return 0;
        }

        List<PostDocment> documents = new ArrayList<>();
        //批量存储 还是使用的postRepository的API
        for(PostVo vo : records) {
            // 映射转换
            PostDocment postDocment = modelMapper.map(vo, PostDocment.class);
            documents.add(postDocment);
        }
        postRepository.saveAll(documents);
        return documents.size();
    }
//
    @Override
    public void
    createOrUpdateIndex(PostMqIndexMessage message) {

        Long postId = message.getPostId();
        PostVo postVo = postService.selectOnePost(new QueryWrapper<MPost>().eq("p.id", postId));

        PostDocment postDocment = modelMapper.map(postVo, PostDocment.class);

        postRepository.save(postDocment);

        log.info("es 索引更新成功！ ---> {}", postDocment.toString());
    }

    @Override
    public void removeIndex(PostMqIndexMessage message) {
        Long postId = message.getPostId();

        postRepository.deleteById(postId);
        log.info("es 索引删除成功！ ---> {}", message.toString());
    }
}
