package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.ProductCategoryGroup;
import com.aimpl.domain.archive.dto.CustomerCreateDTO;
import com.aimpl.domain.archive.dto.ProductCreateDTO;
import com.aimpl.domain.archive.entity.ProductCategory;
import com.aimpl.domain.archive.service.ProductCategoryService;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.training.dto.ExamRecordCreateDTO;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private TraineeProfileMapper traineeProfileMapper;

    // ========================= Helpers =========================

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

    private Long postProjectAndGetId(String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProjectDTO(code))))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private ProductCategory insertCategory(String code, String name, ProductCategoryGroup group) {
        ProductCategory cat = new ProductCategory();
        cat.setCategoryCode(code);
        cat.setCategoryName(name);
        cat.setCategoryGroup(group);
        cat.setLevel(1);
        cat.setSortOrder(1);
        return categoryService.createCategory(cat);
    }

    private ProductCreateDTO validProductDTO(Long categoryId) {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setProductCode("TP001");
        dto.setProductName("测试方矩管");
        dto.setSpec("20*40*1.5");
        dto.setMaterial("Q235B");
        dto.setSteelMill("宝钢");
        dto.setSurface("镀锌");
        dto.setCategoryId(categoryId);
        dto.setUnit("吨");
        dto.setPricingUnit("吨");
        dto.setTaxRate(new BigDecimal("0.13"));
        return dto;
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

    private ExamRecordCreateDTO validExamDTO(Long projectId, Long traineeId) {
        ExamRecordCreateDTO dto = new ExamRecordCreateDTO();
        dto.setProjectId(projectId);
        dto.setTraineeId(traineeId);
        dto.setModule("采购管理");
        dto.setExamType("MODULE");
        dto.setScore(85);
        dto.setRequiredCourse(true);
        return dto;
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ========================= Project – happy paths =========================

    @Test
    void createProject_happyPath() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validProjectDTO("PRJ001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectCode").value("PRJ001"))
                .andExpect(jsonPath("$.data.customerName").value("客户_PRJ001"))
                .andExpect(jsonPath("$.data.industryType").value("STEEL_TRADER"))
                .andExpect(jsonPath("$.data.scale").value("大型"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.region").value("华东"));
    }

    @Test
    void getProject_happyPath() throws Exception {
        Long id = postProjectAndGetId("PRJ002");

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id.intValue()))
                .andExpect(jsonPath("$.data.projectCode").value("PRJ002"));
    }

    @Test
    void listProjects_happyPath() throws Exception {
        postProjectAndGetId("LP-A");
        postProjectAndGetId("LP-B");

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= Project – validation =========================

    @Test
    void createProject_blankProjectCode_returns400() throws Exception {
        ProjectCreateDTO dto = validProjectDTO("X");
        dto.setProjectCode("");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("项目编号不能为空")));
    }

    @Test
    void createProject_blankCustomerName_returns400() throws Exception {
        ProjectCreateDTO dto = validProjectDTO("PRJ-V1");
        dto.setCustomerName("");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("客户名称不能为空")));
    }

    @Test
    void createProject_nullIndustryType_returns400() throws Exception {
        ProjectCreateDTO dto = validProjectDTO("PRJ-V2");
        dto.setIndustryType(null);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("行业类型不能为空")));
    }

    @Test
    void createProject_nullPmId_returns400() throws Exception {
        ProjectCreateDTO dto = validProjectDTO("PRJ-V3");
        dto.setPmId(null);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("请指定项目经理")));
    }

    @Test
    void createProject_blankScale_returns400() throws Exception {
        ProjectCreateDTO dto = validProjectDTO("PRJ-V4");
        dto.setScale("");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("企业规模不能为空")));
    }

    // ========================= Project – business rules =========================

    @Test
    void createProject_duplicateProjectCode_returns400() throws Exception {
        postProjectAndGetId("DUP-PRJ");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validProjectDTO("DUP-PRJ"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("项目编号已存在")));
    }

    // ========================= Product – happy paths =========================

    @Test
    void createProduct_happyPath() throws Exception {
        ProductCategory cat = insertCategory("GFJ", "方矩管", ProductCategoryGroup.SQUARE_PIPE);
        ProductCreateDTO dto = validProductDTO(cat.getId());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.productCode").value("TP001"))
                .andExpect(jsonPath("$.data.productName").value("测试方矩管"))
                .andExpect(jsonPath("$.data.spec").value("20*40*1.5"))
                .andExpect(jsonPath("$.data.material").value("Q235B"))
                .andExpect(jsonPath("$.data.codeAutoGenerated").value(false))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void getProduct_happyPath() throws Exception {
        ProductCategory cat = insertCategory("GFJ", "方矩管", ProductCategoryGroup.SQUARE_PIPE);
        ProductCreateDTO dto = validProductDTO(cat.getId());
        MvcResult res = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        Long id = extractId(res);

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.intValue()))
                .andExpect(jsonPath("$.data.productCode").value("TP001"))
                .andExpect(jsonPath("$.data.productName").value("测试方矩管"));
    }

    @Test
    void listProducts_happyPath() throws Exception {
        ProductCategory cat = insertCategory("GFJ", "方矩管", ProductCategoryGroup.SQUARE_PIPE);

        ProductCreateDTO d1 = validProductDTO(cat.getId());
        d1.setProductCode("LP-P1");
        d1.setProductName("产品A");
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(d1)))
                .andExpect(status().isOk());

        ProductCreateDTO d2 = validProductDTO(cat.getId());
        d2.setProductCode("LP-P2");
        d2.setProductName("产品B");
        d2.setSpec("30*50*2.0");
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(d2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= Product – validation =========================

    @Test
    void createProduct_blankProductName_returns400() throws Exception {
        ProductCategory cat = insertCategory("GFJ", "方矩管", ProductCategoryGroup.SQUARE_PIPE);
        ProductCreateDTO dto = validProductDTO(cat.getId());
        dto.setProductName("");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("品名不能为空")));
    }

    @Test
    void createProduct_blankSpec_returns400() throws Exception {
        ProductCategory cat = insertCategory("GFJ", "方矩管", ProductCategoryGroup.SQUARE_PIPE);
        ProductCreateDTO dto = validProductDTO(cat.getId());
        dto.setSpec("");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("规格不能为空")));
    }

    @Test
    void createProduct_nullCategoryId_returns400() throws Exception {
        ProductCreateDTO dto = validProductDTO(1L);
        dto.setCategoryId(null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("货品类别不能为空")));
    }

    // ========================= Product – business rules =========================

    @Test
    void createProduct_duplicateCode_returns400() throws Exception {
        ProductCategory cat = insertCategory("GFJ", "方矩管", ProductCategoryGroup.SQUARE_PIPE);

        ProductCreateDTO d1 = validProductDTO(cat.getId());
        d1.setProductCode("SAME-CODE");
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(d1)))
                .andExpect(status().isOk());

        ProductCreateDTO d2 = validProductDTO(cat.getId());
        d2.setProductCode("SAME-CODE");
        d2.setProductName("另一品名");
        d2.setSpec("30*50*2.0");
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(d2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("货品编码已存在")));
    }

    // ========================= Product – auto code generation =========================

    @Test
    void createProduct_autoCodeGeneration_squarePipe() throws Exception {
        ProductCategory cat = insertCategory("GFJ", "方矩管", ProductCategoryGroup.SQUARE_PIPE);

        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setProductCode("");
        dto.setProductName("自动编码方矩管");
        dto.setSpec("20*40*1.5");
        dto.setCategoryId(cat.getId());
        dto.setMaterial("Q235B");
        dto.setSteelMill("宝钢");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.productCode").value("GFJ020040015"))
                .andExpect(jsonPath("$.data.codeAutoGenerated").value(true));
    }

    // ========================= Customer – happy paths =========================

    @Test
    void createCustomer_happyPath() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCustomerDTO("C001", "测试钢贸有限公司"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.customerCode").value("C001"))
                .andExpect(jsonPath("$.data.fullName").value("测试钢贸有限公司"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.creditCode").value("91310000MA1K3P2X54"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void getCustomer_happyPath() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCustomerDTO("C002", "获取测试公司"))))
                .andExpect(status().isOk())
                .andReturn();
        Long id = extractId(res);

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.intValue()))
                .andExpect(jsonPath("$.data.customerCode").value("C002"))
                .andExpect(jsonPath("$.data.fullName").value("获取测试公司"));
    }

    @Test
    void listCustomers_happyPath() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCustomerDTO("LC-A", "公司A"))))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCustomerDTO("LC-B", "公司B"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= Customer – validation =========================

    @Test
    void createCustomer_blankCustomerCode_returns400() throws Exception {
        CustomerCreateDTO dto = validCustomerDTO("", "公司X");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("客户编码不能为空")));
    }

    @Test
    void createCustomer_blankFullName_returns400() throws Exception {
        CustomerCreateDTO dto = validCustomerDTO("CX", "");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("客户全称不能为空")));
    }

    @Test
    void createCustomer_invalidPhone_returns400() throws Exception {
        CustomerCreateDTO dto = validCustomerDTO("CP", "电话错误公司");
        dto.setPhone("12345");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("联系电话格式不正确")));
    }

    @Test
    void createCustomer_invalidCreditCode_returns400() throws Exception {
        CustomerCreateDTO dto = validCustomerDTO("CC", "信用代码错误公司");
        dto.setCreditCode("ABC");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("统一社会信用代码必须为18位字母数字")));
    }

    // ========================= Customer – business rules =========================

    @Test
    void createCustomer_duplicateCode_returns400() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCustomerDTO("DUP-C", "公司甲"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCustomerDTO("DUP-C", "公司乙"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("客户编码已存在")));
    }

    @Test
    void createCustomer_duplicateName_returns400() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCustomerDTO("DN-1", "重名公司"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCustomerDTO("DN-2", "重名公司"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("客户名称已存在")));
    }

    // ========================= Exam – happy path =========================

    @Test
    void submitExam_happyPath() throws Exception {
        Long projectId = postProjectAndGetId("EX-PRJ");
        TraineeProfile trainee = insertTrainee(projectId, 100L, "李四", true);

        ExamRecordCreateDTO dto = validExamDTO(projectId, trainee.getId());
        dto.setScore(85);

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.score").value(85))
                .andExpect(jsonPath("$.data.passed").value(true))
                .andExpect(jsonPath("$.data.module").value("采购管理"))
                .andExpect(jsonPath("$.data.requiredCourse").value(true));
    }

    @Test
    void submitExam_scoreBelowPassThreshold_passedIsFalse() throws Exception {
        Long projectId = postProjectAndGetId("EX-FAIL");
        TraineeProfile trainee = insertTrainee(projectId, 101L, "王五", false);

        ExamRecordCreateDTO dto = validExamDTO(projectId, trainee.getId());
        dto.setScore(60);

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(60))
                .andExpect(jsonPath("$.data.passed").value(false));
    }

    // ========================= Exam – validation =========================

    @Test
    void submitExam_blankModule_returns400() throws Exception {
        ExamRecordCreateDTO dto = new ExamRecordCreateDTO();
        dto.setProjectId(1L);
        dto.setTraineeId(1L);
        dto.setModule("");
        dto.setExamType("MODULE");
        dto.setScore(80);

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("考核模块不能为空")));
    }

    @Test
    void submitExam_nullScore_returns400() throws Exception {
        ExamRecordCreateDTO dto = new ExamRecordCreateDTO();
        dto.setProjectId(1L);
        dto.setTraineeId(1L);
        dto.setModule("采购管理");
        dto.setExamType("MODULE");

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("考核分数不能为空")));
    }

    @Test
    void submitExam_scoreNegative_returns400() throws Exception {
        ExamRecordCreateDTO dto = new ExamRecordCreateDTO();
        dto.setProjectId(1L);
        dto.setTraineeId(1L);
        dto.setModule("采购管理");
        dto.setExamType("MODULE");
        dto.setScore(-1);

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("考核分数不能为负数")));
    }

    @Test
    void submitExam_scoreOver100_returns400() throws Exception {
        ExamRecordCreateDTO dto = new ExamRecordCreateDTO();
        dto.setProjectId(1L);
        dto.setTraineeId(1L);
        dto.setModule("采购管理");
        dto.setExamType("MODULE");
        dto.setScore(101);

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("考核分数不能超过100")));
    }

    @Test
    void submitExam_nullProjectId_returns400() throws Exception {
        ExamRecordCreateDTO dto = new ExamRecordCreateDTO();
        dto.setTraineeId(1L);
        dto.setModule("采购管理");
        dto.setExamType("MODULE");
        dto.setScore(80);

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目ID不能为空")));
    }

    @Test
    void submitExam_blankExamType_returns400() throws Exception {
        ExamRecordCreateDTO dto = new ExamRecordCreateDTO();
        dto.setProjectId(1L);
        dto.setTraineeId(1L);
        dto.setModule("采购管理");
        dto.setExamType("");
        dto.setScore(80);

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("考核类型不能为空")));
    }

    // ========================= Go-live check =========================

    @Test
    void goLiveCheck_allKaUsersPassed_returnsReady() throws Exception {
        Long projectId = postProjectAndGetId("GL-PASS");
        TraineeProfile ka1 = insertTrainee(projectId, 201L, "KA用户甲", true);
        TraineeProfile ka2 = insertTrainee(projectId, 202L, "KA用户乙", true);

        ExamRecordCreateDTO e1 = validExamDTO(projectId, ka1.getId());
        e1.setScore(80);
        e1.setRequiredCourse(true);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(e1)))
                .andExpect(status().isOk());

        ExamRecordCreateDTO e2 = validExamDTO(projectId, ka2.getId());
        e2.setScore(75);
        e2.setRequiredCourse(true);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(e2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/exams/go-live-check/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.goLiveReady").value(true))
                .andExpect(jsonPath("$.data.message", containsString("允许推进上线")));
    }

    @Test
    void goLiveCheck_someKaUserFailed_returnsNotReady() throws Exception {
        Long projectId = postProjectAndGetId("GL-FAIL");
        TraineeProfile ka1 = insertTrainee(projectId, 301L, "KA通过者", true);
        TraineeProfile ka2 = insertTrainee(projectId, 302L, "KA未通过者", true);

        ExamRecordCreateDTO e1 = validExamDTO(projectId, ka1.getId());
        e1.setScore(80);
        e1.setRequiredCourse(true);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(e1)))
                .andExpect(status().isOk());

        ExamRecordCreateDTO e2 = validExamDTO(projectId, ka2.getId());
        e2.setScore(60);
        e2.setRequiredCourse(true);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(e2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/exams/go-live-check/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.goLiveReady").value(false))
                .andExpect(jsonPath("$.data.message", containsString("不允许推进上线")));
    }

    @Test
    void goLiveCheck_kaUserNoExam_returnsNotReady() throws Exception {
        Long projectId = postProjectAndGetId("GL-NOEX");
        insertTrainee(projectId, 401L, "KA无考试", true);

        mockMvc.perform(get("/api/exams/go-live-check/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.goLiveReady").value(false));
    }

    @Test
    void goLiveCheck_mixedKaAndNonKa_onlyChecksKa() throws Exception {
        Long projectId = postProjectAndGetId("GL-MIX");
        TraineeProfile ka = insertTrainee(projectId, 501L, "KA用户", true);
        insertTrainee(projectId, 502L, "普通用户", false);

        ExamRecordCreateDTO e = validExamDTO(projectId, ka.getId());
        e.setScore(90);
        e.setRequiredCourse(true);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(e)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/exams/go-live-check/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.goLiveReady").value(true));
    }
}
