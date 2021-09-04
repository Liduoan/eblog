package com.example.eblog.search.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(indexName="post", type="post", createIndex=true)
public class PostDocment implements Serializable {

    @Id
    private Long id;
    //一条记录的Id

    // ik分词器
    @Field(type = FieldType.Text, searchAnalyzer="ik_smart", analyzer = "ik_max_word")
    private String title;
    //文章的title

    @Field(type = FieldType.Long)
    private Long authorId;
    //作者的ID

    @Field(type = FieldType.Keyword)
    private String authorName;
    private String authorAvatar;
    //作者的名字 头像

    private Long categoryId;
    //分类ID
    @Field(type = FieldType.Keyword)
    private String categoryName;
    //分类的名称

    //置顶
    private Integer level;
    //精华
    private Boolean recomment;

    //评论数量
    private Integer commentCount;
    //阅读量
    private Integer viewCount;

    @Field(type = FieldType.Date)
    private Date created;
    //时间

}
