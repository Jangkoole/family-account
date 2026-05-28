package com.family.account.controller;

import com.family.account.common.Result;
import com.family.account.dto.bill.*;
import com.family.account.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    // 3.1 新增收支记录
    @PostMapping("/add")
    public Result add(@Valid @RequestBody BillAddDTO dto) {
        return billService.add(dto);
    }

    // 3.2 修改收支记录
    @PutMapping("/update")
    public Result update(@Valid @RequestBody BillUpdateDTO dto) {
        return billService.update(dto);
    }

    // 3.3 删除收支记录
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return billService.delete(id);
    }

    // 3.4 查询收支记录列表
    @GetMapping("/list")
    public Result list(@Valid BillQueryDTO dto) {
        return billService.list(dto);
    }

    // 3.5 获取收支记录详情
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return billService.detail(id);
    }

    // 3.6 修改记录可见范围
    @PutMapping("/visible")
    public Result updateVisible(@Valid @RequestBody BillVisibleDTO dto) {
        return billService.updateVisible(dto);
    }

    // 3.7 批量导入收支记录
    @PostMapping("/import")
    public Result importBills(@RequestParam("file") MultipartFile file,
                              @RequestParam("source") String source) {
        return billService.importBills(file, source);
    }

    // 3.8 预览导入映射结果
    @PostMapping("/import/preview")
    public Result previewImport(@RequestParam("file") MultipartFile file,
                                @RequestParam("source") String source) {
        return billService.previewImport(file, source);
    }

    // 3.9 家庭管理员查询成员记录明细
    @GetMapping("/family/list")
    public Result familyList(@Valid BillQueryDTO dto,
                             @RequestParam(required = false) Integer userId) {
        return billService.familyList(dto, userId);
    }
}