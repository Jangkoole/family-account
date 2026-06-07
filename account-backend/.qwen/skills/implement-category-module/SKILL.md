---
name: implement-category-module
description: 实现收支分类管理模块（后端9个文件 + 前端2个文件）的完整步骤，含后续bug修复和UI布局决策
source: auto-skill
extracted_at: '2026-06-07T12:09:25.839Z'
---

# 实现收支分类管理模块

## 背景

该项目是一个家庭记账系统（Spring Boot + Vue3），分类管理模块是唯一未实现的模块。后端已有 `Category.java` 实体和 `CategoryMapper.java`，其余7个后端文件和2个前端文件均为空壳或缺失。

**实际实现中发现需要额外新建2个文件：**
- `mapper/CategoryApplyMapper.java` — 操作 `category_apply` 表的 Mapper（之前遗漏）
- `dto/category/CategoryReviewDTO.java` — 审核分类申请的 DTO（之前遗漏，且不能复用家庭模块的 `FamilyReviewDTO`，因为字段不同）

## 数据库表

`category_apply` 表结构（已在数据库中创建）：

```sql
CREATE TABLE `category_apply` (
    `id`                   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `family_id`            BIGINT      NOT NULL COMMENT '家庭组ID',
    `user_id`              BIGINT      NOT NULL COMMENT '申请人用户ID',
    `category_name`        VARCHAR(50) NOT NULL COMMENT '申请新增的分类名称',
    `type`                 VARCHAR(10) NOT NULL COMMENT '收支类型：INCOME/EXPENSE',
    `status`               TINYINT     NOT NULL DEFAULT 0 COMMENT '申请状态：0待审核 1已通过 2已拒绝',
    `merge_to_category_id` BIGINT      NULL COMMENT '合并到的目标分类ID，null表示独立新增',
    `apply_time`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `review_time`          DATETIME    NULL COMMENT '审核时间',
    PRIMARY KEY (`id`),
    KEY `idx_family_id` (`family_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类审核申请表';
