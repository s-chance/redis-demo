package org.entropy.merchantquerycaching.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopType {
    private Long id;
    private String type;
    private String description;
}
