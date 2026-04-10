import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useClientAuthStore } from '@/stores/clientAuth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/client/login',
      name: 'ClientLogin',
      component: () => import('@/views/client/ClientLogin.vue'),
      meta: { title: '培训助手登录', public: true, clientRoute: true },
    },
    {
      path: '/client/chat',
      name: 'ClientChat',
      component: () => import('@/views/client/ClientChat.vue'),
      meta: { title: 'ERP培训助手', clientRoute: true, requiresClientAuth: true },
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginPage.vue'),
      meta: { title: '登录', public: true },
    },
    {
      path: '/',
      component: () => import('@/views/layout/AppLayout.vue'),
      redirect: '/projects',
      children: [
        {
          path: 'dispatch',
          name: 'Dispatch',
          component: () => import('@/views/dispatch/DispatchPage.vue'),
          meta: { title: '项目调度' },
        },
        {
          path: 'projects',
          name: 'Projects',
          component: () => import('@/views/project/ProjectList.vue'),
          meta: { title: '项目管理' },
        },
        {
          path: 'products',
          name: 'Products',
          component: () => import('@/views/archive/ProductList.vue'),
          meta: { title: '货品档案' },
        },
        {
          path: 'customers',
          name: 'Customers',
          component: () => import('@/views/archive/CustomerList.vue'),
          meta: { title: '客户档案' },
        },
        {
          path: 'training',
          name: 'Training',
          component: () => import('@/views/training/ExamManage.vue'),
          meta: { title: '培训考核' },
        },
        {
          path: 'research',
          name: 'Research',
          component: () => import('@/views/research/CustomerProfileList.vue'),
          meta: { title: '调研分析' },
        },
        {
          path: 'account-sets',
          name: 'AccountSets',
          component: () => import('@/views/accountset/AccountSetList.vue'),
          meta: { title: '帐套管理' },
        },
        {
          path: 'engineers',
          name: 'Engineers',
          component: () => import('@/views/workforce/EngineerList.vue'),
          meta: { title: '工程师管理' },
        },
        {
          path: 'project-evals',
          name: 'ProjectEvals',
          component: () => import('@/views/workforce/ProjectEvalList.vue'),
          meta: { title: '项目评价' },
        },
        {
          path: 'servers',
          name: 'Servers',
          component: () => import('@/views/server/ServerProfileList.vue'),
          meta: { title: '服务器管理' },
        },
        {
          path: 'go-live-checks',
          name: 'GoLiveChecks',
          component: () => import('@/views/golive/GoLiveCheckList.vue'),
          meta: { title: '上线检查' },
        },
        {
          path: 'simulations',
          name: 'Simulations',
          component: () => import('@/views/simulation/SimulationSceneList.vue'),
          meta: { title: '模拟演练' },
        },
        {
          path: 'reports',
          name: 'Reports',
          component: () => import('@/views/report/DeliveryReportList.vue'),
          meta: { title: '交付报告' },
        },
        {
          path: 'suppliers',
          name: 'Suppliers',
          component: () => import('@/views/archive/SupplierList.vue'),
          meta: { title: '供应商档案' },
        },
        {
          path: 'warehouses',
          name: 'Warehouses',
          component: () => import('@/views/archive/WarehouseList.vue'),
          meta: { title: '仓库档案' },
        },
        {
          path: 'categories',
          name: 'Categories',
          component: () => import('@/views/archive/CategoryList.vue'),
          meta: { title: '品类档案' },
        },
        {
          path: 'related-units',
          name: 'RelatedUnits',
          component: () => import('@/views/archive/RelatedUnitList.vue'),
          meta: { title: '往来单位' },
        },
        {
          path: 'trainees',
          name: 'Trainees',
          component: () => import('@/views/training/TraineeList.vue'),
          meta: { title: '学员管理' },
        },
        {
          path: 'training-logs',
          name: 'TrainingLogs',
          component: () => import('@/views/training/TrainingLogList.vue'),
          meta: { title: '培训日志' },
        },
        {
          path: 'worklogs',
          name: 'Worklogs',
          component: () => import('@/views/workforce/WorklogList.vue'),
          meta: { title: '工作日志' },
        },
        {
          path: 'training-dashboard',
          name: 'TrainingDashboard',
          component: () => import('@/views/training/TrainingDashboard.vue'),
          meta: { title: '培训仪表盘' },
        },
        {
          path: 'departments',
          name: 'Departments',
          component: () => import('@/views/org/DepartmentList.vue'),
          meta: { title: '部门管理' },
        },
        {
          path: 'employees',
          name: 'Employees',
          component: () => import('@/views/org/EmployeeList.vue'),
          meta: { title: '员工管理' },
        },
        {
          path: 'bank-accounts',
          name: 'BankAccounts',
          component: () => import('@/views/finance/BankAccountList.vue'),
          meta: { title: '银行账号' },
        },
        {
          path: 'storage-locations',
          name: 'StorageLocations',
          component: () => import('@/views/archive/StorageLocationList.vue'),
          meta: { title: '库位管理' },
        },
        {
          path: 'account-subjects',
          name: 'AccountSubjects',
          component: () => import('@/views/finance/AccountSubjectList.vue'),
          meta: { title: '财务科目' },
        },
        {
          path: 'inventory-balances',
          name: 'InventoryBalances',
          component: () => import('@/views/openingbalance/InventoryBalanceList.vue'),
          meta: { title: '库存期初' },
        },
        {
          path: 'customer-balances',
          name: 'CustomerBalances',
          component: () => import('@/views/openingbalance/CustomerBalanceList.vue'),
          meta: { title: '客户往来' },
        },
        {
          path: 'supplier-balances',
          name: 'SupplierBalances',
          component: () => import('@/views/openingbalance/SupplierBalanceList.vue'),
          meta: { title: '供应商往来' },
        },
        {
          path: 'account-balances',
          name: 'AccountBalances',
          component: () => import('@/views/openingbalance/AccountBalanceList.vue'),
          meta: { title: '账户余额' },
        },
        {
          path: 'subject-balances',
          name: 'SubjectBalances',
          component: () => import('@/views/openingbalance/SubjectBalanceList.vue'),
          meta: { title: '科目余额' },
        },
        {
          path: 'other-receivables',
          name: 'OtherReceivables',
          component: () => import('@/views/openingbalance/OtherReceivableList.vue'),
          meta: { title: '其他应收' },
        },
        {
          path: 'other-payables',
          name: 'OtherPayables',
          component: () => import('@/views/openingbalance/OtherPayableList.vue'),
          meta: { title: '其他应付' },
        },
        {
          path: 'invoice-balances',
          name: 'InvoiceBalances',
          component: () => import('@/views/openingbalance/InvoiceBalanceList.vue'),
          meta: { title: '发票期初' },
        },
        {
          path: 'project-plans',
          name: 'ProjectPlans',
          component: () => import('@/views/project/ProjectPlanList.vue'),
          meta: { title: '交付计划' },
        },
        {
          path: 'required-courses',
          name: 'RequiredCourses',
          component: () => import('@/views/training/RequiredCourseList.vue'),
          meta: { title: 'KA必学课程' },
        },
        {
          path: 'llm-providers',
          name: 'LlmProviders',
          component: () => import('@/views/agent/LlmProviderList.vue'),
          meta: { title: 'LLM模型配置' },
        },
        {
          path: 'agent-configs',
          name: 'AgentConfigs',
          component: () => import('@/views/agent/AgentConfigList.vue'),
          meta: { title: '智能体配置' },
        },
        {
          path: 'qa-sessions',
          name: 'QaSessions',
          component: () => import('@/views/training/QaSessionList.vue'),
          meta: { title: '问答管理' },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  if (to.meta.clientRoute) {
    if (to.meta.requiresClientAuth) {
      const clientAuth = useClientAuthStore()
      if (!clientAuth.isLoggedIn) {
        return '/client/login'
      }
    }
    return
  }

  const authStore = useAuthStore()
  if (!authStore.isLoggedIn && !to.meta.public) {
    return '/login'
  }
  if (authStore.isLoggedIn && to.path === '/login') {
    return '/projects'
  }
})

export default router
