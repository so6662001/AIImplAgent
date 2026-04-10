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
      ],
    },
  ],
})

export default router