```

## 填充顺序

按依赖关系从底层到上层填充，**一次只填充一个文件**，让用户逐个检查。

### 第一步：Entity — `CategoryApply.java`

对应 `category_apply` 表，使用 MyBatis-Plus 注解，参考已有的 `FamilyApply.java` 写法。

```java
package com.family.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category_apply")
public class CategoryApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private Long userId;
    private String categoryName;
    private String type;
    private Integer status;
    private Long mergeToCategoryId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime applyTime;

    private LocalDateTime reviewTime;  // 注意：reviewTime 由代码手动设置，不需要自动填充
}
```

### 第二步：DTO（4个文件）

参考已有的 DTO 风格（Lombok + 校验注解）：

- **`CategoryAddDTO`**：`name`(NotBlank), `type`(NotBlank, pattern INCOME/EXPENSE)
- **`CategoryUpdateDTO`**：`id`(NotNull), `name`(NotBlank)
- **`CategoryMigrateDTO`**：`fromCategoryId`(NotNull), `toCategoryId`(NotNull)
- **`CategoryReviewDTO`**（**需新建**）：`applyId`(NotNull), `approve`(NotNull Boolean), `mergeToCategoryId`(可为null)

### 第三步：Mapper — `CategoryApplyMapper.java`（需新建）

```java
@Mapper
public interface CategoryApplyMapper extends BaseMapper<CategoryApply> {}
```

### 第四步：Service 接口 — `CategoryService.java`

**注意：当前文件是 class，需要改为 interface**。声明7个方法，每个返回 `Result`，参考 `FamilyService.java` 的写法：

```java
public interface CategoryService {
    Result getCategoryList(Long userId);
    Result addCategory(Long userId, CategoryAddDTO dto);
    Result updateCategory(Long userId, CategoryUpdateDTO dto);
    Result deleteCategory(Long userId, Long categoryId);
    Result migrateCategory(Long userId, CategoryMigrateDTO dto);
    Result getApplyList(Long userId);
    Result reviewApply(Long userId, CategoryReviewDTO dto);
}
```

### 第五步：Service 实现 — `CategoryServiceImpl.java`

核心业务逻辑，需要处理：

1. **获取分类列表**：查询系统内置分类（`is_system=1`）+ 用户个人分类（`user_id=当前用户`）+ 家庭分类（`family_id=当前家庭`），用 `LinkedHashMap` 按插入顺序去重
2. **新增分类**：
   - 独立用户：直接新增，`user_id=当前用户`
   - 家庭成员：写入 `category_apply` 表，状态为待审核（需检查同名待审核申请防重复）
3. **修改分类**：只能修改自定义分类名称，系统内置分类不可修改；校验权限（本人或家庭管理员）；检查名称冲突
4. **删除分类**：系统内置不可删；校验权限；已被 bill 表引用的分类需先迁移
5. **迁移分类**：校验源/目标分类存在且不同；校验权限；使用 `LambdaUpdateWrapper` 批量更新 bill 表的 `category_id`
6. **获取审核列表**：仅家庭管理员可调用，查询 `category_apply` 表
7. **审核申请**：通过时可选合并到已有分类或独立新增为家庭分类；拒绝时仅更新状态

**关键实现细节：**
- 使用 `StpUtil.getLoginIdAsLong()` 获取当前用户ID
- 使用 `LambdaQueryWrapper` 构建查询条件
- 使用 `CategoryMapper` 操作 `category` 表
- 需要注入 `FamilyMemberMapper` 判断用户是否为家庭管理员
- 需要注入 `CategoryApplyMapper` 操作 `category_apply` 表
- 需要注入 `BillMapper` 检查分类是否已被使用
- 删除分类前检查 `bill` 表中是否有记录引用该分类

### 第六步：Controller — `CategoryController.java`

参考 `UserController.java` 的写法风格，`@RestController` + `@RequestMapping("/category")`，7个端点：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/list` | 获取分类列表 |
| POST | `/add` | 新增自定义分类 |
| PUT | `/update` | 修改自定义分类 |
| DELETE | `/delete/{id}` | 删除自定义分类 |
| PUT | `/migrate` | 迁移分类下的记录 |
| GET | `/apply/list` | 获取待审核分类申请列表 |
| PUT | `/apply/review` | 审核分类申请 |

### 第七步：前端 API — `api/category.js`

重写文件，移除 mock 模式，添加7个 API 调用函数，参考 `api/family.js` 的写法风格。

### 第八步：前端页面 — `views/CategoryView.vue`

开发完整的分类管理页面，包含：
- 分类列表展示（区分系统内置/自定义，带类型标签和来源标签）
- 新增分类弹窗
- 修改分类弹窗
- 删除分类（含二次确认）
- 家庭管理员：审核申请列表 + 审核对话框（可选择独立新增或合并到已有分类）

## 注意事项

1. `CategoryService.java` 当前是 `class`，需要改为 `interface`，同时创建 `CategoryServiceImpl` 实现它
2. **不要复用家庭模块的 `FamilyReviewDTO`**，分类审核需要独立的 `CategoryReviewDTO`（字段不同：`applyId` + `approve` + `mergeToCategoryId`）
3. **`CategoryApplyMapper.java` 需要新建**，否则 `CategoryServiceImpl` 中注入 `categoryApplyMapper` 会报错
4. 前端 `api/category.js` 当前只有 `getCategoryList` 且 `USE_MOCK=true`，需要扩展并关闭 mock
5. 删除分类前必须检查 bill 表是否有引用，防止外键约束错误
6. 系统内置分类（`is_system=1`）不可删除、不可修改
7. 家庭成员新增分类时，需检查是否已有同名待审核申请，防止重复提交
8. 审核通过时，如果选择独立新增，需检查同名分类是否已存在

## 后续修复记录

### Bug 1：管理员新增分类走了审核流程

**问题：** 家庭管理员新增分类时，也走了普通家庭成员的审核流程，导致管理员自己审核自己。

**修复：** 在 `addCategory()` 中增加管理员分支，管理员直接新增家庭分类，无需审核：

