package com.example.eblog.Vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: Liduoan
 * @time: 2021/3/2
 */
public class MovieVo {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id; //主键
    private String movieName;//电影名字
    private Long likeSum; //喜欢数
    private Long score;//推分
    private Date created; //创建时间
    private List<String> region;//地区
    private List<String> stars;//主演
    private String type;//类型
    private String director;//导演


    public static void main(String[] args) {
        System.out.println(3^2);
    }

}
