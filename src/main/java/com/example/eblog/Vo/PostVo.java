package com.example.eblog.Vo;

import com.example.eblog.entity.MPost;
import lombok.Data;

@Data
public class PostVo extends MPost {
    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private String categoryName;
}
