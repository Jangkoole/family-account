package com.family.account.service;

import com.family.account.common.Result;
import com.family.account.dto.bill.*;
import org.springframework.web.multipart.MultipartFile;

public interface BillService {

    Result add(BillAddDTO dto);

    Result update(BillUpdateDTO dto);

    Result delete(Long id);

    Result deleteBatch(java.util.List<Long> ids);

    Result list(BillQueryDTO dto);

    Result detail(Long id);

    Result updateVisible(BillVisibleDTO dto);

    Result importBills(String previewId);

    Result previewImport(MultipartFile file, String source);

    Result familyList(BillQueryDTO dto, Integer userId);
}