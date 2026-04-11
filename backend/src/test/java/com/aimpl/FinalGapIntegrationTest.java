package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.ProductCategoryGroup;
import com.aimpl.domain.archive.dto.*;
import com.aimpl.domain.archive.entity.ProductCategory;
import com.aimpl.domain.archive.service.ProductCategoryService;
import com.aimpl.domain.finance.dto.AccountSubjectCreateDTO;
import com.aimpl.domain.finance.dto.BankAccountCreateDTO;
import com.aimpl.domain.openingbalance.dto.*;
import com.aimpl.domain.org.dto.DepartmentCreateDTO;
import com.aimpl.domain.org.dto.EmployeeCreateDTO;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.project.dto.ProjectPlanCreateDTO;
import com.aimpl.domain.training.entity.RequiredCourse;
import com.aimpl.domain.training.mapper.RequiredCourseMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FinalGapIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private RequiredCourseMapper requiredCourseMapper;

    // ========================= Helpers =========================

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private Long postAndGetId(String url, Object dto) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private ProjectCreateDTO validProjectDTO(String code) {
        ProjectCreateDTO dto = new ProjectCreateDTO();
        dto.setProjectCode(code);
        dto.setCustomerName("客户_" + code);
        dto.setIndustryType(IndustryType.STEEL_TRADER);
        dto.setScale("大型");
        dto.setPmId(1L);
        dto.setModules(List.of("采购", "销售"));
        dto.setRegion("华东");
        dto.setStartDate(LocalDate.of(2024, 6, 1));
        dto.setRemark("集成测试");
        return dto;
    }

    private Long createProject(String code) throws Exception {
        return postAndGetId("/api/projects", validProjectDTO(code));
    }

    private DepartmentCreateDTO validDeptDTO(String code, String name) {
        DepartmentCreateDTO dto = new DepartmentCreateDTO();
        dto.setDeptCode(code);
        dto.setDeptName(name);
        dto.setEnabled(true);
        return dto;
    }

    private Long createDepartment(String code, String name) throws Exception {
        return postAndGetId("/api/departments", validDeptDTO(code, name));
    }

    private EmployeeCreateDTO validEmployeeDTO(String code, String name, Long deptId) {
        EmployeeCreateDTO dto = new EmployeeCreateDTO();
        dto.setEmployeeCode(code);
        dto.setName(name);
        dto.setGender("男");
        dto.setIdCard("110101199003074512");
        dto.setPhone("13800138000");
        dto.setEmail("emp@test.com");
        dto.setDeptId(deptId);
        dto.setPosition("工程师");
        dto.setJoinDate(LocalDate.of(2024, 1, 1));
        dto.setEnabled(true);
        return dto;
    }

    private BankAccountCreateDTO validBankAccountDTO(String code, String name) {
        BankAccountCreateDTO dto = new BankAccountCreateDTO();
        dto.setAccountCode(code);
        dto.setAccountName(name);
        dto.setAccountType("BANK");
        dto.setBankName("工商银行");
        dto.setBankAccountNo("6222021234567890");
        dto.setBankBranch("上海分行");
        dto.setCurrency("CNY");
        dto.setEnabled(true);
        return dto;
    }

    private WarehouseCreateDTO validWarehouseDTO(String code, String name) {
        WarehouseCreateDTO dto = new WarehouseCreateDTO();
        dto.setWarehouseCode(code);
        dto.setWarehouseName(name);
        dto.setWarehouseType("MATERIAL");
        dto.setWarehouseNature("SELF");
        dto.setManagementMode("STANDARD");
        dto.setAddress("上海市浦东新区");
        dto.setContact("李四");
        dto.setPhone("13900139000");
        dto.setAreaSqm(new BigDecimal("5000"));
        dto.setCraneCount(2);
        dto.setEnabled(true);
        return dto;
    }

    private Long createWarehouse(String code, String name) throws Exception {
        return postAndGetId("/api/warehouses", validWarehouseDTO(code, name));
    }

    private StorageLocationCreateDTO validLocationDTO(String code, String name, Long warehouseId) {
        StorageLocationCreateDTO dto = new StorageLocationCreateDTO();
        dto.setLocationCode(code);
        dto.setLocationName(name);
        dto.setWarehouseId(warehouseId);
        dto.setLocationType("NORMAL");
        dto.setEnabled(true);
        return dto;
    }

    private AccountSubjectCreateDTO validSubjectDTO(String code, String name, String category, String direction) {
        AccountSubjectCreateDTO dto = new AccountSubjectCreateDTO();
        dto.setSubjectCode(code);
        dto.setSubjectName(name);
        dto.setSubjectCategory(category);
        dto.setBalanceDirection(direction);
        dto.setIsLeaf(true);
        dto.setEnabled(true);
        return dto;
    }

    private Long createAccountSubject(String code, String name, String category, String direction) throws Exception {
        return postAndGetId("/api/account-subjects", validSubjectDTO(code, name, category, direction));
    }

    private ProductCategory insertCategory(String code, String name, ProductCategoryGroup group) {
        ProductCategory cat = new ProductCategory();
        cat.setCategoryCode(code);
        cat.setCategoryName(name);
        cat.setCategoryGroup(group);
        cat.setLevel(1);
        cat.setSortOrder(1);
        cat.setDefaultUnit("吨");
        categoryService.save(cat);
        return cat;
    }

    private Long createProduct(Long categoryId) throws Exception {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setProductCode("FGP001");
        dto.setProductName("测试钢材");
        dto.setSpec("20*40*1.5");
        dto.setMaterial("Q235B");
        dto.setCategoryId(categoryId);
        dto.setUnit("吨");
        dto.setPricingUnit("吨");
        dto.setTaxRate(new BigDecimal("0.13"));
        return postAndGetId("/api/products", dto);
    }

    private CustomerCreateDTO validCustomerDTO(String code, String fullName) {
        CustomerCreateDTO dto = new CustomerCreateDTO();
        dto.setCustomerCode(code);
        dto.setFullName(fullName);
        dto.setShortName("简称");
        dto.setCustomerType("DEALER");
        dto.setCreditCode("91310000MA1K3P2X54");
        dto.setContact("张三");
        dto.setPhone("13800138000");
        dto.setProvince("上海");
        dto.setCity("上海");
        dto.setDistrict("浦东新区");
        dto.setAddress("张江高科技园区");
        dto.setCategory("A类");
        dto.setSettlementMethod("MONTHLY");
        dto.setCreditLimit(new BigDecimal("1000000"));
        dto.setTaxRate(new BigDecimal("0.13"));
        dto.setSalesRepId(1L);
        return dto;
    }

    private Long createCustomer(String code, String fullName) throws Exception {
        return postAndGetId("/api/customers", validCustomerDTO(code, fullName));
    }

    private SupplierCreateDTO validSupplierDTO(String code, String fullName) {
        SupplierCreateDTO dto = new SupplierCreateDTO();
        dto.setSupplierCode(code);
        dto.setFullName(fullName);
        dto.setShortName("简称");
        dto.setSupplierType("MATERIAL");
        dto.setCreditCode("91310000MA1K3P2X54");
        dto.setContact("张三");
        dto.setPhone("13800138000");
        dto.setAddress("上海市浦东新区");
        dto.setSettlementMethod("MONTHLY");
        dto.setTaxRate(new BigDecimal("0.13"));
        return dto;
    }

    private Long createSupplier(String code, String fullName) throws Exception {
        return postAndGetId("/api/suppliers", validSupplierDTO(code, fullName));
    }

    private void insertRequiredCourse(String industryType, String module, boolean kaRequired, int order) {
        RequiredCourse c = new RequiredCourse();
        c.setIndustryType(industryType);
        c.setCourseModule(module);
        c.setKaRequired(kaRequired);
        c.setSortOrder(order);
        requiredCourseMapper.insert(c);
    }

    // ========================= Department – happy path =========================

    @Test
    void createDepartment_happyPath() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validDeptDTO("D001", "研发部"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.deptCode").value("D001"))
                .andExpect(jsonPath("$.data.deptName").value("研发部"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void createDepartment_withParent_happyPath() throws Exception {
        Long parentId = createDepartment("DP01", "总公司");

        DepartmentCreateDTO child = validDeptDTO("DP02", "子部门");
        child.setParentId(parentId);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(child)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.parentId").value(parentId.intValue()));
    }

    @Test
    void getDepartment_byId() throws Exception {
        Long id = createDepartment("DG01", "查询部");

        mockMvc.perform(get("/api/departments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.intValue()))
                .andExpect(jsonPath("$.data.deptCode").value("DG01"));
    }

    // ========================= Department – validation =========================

    @Test
    void createDepartment_blankDeptCode_returns400() throws Exception {
        DepartmentCreateDTO dto = validDeptDTO("", "名称");

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("部门编码不能为空")));
    }

    @Test
    void createDepartment_blankDeptName_returns400() throws Exception {
        DepartmentCreateDTO dto = validDeptDTO("DC01", "");

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("部门名称不能为空")));
    }

    // ========================= Department – business rules =========================

    @Test
    void createDepartment_nonExistentParent_returns400() throws Exception {
        DepartmentCreateDTO dto = validDeptDTO("DB01", "子部门");
        dto.setParentId(99999L);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("上级部门不存在")));
    }

    @Test
    void createDepartment_duplicateCode_returns400() throws Exception {
        createDepartment("DD01", "部门A");

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validDeptDTO("DD01", "部门B"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("部门编码已存在")));
    }

    // ========================= Employee – happy path =========================

    @Test
    void createEmployee_happyPath() throws Exception {
        Long deptId = createDepartment("ED01", "员工测试部");

        EmployeeCreateDTO dto = validEmployeeDTO("E001", "张三", deptId);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.employeeCode").value("E001"))
                .andExpect(jsonPath("$.data.name").value("张三"))
                .andExpect(jsonPath("$.data.deptId").value(deptId.intValue()))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void getEmployee_byId() throws Exception {
        Long deptId = createDepartment("EG01", "员工查询部");
        Long empId = postAndGetId("/api/employees", validEmployeeDTO("EG02", "李四", deptId));

        mockMvc.perform(get("/api/employees/{id}", empId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeCode").value("EG02"))
                .andExpect(jsonPath("$.data.name").value("李四"));
    }

    // ========================= Employee – validation =========================

    @Test
    void createEmployee_invalidIdCard_returns400() throws Exception {
        Long deptId = createDepartment("EV01", "验证部");

        EmployeeCreateDTO dto = validEmployeeDTO("EV02", "王五", deptId);
        dto.setIdCard("INVALID");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("身份证号格式不正确")));
    }

    @Test
    void createEmployee_invalidPhone_returns400() throws Exception {
        Long deptId = createDepartment("EP01", "电话验证部");

        EmployeeCreateDTO dto = validEmployeeDTO("EP02", "赵六", deptId);
        dto.setPhone("12345");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("手机号格式不正确")));
    }

    // ========================= Employee – business rules =========================

    @Test
    void createEmployee_nonExistentDept_returns400() throws Exception {
        EmployeeCreateDTO dto = validEmployeeDTO("EB01", "孙七", 99999L);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("部门不存在")));
    }

    @Test
    void createEmployee_duplicateCode_returns400() throws Exception {
        Long deptId = createDepartment("EC01", "重码部");

        postAndGetId("/api/employees", validEmployeeDTO("EDUP", "员工A", deptId));

        EmployeeCreateDTO dto2 = validEmployeeDTO("EDUP", "员工B", deptId);
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("员工编码已存在")));
    }

    // ========================= BankAccount – happy path =========================

    @Test
    void createBankAccount_happyPath() throws Exception {
        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validBankAccountDTO("BA001", "工行基本户"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.accountCode").value("BA001"))
                .andExpect(jsonPath("$.data.accountName").value("工行基本户"))
                .andExpect(jsonPath("$.data.accountType").value("BANK"))
                .andExpect(jsonPath("$.data.currency").value("CNY"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    // ========================= BankAccount – validation =========================

    @Test
    void createBankAccount_blankCode_returns400() throws Exception {
        BankAccountCreateDTO dto = validBankAccountDTO("", "无编码账户");

        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("账户编码不能为空")));
    }

    @Test
    void createBankAccount_blankAccountType_returns400() throws Exception {
        BankAccountCreateDTO dto = validBankAccountDTO("BV01", "无类型");
        dto.setAccountType("");

        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("账户类型不能为空")));
    }

    // ========================= BankAccount – business rules =========================

    @Test
    void createBankAccount_duplicateCode_returns400() throws Exception {
        postAndGetId("/api/bank-accounts", validBankAccountDTO("BDUP", "账户A"));

        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validBankAccountDTO("BDUP", "账户B"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("账户编码已存在")));
    }

    // ========================= StorageLocation – happy path =========================

    @Test
    void createStorageLocation_happyPath() throws Exception {
        Long whId = createWarehouse("WH01", "主仓库");

        mockMvc.perform(post("/api/storage-locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validLocationDTO("SL001", "A区1号", whId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.locationCode").value("SL001"))
                .andExpect(jsonPath("$.data.locationName").value("A区1号"))
                .andExpect(jsonPath("$.data.warehouseId").value(whId.intValue()))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void getStorageLocation_byId() throws Exception {
        Long whId = createWarehouse("WHG01", "查询仓");
        Long locId = postAndGetId("/api/storage-locations", validLocationDTO("SLG01", "查询位", whId));

        mockMvc.perform(get("/api/storage-locations/{id}", locId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.locationCode").value("SLG01"));
    }

    @Test
    void listStorageLocations_byWarehouseId() throws Exception {
        Long whId = createWarehouse("WHL01", "列表仓");
        postAndGetId("/api/storage-locations", validLocationDTO("SLL01", "位置A", whId));
        postAndGetId("/api/storage-locations", validLocationDTO("SLL02", "位置B", whId));

        mockMvc.perform(get("/api/storage-locations").param("warehouseId", whId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= StorageLocation – validation =========================

    @Test
    void createStorageLocation_blankCode_returns400() throws Exception {
        StorageLocationCreateDTO dto = validLocationDTO("", "无编码位", 1L);

        mockMvc.perform(post("/api/storage-locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("库位编码不能为空")));
    }

    @Test
    void createStorageLocation_nullWarehouseId_returns400() throws Exception {
        StorageLocationCreateDTO dto = validLocationDTO("SLV01", "无仓库", null);

        mockMvc.perform(post("/api/storage-locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("仓库ID不能为空")));
    }

    // ========================= StorageLocation – business rules =========================

    @Test
    void createStorageLocation_nonExistentWarehouse_returns400() throws Exception {
        StorageLocationCreateDTO dto = validLocationDTO("SLB01", "幽灵仓", 99999L);

        mockMvc.perform(post("/api/storage-locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("仓库不存在")));
    }

    // ========================= AccountSubject – happy path =========================

    @Test
    void createAccountSubject_happyPath() throws Exception {
        mockMvc.perform(post("/api/account-subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validSubjectDTO("1001", "库存现金", "资产", "借"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.subjectCode").value("1001"))
                .andExpect(jsonPath("$.data.subjectName").value("库存现金"))
                .andExpect(jsonPath("$.data.subjectCategory").value("资产"))
                .andExpect(jsonPath("$.data.balanceDirection").value("借"))
                .andExpect(jsonPath("$.data.isLeaf").value(true));
    }

    @Test
    void createAccountSubject_withValidParent() throws Exception {
        AccountSubjectCreateDTO parentDTO = validSubjectDTO("1000", "资产总科目", "资产", "借");
        parentDTO.setIsLeaf(false);
        postAndGetId("/api/account-subjects", parentDTO);

        AccountSubjectCreateDTO childDTO = validSubjectDTO("1001", "库存现金", "资产", "借");
        childDTO.setParentCode("1000");

        mockMvc.perform(post("/api/account-subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(childDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.parentCode").value("1000"));
    }

    // ========================= AccountSubject – validation =========================

    @Test
    void createAccountSubject_blankCode_returns400() throws Exception {
        AccountSubjectCreateDTO dto = validSubjectDTO("", "无编码", "资产", "借");

        mockMvc.perform(post("/api/account-subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("科目编码不能为空")));
    }

    @Test
    void createAccountSubject_blankCategory_returns400() throws Exception {
        AccountSubjectCreateDTO dto = validSubjectDTO("1099", "无类别", "", "借");

        mockMvc.perform(post("/api/account-subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("科目类别不能为空")));
    }

    // ========================= AccountSubject – business rules =========================

    @Test
    void createAccountSubject_nonExistentParent_returns400() throws Exception {
        AccountSubjectCreateDTO dto = validSubjectDTO("1099", "子科目", "资产", "借");
        dto.setParentCode("NONEXIST");

        mockMvc.perform(post("/api/account-subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("上级科目不存在")));
    }

    @Test
    void createAccountSubject_duplicateCode_returns400() throws Exception {
        postAndGetId("/api/account-subjects", validSubjectDTO("ADUP", "科目A", "资产", "借"));

        mockMvc.perform(post("/api/account-subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validSubjectDTO("ADUP", "科目B", "资产", "借"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("科目编码已存在")));
    }

    // ========================= InventoryBalance – happy path =========================

    @Test
    void createInventoryBalance_happyPath_autoCalculated() throws Exception {
        Long projId = createProject("INV-PRJ01");
        Long whId = createWarehouse("INV-WH01", "库存仓");
        ProductCategory cat = insertCategory("INVCAT", "库存品类", ProductCategoryGroup.PLATE);
        Long productId = createProduct(cat.getId());

        InventoryBalanceCreateDTO dto = new InventoryBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setProductId(productId);
        dto.setWarehouseId(whId);
        dto.setBatchNo("B2024001");
        dto.setInboundDate(LocalDate.of(2024, 5, 1));
        dto.setQuantity(new BigDecimal("100"));
        dto.setWeight(new BigDecimal("5.000"));
        dto.setPackQuantity(new BigDecimal("25"));
        dto.setCostUnitPrice(new BigDecimal("4500"));

        mockMvc.perform(post("/api/inventory-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.wholeUnits").value(4))
                .andExpect(jsonPath("$.data.oddUnits").value(0))
                .andExpect(jsonPath("$.data.costAmount").value(22500.00))
                .andExpect(jsonPath("$.data.unitWeight").value(50.000));
    }

    @Test
    void createInventoryBalance_noPackQuantity_allOddUnits() throws Exception {
        Long projId = createProject("INV-PRJ02");
        Long whId = createWarehouse("INV-WH02", "库存仓2");
        ProductCategory cat = insertCategory("INVCT2", "库存品类2", ProductCategoryGroup.PLATE);
        Long productId = createProduct(cat.getId());

        InventoryBalanceCreateDTO dto = new InventoryBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setProductId(productId);
        dto.setWarehouseId(whId);
        dto.setQuantity(new BigDecimal("50"));
        dto.setWeight(new BigDecimal("2.500"));
        dto.setCostUnitPrice(new BigDecimal("3000"));

        mockMvc.perform(post("/api/inventory-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wholeUnits").value(0))
                .andExpect(jsonPath("$.data.oddUnits").value(50))
                .andExpect(jsonPath("$.data.costAmount").value(7500.00))
                .andExpect(jsonPath("$.data.unitWeight").value(50.000));
    }

    // ========================= InventoryBalance – validation =========================

    @Test
    void createInventoryBalance_nullProjectId_returns400() throws Exception {
        InventoryBalanceCreateDTO dto = new InventoryBalanceCreateDTO();
        dto.setProductId(1L);
        dto.setWarehouseId(1L);
        dto.setQuantity(new BigDecimal("10"));
        dto.setCostUnitPrice(new BigDecimal("100"));

        mockMvc.perform(post("/api/inventory-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目ID不能为空")));
    }

    // ========================= InventoryBalance – business rules =========================

    @Test
    void createInventoryBalance_nonExistentProject_returns400() throws Exception {
        InventoryBalanceCreateDTO dto = new InventoryBalanceCreateDTO();
        dto.setProjectId(99999L);
        dto.setProductId(1L);
        dto.setWarehouseId(1L);
        dto.setQuantity(new BigDecimal("10"));
        dto.setCostUnitPrice(new BigDecimal("100"));

        mockMvc.perform(post("/api/inventory-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ========================= CustomerBalance – happy path =========================

    @Test
    void createCustomerBalance_positiveBalance_typeIsReceivable() throws Exception {
        Long projId = createProject("CB-PRJ01");
        Long custId = createCustomer("CB-C01", "客户余额测试公司");

        CustomerBalanceCreateDTO dto = new CustomerBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setCustomerId(custId);
        dto.setDocType("销售单");
        dto.setDocNo("SO-2024-001");
        dto.setBalance(new BigDecimal("50000"));
        dto.setRemark("正余额测试");

        mockMvc.perform(post("/api/customer-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.balance").value(50000))
                .andExpect(jsonPath("$.data.balanceType").value("应收账款"));
    }

    @Test
    void createCustomerBalance_negativeBalance_typeIsPreReceived() throws Exception {
        Long projId = createProject("CB-PRJ02");
        Long custId = createCustomer("CB-C02", "预收测试公司");

        CustomerBalanceCreateDTO dto = new CustomerBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setCustomerId(custId);
        dto.setBalance(new BigDecimal("-20000"));

        mockMvc.perform(post("/api/customer-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balanceType").value("预收账款"));
    }

    // ========================= CustomerBalance – validation =========================

    @Test
    void createCustomerBalance_nullBalance_returns400() throws Exception {
        CustomerBalanceCreateDTO dto = new CustomerBalanceCreateDTO();
        dto.setProjectId(1L);
        dto.setCustomerId(1L);

        mockMvc.perform(post("/api/customer-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("余额不能为空")));
    }

    // ========================= CustomerBalance – business rules =========================

    @Test
    void createCustomerBalance_nonExistentCustomer_returns400() throws Exception {
        Long projId = createProject("CB-PRJ03");

        CustomerBalanceCreateDTO dto = new CustomerBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setCustomerId(99999L);
        dto.setBalance(new BigDecimal("1000"));

        mockMvc.perform(post("/api/customer-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("客户不存在")));
    }

    // ========================= SupplierBalance – happy path =========================

    @Test
    void createSupplierBalance_positiveBalance_typeIsPayable() throws Exception {
        Long projId = createProject("SB-PRJ01");
        Long suppId = createSupplier("SB-S01", "供应商余额测试");

        SupplierBalanceCreateDTO dto = new SupplierBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setSupplierId(suppId);
        dto.setDocType("采购单");
        dto.setDocNo("PO-2024-001");
        dto.setBalance(new BigDecimal("30000"));

        mockMvc.perform(post("/api/supplier-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.balance").value(30000))
                .andExpect(jsonPath("$.data.balanceType").value("应付账款"));
    }

    @Test
    void createSupplierBalance_negativeBalance_typeIsPrePaid() throws Exception {
        Long projId = createProject("SB-PRJ02");
        Long suppId = createSupplier("SB-S02", "预付测试供应商");

        SupplierBalanceCreateDTO dto = new SupplierBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setSupplierId(suppId);
        dto.setBalance(new BigDecimal("-15000"));

        mockMvc.perform(post("/api/supplier-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balanceType").value("预付账款"));
    }

    // ========================= SupplierBalance – validation =========================

    @Test
    void createSupplierBalance_nullBalance_returns400() throws Exception {
        SupplierBalanceCreateDTO dto = new SupplierBalanceCreateDTO();
        dto.setProjectId(1L);
        dto.setSupplierId(1L);

        mockMvc.perform(post("/api/supplier-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("余额不能为空")));
    }

    // ========================= SupplierBalance – business rules =========================

    @Test
    void createSupplierBalance_nonExistentSupplier_returns400() throws Exception {
        Long projId = createProject("SB-PRJ03");

        SupplierBalanceCreateDTO dto = new SupplierBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setSupplierId(99999L);
        dto.setBalance(new BigDecimal("1000"));

        mockMvc.perform(post("/api/supplier-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("供应商不存在")));
    }

    // ========================= SubjectBalance – happy path =========================

    @Test
    void createSubjectBalance_happyPath() throws Exception {
        Long projId = createProject("SUB-PRJ01");
        createAccountSubject("SB1001", "库存现金", "资产", "借");

        SubjectBalanceCreateDTO dto = new SubjectBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setSubjectCode("SB1001");
        dto.setDebitBalance(new BigDecimal("100000"));
        dto.setCreditBalance(BigDecimal.ZERO);

        mockMvc.perform(post("/api/subject-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.subjectCode").value("SB1001"))
                .andExpect(jsonPath("$.data.subjectName").value("库存现金"))
                .andExpect(jsonPath("$.data.debitBalance").value(100000));
    }

    // ========================= SubjectBalance – trial balance =========================

    @Test
    void trialBalance_balanced_returnsTrue() throws Exception {
        Long projId = createProject("TB-PRJ01");
        createAccountSubject("TB1001", "现金", "资产", "借");
        createAccountSubject("TB2001", "实收资本", "权益", "贷");

        SubjectBalanceCreateDTO d1 = new SubjectBalanceCreateDTO();
        d1.setProjectId(projId);
        d1.setSubjectCode("TB1001");
        d1.setDebitBalance(new BigDecimal("50000"));
        d1.setCreditBalance(BigDecimal.ZERO);
        postAndGetId("/api/subject-balances", d1);

        SubjectBalanceCreateDTO d2 = new SubjectBalanceCreateDTO();
        d2.setProjectId(projId);
        d2.setSubjectCode("TB2001");
        d2.setDebitBalance(BigDecimal.ZERO);
        d2.setCreditBalance(new BigDecimal("50000"));
        postAndGetId("/api/subject-balances", d2);

        mockMvc.perform(get("/api/subject-balances/trial-balance/{projectId}", projId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalDebit").value(50000))
                .andExpect(jsonPath("$.data.totalCredit").value(50000))
                .andExpect(jsonPath("$.data.balanced").value(true));
    }

    @Test
    void trialBalance_unbalanced_returnsFalse() throws Exception {
        Long projId = createProject("TB-PRJ02");
        createAccountSubject("TU1001", "现金不平", "资产", "借");

        SubjectBalanceCreateDTO dto = new SubjectBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setSubjectCode("TU1001");
        dto.setDebitBalance(new BigDecimal("30000"));
        dto.setCreditBalance(BigDecimal.ZERO);
        postAndGetId("/api/subject-balances", dto);

        mockMvc.perform(get("/api/subject-balances/trial-balance/{projectId}", projId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balanced").value(false));
    }

    // ========================= SubjectBalance – validation =========================

    @Test
    void createSubjectBalance_blankSubjectCode_returns400() throws Exception {
        SubjectBalanceCreateDTO dto = new SubjectBalanceCreateDTO();
        dto.setProjectId(1L);
        dto.setSubjectCode("");
        dto.setDebitBalance(new BigDecimal("100"));

        mockMvc.perform(post("/api/subject-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("科目编码不能为空")));
    }

    // ========================= SubjectBalance – business rules =========================

    @Test
    void createSubjectBalance_bothDebitAndCreditPositive_returns400() throws Exception {
        Long projId = createProject("SBR-PRJ01");
        createAccountSubject("SBR001", "双向科目", "资产", "借");

        SubjectBalanceCreateDTO dto = new SubjectBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setSubjectCode("SBR001");
        dto.setDebitBalance(new BigDecimal("1000"));
        dto.setCreditBalance(new BigDecimal("500"));

        mockMvc.perform(post("/api/subject-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("同一科目借方和贷方不能同时大于0")));
    }

    @Test
    void createSubjectBalance_nonLeafSubject_returns400() throws Exception {
        Long projId = createProject("SBR-PRJ02");
        AccountSubjectCreateDTO parentDTO = validSubjectDTO("SBNL01", "非末级科目", "资产", "借");
        parentDTO.setIsLeaf(false);
        postAndGetId("/api/account-subjects", parentDTO);

        SubjectBalanceCreateDTO dto = new SubjectBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setSubjectCode("SBNL01");
        dto.setDebitBalance(new BigDecimal("1000"));

        mockMvc.perform(post("/api/subject-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("只能对末级科目录入余额")));
    }

    // ========================= InvoiceBalance – happy path =========================

    @Test
    void createInvoiceBalance_salesOutput_taxAutoCalculated() throws Exception {
        Long projId = createProject("IB-PRJ01");
        Long custId = createCustomer("IB-C01", "发票客户");
        ProductCategory cat = insertCategory("IBCAT", "发票品类", ProductCategoryGroup.PLATE);
        Long productId = createProduct(cat.getId());

        InvoiceBalanceCreateDTO dto = new InvoiceBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setInvoiceType("SALES_OUTPUT");
        dto.setCounterpartyId(custId);
        dto.setCounterpartyName("发票客户");
        dto.setProductId(productId);
        dto.setProductName("测试钢材");
        dto.setQuantity(new BigDecimal("10"));
        dto.setUnitPrice(new BigDecimal("1000"));
        dto.setAmount(new BigDecimal("10000"));
        dto.setTaxRate(new BigDecimal("0.13"));

        mockMvc.perform(post("/api/invoice-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.invoiceType").value("SALES_OUTPUT"))
                .andExpect(jsonPath("$.data.taxAmount").value(1300.00))
                .andExpect(jsonPath("$.data.totalAmount").value(11300.00));
    }

    @Test
    void createInvoiceBalance_purchaseInput_happyPath() throws Exception {
        Long projId = createProject("IB-PRJ02");
        Long suppId = createSupplier("IB-S01", "发票供应商");
        ProductCategory cat = insertCategory("IBCT2", "发票品类2", ProductCategoryGroup.PLATE);
        Long productId = createProduct(cat.getId());

        InvoiceBalanceCreateDTO dto = new InvoiceBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setInvoiceType("PURCHASE_INPUT");
        dto.setCounterpartyId(suppId);
        dto.setProductId(productId);
        dto.setQuantity(new BigDecimal("5"));
        dto.setUnitPrice(new BigDecimal("2000"));
        dto.setAmount(new BigDecimal("10000"));
        dto.setTaxRate(new BigDecimal("0.09"));

        mockMvc.perform(post("/api/invoice-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taxAmount").value(900.00))
                .andExpect(jsonPath("$.data.totalAmount").value(10900.00));
    }

    // ========================= InvoiceBalance – validation =========================

    @Test
    void createInvoiceBalance_nullAmount_returns400() throws Exception {
        InvoiceBalanceCreateDTO dto = new InvoiceBalanceCreateDTO();
        dto.setProjectId(1L);
        dto.setInvoiceType("SALES_OUTPUT");
        dto.setCounterpartyId(1L);
        dto.setProductId(1L);
        dto.setQuantity(new BigDecimal("10"));
        dto.setUnitPrice(new BigDecimal("100"));
        dto.setTaxRate(new BigDecimal("0.13"));

        mockMvc.perform(post("/api/invoice-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("金额不能为空")));
    }

    @Test
    void createInvoiceBalance_blankInvoiceType_returns400() throws Exception {
        InvoiceBalanceCreateDTO dto = new InvoiceBalanceCreateDTO();
        dto.setProjectId(1L);
        dto.setInvoiceType("");
        dto.setCounterpartyId(1L);
        dto.setProductId(1L);
        dto.setQuantity(new BigDecimal("10"));
        dto.setUnitPrice(new BigDecimal("100"));
        dto.setAmount(new BigDecimal("1000"));
        dto.setTaxRate(new BigDecimal("0.13"));

        mockMvc.perform(post("/api/invoice-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("发票类型不能为空")));
    }

    // ========================= InvoiceBalance – business rules =========================

    @Test
    void createInvoiceBalance_invalidInvoiceType_returns400() throws Exception {
        Long projId = createProject("IB-PRJ03");
        ProductCategory cat = insertCategory("IBCT3", "发票品类3", ProductCategoryGroup.PLATE);
        Long productId = createProduct(cat.getId());

        InvoiceBalanceCreateDTO dto = new InvoiceBalanceCreateDTO();
        dto.setProjectId(projId);
        dto.setInvoiceType("INVALID_TYPE");
        dto.setCounterpartyId(1L);
        dto.setProductId(productId);
        dto.setQuantity(new BigDecimal("10"));
        dto.setUnitPrice(new BigDecimal("100"));
        dto.setAmount(new BigDecimal("1000"));
        dto.setTaxRate(new BigDecimal("0.13"));

        mockMvc.perform(post("/api/invoice-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("发票类型无效")));
    }

    // ========================= ProjectPlan – happy path =========================

    @Test
    void createProjectPlan_happyPath() throws Exception {
        Long projId = createProject("PP-PRJ01");

        ProjectPlanCreateDTO dto = new ProjectPlanCreateDTO();
        dto.setProjectId(projId);
        dto.setPlanName("实施计划V1");
        dto.setTotalDays(90);
        dto.setMilestones("调研→培训→上线");
        dto.setWbsItems("WBS-001,WBS-002");
        dto.setResources("2名工程师");
        dto.setRisks("人员变动风险");

        mockMvc.perform(post("/api/project-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectId").value(projId.intValue()))
                .andExpect(jsonPath("$.data.planName").value("实施计划V1"))
                .andExpect(jsonPath("$.data.totalDays").value(90))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void getProjectPlan_byId() throws Exception {
        Long projId = createProject("PP-PRJ02");

        ProjectPlanCreateDTO dto = new ProjectPlanCreateDTO();
        dto.setProjectId(projId);
        dto.setPlanName("查询计划");
        dto.setTotalDays(60);
        dto.setMilestones("里程碑A");
        Long planId = postAndGetId("/api/project-plans", dto);

        mockMvc.perform(get("/api/project-plans/{id}", planId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planName").value("查询计划"));
    }

    @Test
    void listProjectPlans_byProject() throws Exception {
        Long projId = createProject("PP-PRJ03");

        ProjectPlanCreateDTO dto = new ProjectPlanCreateDTO();
        dto.setProjectId(projId);
        dto.setPlanName("列表计划");
        dto.setTotalDays(30);
        dto.setMilestones("M1");
        postAndGetId("/api/project-plans", dto);

        mockMvc.perform(get("/api/project-plans").param("projectId", projId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    // ========================= ProjectPlan – validation =========================

    @Test
    void createProjectPlan_blankPlanName_returns400() throws Exception {
        ProjectPlanCreateDTO dto = new ProjectPlanCreateDTO();
        dto.setProjectId(1L);
        dto.setPlanName("");
        dto.setMilestones("M1");

        mockMvc.perform(post("/api/project-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("计划名称不能为空")));
    }

    @Test
    void createProjectPlan_blankMilestones_returns400() throws Exception {
        ProjectPlanCreateDTO dto = new ProjectPlanCreateDTO();
        dto.setProjectId(1L);
        dto.setPlanName("计划名");
        dto.setMilestones("");

        mockMvc.perform(post("/api/project-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("里程碑不能为空")));
    }

    // ========================= ProjectPlan – business rules =========================

    @Test
    void createProjectPlan_duplicatePerProject_returns400() throws Exception {
        Long projId = createProject("PP-PRJ04");

        ProjectPlanCreateDTO dto1 = new ProjectPlanCreateDTO();
        dto1.setProjectId(projId);
        dto1.setPlanName("第一份计划");
        dto1.setTotalDays(60);
        dto1.setMilestones("M1");
        postAndGetId("/api/project-plans", dto1);

        ProjectPlanCreateDTO dto2 = new ProjectPlanCreateDTO();
        dto2.setProjectId(projId);
        dto2.setPlanName("第二份计划");
        dto2.setTotalDays(90);
        dto2.setMilestones("M2");

        mockMvc.perform(post("/api/project-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("该项目已存在计划")));
    }

    @Test
    void createProjectPlan_nonExistentProject_returns400() throws Exception {
        ProjectPlanCreateDTO dto = new ProjectPlanCreateDTO();
        dto.setProjectId(99999L);
        dto.setPlanName("幽灵项目计划");
        dto.setTotalDays(30);
        dto.setMilestones("M1");

        mockMvc.perform(post("/api/project-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ========================= RequiredCourse – GET with filter =========================

    @Test
    void listRequiredCourses_byIndustryType_returnsCourses() throws Exception {
        insertRequiredCourse("STEEL_TRADER", "基础资料", true, 1);
        insertRequiredCourse("STEEL_TRADER", "采购管理", true, 2);
        insertRequiredCourse("STEEL_TRADER", "系统配置", false, 3);
        insertRequiredCourse("STEEL_MILL", "基础资料", true, 1);

        mockMvc.perform(get("/api/required-courses").param("industryType", "STEEL_TRADER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].industryType").value("STEEL_TRADER"))
                .andExpect(jsonPath("$.data[0].courseModule").value("基础资料"))
                .andExpect(jsonPath("$.data[1].courseModule").value("采购管理"))
                .andExpect(jsonPath("$.data[2].courseModule").value("系统配置"));
    }

    @Test
    void listRequiredCourses_differentIndustry_filtersCorrectly() throws Exception {
        insertRequiredCourse("STEEL_MILL", "库存管理", true, 1);
        insertRequiredCourse("STEEL_MILL", "财务管理", true, 2);
        insertRequiredCourse("STEEL_TRADER", "采购管理", true, 1);

        mockMvc.perform(get("/api/required-courses").param("industryType", "STEEL_MILL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].industryType").value("STEEL_MILL"));
    }

    @Test
    void listRequiredCourses_emptyResult() throws Exception {
        mockMvc.perform(get("/api/required-courses").param("industryType", "NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    // ========================= ProductCategory – happy path =========================

    @Test
    void createProductCategory_happyPath() throws Exception {
        ProductCategoryCreateDTO dto = new ProductCategoryCreateDTO();
        dto.setCategoryCode("FGC01");
        dto.setCategoryName("最终测试品类");
        dto.setLevel(1);
        dto.setSortOrder(1);
        dto.setCategoryGroup(ProductCategoryGroup.PLATE);
        dto.setDefaultUnit("吨");

        mockMvc.perform(post("/api/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.categoryCode").value("FGC01"))
                .andExpect(jsonPath("$.data.categoryName").value("最终测试品类"));
    }

    // ========================= ProductCategory – validation =========================

    @Test
    void createProductCategory_blankCode_returns400() throws Exception {
        ProductCategoryCreateDTO dto = new ProductCategoryCreateDTO();
        dto.setCategoryCode("");
        dto.setCategoryName("无编码品类");
        dto.setLevel(1);

        mockMvc.perform(post("/api/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("品类编码不能为空")));
    }
}