```java
// 家庭管理员：直接新增家庭分类，无需审核，立即对全体成员生效
if (member.getRole().equals("ADMIN")) {
    Category category = new Category();
    category.setName(dto.getName());
    category.setType(dto.getType());
    category.setIsSystem(0);
    category.setFamilyId(member.getFamilyId());
    categoryMapper.insert(category);
    return Result.success(data);
}
```

**三种用户类型的新增分类行为：**
| 用户类型 | 新增分类行为 |
|---|---|
| 独立用户（未加入家庭组） | 直接新增个人分类 |
| 家庭管理员 | 直接新增家庭分类，无需审核 |
| 普通家庭成员 | 提交分类申请，等待管理员审核 |

### Bug 2：401未登录不跳转登录页

**问题：** 后端返回401时使用统一响应格式 `Result.error(401, ...)`，HTTP状态码是200，前端的响应拦截器只检查了 `error.response.status === 401`，永远匹配不到，导致token失效后页面无反应。

**修复：** 在 `request.js` 的响应拦截器成功分支中增加对 `response.data.code === 401` 的检查：

```javascript
request.interceptors.response.use(
    response => {
        if (response.data && response.data.code === 401) {
            const userStore = useUserStore()
            userStore.clear()
            router.push('/login')
            return Promise.reject(new Error(response.data.message || '未登录或token已失效'))
        }
        return response.data
    },
    error => {
        if (error.response && error.response.status === 401) {
            const userStore = useUserStore()
            userStore.clear()
            router.push('/login')
        }
        return Promise.reject(error)
    }
)
```

### UI布局决策（用户偏好）

用户对 `CategoryView.vue` 的布局有明确偏好，后续开发前端页面时参考：

1. **收入/支出分类分卡片展示**，上下排列（收入在上，支出在下），不要左右分栏
2. **每个卡片头部带数量标签 + 圆形新增按钮**（`circle` 按钮，只显示 `+` 图标，hover有title提示），不要顶部统一的新增按钮
3. **用户自己处理CSS样式美化**，不要花时间在前端样式上，只保证功能正确和布局结构合理

## 导入性能优化

### 问题：导入速度慢 + 超时

导入三个月账单时出现两个问题：
1. 前端 Axios 全局超时 10 秒（`timeout: 10000`），导致 `time out of 100000ms exceed` 错误
2. 后端逐条 `billMapper.insert(bill)`，N 条记录 = N 次数据库网络往返

### 修复1：前端超时设置

在 `api/bill.js` 中为导入相关接口单独设置更长的超时时间（覆盖全局默认值）：

```javascript
// 上传解析（文件上传 + 解析）：60秒
export const previewImport = (data) => {
  return request.post('/bill/import/preview', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}

// 确认导入（逐条写入数据库）：120秒
export const confirmImport = (previewId) => {
  return request.post('/bill/import', { previewId }, {
    timeout: 120000
  })
}
```

**关键点：** Axios 的 `timeout` 可以在单个请求中覆盖实例的默认超时时间，不需要修改全局配置。

### 修复2：后端批量插入

将逐条 insert 改为批量 insert，减少数据库网络往返：

**BillMapper.java** 新增方法：
```java
void insertBatch(@Param("list") List<Bill> list);
```

**BillMapper.xml** 新增 SQL：
```xml
<insert id="insertBatch">
    INSERT INTO bill (user_id, family_id, category_id, type, amount, date, note, visible)
    VALUES
    <foreach collection="list" item="item" separator=",">
        (#{item.userId}, #{item.familyId}, #{item.categoryId}, #{item.type},
         #{item.amount}, #{item.date}, #{item.note}, #{item.visible})
    </foreach>
</insert>
```

