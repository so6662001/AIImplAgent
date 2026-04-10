package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.ProductCategoryGroup;
import com.aimpl.domain.archive.dto.ProductCategoryCreateDTO;
import com.aimpl.domain.archive.dto.RelatedUnitCreateDTO;
import com.aimpl.domain.archive.dto.SupplierCreateDTO;
import com.aimpl.domain.archive.dto.WarehouseCreateDTO;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.training.dto.ExamRecordCreateDTO;
import com.aimpl.domain.training.dto.TraineeProfileCreateDTO;
import com.aimpl.domain.training.dto.TrainingDailyLogCreateDTO;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.entity.TrainingDailyLog;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.aimpl.domain.workforce.dto.EngineerCreateDTO;
import com.aimpl.domain.workforce.dto.EngineerWorklogCreateDTO;
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
class GapFillIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TraineeProfileMapper traineeProfileMapper;

    // ========================= Helpers =========================

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
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
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validProjectDTO(code))))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private Long createAndGetId(String url, Object dto) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private TraineeProfileCreateDTO validTraineeDTO(Long projectId, String name) {
        TraineeProfileCreateDTO dto = new TraineeProfileCreateDTO();
        dto.setProjectId(projectId);
        dto.setEmployeeName(name);
        dto.setRole("操作员");
        dto.setDepartment("销售部");
        dto.setKaUser(false);
        return dto;
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

    private ProductCategoryCreateDTO validCategoryDTO(String code, String name) {
        ProductCategoryCreateDTO dto = new ProductCategoryCreateDTO();
        dto.setCategoryCode(code);
        dto.setCategoryName(name);
        dto.setLevel(1);
        dto.setSortOrder(1);
        dto.setCategoryGroup(ProductCategoryGroup.SQUARE_PIPE);
        dto.setDefaultUnit("吨");
        return dto;
    }

    private RelatedUnitCreateDTO validRelatedUnitDTO(String code, String fullName) {
        RelatedUnitCreateDTO dto = new RelatedUnitCreateDTO();
        dto.setUnitCode(code);
        dto.setFullName(fullName);
        dto.setUnitType("CONSTRUCTION");
        dto.setCreditCode("91310000MA1K3P2X54");
        dto.setContactPerson("王五");
        dto.setPhone("13700137000");
        dto.setAddress("北京市朝阳区");
        dto.setBankName("工商银行");
        dto.setBankAccount("6222021234567890123");
        return dto;
    }

    private TrainingDailyLogCreateDTO validDailyLogDTO(Long projectId, LocalDate date) {
        TrainingDailyLogCreateDTO dto = new TrainingDailyLogCreateDTO();
        dto.setProjectId(projectId);
        dto.setLogDate(date);
        dto.setTopic("采购模块操作培训");
        dto.setTrainerName("讲师A");
        dto.setAttendeeCount(20);
        dto.setSignInCompleted(true);
        dto.setCoursewareUploaded(true);
        dto.setSummaryUploaded(false);
        dto.setExamConducted(false);
        dto.setDailyReportSubmitted(true);
        return dto;
    }

    private EngineerCreateDTO validEngineerDTO(String code, String name) {
        EngineerCreateDTO dto = new EngineerCreateDTO();
        dto.setEngineerCode(code);
        dto.setName(name);
        dto.setLevel("SENIOR");
        dto.setSkills("Java,SQL");
        dto.setJoinDate(LocalDate.of(2023, 1, 1));
        dto.setPhone("13800138001");
        dto.setEmail("test@example.com");
        return dto;
    }

    private EngineerWorklogCreateDTO validWorklogDTO(Long engineerId, Long projectId, LocalDate date) {
        EngineerWorklogCreateDTO dto = new EngineerWorklogCreateDTO();
        dto.setEngineerId(engineerId);
        dto.setProjectId(projectId);
        dto.setWorkDate(date);
        dto.setTasksPlan("完成采购模块配置");
        dto.setTasksCompleted("已完成基础配置");
        dto.setDocumentsSubmitted("配置手册v1");
        dto.setIssues("无");
        dto.setNextDayPlan("继续销售模块");
        return dto;
    }

    private TraineeProfile insertTrainee(Long projectId, long employeeId,
                                         String name, boolean kaUser) {
        TraineeProfile tp = new TraineeProfile();
        tp.setProjectId(projectId);
        tp.setEmployeeId(employeeId);
        tp.setEmployeeName(name);
        tp.setRole("操作员");
        tp.setDepartment("销售部");
        tp.setKaUser(kaUser);
        tp.setProgressPercent(0);
        tp.setAttendanceDays(0);
        tp.setTotalDays(10);
        tp.setRiskLevel("NORMAL");
        traineeProfileMapper.insert(tp);
        return tp;
    }

    private ExamRecordCreateDTO validExamDTO(Long projectId, Long traineeId, String module, int score) {
        ExamRecordCreateDTO dto = new ExamRecordCreateDTO();
        dto.setProjectId(projectId);
        dto.setTraineeId(traineeId);
        dto.setModule(module);
        dto.setExamType("MODULE");
        dto.setScore(score);
        dto.setRequiredCourse(true);
        return dto;
    }

    // ========================= TraineeProfile – happy path =========================

    @Test
    void trainee_createAndVerifyKaMarking() throws Exception {
        Long projectId = createProject("GF-TP-01");
        TraineeProfileCreateDTO dto = validTraineeDTO(projectId, "KA用户甲");
        dto.setKaUser(true);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.employeeName").value("KA用户甲"))
                .andExpect(jsonPath("$.data.kaUser").value(true))
                .andExpect(jsonPath("$.data.role").value("操作员"))
                .andExpect(jsonPath("$.data.progressPercent").value(0))
                .andExpect(jsonPath("$.data.riskLevel").value("NORMAL"));
    }

    @Test
    void trainee_createNonKaUser() throws Exception {
        Long projectId = createProject("GF-TP-02");
        TraineeProfileCreateDTO dto = validTraineeDTO(projectId, "普通学员");

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kaUser").value(false));
    }

    @Test
    void trainee_getById() throws Exception {
        Long projectId = createProject("GF-TP-03");
        Long traineeId = createAndGetId("/api/trainees", validTraineeDTO(projectId, "学员查询"));

        mockMvc.perform(get("/api/trainees/{id}", traineeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(traineeId.intValue()))
                .andExpect(jsonPath("$.data.employeeName").value("学员查询"));
    }

    @Test
    void trainee_listByProject() throws Exception {
        Long projectId = createProject("GF-TP-04");
        createAndGetId("/api/trainees", validTraineeDTO(projectId, "学员A"));
        createAndGetId("/api/trainees", validTraineeDTO(projectId, "学员B"));

        mockMvc.perform(get("/api/trainees").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= TraineeProfile – validation =========================

    @Test
    void trainee_blankName_returns400() throws Exception {
        Long projectId = createProject("GF-TP-V1");
        TraineeProfileCreateDTO dto = validTraineeDTO(projectId, "");

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("员工姓名不能为空")));
    }

    @Test
    void trainee_nullProjectId_returns400() throws Exception {
        TraineeProfileCreateDTO dto = validTraineeDTO(1L, "某学员");
        dto.setProjectId(null);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("项目ID不能为空")));
    }

    // ========================= TraineeProfile – business rules =========================

    @Test
    void trainee_duplicateNameInSameProject_returns400() throws Exception {
        Long projectId = createProject("GF-TP-DUP");
        createAndGetId("/api/trainees", validTraineeDTO(projectId, "重复姓名"));

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validTraineeDTO(projectId, "重复姓名"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("该项目下员工姓名已存在")));
    }

    @Test
    void trainee_invalidProjectId_returns400() throws Exception {
        TraineeProfileCreateDTO dto = validTraineeDTO(999999L, "无效项目学员");

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ========================= Supplier – happy path =========================

    @Test
    void supplier_createHappyPath() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validSupplierDTO("SUP-001", "测试供应商有限公司"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.supplierCode").value("SUP-001"))
                .andExpect(jsonPath("$.data.fullName").value("测试供应商有限公司"))
                .andExpect(jsonPath("$.data.creditCode").value("91310000MA1K3P2X54"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.taxRate").value(0.13));
    }

    @Test
    void supplier_getById() throws Exception {
        Long id = createAndGetId("/api/suppliers", validSupplierDTO("SUP-002", "获取供应商"));

        mockMvc.perform(get("/api/suppliers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.intValue()))
                .andExpect(jsonPath("$.data.supplierCode").value("SUP-002"));
    }

    @Test
    void supplier_list() throws Exception {
        createAndGetId("/api/suppliers", validSupplierDTO("SUP-L1", "供应商A"));

        SupplierCreateDTO dto2 = validSupplierDTO("SUP-L2", "供应商B");
        dto2.setCreditCode("91310000MA1K3P2X55");
        createAndGetId("/api/suppliers", dto2);

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= Supplier – validation =========================

    @Test
    void supplier_blankCode_returns400() throws Exception {
        SupplierCreateDTO dto = validSupplierDTO("", "空编码供应商");

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("供应商编码不能为空")));
    }

    @Test
    void supplier_invalidCreditCode_returns400() throws Exception {
        SupplierCreateDTO dto = validSupplierDTO("SUP-CC", "信用代码错误");
        dto.setCreditCode("INVALID");

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("统一社会信用代码必须为18位字母数字")));
    }

    @Test
    void supplier_invalidPhone_returns400() throws Exception {
        SupplierCreateDTO dto = validSupplierDTO("SUP-PH", "电话错误供应商");
        dto.setPhone("12345");

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("手机号格式不正确")));
    }

    // ========================= Supplier – business rules =========================

    @Test
    void supplier_duplicateCode_returns400() throws Exception {
        createAndGetId("/api/suppliers", validSupplierDTO("SUP-DUP", "供应商甲"));

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validSupplierDTO("SUP-DUP", "供应商乙"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("供应商编码已存在")));
    }

    @Test
    void supplier_duplicateName_returns400() throws Exception {
        createAndGetId("/api/suppliers", validSupplierDTO("SUP-DN1", "重名供应商"));

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validSupplierDTO("SUP-DN2", "重名供应商"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("供应商名称已存在")));
    }

    // ========================= Warehouse – happy path =========================

    @Test
    void warehouse_createHappyPath() throws Exception {
        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validWarehouseDTO("WH-001", "主仓库"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.warehouseCode").value("WH-001"))
                .andExpect(jsonPath("$.data.warehouseName").value("主仓库"))
                .andExpect(jsonPath("$.data.areaSqm").value(5000))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.craneCount").value(2));
    }

    @Test
    void warehouse_getById() throws Exception {
        Long id = createAndGetId("/api/warehouses", validWarehouseDTO("WH-002", "获取仓库"));

        mockMvc.perform(get("/api/warehouses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.intValue()))
                .andExpect(jsonPath("$.data.warehouseCode").value("WH-002"));
    }

    @Test
    void warehouse_list() throws Exception {
        createAndGetId("/api/warehouses", validWarehouseDTO("WH-L1", "仓库A"));
        createAndGetId("/api/warehouses", validWarehouseDTO("WH-L2", "仓库B"));

        mockMvc.perform(get("/api/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= Warehouse – validation =========================

    @Test
    void warehouse_blankCode_returns400() throws Exception {
        WarehouseCreateDTO dto = validWarehouseDTO("", "无编码仓库");

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("仓库编码不能为空")));
    }

    @Test
    void warehouse_negativeAreaSqm_returns400() throws Exception {
        WarehouseCreateDTO dto = validWarehouseDTO("WH-NEG", "负面积仓库");
        dto.setAreaSqm(new BigDecimal("-100"));

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("面积不能为负数")));
    }

    @Test
    void warehouse_negativeCraneCount_returns400() throws Exception {
        WarehouseCreateDTO dto = validWarehouseDTO("WH-CR", "行车负数仓库");
        dto.setCraneCount(-1);

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("行车数量不能为负数")));
    }

    // ========================= Warehouse – business rules =========================

    @Test
    void warehouse_duplicateCode_returns400() throws Exception {
        createAndGetId("/api/warehouses", validWarehouseDTO("WH-DUP", "仓库甲"));

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validWarehouseDTO("WH-DUP", "仓库乙"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("仓库编码已存在")));
    }

    // ========================= ProductCategory – happy path =========================

    @Test
    void category_createWithCategoryGroup() throws Exception {
        ProductCategoryCreateDTO dto = validCategoryDTO("CAT-001", "方矩管");

        mockMvc.perform(post("/api/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.categoryCode").value("CAT-001"))
                .andExpect(jsonPath("$.data.categoryName").value("方矩管"))
                .andExpect(jsonPath("$.data.categoryGroup").value("SQUARE_PIPE"))
                .andExpect(jsonPath("$.data.defaultUnit").value("吨"))
                .andExpect(jsonPath("$.data.level").value(1))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void category_getById() throws Exception {
        Long id = createAndGetId("/api/product-categories", validCategoryDTO("CAT-002", "板材"));

        mockMvc.perform(get("/api/product-categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.intValue()))
                .andExpect(jsonPath("$.data.categoryCode").value("CAT-002"));
    }

    @Test
    void category_list() throws Exception {
        createAndGetId("/api/product-categories", validCategoryDTO("CAT-L1", "品类A"));
        createAndGetId("/api/product-categories", validCategoryDTO("CAT-L2", "品类B"));

        mockMvc.perform(get("/api/product-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= ProductCategory – validation =========================

    @Test
    void category_blankCode_returns400() throws Exception {
        ProductCategoryCreateDTO dto = validCategoryDTO("", "无编码品类");

        mockMvc.perform(post("/api/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("品类编码不能为空")));
    }

    @Test
    void category_blankName_returns400() throws Exception {
        ProductCategoryCreateDTO dto = validCategoryDTO("CAT-VN", "");

        mockMvc.perform(post("/api/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("品类名称不能为空")));
    }

    // ========================= ProductCategory – business rules =========================

    @Test
    void category_duplicateCode_returns400() throws Exception {
        createAndGetId("/api/product-categories", validCategoryDTO("CAT-DUP", "品类甲"));

        mockMvc.perform(post("/api/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCategoryDTO("CAT-DUP", "品类乙"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("品类编码已存在")));
    }

    @Test
    void category_invalidParentId_returns400() throws Exception {
        ProductCategoryCreateDTO dto = validCategoryDTO("CAT-PAR", "子品类");
        dto.setParentId(999999L);

        mockMvc.perform(post("/api/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("上级品类不存在")));
    }

    // ========================= RelatedUnit – happy path =========================

    @Test
    void relatedUnit_createHappyPath() throws Exception {
        mockMvc.perform(post("/api/related-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRelatedUnitDTO("RU-001", "关联单位有限公司"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.unitCode").value("RU-001"))
                .andExpect(jsonPath("$.data.fullName").value("关联单位有限公司"))
                .andExpect(jsonPath("$.data.unitType").value("CONSTRUCTION"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void relatedUnit_getById() throws Exception {
        Long id = createAndGetId("/api/related-units", validRelatedUnitDTO("RU-002", "获取单位"));

        mockMvc.perform(get("/api/related-units/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.intValue()))
                .andExpect(jsonPath("$.data.unitCode").value("RU-002"));
    }

    @Test
    void relatedUnit_list() throws Exception {
        createAndGetId("/api/related-units", validRelatedUnitDTO("RU-L1", "单位A"));
        createAndGetId("/api/related-units", validRelatedUnitDTO("RU-L2", "单位B"));

        mockMvc.perform(get("/api/related-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= RelatedUnit – validation =========================

    @Test
    void relatedUnit_blankCode_returns400() throws Exception {
        RelatedUnitCreateDTO dto = validRelatedUnitDTO("", "无编码单位");

        mockMvc.perform(post("/api/related-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("单位编码不能为空")));
    }

    @Test
    void relatedUnit_blankUnitType_returns400() throws Exception {
        RelatedUnitCreateDTO dto = validRelatedUnitDTO("RU-VT", "无类型单位");
        dto.setUnitType("");

        mockMvc.perform(post("/api/related-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("单位类型不能为空")));
    }

    // ========================= RelatedUnit – business rules =========================

    @Test
    void relatedUnit_duplicateCode_returns400() throws Exception {
        createAndGetId("/api/related-units", validRelatedUnitDTO("RU-DUP", "单位甲"));

        mockMvc.perform(post("/api/related-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRelatedUnitDTO("RU-DUP", "单位乙"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("单位编码已存在")));
    }

    @Test
    void relatedUnit_duplicateName_returns400() throws Exception {
        createAndGetId("/api/related-units", validRelatedUnitDTO("RU-DN1", "重名单位"));

        mockMvc.perform(post("/api/related-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRelatedUnitDTO("RU-DN2", "重名单位"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("单位名称已存在")));
    }

    // ========================= TrainingDailyLog – happy path =========================

    @Test
    void dailyLog_createHappyPath() throws Exception {
        Long projectId = createProject("GF-DL-01");
        TrainingDailyLogCreateDTO dto = validDailyLogDTO(projectId, LocalDate.of(2024, 7, 1));

        mockMvc.perform(post("/api/training-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.topic").value("采购模块操作培训"))
                .andExpect(jsonPath("$.data.trainerName").value("讲师A"))
                .andExpect(jsonPath("$.data.attendeeCount").value(20))
                .andExpect(jsonPath("$.data.signInCompleted").value(true))
                .andExpect(jsonPath("$.data.coursewareUploaded").value(true))
                .andExpect(jsonPath("$.data.summaryUploaded").value(false))
                .andExpect(jsonPath("$.data.dailyReportSubmitted").value(true));
    }

    @Test
    void dailyLog_listByProject() throws Exception {
        Long projectId = createProject("GF-DL-02");
        createAndGetId("/api/training-logs", validDailyLogDTO(projectId, LocalDate.of(2024, 7, 1)));
        createAndGetId("/api/training-logs", validDailyLogDTO(projectId, LocalDate.of(2024, 7, 2)));

        mockMvc.perform(get("/api/training-logs").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= TrainingDailyLog – validation =========================

    @Test
    void dailyLog_nullProjectId_returns400() throws Exception {
        TrainingDailyLogCreateDTO dto = validDailyLogDTO(1L, LocalDate.of(2024, 7, 1));
        dto.setProjectId(null);

        mockMvc.perform(post("/api/training-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("项目ID不能为空")));
    }

    @Test
    void dailyLog_blankTopic_returns400() throws Exception {
        Long projectId = createProject("GF-DL-V1");
        TrainingDailyLogCreateDTO dto = validDailyLogDTO(projectId, LocalDate.of(2024, 7, 1));
        dto.setTopic("");

        mockMvc.perform(post("/api/training-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("培训主题不能为空")));
    }

    // ========================= TrainingDailyLog – business rules =========================

    @Test
    void dailyLog_duplicateDateSameProject_returns400() throws Exception {
        Long projectId = createProject("GF-DL-DUP");
        LocalDate date = LocalDate.of(2024, 7, 10);
        createAndGetId("/api/training-logs", validDailyLogDTO(projectId, date));

        mockMvc.perform(post("/api/training-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validDailyLogDTO(projectId, date))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("该项目当天已存在培训日志")));
    }

    @Test
    void dailyLog_updateBooleanFlags() throws Exception {
        Long projectId = createProject("GF-DL-UPD");
        Long logId = createAndGetId("/api/training-logs", validDailyLogDTO(projectId, LocalDate.of(2024, 7, 15)));

        TrainingDailyLog update = new TrainingDailyLog();
        update.setSignInCompleted(true);
        update.setCoursewareUploaded(true);
        update.setSummaryUploaded(true);
        update.setExamConducted(true);
        update.setDailyReportSubmitted(true);

        mockMvc.perform(put("/api/training-logs/{id}", logId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signInCompleted").value(true))
                .andExpect(jsonPath("$.data.coursewareUploaded").value(true))
                .andExpect(jsonPath("$.data.summaryUploaded").value(true))
                .andExpect(jsonPath("$.data.examConducted").value(true))
                .andExpect(jsonPath("$.data.dailyReportSubmitted").value(true));
    }

    @Test
    void dailyLog_invalidProjectId_returns400() throws Exception {
        TrainingDailyLogCreateDTO dto = validDailyLogDTO(999999L, LocalDate.of(2024, 7, 1));

        mockMvc.perform(post("/api/training-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ========================= EngineerWorklog – happy path =========================

    @Test
    void worklog_createHappyPath() throws Exception {
        Long projectId = createProject("GF-WL-01");
        Long engineerId = createAndGetId("/api/engineers", validEngineerDTO("GF-ENG-01", "工程师甲"));

        EngineerWorklogCreateDTO dto = validWorklogDTO(engineerId, projectId, LocalDate.of(2024, 7, 1));

        mockMvc.perform(post("/api/engineer-worklogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.engineerId").value(engineerId.intValue()))
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.tasksPlan").value("完成采购模块配置"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void worklog_listByEngineer() throws Exception {
        Long projectId = createProject("GF-WL-02");
        Long engineerId = createAndGetId("/api/engineers", validEngineerDTO("GF-ENG-02", "工程师乙"));

        createAndGetId("/api/engineer-worklogs", validWorklogDTO(engineerId, projectId, LocalDate.of(2024, 7, 1)));
        createAndGetId("/api/engineer-worklogs", validWorklogDTO(engineerId, projectId, LocalDate.of(2024, 7, 2)));

        mockMvc.perform(get("/api/engineer-worklogs").param("engineerId", engineerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= EngineerWorklog – submit =========================

    @Test
    void worklog_submitDraftToSubmitted() throws Exception {
        Long projectId = createProject("GF-WL-SUB");
        Long engineerId = createAndGetId("/api/engineers", validEngineerDTO("GF-ENG-SUB", "提交工程师"));
        Long worklogId = createAndGetId("/api/engineer-worklogs",
                validWorklogDTO(engineerId, projectId, LocalDate.of(2024, 7, 5)));

        mockMvc.perform(put("/api/engineer-worklogs/{id}/submit", worklogId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(worklogId.intValue()))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    void worklog_submitAlreadySubmitted_returns400() throws Exception {
        Long projectId = createProject("GF-WL-SS");
        Long engineerId = createAndGetId("/api/engineers", validEngineerDTO("GF-ENG-SS", "重复提交工程师"));
        Long worklogId = createAndGetId("/api/engineer-worklogs",
                validWorklogDTO(engineerId, projectId, LocalDate.of(2024, 7, 6)));

        mockMvc.perform(put("/api/engineer-worklogs/{id}/submit", worklogId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/engineer-worklogs/{id}/submit", worklogId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("只有草稿状态的日志才能提交")));
    }

    // ========================= EngineerWorklog – validation =========================

    @Test
    void worklog_nullEngineerId_returns400() throws Exception {
        EngineerWorklogCreateDTO dto = validWorklogDTO(1L, 1L, LocalDate.of(2024, 7, 1));
        dto.setEngineerId(null);

        mockMvc.perform(post("/api/engineer-worklogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("工程师ID不能为空")));
    }

    @Test
    void worklog_blankTasksPlan_returns400() throws Exception {
        EngineerWorklogCreateDTO dto = validWorklogDTO(1L, 1L, LocalDate.of(2024, 7, 1));
        dto.setTasksPlan("");

        mockMvc.perform(post("/api/engineer-worklogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("计划任务不能为空")));
    }

    // ========================= EngineerWorklog – business rules =========================

    @Test
    void worklog_duplicateDatePerEngineer_returns400() throws Exception {
        Long projectId = createProject("GF-WL-DUP");
        Long engineerId = createAndGetId("/api/engineers", validEngineerDTO("GF-ENG-DUP", "重复日期工程师"));
        LocalDate date = LocalDate.of(2024, 7, 20);
        createAndGetId("/api/engineer-worklogs", validWorklogDTO(engineerId, projectId, date));

        mockMvc.perform(post("/api/engineer-worklogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validWorklogDTO(engineerId, projectId, date))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("该工程师当天已存在工作日志")));
    }

    @Test
    void worklog_invalidEngineerId_returns400() throws Exception {
        Long projectId = createProject("GF-WL-IE");
        EngineerWorklogCreateDTO dto = validWorklogDTO(999999L, projectId, LocalDate.of(2024, 7, 1));

        mockMvc.perform(post("/api/engineer-worklogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("工程师不存在")));
    }

    @Test
    void worklog_invalidProjectId_returns400() throws Exception {
        Long engineerId = createAndGetId("/api/engineers", validEngineerDTO("GF-ENG-IP", "无效项目工程师"));
        EngineerWorklogCreateDTO dto = validWorklogDTO(engineerId, 999999L, LocalDate.of(2024, 7, 1));

        mockMvc.perform(post("/api/engineer-worklogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ========================= TrainingDashboard =========================

    @Test
    void dashboard_aggregatedNumbers() throws Exception {
        Long projectId = createProject("GF-DASH-01");

        TraineeProfile ka1 = insertTrainee(projectId, 901L, "KA仪表盘用户1", true);
        TraineeProfile ka2 = insertTrainee(projectId, 902L, "KA仪表盘用户2", true);
        insertTrainee(projectId, 903L, "普通仪表盘用户", false);

        ExamRecordCreateDTO e1 = validExamDTO(projectId, ka1.getId(), "采购管理", 85);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(e1)))
                .andExpect(status().isOk());

        ExamRecordCreateDTO e2 = validExamDTO(projectId, ka2.getId(), "采购管理", 60);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(e2)))
                .andExpect(status().isOk());

        ExamRecordCreateDTO e3 = validExamDTO(projectId, ka1.getId(), "销售管理", 90);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(e3)))
                .andExpect(status().isOk());

        TrainingDailyLogCreateDTO log1 = validDailyLogDTO(projectId, LocalDate.of(2024, 8, 1));
        log1.setSignInCompleted(true);
        log1.setCoursewareUploaded(true);
        log1.setSummaryUploaded(true);
        log1.setExamConducted(true);
        log1.setDailyReportSubmitted(true);
        createAndGetId("/api/training-logs", log1);

        TrainingDailyLogCreateDTO log2 = validDailyLogDTO(projectId, LocalDate.of(2024, 8, 2));
        log2.setSignInCompleted(true);
        log2.setCoursewareUploaded(false);
        log2.setSummaryUploaded(false);
        log2.setExamConducted(false);
        log2.setDailyReportSubmitted(false);
        createAndGetId("/api/training-logs", log2);

        mockMvc.perform(get("/api/training-dashboard/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.totalTrainees").value(3))
                .andExpect(jsonPath("$.data.kaUserCount").value(2))
                .andExpect(jsonPath("$.data.overallPassRate").isNumber())
                .andExpect(jsonPath("$.data.moduleStats").isArray())
                .andExpect(jsonPath("$.data.moduleStats", hasSize(2)))
                .andExpect(jsonPath("$.data.documentCompletionRate").isNumber())
                .andExpect(jsonPath("$.data.attendanceRate").isNumber());
    }

    @Test
    void dashboard_emptyProject() throws Exception {
        Long projectId = createProject("GF-DASH-02");

        mockMvc.perform(get("/api/training-dashboard/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTrainees").value(0))
                .andExpect(jsonPath("$.data.kaUserCount").value(0))
                .andExpect(jsonPath("$.data.overallPassRate").value(0))
                .andExpect(jsonPath("$.data.documentCompletionRate").value(0));
    }

    @Test
    void dashboard_invalidProjectId_returns400() throws Exception {
        mockMvc.perform(get("/api/training-dashboard/{projectId}", 999999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    @Test
    void dashboard_goLiveReadyWhenAllKaPassed() throws Exception {
        Long projectId = createProject("GF-DASH-GL");
        TraineeProfile ka = insertTrainee(projectId, 911L, "KA全通过", true);

        ExamRecordCreateDTO exam = validExamDTO(projectId, ka.getId(), "采购管理", 80);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(exam)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/training-dashboard/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.goLiveReady").value(true));
    }

    @Test
    void dashboard_verifyDocumentCompletionRate() throws Exception {
        Long projectId = createProject("GF-DASH-DOC");

        TrainingDailyLogCreateDTO log = validDailyLogDTO(projectId, LocalDate.of(2024, 9, 1));
        log.setSignInCompleted(true);
        log.setCoursewareUploaded(true);
        log.setSummaryUploaded(true);
        log.setExamConducted(true);
        log.setDailyReportSubmitted(true);
        createAndGetId("/api/training-logs", log);

        mockMvc.perform(get("/api/training-dashboard/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentCompletionRate").value(100.00));
    }

    @Test
    void dashboard_verifyPassRateCalculation() throws Exception {
        Long projectId = createProject("GF-DASH-PR");
        TraineeProfile t1 = insertTrainee(projectId, 921L, "考试学员1", false);
        TraineeProfile t2 = insertTrainee(projectId, 922L, "考试学员2", false);

        ExamRecordCreateDTO pass = validExamDTO(projectId, t1.getId(), "模块A", 80);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(pass)))
                .andExpect(status().isOk());

        ExamRecordCreateDTO fail = validExamDTO(projectId, t2.getId(), "模块A", 50);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fail)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/training-dashboard/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallPassRate").value(50.00));
    }
}
