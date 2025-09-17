package com.firefly.ragdemo.VO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationVO {

    private Integer page;
    private Integer limit;
    private Long total;
    private Integer totalPages;
}
