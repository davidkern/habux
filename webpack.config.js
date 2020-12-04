const path = require('path');
const WasmPackPlugin = require('@wasm-tool/wasm-pack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const distPath = path.resolve(__dirname, 'dist');
const pkgPath = path.resolve(__dirname, 'pkg');

module.exports = (env, argv) => {
    return {
        experiments: {
            asyncWebAssembly: true,
        },
        devServer: {
            contentBase: distPath,
            compress: argv.mode === 'production',
            port: 8000
        },
        entry: './js/index.js',
        output: {
            path: distPath,
            filename: 'index.js',
            webassemblyModuleFilename: 'index.wasm'
        },
        module: {
            rules: [
                {
                    test: /\.s[ac]ss$/i,
                    use: [
                        'style-loader',
                        'css-loader',
                        'sass-loader'
                    ]
                }
            ]
        },
        plugins: [
            new CopyWebpackPlugin({
                patterns: [
                    {from: './static', to: distPath}
                ]
            }),
            new WasmPackPlugin({
                crateDirectory: '.',
                extraArgs: '--no-typescript'
            })
        ],
        watch: argv.mode !== 'production'
    }
}
