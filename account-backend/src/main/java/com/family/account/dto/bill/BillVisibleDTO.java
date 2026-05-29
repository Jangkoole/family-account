package com.family.account.dto.bill;

import lombok.Data;
import java.util.List;

@Data
public class BillVisibleDTO {
    private List<Long> ids;
    private String visible;      // PRIVATE / FAMILY
}
