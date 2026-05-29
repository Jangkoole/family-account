package com.family.account.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.account.common.Result;
import com.family.account.dto.bill.*;
import com.family.account.entity.Bill;
import com.family.account.mapper.BillMapper;
import com.family.account.service.BillService;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;

    // 获取当前登录用户ID
    private Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    // 获取当前用户的家庭ID，若未加入家庭则返回null
    private Long getFamilyId() {
        return billMapper.selectFamilyIdByUserId(getCurrentUserId());
    }

    // 获取当前用户在家庭中的角色
    private String getFamilyRole() {
        return billMapper.selectFamilyRoleByUserId(getCurrentUserId());
    }

    // 解析日期字符串
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank())
            return null;
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // 将 Bill 实体转为 Map，用于返回给前端
    private Map<String, Object> billToMap(Bill bill) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", bill.getId());
        map.put("type", bill.getType());
        map.put("categoryId", bill.getCategoryId());
        map.put("categoryName", bill.getCategoryName());
        map.put("amount", bill.getAmount());
        map.put("date", bill.getDate() != null ? bill.getDate().toString() : null);
        map.put("note", bill.getNote());
        map.put("visible", bill.getVisible());
        // 如果是家庭管理员查询，可能包含 nickname
        if (bill.getNickname() != null) {
            map.put("nickname", bill.getNickname());
        }
        return map;
    }

    @Override
    @Transactional
    public Result add(BillAddDTO dto) {
        Long userId = getCurrentUserId();
        Long familyId = getFamilyId(); // 可能为 null

        Bill bill = new Bill();
        bill.setUserId(userId);
        bill.setFamilyId(familyId);
        bill.setCategoryId(dto.getCategoryId());
        bill.setType(dto.getType());
        bill.setAmount(dto.getAmount());
        bill.setDate(parseDate(dto.getDate()));
        bill.setNote(dto.getNote());

        // 可见性处理：家庭成员可设置，否则默认 PRIVATE
        if (familyId != null && dto.getVisible() != null) {
            bill.setVisible(dto.getVisible());
        } else {
            bill.setVisible("PRIVATE");
        }

        billMapper.insert(bill);
        return Result.success(Map.of("id", bill.getId()));
    }

    @Override
    @Transactional
    public Result update(BillUpdateDTO dto) {
        Long userId = getCurrentUserId();
        Bill bill = billMapper.selectById(dto.getId());
        if (bill == null || !bill.getUserId().equals(userId)) {
            return Result.error(403, "无权修改此记录");
        }

        bill.setType(dto.getType());
        bill.setCategoryId(dto.getCategoryId());
        bill.setAmount(dto.getAmount());
        bill.setDate(parseDate(dto.getDate()));
        bill.setNote(dto.getNote());

        // 只有家庭记录且用户属于家庭组时，才允许修改可见性
        if (bill.getFamilyId() != null && getFamilyId() != null && dto.getVisible() != null) {
            bill.setVisible(dto.getVisible());
        }

        billMapper.updateById(bill);
        return Result.success();
    }

    @Override
    @Transactional
    public Result delete(Long id) {
        Long userId = getCurrentUserId();
        Bill bill = billMapper.selectById(id);
        if (bill == null || !bill.getUserId().equals(userId)) {
            return Result.error(403, "无权删除此记录");
        }
        billMapper.deleteById(id);
        return Result.success();
    }

    @Override
    public Result list(BillQueryDTO dto) {
        Long userId = getCurrentUserId();
        Long familyId = getFamilyId();

        Page<Bill> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<Bill> resultPage = billMapper.selectBillPage(
                page, userId, familyId,
                parseDate(dto.getStartDate()), parseDate(dto.getEndDate()),
                dto.getType(), dto.getCategoryId(), dto.getVisible(),
                dto.getMinAmount(), dto.getMaxAmount());

        List<Map<String, Object>> list = new ArrayList<>();
        for (Bill bill : resultPage.getRecords()) {
            list.add(billToMap(bill));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", resultPage.getTotal());
        data.put("page", dto.getPage());
        data.put("pageSize", dto.getPageSize());
        data.put("list", list);
        return Result.success(data);
    }

    @Override
    public Result detail(Long id) {
        Bill bill = billMapper.selectDetailById(id);
        if (bill == null) {
            return Result.error(404, "记录不存在");
        }
        Long userId = getCurrentUserId();
        // 如果不是自己的记录，检查是否是家庭管理员且属于同一家庭组
        if (!bill.getUserId().equals(userId)) {
            if (!"ADMIN".equals(getFamilyRole()) || !bill.getFamilyId().equals(getFamilyId())) {
                return Result.error(403, "无权查看此记录");
            }
        }
        return Result.success(billToMap(bill));
    }

    @Override
    @Transactional
    public Result updateVisible(BillVisibleDTO dto) {
        Long userId = getCurrentUserId();
        Long familyId = getFamilyId();
        List<Bill> bills = billMapper.selectBatchIds(dto.getIds());
        for (Bill bill : bills) {
            // 只能修改自己的记录
            if (!bill.getUserId().equals(userId)) {
                return Result.error(403, "包含无权修改的记录");
            }
            // 只有家庭组内的记录才能修改可见性
            if (bill.getFamilyId() == null || !bill.getFamilyId().equals(familyId)) {
                return Result.error(400, "记录不属于当前家庭组，无法修改可见性");
            }
            bill.setVisible(dto.getVisible());
            billMapper.updateById(bill);
        }
        return Result.success();
    }

    @Override
    @Transactional
    public Result importBills(MultipartFile file, String source) {
        List<Map<String, Object>> failReasons = new ArrayList<>();
        int successCount = 0;

        try {
            List<Bill> bills;
            if (source.equalsIgnoreCase("SYSTEM")) {
                // 系统模板，这里假设是 xlsx 文件
                bills = parseSystemTemplate(file);
            } else if (source.equalsIgnoreCase("WECHAT")) {
                bills = parseWechatCsv(file);
            } else if (source.equalsIgnoreCase("ALIPAY")) {
                bills = parseAlipayCsv(file);
            } else {
                return Result.error(400, "不支持的文件来源");
            }

            // 逐条校验并保存
            Long userId = getCurrentUserId();
            Long familyId = getFamilyId();
            for (int i = 0; i < bills.size(); i++) {
                try {
                    Bill bill = bills.get(i);
                    bill.setUserId(userId);
                    bill.setFamilyId(familyId);

                    // 根据分类名称获取分类ID
                    if (bill.getCategoryName() != null && !bill.getCategoryName().isEmpty()) {
                        Long categoryId = getCategoryIdByName(bill.getCategoryName());
                        if (categoryId == null) {
                            throw new IllegalArgumentException("分类名称不存在: " + bill.getCategoryName());
                        }
                        bill.setCategoryId(categoryId);
                    } else {
                        throw new IllegalArgumentException("分类名称不能为空");
                    }

                    // 如果解析时已经设置了可见性，则使用解析的值
                    if (bill.getVisible() == null || bill.getVisible().isEmpty()) {
                        bill.setVisible("PRIVATE");
                    }
                    // 简单校验
                    if (bill.getType() == null
                            || (!bill.getType().equals("INCOME") && !bill.getType().equals("EXPENSE"))) {
                        throw new IllegalArgumentException("收支类型错误");
                    }
                    if (bill.getAmount() == null || bill.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("金额必须大于0");
                    }
                    if (bill.getDate() == null) {
                        throw new IllegalArgumentException("日期不能为空");
                    }
                    billMapper.insert(bill);
                    successCount++;
                } catch (Exception e) {
                    Map<String, Object> fail = new LinkedHashMap<>();
                    fail.put("row", i + 2); // Excel 行号从 2 开始（标题行是第1行）
                    fail.put("reason", e.getMessage());
                    failReasons.add(fail);
                }
            }
        } catch (Exception e) {
            return Result.error(400, "文件解析失败: " + e.getMessage());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("successCount", successCount);
        data.put("failCount", failReasons.size());
        data.put("failReasons", failReasons);
        return Result.success(data);
    }

    @Override
    public Result previewImport(MultipartFile file, String source) {
        try {
            List<Map<String, Object>> previewList = new ArrayList<>();
            Map<String, String> fieldMapping = new LinkedHashMap<>();
            int totalCount = 0;

            if (source.equalsIgnoreCase("SYSTEM")) {
                // 系统模板预览，解析前5条
                List<Bill> bills = parseSystemTemplate(file);
                totalCount = bills.size();
                for (int i = 0; i < Math.min(5, bills.size()); i++) {
                    Bill b = bills.get(i);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type", b.getType());
                    item.put("categoryName", b.getCategoryName());
                    item.put("amount", b.getAmount());
                    item.put("date", b.getDate() != null ? b.getDate().toString() : null);
                    item.put("note", b.getNote());
                    previewList.add(item);
                }
                fieldMapping.put("交易时间", "date");
                fieldMapping.put("交易类型", "type");
                fieldMapping.put("金额", "amount");
                fieldMapping.put("分类", "categoryName");
                fieldMapping.put("备注", "note");
            } else if (source.equalsIgnoreCase("WECHAT")) {
                List<Bill> bills = parseWechatCsv(file);
                totalCount = bills.size();
                for (int i = 0; i < Math.min(5, bills.size()); i++) {
                    Bill b = bills.get(i);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type", b.getType());
                    item.put("categoryName", b.getCategoryName());
                    item.put("amount", b.getAmount());
                    item.put("date", b.getDate() != null ? b.getDate().toString() : null);
                    item.put("note", b.getNote());
                    previewList.add(item);
                }
                fieldMapping.put("交易时间", "date");
                fieldMapping.put("交易类型", "type");
                fieldMapping.put("商品", "note");
            } else if (source.equalsIgnoreCase("ALIPAY")) {
                List<Bill> bills = parseAlipayCsv(file);
                totalCount = bills.size();
                for (int i = 0; i < Math.min(5, bills.size()); i++) {
                    Bill b = bills.get(i);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type", b.getType());
                    item.put("categoryName", b.getCategoryName());
                    item.put("amount", b.getAmount());
                    item.put("date", b.getDate() != null ? b.getDate().toString() : null);
                    item.put("note", b.getNote());
                    previewList.add(item);
                }
                fieldMapping.put("记录时间", "date");
                fieldMapping.put("收支类型", "type");
                fieldMapping.put("分类", "categoryName");
                fieldMapping.put("备注", "note");
            } else {
                return Result.error(400, "不支持的文件来源");
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("totalCount", totalCount);
            data.put("previewList", previewList);
            data.put("fieldMapping", fieldMapping);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(400, "预览失败: " + e.getMessage());
        }
    }

    @Override
    public Result familyList(BillQueryDTO dto, Integer userId) {
        // 仅家庭管理员可调用
        if (!"ADMIN".equals(getFamilyRole())) {
            return Result.error(403, "仅家庭管理员可查看成员记录明细");
        }
        Long familyId = getFamilyId();
        if (familyId == null) {
            return Result.error(400, "当前用户未加入任何家庭组");
        }

        Page<Bill> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<Bill> resultPage = billMapper.selectFamilyBillPage(
                page, familyId,
                userId != null ? Long.valueOf(userId) : null,
                parseDate(dto.getStartDate()), parseDate(dto.getEndDate()),
                dto.getType(), dto.getCategoryId());

        // 获取当前用户ID（排除自己的记录）
        Long currentUserId = StpUtil.getLoginIdAsLong();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Bill bill : resultPage.getRecords()) {
            // 排除当前用户自己的记录，只显示家庭成员的记录
            if (!currentUserId.equals(bill.getUserId())) {
                Map<String, Object> map = billToMap(bill);
                // 家庭管理员查询时附带用户信息
                map.put("userId", bill.getUserId());
                map.put("nickname", bill.getNickname());
                list.add(map);
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", list.size()); // 返回过滤后的总数
        data.put("page", dto.getPage());
        data.put("pageSize", dto.getPageSize());
        data.put("list", list);
        return Result.success(data);
    }

    // ==================== 文件解析私有方法 ====================

    /**
     * 解析系统标准模板 Excel
     * 假设模板列顺序：交易时间, 交易类型, 分类, 金额, 备注
     */
    private List<Bill> parseSystemTemplate(MultipartFile file) throws Exception {
        List<Bill> bills = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;
            Bill bill = new Bill();
            // 第一列：日期
            Cell dateCell = row.getCell(0);
            if (dateCell != null) {
                if (dateCell.getCellType() == CellType.NUMERIC) {
                    bill.setDate(dateCell.getLocalDateTimeCellValue().toLocalDate());
                } else {
                    bill.setDate(LocalDate.parse(dateCell.getStringCellValue(), DateTimeFormatter.ISO_LOCAL_DATE));
                }
            }
            // 第二列：类型
            Cell typeCell = row.getCell(1);
            if (typeCell != null) {
                bill.setType(typeCell.getStringCellValue().trim());
            }
            // 第三列：分类名称（临时存到 categoryName 属性，后续需映射到 categoryId）
            Cell catCell = row.getCell(2);
            if (catCell != null) {
                bill.setCategoryName(catCell.getStringCellValue().trim());
            }
            // 第四列：金额
            Cell amountCell = row.getCell(3);
            if (amountCell != null) {
                if (amountCell.getCellType() == CellType.NUMERIC) {
                    bill.setAmount(BigDecimal.valueOf(amountCell.getNumericCellValue()));
                } else {
                    bill.setAmount(new BigDecimal(amountCell.getStringCellValue().trim()));
                }
            }
            // 第五列：可见范围（选填）
            Cell visibleCell = row.getCell(4);
            if (visibleCell != null) {
                String visible = visibleCell.getStringCellValue().trim();
                if ("PRIVATE".equalsIgnoreCase(visible) || "FAMILY".equalsIgnoreCase(visible)) {
                    bill.setVisible(visible.toUpperCase());
                } else {
                    bill.setVisible("PRIVATE"); // 默认仅自己可见
                }
            } else {
                bill.setVisible("PRIVATE"); // 默认仅自己可见
            }
            // 第六列：备注
            Cell noteCell = row.getCell(5);
            if (noteCell != null) {
                bill.setNote(noteCell.getStringCellValue().trim());
            }
            bills.add(bill);
        }
        workbook.close();
        return bills;
    }

    /**
     * 解析微信账单 CSV
     * 假设列顺序：交易时间, 交易类型, 商品, 金额(元), ...
     */
    private List<Bill> parseWechatCsv(MultipartFile file) throws Exception {
        List<Bill> bills = new ArrayList<>();
        CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
        String[] line;
        boolean skipHeader = true;
        while ((line = reader.readNext()) != null) {
            if (skipHeader) {
                skipHeader = false;
                continue;
            }
            if (line.length < 4)
                continue;
            Bill bill = new Bill();
            try {
                bill.setDate(LocalDate.parse(line[0].trim(), DateTimeFormatter.ofPattern("yyyyMMdd")));
            } catch (DateTimeParseException e) {
                // 尝试其他格式
                bill.setDate(LocalDate.parse(line[0].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
            }
            String wechatType = line[1].trim();
            if (wechatType.contains("支出")) {
                bill.setType("EXPENSE");
            } else if (wechatType.contains("收入")) {
                bill.setType("INCOME");
            }
            bill.setNote(line[2].trim());
            String amountStr = line[3].trim().replace("¥", "").replace(",", "");
            if (!amountStr.isEmpty()) {
                bill.setAmount(new BigDecimal(amountStr));
            }
            bills.add(bill);
        }
        reader.close();
        return bills;
    }

    /**
     * 解析支付宝账单 CSV
     * 假设列顺序：记录时间, 商品名称, 金额, 收支类型, 分类, 备注, ...
     */
    private List<Bill> parseAlipayCsv(MultipartFile file) throws Exception {
        List<Bill> bills = new ArrayList<>();
        CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
        String[] line;
        boolean skipHeader = true;
        while ((line = reader.readNext()) != null) {
            if (skipHeader) {
                skipHeader = false;
                continue;
            }
            if (line.length < 6)
                continue;
            Bill bill = new Bill();
            try {
                bill.setDate(LocalDate.parse(line[0].trim(), DateTimeFormatter.ofPattern("yyyyMMdd")));
            } catch (DateTimeParseException e) {
                bill.setDate(LocalDate.parse(line[0].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
            }
            // 支付宝的收支类型在第4列（索引3）
            String alipayType = line[3].trim();
            if (alipayType.contains("收入")) {
                bill.setType("INCOME");
            } else if (alipayType.contains("支出")) {
                bill.setType("EXPENSE");
            }
            // 分类在第5列（索引4）
            bill.setCategoryName(line[4].trim());
            // 金额在第3列（索引2）
            String amountStr = line[2].trim().replace("¥", "").replace(",", "");
            if (!amountStr.isEmpty()) {
                bill.setAmount(new BigDecimal(amountStr));
            }
            // 备注在第6列（索引5）
            bill.setNote(line[5].trim());
            bills.add(bill);
        }
        reader.close();
        return bills;
    }

    /**
     * 根据分类名称获取分类ID
     */
    private Long getCategoryIdByName(String categoryName) {
        Map<String, Long> categoryMap = new HashMap<>();
        categoryMap.put("餐饮", 1L);
        categoryMap.put("交通", 2L);
        categoryMap.put("购物", 3L);
        categoryMap.put("住房", 4L);
        categoryMap.put("医疗", 5L);
        categoryMap.put("教育", 6L);
        categoryMap.put("娱乐", 7L);
        categoryMap.put("其他支出", 8L);
        categoryMap.put("工资", 9L);
        categoryMap.put("奖金", 10L);
        categoryMap.put("理财收益", 11L);
        categoryMap.put("其他收入", 12L);
        return categoryMap.get(categoryName);
    }
}