const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  devServer: {
    port: 9001
  },
  transpileDependencies: true,
  lintOnSave: false,
  publicPath: './'
})
