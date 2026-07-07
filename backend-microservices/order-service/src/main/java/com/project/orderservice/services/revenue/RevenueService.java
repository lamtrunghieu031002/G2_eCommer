package com.project.orderservice.services.revenue;

import com.project.orderservice.repositories.OrderDetailRepository;
import com.project.orderservice.responses.ProductStatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueService {
    private final OrderDetailRepository orderDetailRepository;

    public Page<ProductStatResponse> getProductStat(LocalDate start, LocalDate end, PageRequest pageRequest) {
        Page<Object[]> page = orderDetailRepository.getProductStat(start, end, pageRequest);
        List<ProductStatResponse> responses = page.stream().map(row -> ProductStatResponse.builder()
                .productName((String) row[0])
                .quantitySold((Long) row[1])
                .revenue((Double) row[2])
                .build()).toList();
        return new PageImpl<>(responses, pageRequest, page.getTotalElements());
    }
}
