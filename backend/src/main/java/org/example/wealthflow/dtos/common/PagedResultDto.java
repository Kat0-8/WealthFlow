package org.example.wealthflow.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResultDto<T> {
    private List<T> items;
    private long total;
    private int page;
    private int size;
}