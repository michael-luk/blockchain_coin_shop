# blockchain_coin_shop

虚拟货币微信商城(公众号商城微信支付)

自动获取虚拟货币汇率, 计算商品价格, 响应式布局支持pc和手机界面, 对接微信支付, 支持虚拟货币和人民币混合支付, pc端支持facebook登录, 微信扫码登录.

包含促销积分, 三级分销, 多语言, 截图如下:

![Image text](http://www.woyik.com/img/coin_shop.png)

![Image text](http://www.woyik.com/img/coin_shop_site.png)

![Image text](http://www.woyik.com/img/coin_shop_admin.png)


开发框架: 后台Play框架2.3.8版本 https://www.playframework.com/documentation/2.3.x/Home, 前台AngularJS+JQuery

数据库:   MySql5.5以上

部署流程:

1. 安装Java8以上, 设置环境变量, 命令行输入javac可以看到信息. (可下载下方网盘的Java一键安装配置包)
2. 安装MySql, 连接配置位于 /conf/application.conf, 新建库(编码utf8_unicode_ci), 并导入db文件夹内的sql脚本 (库内皆为演示数据, 特此声明)
3. 下载代码, 命令行在代码文件夹内直接输入命令即可下载依赖以及运行 (Windows下命令: Activator run) (Linux下: Bash Activator run), 系统会运行在 http://localhost:9000 
4. 若输入以上指令后下载依赖报错, 估计是由于依赖被墙所引起. 则下载下方网盘的依赖包, 复制到 C:\Users\你的用户名\.ivy2\cache 目录下, 重新输入命令即可.
5. 微商城需要绑定微信公众号运行, 微信公众号的各种key填入到 /app/controllers/WeiXinController
6. 初始管理员密码为 admin_3/123456


Java及依赖安装:
百度网盘: https://pan.baidu.com/s/10H82J6i0uuWcucs4CGjYtg 提取码: 57nq


代码解析:
1. api路由在 /conf/routes
2. 业务代码在 /app 目录下的controller, model, views
3. js及静态资源在 /public 目录下

有问题可报Issue或联系:

微信 ly17040643
邮箱 17040643@QQ.com
