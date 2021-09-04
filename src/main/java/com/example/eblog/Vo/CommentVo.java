package com.example.eblog.Vo;

        import com.example.eblog.entity.MComment;
        import lombok.Data;

@Data
public class CommentVo extends MComment {

    private Long authorId;
    private String authorName;
    private String authorAvatar;

}