**注意：** Bill 实体类中备注字段名为 `note`（不是 `remark`），XML 中必须使用 `#{item.note}`，否则 MyBatis 会报 `There is no getter for property named 'remark'` 错误。
```

**BillServiceImpl.java** 修改 `importBills` 方法：
- 先校验所有数据，合法的放入 `validBills` 列表
- 最后一次性 `billMapper.insertBatch(validBills)`

### 修复3：事务回滚（要么全成功，要么全失败）

**问题：** 原代码中异常被 catch 住了，`@Transactional` 不会触发回滚。如果批量插入中途失败，可能只插入了部分数据。

**修复：**
1. `@Transactional(rollbackFor = Exception.class)` — 明确指定所有异常都回滚
2. 校验与插入分离：先全部校验，**有任何一条数据校验失败，整批不导入**，返回失败原因
3. 提取 `validateImportBill` 私有方法，校验逻辑独立

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Result importBills(String previewId) {
    // ... 获取缓存数据 ...
    
    // 第一阶段：全部校验
    for (int i = 0; i < bills.size(); i++) {
        String error = validateImportBill(bill, i);
        if (error != null) {
            failReasons.add(...);
        } else {
            validBills.add(bill);
        }
    }
    
    // 有校验失败的数据时，全部不导入
    if (!failReasons.isEmpty()) {
        return Result.success(data); // successCount=0, 返回失败原因
    }
    
    // 第二阶段：全部校验通过，批量插入
    if (!validBills.isEmpty()) {
        billMapper.insertBatch(validBills);
    }
    return Result.success(data); // successCount=validBills.size()
}
```

## 批量删除功能

### 后端

**BillController.java** 新增接口：
```java
@DeleteMapping("/delete/batch")
public Result deleteBatch(@RequestBody java.util.List<Long> ids) {
    return billService.deleteBatch(ids);
}
```

**BillMapper.xml** 新增 SQL（带 `user_id` 过滤，只能删自己的）：
```xml
<delete id="deleteBatchIds">
    DELETE FROM bill
    WHERE user_id = #{userId}
        AND id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</delete>
```

### 前端

**api/bill.js** 新增 API：
```javascript
export const deleteBatchBill = (ids) => {
  return request.delete('/bill/delete/batch', { data: ids })
}
```

**BillView.vue** 添加：
- 表格勾选列：`<el-table-column type="selection" width="40" />`
- 选中状态：`const selectedIds = ref([])`
- 选择变化处理：`@selection-change="handleSelectionChange"`
- 批量操作工具栏：选中后显示"批量删除（N）"按钮
- 确认弹窗 + 调用 `deleteBatchBill`

### Bug 3：批量删除后勾选计数不清零

**问题：** 删除成功后 `selectedIds.value = []` 并重新加载数据，但 `el-table` 的 `:reserve-selection="true"` 会在数据重新加载后恢复之前的勾选状态，导致 `selectedIds` 又被表格内部状态重新填充，计数累加。

**修复：** 给 `el-table` 添加 `ref`，删除成功后调用 `clearSelection()` 清除表格内部选择状态：

```vue
<!-- 模板中 -->
<el-table :data="billList" stripe fit @selection-change="handleSelectionChange" row-key="id" ref="tableRef">

<!-- script setup 中 -->
const tableRef = ref(null)

<!-- 删除成功后 -->
selectedIds.value = []
tableRef.value.clearSelection()  // 清除表格内部选择状态
fetchBills()
```

**关键点：** `reserve-selection` 依赖 `row-key` 来跨页记忆勾选。即使 `selectedIds` 被清空，表格内部仍然记住了哪些行的 key 被选中。`clearSelection()` 是 Element Plus 提供的官方方法，用于清除表格内部的选择状态。

## 数据库字段变更注意事项

### 问题：新增实体字段导致登录失败

**现象：** 在 `User.java` 实体类中新增 `defaultVisible` 字段后，MyBatis-Plus 的 `BaseMapper` 自动生成 `SELECT ..., default_visible, ... FROM user` SQL，但数据库表没有该列，导致 `SQLSyntaxErrorException`，所有用户无法登录。

**根因：** MyBatis-Plus 的 `BaseMapper` 根据实体类的所有非静态字段自动生成查询列。新增字段 = 新增 SQL 列引用。

**修复方案：**

**方案 A（推荐，不改数据库）：** 使用 `@TableField(exist = false)` 排除字段，仅作为内存数据传输：

```java
@TableField(exist = false)
private String defaultVisible;
```

**方案 B（需要持久化）：** 同时提供 ALTER TABLE SQL 让用户手动执行：

```sql
ALTER TABLE `user` ADD COLUMN `default_visible` VARCHAR(10) NOT NULL DEFAULT 'PRIVATE' COMMENT '说明' AFTER `status`;
```

