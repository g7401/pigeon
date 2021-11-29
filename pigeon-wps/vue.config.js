const path = require('path')
const webpack = require('webpack')
const MonacoEditorPlugin = require('monaco-editor-webpack-plugin')
const createThemeColorReplacerPlugin = require('./config/plugin.config')

function resolve (dir) {
  return path.join(__dirname, dir)
}

const isProd = process.env.NODE_ENV === 'production'

const configExternals = {
  serverConfig: 'serverConfig'
}

const assetsCDN = {
  // webpack build externals
  externals: {
    // vue: 'Vue',
    // 'vue-router': 'VueRouter',
    // vuex: 'Vuex',
    // axios: 'axios',
    // '@antv/g6': 'G6'
    ...configExternals
  },
  css: [],
  // https://unpkg.com/browse/vue@2.6.10/
  js: [
    // '//cdn.jsdelivr.net/npm/vue@2.6.10/dist/vue.min.js',
    // '//cdn.jsdelivr.net/npm/vue-router@3.1.3/dist/vue-router.min.js',
    // '//cdn.jsdelivr.net/npm/vuex@3.1.1/dist/vuex.min.js',
    // '//cdn.jsdelivr.net/npm/axios@0.19.0/dist/axios.min.js',
    // '//gw.alipayobjects.com/os/antv/pkg/_antv.g6-3.7.1/dist/g6.min.js'
  ]
}

// vue.config.js
const vueConfig = {
  publicPath: isProd ? '/pigeon-wfe/' : '/',
  // runtimeCompiler: true,
  configureWebpack: {
    // webpack plugins
    plugins: [
      // Ignore all locale files of moment.js
      new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
      new MonacoEditorPlugin({
        languages: ['sql', 'json']
      })
    ],
    // if prod, add externals
    externals: isProd ? assetsCDN.externals : configExternals,
    devtool: 'source-map'
  },

  chainWebpack: (config) => {
    config.plugins.delete('prefetch')
    config.resolve.alias
      .set('@$', resolve('src'))

    const svgRule = config.module.rule('svg')
    svgRule.uses.clear()
    svgRule
      .oneOf('inline')
      .resourceQuery(/inline/)
      .use('vue-svg-icon-loader')
      .loader('vue-svg-icon-loader')
      .end()
      .end()
      .oneOf('external')
      .use('file-loader')
      .loader('file-loader')
      .options({
        name: 'assets/[name].[hash:8].[ext]'
      })

    // if prod is on
    // assets require on cdn
    if (isProd) {
      config.plugin('html').tap(args => {
        args[0] && (args[0].cdn = assetsCDN)
        return args
      })
    }
  },

  css: {
    loaderOptions: {
      less: {
        modifyVars: {
          // less vars，customize ant design theme

          // 'primary-color': '#F5222D',
          // 'link-color': '#F5222D',
          'border-radius-base': '2px'
        },
        // DO NOT REMOVE THIS LINE
        javascriptEnabled: true
      }
    }
  },

  devServer: {
    // development server port 8000
    port: 8000,
    open: true,
    inline: false,
    // liveReload: false,
    compress: true,
    // If you want to turn on the proxy, please remove the mockjs /src/main.jsL11
    proxy: {
      // '/fs': {
      //   target: process.env.VUE_APP_FILE_API_BASE_URL,
      //   changeOrigin: true
      // },
      '/': {
          target: process.env.VUE_APP_PROXY_BASE_URL,
          changeOrigin: true,
          bypass: function (req, res, proxyOptions) {
            if (req.headers.accept.indexOf('html') !== -1 && req.originalUrl.indexOf('/storage/download') === -1 && req.originalUrl.indexOf('/file_transfer/download') === -1) {
              return '/index.html'
            }
          }
        }
    }
  },

  // disable source map in production
  productionSourceMap: false,
  lintOnSave: undefined,
  // babel-loader no-ignore node_modules/*
  transpileDependencies: []
}

// preview.pro.loacg.com only do not use in your production;
if (process.env.VUE_APP_PREVIEW === 'true') {
  console.log('VUE_APP_PREVIEW', true)
  // add `ThemeColorReplacer` plugin to webpack plugins
  vueConfig.configureWebpack.plugins.push(createThemeColorReplacerPlugin())
}

module.exports = vueConfig
