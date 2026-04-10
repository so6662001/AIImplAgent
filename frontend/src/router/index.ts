import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/views/layout/AppLayout.vue'),
      redirect: '/projects',
      children: [
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
      ],
    },
  ],
})

export default router