**关键原则：** 任何时候在实体类中新增字段，必须明确告知用户是否需要改数据库。如果用户选择不改，立即用 `@TableField(exist = false)` 排除。

### 默认可见范围功能实现（纯后端+前端，不依赖数据库持久化）

如果用户选择不修改数据库，默认可见范围可以纯前端实现：

1. **前端 localStorage 存储** — `ProfileView.vue` 中修改时写入 `localStorage`
2. **前端读取** — `BillView.vue` 新建记录时从 `localStorage` 读取
3. **后端不感知** — 后端 `add` 接口接收前端传的 `visible` 值即可

这种方式换设备或清缓存后会恢复默认值 `PRIVATE`。

## 可见性修改 Bug

### Bug A：修改账单可见性后表格文字不更新

**现象：** 在前端编辑账单，将可见范围从"家庭成员可见"改为"仅自己可见"后，表格中对应行的"可见范围"标签文字没有变化。

**原因：** 后端 `update()` 方法中更新 `visible` 的条件过于严格：

```java
// 原代码：要求账单必须有 family_id 才能修改可见性
if (bill.getFamilyId() != null && getFamilyId() != null && dto.getVisible() != null) {
    bill.setVisible(dto.getVisible());
}
```

`bill.getFamilyId() != null` 这个条件导致很多账单（如个人记录、已退出家庭的记录）的可见性无法被更新。

**修复：** 去掉 `bill.getFamilyId() != null` 的限制，改为只校验用户权限：

```java
// 修改可见性：仅当用户属于家庭组时才允许设为 FAMILY，否则只能设为 PRIVATE
if (dto.getVisible() != null) {
    if ("FAMILY".equals(dto.getVisible()) && getFamilyId() == null) {
        return Result.error(400, "未加入家庭组，无法设为家庭成员可见");
    }
    bill.setVisible(dto.getVisible());
}
```

**原则：** 用户自己的账单，可见性应该始终可修改。限制只应作用于"能否设为 FAMILY"（需要家庭组），而不是"能否修改 visible 字段"。

### Bug B：设置 FAMILY 可见后记录消失

**现象：** 用户创建家庭组后，将某条原本"仅自己可见"的记录改为"家庭成员可见"，该记录从列表中消失。

**根因：** 后端 `update()` 方法只修改了 `visible` 字段为 `FAMILY`，但没有同步更新 `family_id`。数据库中记录变成 `visible='FAMILY', family_id=NULL`。而列表查询的 SQL 条件是：

```sql
WHERE b.user_id = #{userId}
  AND (b.visible = 'PRIVATE' OR b.family_id = #{familyId})
```

`visible='FAMILY'` 不满足 `b.visible = 'PRIVATE'`，`family_id=NULL` 不满足 `b.family_id = #{familyId}`，两个条件都不命中，记录被过滤掉。

**修复：** 在 `update()` 方法中，当设置 `visible` 为 `FAMILY` 时，同时将记录的 `family_id` 更新为当前用户的家庭组 ID：

```java
if (dto.getVisible() != null) {
    if ("FAMILY".equals(dto.getVisible())) {
        Long familyId = getFamilyId();
        if (familyId == null) {
            return Result.error(400, "未加入家庭组，无法设为家庭成员可见");
        }
        bill.setFamilyId(familyId);  // 同步更新 family_id
    }
    bill.setVisible(dto.getVisible());
}
```

**教训：** 当修改记录的可见性为 FAMILY 时，必须确保 `family_id` 也被正确设置，否则查询条件无法匹配到该记录。`visible` 和 `family_id` 是一对联动字段。

## 查询成功反馈（区分搜索与分页）

### 需求

用户点击"查询"按钮后，页面数据更新了但没有任何视觉反馈，用户不确定是否查询成功。需要在查询成功后给出提示，但分页切换时不提示。

### 实现方案

使用一个临时标记 `window._isPaging` 来区分是"点击查询"还是"分页操作"：

