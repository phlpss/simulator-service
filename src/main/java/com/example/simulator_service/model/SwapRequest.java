package com.example.simulator_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwapRequest {
    private String id;
    private String fromBarista;
    private String toBarista;
    private String type;
    private long timestamp;
}
