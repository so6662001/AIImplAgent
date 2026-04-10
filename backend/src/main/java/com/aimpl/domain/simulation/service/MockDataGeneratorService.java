package com.aimpl.domain.simulation.service;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.research.entity.CustomerProfile;
import com.aimpl.domain.research.mapper.CustomerProfileMapper;
import com.aimpl.domain.simulation.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MockDataGeneratorService {

    private final CustomerProfileMapper customerProfileMapper;

    private static final String[] STEEL_MILLS = {"宝钢", "沙钢", "鞍钢", "河钢", "首钢", "马钢", "太钢", "柳钢", "日照钢铁", "永钢"};

    private static final String[][] TRADER_PRODUCTS = {
            {"热轧卷板", "3.0*1250*C", "Q235B", "吨", "板材"},
            {"热轧卷板", "5.5*1500*C", "Q235B", "吨", "板材"},
            {"热轧卷板", "4.0*1250*C", "Q355B", "吨", "板材"},
            {"冷轧板", "1.0*1250*C", "SPCC", "吨", "板材"},
            {"冷轧板", "0.8*1000*C", "SPCC", "吨", "板材"},
            {"冷轧板", "1.5*1250*C", "DC01", "吨", "板材"},
            {"镀锌板", "1.0*1250*C", "DX51D+Z", "吨", "板材"},
            {"镀锌板", "0.7*1000*C", "DX51D+Z", "吨", "板材"},
            {"螺纹钢", "Φ20*12000", "HRB400E", "吨", "长材"},
            {"螺纹钢", "Φ16*12000", "HRB400E", "吨", "长材"},
            {"螺纹钢", "Φ25*12000", "HRB400E", "吨", "长材"},
            {"H型钢", "200*200*8*12", "Q235B", "吨", "型材"},
            {"H型钢", "300*300*10*15", "Q235B", "吨", "型材"},
            {"中厚板", "20*2200*8000", "Q235B", "吨", "板材"},
            {"中厚板", "16*2000*6000", "Q355B", "吨", "板材"},
    };

    private static final String[][] MILL_RAW_MATERIALS = {
            {"铁矿石", "品位62%", "PB粉", "吨", "原料"},
            {"焦炭", "一级冶金焦", "准一级", "吨", "原料"},
            {"废钢", "重型废钢", "重废", "吨", "原料"},
            {"合金", "硅锰合金", "FeMn65Si17", "吨", "原料"},
            {"石灰石", "冶金级", "CaO≥52%", "吨", "辅料"},
    };

    private static final String[][] PROCESSING_INCOMING = {
            {"热轧卷板", "3.0*1500*C", "Q235B", "吨", "来料"},
            {"热轧卷板", "4.0*1250*C", "Q235B", "吨", "来料"},
            {"冷轧板", "1.0*1250*C", "SPCC", "吨", "来料"},
            {"镀锌板", "1.0*1250*C", "DX51D+Z", "吨", "来料"},
            {"中厚板", "20*2200*8000", "Q235B", "吨", "来料"},
    };

    private static final String[][] PROCESSING_OUTGOING = {
            {"纵剪带钢", "3.0*156*C", "Q235B", "吨", "成品"},
            {"横切板", "3.0*1500*3000", "Q235B", "吨", "成品"},
            {"开平板", "4.0*1250*2500", "Q235B", "吨", "成品"},
            {"分条卷", "1.0*312*C", "SPCC", "吨", "成品"},
            {"定尺板", "20*2200*6000", "Q235B", "吨", "成品"},
    };

    private static final String[][] CUSTOMER_TEMPLATES = {
            {"C001", "上海建工集团有限公司", "建筑企业", "张经理", "138"},
            {"C002", "中建三局集团有限公司", "建筑企业", "李主管", "139"},
            {"C003", "江苏沙钢集团有限公司", "钢铁企业", "王经理", "136"},
            {"C004", "宝信软件股份有限公司", "科技企业", "赵总", "137"},
            {"C005", "上海宝冶集团有限公司", "建筑企业", "刘经理", "135"},
            {"C006", "中铁十四局集团有限公司", "建筑企业", "陈主管", "158"},
            {"C007", "南京钢铁股份有限公司", "钢铁企业", "吴经理", "159"},
            {"C008", "广州市鸿达兴业集团", "贸易企业", "周总", "186"},
            {"C009", "安徽富煌钢构股份有限公司", "制造企业", "孙经理", "187"},
            {"C010", "浙江精工钢构集团有限公司", "制造企业", "马主管", "188"},
    };

    private static final String[][] SUPPLIER_TEMPLATES = {
            {"S001", "宝山钢铁股份有限公司", "钢厂", "钱经理", "131"},
            {"S002", "江苏沙钢集团有限公司", "钢厂", "孙经理", "132"},
            {"S003", "鞍钢股份有限公司", "钢厂", "周经理", "133"},
            {"S004", "河钢集团有限公司", "钢厂", "吴经理", "152"},
            {"S005", "首钢集团有限公司", "钢厂", "郑经理", "153"},
            {"S006", "上海物贸钢铁有限公司", "贸易商", "冯经理", "156"},
            {"S007", "中建材国际贸易有限公司", "贸易商", "卫总", "176"},
            {"S008", "五矿发展股份有限公司", "贸易商", "蒋经理", "177"},
    };

    private static final String[] SETTLEMENT_METHODS = {"月结30天", "月结60天", "款到发货", "货到付款", "承兑汇票"};
    private static final String[] WAREHOUSES = {"主仓库", "二号仓库", "露天堆场", "加工车间仓", "成品仓"};

    private final Random random = new Random(42);

    public MockDataSetVO generateMockData(Long projectId) {
        CustomerProfile profile = loadProfile(projectId);
        IndustryType industryType = profile.getIndustryType() != null
                ? profile.getIndustryType() : IndustryType.STEEL_TRADER;

        BigDecimal monthlyAmount = profile.getMonthlyAmount() != null
                ? profile.getMonthlyAmount() : new BigDecimal("5000000");
        int warehouseCount = profile.getTotalWarehouseCount() != null && profile.getTotalWarehouseCount() > 0
                ? profile.getTotalWarehouseCount() : 3;

        List<MockProductVO> products = generateProducts(industryType);
        List<MockCustomerVO> customers = generateCustomers(industryType);
        List<MockSupplierVO> suppliers = generateSuppliers(industryType);
        List<MockInventoryVO> inventory = generateInventory(products, warehouseCount, monthlyAmount);
        List<MockTransactionVO> transactions = generateTransactions(products, customers, suppliers);

        BigDecimal totalInventoryValue = inventory.stream()
                .map(MockInventoryVO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MockDataSetVO result = new MockDataSetVO();
        result.setProjectId(projectId);
        result.setGeneratedAt(LocalDateTime.now());
        result.setProducts(products);
        result.setCustomers(customers);
        result.setSuppliers(suppliers);
        result.setInventory(inventory);
        result.setTransactions(transactions);

        MockDataSetVO.Summary summary = new MockDataSetVO.Summary();
        summary.setTotalProducts(products.size());
        summary.setTotalCustomers(customers.size());
        summary.setTotalSuppliers(suppliers.size());
        summary.setTotalInventoryValue(totalInventoryValue.setScale(2, RoundingMode.HALF_UP).toPlainString());
        result.setSummary(summary);

        return result;
    }

    private CustomerProfile loadProfile(Long projectId) {
        List<CustomerProfile> profiles = customerProfileMapper.selectList(
                new LambdaQueryWrapper<CustomerProfile>()
                        .eq(CustomerProfile::getProjectId, projectId)
                        .orderByDesc(CustomerProfile::getCreateTime));
        if (profiles.isEmpty()) {
            throw new BizException("项目客户画像不存在，请先完成调研分析: " + projectId);
        }
        return profiles.get(0);
    }

    private List<MockProductVO> generateProducts(IndustryType industryType) {
        List<MockProductVO> products = new ArrayList<>();

        switch (industryType) {
            case STEEL_MILL:
                for (String[] raw : MILL_RAW_MATERIALS) {
                    products.add(new MockProductVO(raw[0], raw[1], raw[2],
                            "", raw[3], raw[4]));
                }
                for (int i = 0; i < 8 && i < TRADER_PRODUCTS.length; i++) {
                    String[] p = TRADER_PRODUCTS[i];
                    products.add(new MockProductVO(p[0], p[1], p[2],
                            STEEL_MILLS[0], p[3], p[4]));
                }
                break;

            case PROCESSING_CENTER:
                for (String[] inc : PROCESSING_INCOMING) {
                    products.add(new MockProductVO(inc[0], inc[1], inc[2],
                            STEEL_MILLS[random.nextInt(5)], inc[3], inc[4]));
                }
                for (String[] out : PROCESSING_OUTGOING) {
                    products.add(new MockProductVO(out[0], out[1], out[2],
                            "自产", out[3], out[4]));
                }
                break;

            default:
                int count = 8 + random.nextInt(8);
                count = Math.min(count, TRADER_PRODUCTS.length);
                for (int i = 0; i < count; i++) {
                    String[] p = TRADER_PRODUCTS[i];
                    products.add(new MockProductVO(p[0], p[1], p[2],
                            STEEL_MILLS[random.nextInt(STEEL_MILLS.length)], p[3], p[4]));
                }
                break;
        }
        return products;
    }

    private List<MockCustomerVO> generateCustomers(IndustryType industryType) {
        int count = 5 + random.nextInt(6);
        count = Math.min(count, CUSTOMER_TEMPLATES.length);
        List<MockCustomerVO> customers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String[] tpl = CUSTOMER_TEMPLATES[i];
            String phone = tpl[4] + String.format("%08d", 10000000 + random.nextInt(90000000));
            String settlement = SETTLEMENT_METHODS[random.nextInt(SETTLEMENT_METHODS.length)];
            customers.add(new MockCustomerVO(tpl[0], tpl[1], tpl[2], tpl[3], phone, settlement));
        }
        return customers;
    }

    private List<MockSupplierVO> generateSuppliers(IndustryType industryType) {
        int count = 3 + random.nextInt(6);
        count = Math.min(count, SUPPLIER_TEMPLATES.length);
        List<MockSupplierVO> suppliers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String[] tpl = SUPPLIER_TEMPLATES[i];
            String phone = tpl[4] + String.format("%08d", 10000000 + random.nextInt(90000000));
            suppliers.add(new MockSupplierVO(tpl[0], tpl[1], tpl[2], tpl[3], phone));
        }
        return suppliers;
    }

    private List<MockInventoryVO> generateInventory(List<MockProductVO> products,
                                                     int warehouseCount,
                                                     BigDecimal monthlyAmount) {
        BigDecimal targetTotal = monthlyAmount.multiply(new BigDecimal("2"));
        List<MockInventoryVO> inventory = new ArrayList<>();

        int usedWarehouses = Math.min(warehouseCount, WAREHOUSES.length);
        int totalSlots = products.size() * usedWarehouses;
        BigDecimal perSlot = targetTotal.divide(new BigDecimal(totalSlots), 2, RoundingMode.HALF_UP);

        for (MockProductVO product : products) {
            int wCount = 1 + random.nextInt(usedWarehouses);
            wCount = Math.min(wCount, usedWarehouses);
            for (int w = 0; w < wCount; w++) {
                BigDecimal unitPrice = generatePrice(product.getName());
                BigDecimal slotAmount = perSlot.multiply(
                        new BigDecimal("0.6").add(
                                new BigDecimal(random.nextDouble()).multiply(new BigDecimal("0.8"))
                        )).setScale(2, RoundingMode.HALF_UP);
                BigDecimal weight = slotAmount.divide(unitPrice, 4, RoundingMode.HALF_UP);
                BigDecimal qty = weight.divide(new BigDecimal("2.5"), 0, RoundingMode.HALF_UP);
                if (qty.compareTo(BigDecimal.ONE) < 0) {
                    qty = BigDecimal.ONE;
                }
                BigDecimal amount = weight.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);

                inventory.add(new MockInventoryVO(
                        product.getName(),
                        product.getSpec(),
                        WAREHOUSES[w],
                        qty,
                        weight.setScale(4, RoundingMode.HALF_UP),
                        unitPrice,
                        amount
                ));
            }
        }
        return inventory;
    }

    private List<MockTransactionVO> generateTransactions(List<MockProductVO> products,
                                                          List<MockCustomerVO> customers,
                                                          List<MockSupplierVO> suppliers) {
        List<MockTransactionVO> transactions = new ArrayList<>();
        LocalDate baseDate = LocalDate.now().minusDays(30);

        int count = 10 + random.nextInt(11);
        for (int i = 0; i < count; i++) {
            boolean isPurchase = random.nextBoolean();
            MockProductVO product = products.get(random.nextInt(products.size()));
            BigDecimal unitPrice = generatePrice(product.getName());
            BigDecimal weight = new BigDecimal(10 + random.nextInt(91))
                    .setScale(4, RoundingMode.HALF_UP);
            BigDecimal qty = weight.divide(new BigDecimal("2.5"), 0, RoundingMode.HALF_UP);
            if (qty.compareTo(BigDecimal.ONE) < 0) {
                qty = BigDecimal.ONE;
            }
            BigDecimal amount = weight.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            LocalDate date = baseDate.plusDays(random.nextInt(30));

            if (isPurchase) {
                MockSupplierVO supplier = suppliers.get(random.nextInt(suppliers.size()));
                transactions.add(new MockTransactionVO("PURCHASE", supplier.getName(),
                        product.getName(), qty, weight, unitPrice, amount, date));
            } else {
                MockCustomerVO customer = customers.get(random.nextInt(customers.size()));
                transactions.add(new MockTransactionVO("SALES", customer.getName(),
                        product.getName(), qty, weight, unitPrice, amount, date));
            }
        }

        transactions.sort(Comparator.comparing(MockTransactionVO::getDate));
        return transactions;
    }

    private BigDecimal generatePrice(String productName) {
        int base;
        if (productName.contains("铁矿石")) {
            base = 800 + random.nextInt(200);
        } else if (productName.contains("焦炭")) {
            base = 2200 + random.nextInt(400);
        } else if (productName.contains("废钢")) {
            base = 2600 + random.nextInt(400);
        } else if (productName.contains("合金")) {
            base = 8000 + random.nextInt(2000);
        } else if (productName.contains("石灰石")) {
            base = 300 + random.nextInt(100);
        } else if (productName.contains("冷轧") || productName.contains("镀锌")) {
            base = 4800 + random.nextInt(1200);
        } else if (productName.contains("H型钢") || productName.contains("型钢")) {
            base = 3800 + random.nextInt(800);
        } else if (productName.contains("中厚板")) {
            base = 3600 + random.nextInt(800);
        } else if (productName.contains("螺纹钢")) {
            base = 3400 + random.nextInt(600);
        } else {
            base = 3800 + random.nextInt(1000);
        }
        return new BigDecimal(base).setScale(2, RoundingMode.HALF_UP);
    }
}
