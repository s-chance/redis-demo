package org.entropy.blogcomment.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_shop")
public class Shop {
    @TableId
    private Long id;
    private String name;
    private Integer typeId;
    private String images;
    private String area;
    private String address;
    private BigDecimal x;
    private BigDecimal y;
    private Integer avgPrice;
    private Integer sold;
    private Integer comments;
    private Byte score;
    @TableField(exist = false)
    private Double distance;
}
