package com.vinaacademy.platform.feature.revenue.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class PayoutApprovalRequest {

	@NotNull
    private Long payoutRequestId;
    
    @NotNull
    private Boolean approved; // true = approve, false = reject
    
    private String rejectionReason; // Required if approved = false
    private String note;
}
