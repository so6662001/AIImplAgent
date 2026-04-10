import type { FormItemRule } from 'element-plus'

export const required = (msg: string): FormItemRule => ({
  required: true,
  message: msg,
  trigger: 'blur',
})

export const maxLen = (max: number): FormItemRule => ({
  max,
  message: `长度不能超过${max}个字符`,
  trigger: 'blur',
})

export const phoneRule: FormItemRule = {
  pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/,
  message: '请输入正确的联系电话',
  trigger: 'blur',
}

export const creditCodeRule: FormItemRule = {
  pattern: /^[0-9A-Z]{18}$/,
  message: '统一社会信用代码必须为18位字母数字',
  trigger: 'blur',
}

export const taxRateRule: FormItemRule = {
  validator: (_rule, value, callback) => {
    if (value !== undefined && value !== null && value !== '') {
      const n = Number(value)
      if (isNaN(n) || n < 0 || n > 1) {
        callback(new Error('税率必须在0~1之间'))
        return
      }
    }
    callback()
  },
  trigger: 'blur',
}

export const scoreRule: FormItemRule = {
  validator: (_rule, value, callback) => {
    if (value === undefined || value === null || value === '') {
      callback(new Error('考核分数不能为空'))
      return
    }
    const n = Number(value)
    if (!Number.isInteger(n) || n < 0 || n > 100) {
      callback(new Error('考核分数必须为0~100的整数'))
      return
    }
    callback()
  },
  trigger: 'blur',
}

export const portRule: FormItemRule = {
  validator: (_rule, value, callback) => {
    if (value === undefined || value === null || value === '') {
      callback()
      return
    }
    const n = Number(value)
    if (!Number.isInteger(n) || n < 1 || n > 65535) {
      callback(new Error('端口号必须为1~65535的整数'))
      return
    }
    callback()
  },
  trigger: 'blur',
}

export const urlRule: FormItemRule = {
  pattern: /^(https?:\/\/[^\s]+)?$/,
  message: '请输入正确的URL地址（http://或https://开头）',
  trigger: 'blur',
}

export const percentRule: FormItemRule = {
  validator: (_rule, value, callback) => {
    if (value === undefined || value === null || value === '') {
      callback()
      return
    }
    const n = Number(value)
    if (isNaN(n) || n < 0 || n > 100) {
      callback(new Error('数值必须在0~100之间'))
      return
    }
    callback()
  },
  trigger: 'blur',
}

export const issuesRule = (form: { totalIssues: number; resolvedIssues: number }): FormItemRule => ({
  validator: (_rule, value, callback) => {
    if (value !== undefined && value !== null && value !== '') {
      const n = Number(value)
      if (n > form.totalIssues) {
        callback(new Error('已解决问题数不能大于问题总数'))
        return
      }
    }
    callback()
  },
  trigger: 'blur',
})