```javascript
// 搜索按钮点击
const searchBills = () => {
  page.value = 1
  fetchBills()  // _isPaging 默认为 false，会触发提示
}

// 分页操作
const handleSizeChange = (val) => {
  window._isPaging = true   // 标记为分页操作
  pageSize.value = val
  page.value = 1
  fetchBills()
}

const handleCurrentChange = (val) => {
  window._isPaging = true   // 标记为分页操作
  page.value = val
  fetchBills()
}

// fetchBills 中
if (res.code === 200) {
  billList.value = res.data.list || []
  total.value = res.data.total || 0
  // 仅在点击查询时提示，分页时不提示
  if (page.value === 1 && !window._isPaging) {
    ElMessage.success(`查询成功，共 ${total.value} 条记录`)
  }
  // ...
}

// finally 中清除标记
finally {
  window._isPaging = false
}
```

**关键点：** 使用 `window._isPaging` 作为临时标记，而不是在 `searchBills` 中传参，因为 `fetchBills` 可能被多个入口调用（搜索、分页、初始化、删除后刷新等），标记方式侵入性最小。

## 导入文件格式支持（CSV + xlsx）

### 问题：支付宝/微信导入只支持 CSV，不支持 xlsx

用户上传支付宝导出的 CSV 文件时提示"无法识别文件编码，请确保文件是CSV格式"，且系统只提示 CSV 格式，但实际上应该同时支持 CSV 和 xlsx。

### 原因分析

1. **`parseAlipayCsv` 方法没有 xlsx 检测** — 微信的 `parseWechatCsv` 已经支持 xlsx（通过检测文件头 `PK` 或扩展名），但支付宝的解析器没有这个逻辑，直接假设文件是 CSV
2. **CSV 编码检测中 `\r\n` 换行符问题** — Windows 下 CSV 文件使用 `\r\n` 换行，`content.split("\n")` 后每行末尾会带 `\r` 字符，导致 `line.contains("交易时间")` 匹配失败，编码检测永远找不到正确的编码
3. **错误提示不准确** — 提示"请确保文件是CSV格式"排除了 xlsx

### 修复方案

#### 1. 重构 `parseAlipayCsv` 方法，先检测 xlsx

参考微信的 `parseWechatCsv` 写法，将原方法拆分为三层：

```java
// 入口：先检测文件格式
private List<Bill> parseAlipayCsv(MultipartFile file) throws Exception {
    String filename = file.getOriginalFilename();
    byte[] fileBytes = file.getBytes();

    // 优先根据文件内容检测（xlsx是zip格式，开头是PK）
    if (fileBytes.length >= 2 && fileBytes[0] == 'P' && fileBytes[1] == 'K') {
        return parseAlipayXlsx(fileBytes);
    }

    // 其次根据扩展名检测
    if (filename != null && (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"))) {
        return parseAlipayXlsx(fileBytes);
    }

    return parseAlipayCsvInternal(fileBytes);
}
```

**关键点：** xlsx 文件本质是 ZIP 压缩包，前两个字节固定为 `PK`（`0x50 0x4B`）。通过检测文件头比检测扩展名更可靠，因为用户可能修改了扩展名。

#### 2. 新增 `parseAlipayXlsx` 方法

参考微信的 `parseWechatXlsx` 写法，使用 Apache POI 解析：

```java
private List<Bill> parseAlipayXlsx(byte[] fileBytes) throws Exception {
    List<Bill> bills = new ArrayList<>();
    Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes));
    Sheet sheet = workbook.getSheetAt(0);

    // 查找表头行（包含"交易时间"和"交易分类"的行）
    int headerRowIdx = -1;
    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row != null) {
            Cell cell0 = row.getCell(0);
            Cell cell1 = row.getCell(1);
            String cell0Value = getCellValueAsString(cell0);
            String cell1Value = getCellValueAsString(cell1);
            if (cell0Value.contains("交易时间") && cell1Value.contains("交易分类")) {
                headerRowIdx = i;
                break;
            }
        }
    }
    // ... 逐行解析，列索引与 CSV 版本保持一致
}
```

**关键点：** xlsx 解析使用 `getCellValueAsString` 工具方法（已存在），统一处理 STRING、NUMERIC、BOOLEAN、FORMULA 四种单元格类型。

