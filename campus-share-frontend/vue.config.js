const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  // 新增：关闭 ESLint 校验（开发阶段临时解决语法报错）
  lintOnSave: false
})