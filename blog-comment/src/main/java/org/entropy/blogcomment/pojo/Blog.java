package org.entropy.blogcomment.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_blog")
public class Blog {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 商户id
     */
    private Long shopId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户头像
     */
    @TableField(exist = false)
    private String avatar;
    /**
     * 用户名称
     */
    @TableField(exist = false)
    private String name;
    /**
     * 标题
     */
    private String title;
    /**
     * 图片，最多九张，以 "," 隔开
     */
    private String images;
    /**
     * 笔记内容
     */
    private String content;
    /**
     * 点赞数量
     */
    private Integer liked;
    /**
     * 评论数量
     */
    private Integer comments;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
