package com.family.account.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.family.account.common.Result;
import com.family.account.dto.bill.*;
import com.family.account.entity.Bill;
import com.family.account.mapper.BillMapper;
import com.family.account.service.BillService;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;

    private static final Map<String, List<Bill>> PREVIEW_DATA_CACHE = new ConcurrentHashMap<>();

    private static final long PREVIEW_CACHE_EXPIRE_MS = 30 * 60 * 1000;

    private static final Map<String, Long> PREVIEW_CREATE_TIME = new ConcurrentHashMap<>();

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

        List<Bill> allBills = billMapper.selectBillListWithoutPage(
                userId, familyId,
                parseDate(dto.getStartDate()), parseDate(dto.getEndDate()),
                dto.getType(), dto.getCategoryId(), dto.getVisible(),
                dto.getMinAmount(), dto.getMaxAmount(), dto.getOrderBy());

        int total = allBills.size();
        int page = dto.getPage() != null ? dto.getPage() : 1;
        int pageSize = dto.getPageSize() != null ? dto.getPageSize() : 20;
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<Bill> pagedBills = fromIndex < total
                ? allBills.subList(fromIndex, toIndex)
                : Collections.emptyList();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Bill bill : pagedBills) {
            list.add(billToMap(bill));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
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
    public Result importBills(String previewId) {
        List<Bill> bills = PREVIEW_DATA_CACHE.remove(previewId);
        PREVIEW_CREATE_TIME.remove(previewId);
        if (bills == null) {
            return Result.error(400, "预览数据已过期，请重新上传");
        }

        int successCount = 0;
        List<Map<String, Object>> failReasons = new ArrayList<>();
        Long userId = getCurrentUserId();
        Long familyId = getFamilyId();

        for (int i = 0; i < bills.size(); i++) {
            try {
                Bill bill = bills.get(i);
                bill.setUserId(userId);
                bill.setFamilyId(familyId);

                if (bill.getCategoryName() != null && !bill.getCategoryName().isEmpty()) {
                    Long categoryId = getCategoryIdByName(bill.getCategoryName());
                    if (categoryId == null) {
                        throw new IllegalArgumentException("分类名称不存在: " + bill.getCategoryName());
                    }
                    bill.setCategoryId(categoryId);
                } else {
                    throw new IllegalArgumentException("分类名称不能为空");
                }

                if (bill.getVisible() == null || bill.getVisible().isEmpty()) {
                    bill.setVisible("PRIVATE");
                }
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
                fail.put("row", i + 2);
                fail.put("reason", e.getMessage());
                failReasons.add(fail);
            }
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
            List<Bill> bills;
            if (source.equalsIgnoreCase("SYSTEM")) {
                bills = parseSystemTemplate(file);
            } else if (source.equalsIgnoreCase("WECHAT")) {
                bills = parseWechatCsv(file);
            } else if (source.equalsIgnoreCase("ALIPAY")) {
                bills = parseAlipayCsv(file);
            } else {
                return Result.error(400, "不支持的文件来源");
            }

            String previewId = UUID.randomUUID().toString().replace("-", "");
            PREVIEW_DATA_CACHE.put(previewId, bills);
            PREVIEW_CREATE_TIME.put(previewId, System.currentTimeMillis());

            List<Map<String, Object>> previewList = new ArrayList<>();
            for (int i = 0; i < Math.min(5, bills.size()); i++) {
                Bill b = bills.get(i);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type", b.getType());
                item.put("categoryName", b.getCategoryName());
                item.put("amount", b.getAmount());
                item.put("date", b.getDate() != null ? b.getDate().toString() : null);
                item.put("visible", b.getVisible());
                item.put("note", b.getNote());
                previewList.add(item);
            }

            Map<String, String> fieldMapping = new LinkedHashMap<>();
            if (source.equalsIgnoreCase("SYSTEM")) {
                fieldMapping.put("交易时间", "date");
                fieldMapping.put("交易类型", "type");
                fieldMapping.put("金额", "amount");
                fieldMapping.put("分类", "categoryName");
                fieldMapping.put("备注", "note");
            } else if (source.equalsIgnoreCase("WECHAT")) {
                fieldMapping.put("交易时间", "date");
                fieldMapping.put("交易类型", "type");
                fieldMapping.put("商品", "note");
            } else if (source.equalsIgnoreCase("ALIPAY")) {
                fieldMapping.put("记录时间", "date");
                fieldMapping.put("收支类型", "type");
                fieldMapping.put("分类", "categoryName");
                fieldMapping.put("备注", "note");
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("previewId", previewId);
            data.put("totalCount", bills.size());
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

        List<Bill> allBills = billMapper.selectFamilyBillListWithoutPage(
                familyId,
                userId != null ? Long.valueOf(userId) : null,
                parseDate(dto.getStartDate()), parseDate(dto.getEndDate()),
                dto.getType(), dto.getCategoryId(),
                dto.getMinAmount(), dto.getMaxAmount(), dto.getOrderBy());

        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<Bill> filteredBills = new ArrayList<>();
        for (Bill bill : allBills) {
            if (!currentUserId.equals(bill.getUserId())) {
                filteredBills.add(bill);
            }
        }

        int total = filteredBills.size();
        int page = dto.getPage() != null ? dto.getPage() : 1;
        int pageSize = dto.getPageSize() != null ? dto.getPageSize() : 20;
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<Bill> pagedBills = fromIndex < total
                ? filteredBills.subList(fromIndex, toIndex)
                : Collections.emptyList();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Bill bill : pagedBills) {
            Map<String, Object> map = billToMap(bill);
            map.put("userId", bill.getUserId());
            map.put("nickname", bill.getNickname());
            list.add(map);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("list", list);
        return Result.success(data);
    }

    // ==================== 文件解析私有方法 ====================

    /**
     * 解析系统标准模板（支持CSV和XLSX格式）
     * 假设模板列顺序：日期, 类型, 分类名称, 金额, 可见范围, 备注
     */
    private List<Bill> parseSystemTemplate(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        byte[] fileBytes = file.getBytes();

        if (fileBytes.length >= 2 && fileBytes[0] == 'P' && fileBytes[1] == 'K') {
            return parseSystemTemplateXlsx(fileBytes);
        }

        if (filename != null && (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"))) {
            return parseSystemTemplateXlsx(fileBytes);
        }

        return parseSystemTemplateCsv(fileBytes);
    }

    private List<Bill> parseSystemTemplateXlsx(byte[] fileBytes) throws Exception {
        List<Bill> bills = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(fileBytes));
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;
            Bill bill = new Bill();
            Cell dateCell = row.getCell(0);
            if (dateCell != null) {
                if (dateCell.getCellType() == CellType.NUMERIC) {
                    bill.setDate(dateCell.getLocalDateTimeCellValue().toLocalDate());
                } else {
                    bill.setDate(LocalDate.parse(dateCell.getStringCellValue(), DateTimeFormatter.ISO_LOCAL_DATE));
                }
            }
            Cell typeCell = row.getCell(1);
            if (typeCell != null) {
                bill.setType(typeCell.getStringCellValue().trim());
            }
            Cell catCell = row.getCell(2);
            if (catCell != null) {
                bill.setCategoryName(catCell.getStringCellValue().trim());
            }
            Cell amountCell = row.getCell(3);
            if (amountCell != null) {
                if (amountCell.getCellType() == CellType.NUMERIC) {
                    bill.setAmount(BigDecimal.valueOf(amountCell.getNumericCellValue()));
                } else {
                    bill.setAmount(new BigDecimal(amountCell.getStringCellValue().trim()));
                }
            }
            Cell visibleCell = row.getCell(4);
            if (visibleCell != null) {
                String visible = visibleCell.getStringCellValue().trim();
                if ("PRIVATE".equalsIgnoreCase(visible) || "FAMILY".equalsIgnoreCase(visible)) {
                    bill.setVisible(visible.toUpperCase());
                } else {
                    bill.setVisible("PRIVATE");
                }
            } else {
                bill.setVisible("PRIVATE");
            }
            Cell noteCell = row.getCell(5);
            if (noteCell != null) {
                bill.setNote(noteCell.getStringCellValue().trim());
            }
            bills.add(bill);
        }
        workbook.close();
        return bills;
    }

    private List<Bill> parseSystemTemplateCsv(byte[] fileBytes) throws Exception {
        List<Bill> bills = new ArrayList<>();
        String[] charsets = { "UTF-8", "GBK", "GB18030", "GB2312" };
        String correctCharset = null;
        String content = null;

        for (String charset : charsets) {
            try {
                content = new String(fileBytes, charset);
                if (content.contains("日期") && content.contains("类型")) {
                    correctCharset = charset;
                    break;
                }
            } catch (Exception e) {
                continue;
            }
        }

        if (correctCharset == null) {
            content = new String(fileBytes, "UTF-8");
            correctCharset = "UTF-8";
        }

        String[] lines = content.split("\n");
        boolean foundHeader = false;
        int headerLineIdx = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.contains("日期") && line.contains("类型")) {
                foundHeader = true;
                headerLineIdx = i;
                break;
            }
        }

        if (!foundHeader) {
            throw new Exception("未找到表头行，请确认文件格式");
        }

        for (int i = headerLineIdx + 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            // 自动检测分隔符（优先使用tab，其次使用逗号）
            char separator = line.contains("\t") ? '\t' : ',';
            String[] fields = parseCsvLine(line, separator);
            if (fields.length < 4) {
                continue;
            }

            Bill bill = new Bill();

            try {
                String dateStr = fields[0].trim();
                LocalDate date = parseFlexibleDate(dateStr);
                if (date != null) {
                    bill.setDate(date);
                } else {
                    continue;
                }
            } catch (Exception e) {
                continue;
            }

            bill.setType(fields[1].trim());

            if (fields.length > 2) {
                bill.setCategoryName(fields[2].trim());
            }

            if (fields.length > 3) {
                String amountStr = fields[3].trim().replace(",", "");
                if (!amountStr.isEmpty()) {
                    bill.setAmount(new BigDecimal(amountStr));
                }
            }

            if (fields.length > 4) {
                String visible = fields[4].trim();
                if ("PRIVATE".equalsIgnoreCase(visible) || "FAMILY".equalsIgnoreCase(visible)) {
                    bill.setVisible(visible.toUpperCase());
                } else {
                    bill.setVisible("PRIVATE");
                }
            } else {
                bill.setVisible("PRIVATE");
            }

            if (fields.length > 5) {
                bill.setNote(fields[5].trim());
            }

            bills.add(bill);
        }

        return bills;
    }

    /**
     * 解析支付宝账单 CSV
     * 假设列顺序：记录时间, 商品名称, 金额, 收支类型, 分类, 备注, ...
     */
    private List<Bill> parseAlipayCsv(MultipartFile file) throws Exception {
        List<Bill> bills = new ArrayList<>();
        String[] charsets = { "GBK", "GB18030", "UTF-8", "GB2312" };
        byte[] fileBytes = file.getBytes();
        String correctCharset = null;
        for (String charset : charsets) {
            try {
                String content = new String(fileBytes, charset);
                String[] lines = content.split("\n");
                boolean foundHeader = false;
                for (String line : lines) {
                    if (line.contains("交易时间") && line.contains("交易分类")) {
                        foundHeader = true;
                        break;
                    }
                }
                if (foundHeader) {
                    correctCharset = charset;
                    break;
                }
            } catch (Exception e) {
                continue;
            }
        }
        if (correctCharset == null) {
            throw new Exception("无法识别文件编码，请确保文件是CSV格式");
        }
        String content = new String(fileBytes, correctCharset);
        String[] lines = content.split("\n");
        int headerLineIdx = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("交易时间") && lines[i].contains("交易分类")) {
                headerLineIdx = i;
                break;
            }
        }
        if (headerLineIdx == -1) {
            throw new Exception("未找到表头行，请确认文件格式");
        }
        for (int i = headerLineIdx + 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] fields = parseCsvLine(line, ',');
            if (fields.length < 7 || fields[0] == null || fields[0].trim().isEmpty()) {
                continue;
            }
            String firstCol = fields[0].trim();
            if (firstCol.contains("交易时间") || firstCol.contains("日期")) {
                continue;
            }
            Bill bill = new Bill();
            try {
                LocalDate date = parseAlipayDate(firstCol);
                if (date == null) {
                    continue;
                }
                bill.setDate(date);
            } catch (Exception e) {
                continue;
            }
            String alipayCategory = fields[1].trim();
            bill.setCategoryName(mapAlipayCategoryToOurCategory(alipayCategory));
            String amountStr = fields[6].trim().replace("¥", "").replace(",", "");
            if (!amountStr.isEmpty()) {
                bill.setAmount(new BigDecimal(amountStr));
            }
            String alipayType = fields[5].trim();
            if (alipayType.contains("收入")) {
                bill.setType("INCOME");
            } else if (alipayType.contains("支出")) {
                bill.setType("EXPENSE");
            } else {
                continue;
            }
            bill.setVisible("FAMILY");
            bill.setNote(fields[4].trim());
            bills.add(bill);
        }
        return bills;
    }

    private String[] parseCsvLine(String line, char delimiter) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == delimiter && !inQuotes) {
                fields.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }

    private LocalDate parseAlipayDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        dateStr = dateStr.trim();
        try {
            if (dateStr.contains("-")) {
                if (dateStr.contains(" ")) {
                    dateStr = dateStr.substring(0, dateStr.indexOf(" "));
                }
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (dateStr.contains("/")) {
                if (dateStr.contains(" ")) {
                    dateStr = dateStr.substring(0, dateStr.indexOf(" "));
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
                return LocalDate.parse(dateStr, formatter);
            } else if (dateStr.contains(".")) {
                if (dateStr.contains(" ")) {
                    dateStr = dateStr.substring(0, dateStr.indexOf(" "));
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                return LocalDate.parse(dateStr, formatter);
            } else if (dateStr.matches("\\d{8}")) {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            } else if (dateStr.matches("\\d+\\.?\\d*")) {
                double excelDate = Double.parseDouble(dateStr);
                LocalDate baseDate = LocalDate.of(1900, 1, 1);
                if (excelDate < 60) {
                    return baseDate.plusDays((long) excelDate - 1);
                } else {
                    return baseDate.plusDays((long) excelDate - 2);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String mapAlipayCategoryToOurCategory(String alipayCategory) {
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("餐饮美食", "餐饮");
        categoryMap.put("服饰装扮", "购物");
        categoryMap.put("日用百货", "其他支出");
        categoryMap.put("家居家装", "住房");
        categoryMap.put("数码电器", "其他支出");
        categoryMap.put("运动户外", "其他支出");
        categoryMap.put("美容美发", "其他支出");
        categoryMap.put("母婴亲子", "教育");
        categoryMap.put("宠物", "其他支出");
        categoryMap.put("交通出行", "交通");
        categoryMap.put("爱车养车", "其他支出");
        categoryMap.put("住房物业", "住房");
        categoryMap.put("酒店旅游", "娱乐");
        categoryMap.put("文化休闲", "娱乐");
        categoryMap.put("教育培训", "教育");
        categoryMap.put("医疗健康", "医疗");
        categoryMap.put("生活服务", "其他支出");
        categoryMap.put("公共服务", "其他支出");
        categoryMap.put("商业服务", "其他支出");
        categoryMap.put("公益捐赠", "其他支出");
        categoryMap.put("互助保障", "其他支出");
        categoryMap.put("投资理财", "理财收益");
        categoryMap.put("保险", "其他支出");
        categoryMap.put("信用借还", "其他支出");
        categoryMap.put("充值缴费", "其他支出");
        categoryMap.put("转账红包", "其他收入");
        categoryMap.put("亲友代付", "其他支出");
        categoryMap.put("账户存取", "其他支出");
        categoryMap.put("退款", "其他收入");
        categoryMap.put("其他", "其他支出");
        String result = categoryMap.get(alipayCategory);
        return result != null ? result : "其他支出";
    }

    /**
     * 解析微信账单（支持CSV和XLSX格式）
     * 微信数据格式：
     * - 第18行为表头，第19行开始是正式数据
     * - 列顺序：交易时间、交易类型、交易对方、商品、收/支、金额(元)、支付方式、当前状态、交易单号、商户单号、备注
     */
    private List<Bill> parseWechatCsv(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();

        // 优先根据文件内容检测（xlsx是zip格式，开头是PK）
        byte[] fileBytes = file.getBytes();
        if (fileBytes.length >= 2 && fileBytes[0] == 'P' && fileBytes[1] == 'K') {
            return parseWechatXlsx(fileBytes);
        }

        // 其次根据扩展名检测
        if (filename != null && (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"))) {
            return parseWechatXlsx(fileBytes);
        }

        return parseWechatCsvInternal(fileBytes);
    }

    /**
     * 获取单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double numValue = cell.getNumericCellValue();
                if (numValue > 25569 && numValue < 50000) {
                    try {
                        java.util.Date date = DateUtil.getJavaDate(numValue);
                        java.time.LocalDateTime ldt = java.time.LocalDateTime.ofInstant(date.toInstant(),
                                java.time.ZoneId.systemDefault());
                        return ldt.toLocalDate().toString();
                    } catch (Exception e) {
                    }
                }
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        double numVal = cell.getNumericCellValue();
                        if (DateUtil.isCellDateFormatted(cell)) {
                            return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                        }
                        if (numVal > 25569 && numVal < 50000) {
                            try {
                                java.util.Date date = DateUtil.getJavaDate(numVal);
                                java.time.LocalDateTime ldt = java.time.LocalDateTime.ofInstant(date.toInstant(),
                                        java.time.ZoneId.systemDefault());
                                return ldt.toLocalDate().toString();
                            } catch (Exception ex) {
                            }
                        }
                        if (numVal == Math.floor(numVal)) {
                            return String.valueOf((long) numVal);
                        }
                        return String.valueOf(numVal);
                    } catch (Exception ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    /**
     * 灵活解析多种日期格式
     */
    private LocalDate parseFlexibleDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        dateStr = dateStr.trim();

        // 如果包含空格，取空格前的部分（去掉时间）
        if (dateStr.contains(" ")) {
            dateStr = dateStr.substring(0, dateStr.indexOf(" ")).trim();
        }

        // 尝试多种日期格式
        String[] patterns = {
                "yyyy-MM-dd",
                "yyyy/MM/dd",
                "yyyy/M/d",
                "yyyy.MM.dd",
                "yyyyMMdd",
                "yyyy年MM月dd日"
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
            }
        }

        // 最后尝试 ISO 格式
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * 解析微信XLSX格式账单
     */
    private List<Bill> parseWechatXlsx(byte[] fileBytes) throws Exception {
        List<Bill> bills = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(fileBytes));
        Sheet sheet = workbook.getSheetAt(0);

        // 查找表头行（包含"交易时间"和"收/支"的行）
        int headerRowIdx = -1;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell0 = row.getCell(0);
                Cell cell4 = row.getCell(4);
                String cell0Value = cell0 != null ? getCellValueAsString(cell0) : "";
                String cell4Value = cell4 != null ? getCellValueAsString(cell4) : "";
                if (cell0Value.contains("交易时间") && cell4Value.contains("收/支")) {
                    headerRowIdx = i;
                    break;
                }
            }
        }

        if (headerRowIdx == -1) {
            throw new Exception("未找到表头行，请确认文件格式");
        }

        // 从表头下一行开始解析数据
        for (int i = headerRowIdx + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            Cell cell0 = row.getCell(0);
            if (cell0 == null)
                continue;

            String firstCol = getCellValueAsString(cell0).trim();
            if (firstCol.isEmpty() || firstCol.contains("交易时间")) {
                continue;
            }

            Bill bill = new Bill();

            // 1. 日期（第一列，支持多种格式：2026-05-28 18:42:39 或 2026-05-28）
            try {
                String dateStr = firstCol;
                LocalDate date = parseFlexibleDate(dateStr);
                if (date != null) {
                    bill.setDate(date);
                } else {
                    continue;
                }
            } catch (Exception e) {
                continue;
            }

            // 2. 类型（第五列，收/支）
            Cell cell4 = row.getCell(4);
            String wechatType = cell4 != null ? getCellValueAsString(cell4).trim() : "";
            if (wechatType.contains("收入")) {
                bill.setType("INCOME");
            } else if (wechatType.contains("支出")) {
                bill.setType("EXPENSE");
            } else {
                continue;
            }

            // 3. 分类名称（根据交易类型和交易对方推断）
            Cell cell1 = row.getCell(1); // 交易类型
            Cell cell2 = row.getCell(2); // 交易对方
            String tradeType = cell1 != null ? getCellValueAsString(cell1).trim() : "";
            String tradeOpponent = cell2 != null ? getCellValueAsString(cell2).trim() : "";
            bill.setCategoryName(mapWechatCategory(tradeType, tradeOpponent));

            // 4. 金额（第六列，格式：¥12.40）
            Cell cell5 = row.getCell(5);
            String amountStr = cell5 != null ? getCellValueAsString(cell5).trim() : "";
            amountStr = amountStr.replace("¥", "").replace(",", "");
            if (!amountStr.isEmpty()) {
                bill.setAmount(new BigDecimal(amountStr));
            }

            // 5. 可见范围（默认所有人可见）
            bill.setVisible("FAMILY");

            // 6. 备注（第四列，商品）
            Cell cell3 = row.getCell(3);
            bill.setNote(cell3 != null ? getCellValueAsString(cell3).trim() : "");

            bills.add(bill);
        }

        workbook.close();
        return bills;
    }

    /**
     * 解析微信CSV格式账单
     */
    private List<Bill> parseWechatCsvInternal(byte[] fileBytes) throws Exception {
        List<Bill> bills = new ArrayList<>();
        String[] charsets = { "GBK", "GB18030", "UTF-8", "GB2312" };
        String correctCharset = null;

        // 检测编码
        for (String charset : charsets) {
            try {
                String content = new String(fileBytes, charset);
                String[] lines = content.split("\n");
                boolean foundHeader = false;
                for (String line : lines) {
                    if (line.contains("交易时间") && line.contains("收/支")) {
                        foundHeader = true;
                        break;
                    }
                }
                if (foundHeader) {
                    correctCharset = charset;
                    break;
                }
            } catch (Exception e) {
                continue;
            }
        }

        if (correctCharset == null) {
            throw new Exception("无法识别文件编码，请确保文件是CSV格式");
        }

        String content = new String(fileBytes, correctCharset);
        String[] lines = content.split("\n");

        // 查找表头行（包含"交易时间"和"收/支"的行）
        int headerLineIdx = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("交易时间") && lines[i].contains("收/支")) {
                headerLineIdx = i;
                break;
            }
        }

        if (headerLineIdx == -1) {
            throw new Exception("未找到表头行，请确认文件格式");
        }

        // 从表头下一行开始解析数据
        for (int i = headerLineIdx + 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] fields = parseCsvLine(line, ',');
            if (fields.length < 6 || fields[0] == null || fields[0].trim().isEmpty()) {
                continue;
            }

            String firstCol = fields[0].trim();
            if (firstCol.contains("交易时间") || firstCol.contains("日期")) {
                continue;
            }

            Bill bill = new Bill();
            try {
                // 1. 日期（第一列，支持多种格式）
                String dateStr = fields[0].trim();
                LocalDate date = parseFlexibleDate(dateStr);
                if (date != null) {
                    bill.setDate(date);
                } else {
                    continue;
                }
            } catch (Exception e) {
                continue;
            }

            // 2. 类型（第五列，收/支）
            String wechatType = fields[4].trim();
            if (wechatType.contains("收入")) {
                bill.setType("INCOME");
            } else if (wechatType.contains("支出")) {
                bill.setType("EXPENSE");
            } else {
                continue;
            }

            // 3. 分类名称（根据交易类型和交易对方推断）
            String tradeType = fields[1].trim(); // 交易类型
            String tradeOpponent = fields[2].trim(); // 交易对方
            bill.setCategoryName(mapWechatCategory(tradeType, tradeOpponent));

            // 4. 金额（第六列，格式：¥12.40）
            String amountStr = fields[5].trim().replace("¥", "").replace(",", "");
            if (!amountStr.isEmpty()) {
                bill.setAmount(new BigDecimal(amountStr));
            }

            // 5. 可见范围（默认所有人可见）
            bill.setVisible("FAMILY");

            // 6. 备注（第四列，商品）
            bill.setNote(fields[3].trim());

            bills.add(bill);
        }

        return bills;
    }

    /**
     * 根据微信交易类型和交易对方映射到系统分类
     */
    private String mapWechatCategory(String tradeType, String tradeOpponent) {
        // 先根据交易类型判断
        if (tradeType != null) {
            // 收入类型
            if (tradeType.contains("红包") || tradeType.contains("转账")) {
                return "其他收入";
            }
            if (tradeType.contains("收款")) {
                return "其他收入";
            }

            // 支出类型
            if (tradeType.contains("餐饮") || tradeType.contains("外卖")) {
                return "餐饮";
            }
            if (tradeType.contains("交通") || tradeType.contains("出行") || tradeType.contains("打车")) {
                return "交通";
            }
            if (tradeType.contains("购物") || tradeType.contains("商城")) {
                return "购物";
            }
            if (tradeType.contains("娱乐") || tradeType.contains("游戏")) {
                return "娱乐";
            }
            if (tradeType.contains("医疗") || tradeType.contains("医院") || tradeType.contains("药店")) {
                return "医疗";
            }
            if (tradeType.contains("教育") || tradeType.contains("培训")) {
                return "教育";
            }
            if (tradeType.contains("生活缴费") || tradeType.contains("水电")) {
                return "住房";
            }
        }

        // 根据交易对方判断
        if (tradeOpponent != null) {
            // 餐饮类
            if (tradeOpponent.contains("美团") || tradeOpponent.contains("饿了么") ||
                    tradeOpponent.contains("外卖") || tradeOpponent.contains("餐厅") ||
                    tradeOpponent.contains("饭店") || tradeOpponent.contains("小吃") ||
                    tradeOpponent.contains("咖啡") || tradeOpponent.contains("奶茶")) {
                return "餐饮";
            }

            // 交通类
            if (tradeOpponent.contains("滴滴") || tradeOpponent.contains("打车") ||
                    tradeOpponent.contains("公交") || tradeOpponent.contains("地铁") ||
                    tradeOpponent.contains("高铁") || tradeOpponent.contains("火车") ||
                    tradeOpponent.contains("机票") || tradeOpponent.contains("出行")) {
                return "交通";
            }

            // 购物类
            if (tradeOpponent.contains("淘宝") || tradeOpponent.contains("天猫") ||
                    tradeOpponent.contains("京东") || tradeOpponent.contains("拼多多") ||
                    tradeOpponent.contains("超市") || tradeOpponent.contains("便利店")) {
                return "购物";
            }

            // 住房类
            if (tradeOpponent.contains("物业") || tradeOpponent.contains("房租") ||
                    tradeOpponent.contains("水电") || tradeOpponent.contains("燃气")) {
                return "住房";
            }

            // 医疗类
            if (tradeOpponent.contains("医院") || tradeOpponent.contains("药店") ||
                    tradeOpponent.contains("诊所")) {
                return "医疗";
            }

            // 教育类
            if (tradeOpponent.contains("学校") || tradeOpponent.contains("培训") ||
                    tradeOpponent.contains("课程")) {
                return "教育";
            }

            // 娱乐类
            if (tradeOpponent.contains("电影") || tradeOpponent.contains("游戏") ||
                    tradeOpponent.contains("KTV") || tradeOpponent.contains("旅游")) {
                return "娱乐";
            }

            // 理财收益
            if (tradeOpponent.contains("理财") || tradeOpponent.contains("基金") ||
                    tradeOpponent.contains("股票") || tradeOpponent.contains("利息")) {
                return "理财收益";
            }

            // 工资收入
            if (tradeOpponent.contains("工资") || tradeOpponent.contains("薪酬")) {
                return "工资";
            }

            // 奖金收入
            if (tradeOpponent.contains("奖金") || tradeOpponent.contains("绩效")) {
                return "奖金";
            }
        }

        // 默认分类
        return "其他支出";
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