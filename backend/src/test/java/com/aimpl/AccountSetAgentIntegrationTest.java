package com.aimpl;

import com.aimpl.common.enums.AccountSetStatus;
import com.aimpl.common.enums.AccountingSystem;
import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.PricingMethod;
import com.aimpl.common.enums.ProductCategoryGroup;
import com.aimpl.domain.accountset.entity.AccountSet;
import com.aimpl.domain.accountset.mapper.AccountSetMapper;
import com.aimpl.domain.archive.entity.ProductCategory;
import com.aimpl.domain.archive.service.ProductCategoryService;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.research.entity.CustomerProfile;
import com.aimpl.domain.research.mapper.CustomerProfileMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountSetAgentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerProfileMapper customerProfileMapper;

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private AccountSetMapper accountSetMapper;

    // ========================= Helpers =========================

    private ProjectCreateDTO projectDTO(String code, IndustryType industry, List<String> modules) {
        ProjectCreateDTO dto = new ProjectCreateDTO();
        dto.setProjectCode(code);
        dto.setCustomerName("客户_" + code);
        dto.setIndustryType(industry);
        dto.setScale("大型");
        dto.setPmId(1L);
        dto.setModules(modules);
        dto.setRegion("华东");
        dto.setStartDate(LocalDate.of(2024, 6, 1));
        dto.setRemark("帐套推荐测试");
        return dto;
    }

    private Long postProjectAndGetId(ProjectCreateDTO dto) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private CustomerProfile insertProfile(Long projectId, String companyName, Integer totalStaff) {
        CustomerProfile p = new CustomerProfile();
        p.setProjectId(projectId);
        p.setCompanyName(companyName);
        p.setTotalStaff(totalStaff);
        customerProfileMapper.insert(p);
        return p;
    }

    private AccountSet insertAccountSet(Long projectId, int amtDecimals) {
        AccountSet as = new AccountSet();
        as.setProjectId(projectId);
        as.setSetName("精度测试帐套");
        as.setAccountingSystem(AccountingSystem.ENTERPRISE);
        as.setPricingMethod(PricingMethod.MOVING_WEIGHTED_AVG);
        as.setQtyDecimals(0);
        as.setWgtDecimals(3);
        as.setPrcDecimals(2);
        as.setAmtDecimals(amtDecimals);
        as.setUseWeight(true);
        as.setCurrency("CNY");
        as.setFiscalYearStart(1);
        as.setStatus(AccountSetStatus.DRAFT);
        accountSetMapper.insert(as);
        return as;
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private ProductCategory createCategory(String code, String name, ProductCategoryGroup group) {
        ProductCategory cat = new ProductCategory();
        cat.setCategoryCode(code);
        cat.setCategoryName(name);
        cat.setCategoryGroup(group);
        cat.setLevel(1);
        cat.setSortOrder(1);
        return categoryService.createCategory(cat);
    }

    // ========================= Test 1: STEEL_TRADER → 移动加权 + useWeight=true =========================

    @Test
    void recommend_steelTrader_pricingMethodAndUseWeight() throws Exception {
        Long projectId = postProjectAndGetId(
                projectDTO("AS-ST", IndustryType.STEEL_TRADER, List.of("采购", "销售", "仓储")));

        mockMvc.perform(get("/api/account-sets/recommend/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.recommendedPricingMethod").value("移动加权"))
                .andExpect(jsonPath("$.data.recommendedUseWeight").value(true));
    }

    // ========================= Test 2: STEEL_MILL → 个别计价 =========================

    @Test
    void recommend_steelMill_pricingMethod() throws Exception {
        Long projectId = postProjectAndGetId(
                projectDTO("AS-SM", IndustryType.STEEL_MILL, List.of("采购", "销售", "仓储")));

        mockMvc.perform(get("/api/account-sets/recommend/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.recommendedPricingMethod").value("个别计价"));
    }

    // ========================= Test 3: Full profile → HIGH confidence =========================

    @Test
    void recommend_fullProfile_highConfidence() throws Exception {
        Long projectId = postProjectAndGetId(
                projectDTO("AS-HIGH", IndustryType.STEEL_TRADER, List.of("采购", "销售", "仓储")));
        insertProfile(projectId, "全量测试钢贸公司", 200);

        mockMvc.perform(get("/api/account-sets/recommend/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.confidence").value("HIGH"))
                .andExpect(jsonPath("$.data.recommendedSetName").value("全量测试钢贸公司帐套"));
    }

    // ========================= Test 4: Minimal profile → MEDIUM confidence =========================

    @Test
    void recommend_minimalProfile_mediumConfidence() throws Exception {
        Long projectId = postProjectAndGetId(
                projectDTO("AS-MED", IndustryType.STEEL_TRADER, List.of("采购", "销售", "仓储")));

        mockMvc.perform(get("/api/account-sets/recommend/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.confidence").value("MEDIUM"));
    }

    // ========================= Test 5: Create from recommendation → DRAFT status =========================

    @Test
    void createFromRecommendation_createsWithDraftStatus() throws Exception {
        Long projectId = postProjectAndGetId(
                projectDTO("AS-CREATE", IndustryType.STEEL_TRADER, List.of("采购", "销售", "仓储")));
        insertProfile(projectId, "推荐创建公司", 100);

        mockMvc.perform(post("/api/account-sets/create-from-recommendation/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.setName").value("推荐创建公司帐套"));
    }

    // ========================= Test 6: Create when account set exists → 400 =========================

    @Test
    void createFromRecommendation_alreadyExists_returns400() throws Exception {
        Long projectId = postProjectAndGetId(
                projectDTO("AS-DUP", IndustryType.STEEL_TRADER, List.of("采购", "销售", "仓储")));

        mockMvc.perform(post("/api/account-sets/create-from-recommendation/{projectId}", projectId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/account-sets/create-from-recommendation/{projectId}", projectId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("已存在帐套")));
    }

    // ========================= Test 7: Recommendations list contains industry suggestions =========================

    @Test
    void recommend_containsIndustrySpecificSuggestions() throws Exception {
        Long projectId = postProjectAndGetId(
                projectDTO("AS-REC", IndustryType.STEEL_TRADER, List.of("采购", "销售", "仓储")));

        mockMvc.perform(get("/api/account-sets/recommend/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendations").isArray())
                .andExpect(jsonPath("$.data.recommendations", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.data.recommendations[0]", containsString("钢贸商")));
    }

    // ========================= Test 8: InventoryBalance uses AccountSet precision =========================

    @Test
    void inventoryBalance_usesAccountSetAmtDecimals() throws Exception {
        Long projectId = postProjectAndGetId(
                projectDTO("AS-PREC", IndustryType.STEEL_TRADER, List.of("采购", "销售", "仓储")));

        insertAccountSet(projectId, 4);

        ProductCategory cat = createCategory("PRC-GFJ", "精度方矩管", ProductCategoryGroup.SQUARE_PIPE);

        String productJson = objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, Object>() {{
            put("productCode", "PREC-P1");
            put("productName", "精度测试方矩管");
            put("spec", "20*40*1.5");
            put("material", "Q235B");
            put("categoryId", cat.getId());
        }});
        MvcResult productResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk())
                .andReturn();
        Long productId = extractId(productResult);

        String warehouseJson = objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, Object>() {{
            put("warehouseCode", "PREC-WH");
            put("warehouseName", "精度测试仓库");
        }});
        MvcResult warehouseResult = mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(warehouseJson))
                .andExpect(status().isOk())
                .andReturn();
        Long warehouseId = extractId(warehouseResult);

        String inventoryJson = objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, Object>() {{
            put("projectId", projectId);
            put("productId", productId);
            put("warehouseId", warehouseId);
            put("quantity", 10);
            put("weight", new BigDecimal("5.678"));
            put("costUnitPrice", new BigDecimal("1234.5"));
            put("inboundDate", "2024-06-01");
        }});
        mockMvc.perform(post("/api/inventory-balances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventoryJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.costAmount").isNumber())
                .andExpect(jsonPath("$.data.costAmount").value(closeTo(5.678 * 1234.5, 0.0001)));
    }
}