#### 3. 修复 CSV 编码检测中的 `\r\n` 问题

在编码检测和数据解析两个阶段都做换行符归一化：

```java
// 编码检测阶段
String content = new String(fileBytes, charset);
String[] lines = content.replace("\r\n", "\n").replace("\r", "\n").split("\n");

// 数据解析阶段（找到编码后重新解析）
String content = new String(fileBytes, correctCharset);
String[] lines = content.replace("\r\n", "\n").replace("\r", "\n").split("\n");
```

**为什么 `\r\n` 会导致编码检测失败：**
- Windows 下 CSV 文件每行以 `\r\n` 结尾
- `content.split("\n")` 后每行末尾保留 `\r`
- `line.contains("交易时间")` 时，`\r` 不影响 `contains` 匹配（`\r` 是回车符，不是字符串的一部分... 等等，实际上 `\r` 是普通字符，`contains` 应该能匹配）

**更准确的原因：** 支付宝 CSV 文件可能是 UTF-8 编码但带 BOM（`\uFEFF`），或者编码检测的 charset 列表顺序问题。但无论如何，`\r\n` 归一化是一个稳健的做法，微信解析器也应同步修复。

#### 4. 更新错误提示

```java
throw new Exception("无法识别文件编码，请确保文件是CSV或xlsx格式");
```

### 架构模式总结

对于多格式文件导入，推荐的分层架构：

```
入口方法(file)
  ├── 检测文件头 PK → xlsx 解析器
  ├── 检测扩展名 .xlsx/.xls → xlsx 解析器
  └── 否则 → CSV 解析器
      ├── 编码检测（GBK → GB18030 → UTF-8 → GB2312）
      ├── 去除 BOM 头（\uFEFF）
      ├── 换行符归一化（\r\n → \n）
      ├── 查找表头行
      └── 逐行解析
```

这种模式已在微信解析器中验证有效，支付宝解析器也应遵循相同模式。

### 导入 CSV 编码检测失败的常见原因

当用户上传 CSV 文件提示"无法识别文件编码"时，排查顺序：

1. **表头字段名不匹配** — 这是最常见的原因。编码检测依赖查找特定关键词（如"交易时间"），如果实际 CSV 的表头字段名不同（如支付宝实际是"记录时间"而非"交易时间"），检测会失败。**修复：** 使用 `||` 匹配多个可能的字段名，如 `line.contains("交易时间") || line.contains("记录时间")`
2. **BOM 头** — UTF-8 编码的 CSV 文件可能带 BOM 头（`\uFEFF`），导致第一行字符串开头多一个不可见字符，`contains` 匹配失败。**修复：** `if (content.charAt(0) == '\uFEFF') content = content.substring(1)`
3. **Windows `\r\n` 换行符** — `content.split("\n")` 后每行末尾带 `\r`，虽然 `contains` 通常不受影响，但某些场景下可能干扰。**修复：** `content.replace("\r\n", "\n").replace("\r", "\n").split("\n")`
4. **文件前有说明文字** — 支付宝 CSV 前 8 行是提示文字，第 9 行才是表头。编码检测时遍历所有行查找关键词即可，不受影响。

### 支付宝 CSV 列索引对照表

支付宝导出的 CSV 实际列顺序与代码预期不同，这是导致解析失败的另一常见原因：

| 列索引 | 实际字段名 | 代码中错误索引 | 正确索引 |
|--------|-----------|---------------|---------|
| 0 | 记录时间 | fields[0] ✓ | fields[0] |
| 1 | 分类 | fields[1] ✓ | fields[1] |
| 2 | 收支类型 | fields[5] ✗ | fields[2] |
| 3 | 金额 | fields[6] ✗ | fields[3] |
| 4 | 备注 | fields[4] ✓ | fields[4] |
| 5 | 账户 | - | - |
| 6 | 来源 | - | - |
| 7 | 标签 | - | - |

**教训：** 支付宝账单的列顺序可能随版本变化。开发导入功能时，最好先让用户提供一份示例 CSV 文件，确认实际列顺序后再编码。或者采用更鲁棒的方式：读取表头行，根据字段名动态映射列索引。
