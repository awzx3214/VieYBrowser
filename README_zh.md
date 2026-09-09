# VieYBrowser - 一个安卓小网浏览器

![VieY 浏览器图标](/icon.png)

[VieY 浏览器官网](https://palhube666.wodemo.net/) / [下载途径①](https://dmpap.lanzouw.com/b02dc84ch) / [下载途径②](https://www.123pan.com/s/UML9jv-fk4bh.html) / [下载途径③](https://t.me/Kawaii_V)

简体中文 | [English](/README.md)

## 1. 项目概述

欢迎下载 / 使用 Vie 浏览器

VieY 浏览器是由 呆毛飘啊飘 开发的**安卓端开源轻量浏览器**

VieY 是本人 Vie 项目的一个分支，去除了多余功能，保留并且强化了关于小网方面的部分

安装包体积约 10 MB

---


## 2. 功能展示

- 支持 smolnet 身份证书的请求
- 支持正常普通网页的访问
- 下拉刷新页面
- 底部操作栏长按快捷功能
- 视频全屏播放
- 网页使用定位、麦克风、摄像头
- 支持外部应用通过本地存储访问内部文件
- 最低支持安卓 5 设备

---


## 3. 与其他安卓 smolnet 浏览器对比

|对比项|VieY|Vie|Lagrange|Zapri|portal.mozz.us|
|---|---|---|---|---|---|
|体积|≈10MB|≈5MB|≈20MB|≈20MB|0MB|
|平台|安卓|安卓|多平台|安卓|多平台|
|身份证书|支持 .bks .p12 .pem .crt/.key|支持 .bks .p12|支持 .pem|不支持|支持 .pem|
|多语言|英语/中文|仅中文|多语言（但是中文无法正常显示）|仅英语|仅英语|
|优点|支持协议多，可正常访问网页|支持油猴脚本可作为日常浏览器使用|支持多平台使用|简约方便|依靠 http 代理访问 smolnet|
|缺点|过于简陋|仅支持中文|界面过时|无法设置身份证书|不稳定，若服务器宕机则无法使用|
|http(s)://|✔支持|✔支持|✘不支持|✘不支持|✔支持|
|gemini://|✔支持|✔支持|✔支持|✔支持|✔支持|
|gopher(s)://|✔支持|✔支持|✔支持|✘不支持|✔支持|
|finger://|✔支持|✔支持|✔支持|✔支持|✔支持|
|nex://|✔支持|✔支持|✔支持|✘不支持|✔支持|
|text://|✔支持|✔支持|✘不支持|✔支持|✔支持|
|spartan://|✔支持|✔支持|✔支持|✘不支持|✔支持|
|kepler(s)://|✔支持|✔支持|✘不支持|✘不支持|✘不支持|
|scorpion(s)://|✔支持|✔支持|✘不支持|✘不支持|✘不支持|
|titan://|✔支持|✔支持|✔支持|✘不支持|✔支持|
|misfin://|✔支持|✔支持|✔支持|✘不支持|✘不支持|

---


## 4. 待办事项

### 功能

- 完善夜间模式
- 多级书签
- 统一 ui 布局
- 补全协议支持
- 翻译页面内容
- 修改软件内字体
- 支持 data: blob: 文件下载
- 模块化解析 text/gemini text/scroll 等类型文件
- 支持 smolnet 各协议下载功能

---


## 5. 协议支持性

### VieY 浏览器支持的协议

以下是 VieY 能够支持的与我已知的 smolnet 协议（按实现的时间排序）

“可用”表示支持进行基本交互功能

如果你还知道什么没有列出的 smolnet 协议，请联系我哦~

- Http://
  - [x] 可用
  - [x] Https:// (Http over TLS)
- File://
  - [x] 可用
- Gemini://
  - [x] 可用
    - [x] 上传使用 Titan:// 附属协议
    - [ ] 上传使用 Nimigem:// 附属协议
    - [ ] 上传使用 Inimeg:// 附属协议
    - [ ] 上传使用 Iapetus:// 附属协议
  - [x] 携带身份证书
- Finger://
  - [x] 可用
  - [ ] 自动将文本转链接
  - [ ] Fingers:// (Finger over TLS)
- Gopher://
  - [x] 可用
    - [x] 文本渲染
    - [x] 文件下载
  - [x] Gopher+
  - [x] Gophers:// (Gopher over TLS)
  - [ ] GopherVR
  - [ ] /_TCP 原始数据流
- Spartan://
  - [x] 可用
  - [ ] 文件上传
- Nex://
  - [x] 可用
    - [x] 上传使用 Nps:// 附属协议
- Text://
  - [x] 可用
  - [x] data: 链接访问
- Scorpion://
  - [x] 可用
    - [x] R 子协议
    - [ ] S 子协议
    - [ ] M 子协议
    - [ ] I 子协议
      - 由于无测试链接，虽已编写完毕其他子协议代码，但是无法保证正确性，故目前只支持 R 子协议
  - [x] 携带身份证书
  - [x] Scorpions:// (Scorpion over TLS)
- Kepler://
  - [x] 可用
  - [x] 携带身份证书
  - [x] Keplers:// (Kepler over TLS)
  - [ ] 页面缓存
    - 文档看的我好累，这个没写，不过已经预留了个接口，后面应该会实现
- Titan://
  - [x] 可用
    - [x] 上传文本
    - [x] 上传文件
  - [x] 携带身份证书
  - [ ] ;edit 参数
- Misfin://
  - [x] 可用
    - [x] Misfin(A)
      - 由于无测试链接，虽已编写完毕，但是无法保证正确性
    - [x] Misfin(B)
    - [x] Misfin(C)
      - Misfin(C) 依据 draft 9 编写
  - [x] 携带身份证书
- Scroll://
  - [x] 可用
    - 渲染时遇到自定义序号列表，内联标记会出现部分不符合规定的错误
  - [x] 语言列表
  - [x] 元数据请求
  - [x] 携带身份证书
  - [ ] 流式渲染文档
- Molerat://
  - [x] 可用
    - 由于无测试链接，不保证正确性
  - [x] 页面缓存
- Nps://
  - [x] 可用
- Guppy://
  - [ ] 可用
    - 由于无测试链接，虽已编写完基础代码，但是暂时不打算支持
- Cso://
  - [ ] 可用
    - 代码写完了，但是不知道要不要加进来
- Fsp://
  - [ ] 可用
    - 正在研究中
- Nimigem://
  - [ ] 可用
    - 正在研究中
- Mark://
  - [ ] 可用
    - 协议必须使用 QUIC，如果加上软件体积可能翻倍
- Terse://
  - [ ] 可用
    - 较难实，可能永远不会实现
- Inimeg://
  - [ ] 可用
    - 有规范文件，但已废弃
- Iapetus://
  - [ ] 可用
    - 有规范文件，但已废弃
- Mercury://
  - [ ] 可用
    - 仅有提议，无规范文件
- Gequilla://
  - [ ] 可用
    - 仅有提议，无规范文件
- Fist://
  - [ ] 可用
    - 仅有提议，无规范文件


### Vie 浏览器支持的协议

如果您需要：

rslsync://, text://, scroll://, scorpion://, molerat://, titan://, misfin://, btsync://, webdav://, webdavs://, davs://, dav://, ircs://, irc://, snmp://, ldap://, cso://, ed2k://, view-source:, freenet:, content://, intent:, android-app://, scp://, http://, https://, mqtts://, mqtt+ssl://, mqtt+ws://, mqtt+wss://, file://, about:, javascript:, news://, fs2you://, bc://, spartan://, nex://, gophers://, gopher://, gemini://, finger://, buss://, curl://, geo:, smsto:, mmsto:, mms:, mailto:, tel:, sms:, qqdl://, flashget://, thunder://, coaps://, magnet:, jsonfeed://, atom://, coap://, mqtt://, rss://, feed://, dns://, whois://, qotd://, daytime://, time://, rtmp://, rtsp://, smtp://, ftps://, sftp://, tftp://, ftp://, nntp://, ntp://, viek://, ws://, wss://

协议的支持，并且是简体中文用户，可选用 Vie 浏览器

---


## 6. 使用指南

### 用户使用指南

**1.** 您可以将本应用当成普通的浏览器使用

**2.** 长按底部每一个按键都有对应操作

**3.** 设置中支持多格式的身份证书导入


### 开发者使用指南

**1.** **请注意**，本项目使用 iApp Studio 进行打包，项目结构不同于传统项目结构！

**2.** 如果您需要在其他编译器进行编译，必须优先修改本项目的结构！

**3.** 除了 kawaii.viey.browser.xy 文件夹内的 .java 文件外，其余文件均为 kawaii.viey.browser 包名下的类，请注意移动！

**4.** iApp Studio 内置 AndroidX 等库的依赖，无需再次导入依赖，如果需要在其他编译器中编译，必须自行添加 AndroidX 等依赖！

**5.** 本项目使用 **Apache License 2.0 开源许可证** ，后续将申请软件著作权，将受到《中华人民共和国著作权法》的保护！
  - 允许：商业使用、修改、私有化、再分发
  - 要求：保留原始版权声明，修改文件需标注变更
  - 专利授权：自动授予使用者专利使用权
  - 禁止：使用项目名称进行推广（除非获得许可）
  - 即您可以免费使用，允许您在使用后制作您自己的软件不开源
  - 但是，需要保留我的版权信息
  - 如果基于本应用二次修改，务必将“本项目基于呆毛飘啊飘开源 VieY 浏览器，原项目开源地址为 https://gitee.com/awzx3214/VieYBrowser/ ”放置在感谢区 / 开源协议区或其他区域的可见位置
  - 如果仅仅使用了部分代码，只需要在开源协议区标明使用了本项目即可

---


## 7. 技术栈

- Android Java + HTML/JavaScript
- iApp Studio 打包
- 自定义链接解析
- 模块化实现

---


## 8. 简单问答

**1.** 问：为什么功能这么少？
 - 答：因为这个是专门给 smolnet 访问写的，当然不排除后面会在这个基础上把 Vie 的功能全部用 java 重写一遍，毕竟 Vie 太卡了


**2.** 问：什么是 smolnet？
 - 答：我不知道咋说，要不问问 AI 吧，它说的仔细，简单来说就是一个没广告的纯文本网页


**3.** 问：为什么代码没注释？
 - 答：因为写这个就是兴趣爱好，而且我还是学生时间不多（其实就是懒），一部分代码是从之前的项目“ Vie 浏览器”直接搬过来的，比如 html 文件，和部分 java 文件，好多啊，要不就当 VieY 浏览器是闭源软件算了吧


**4.** 问：为什么项目结构这么奇怪？
 - 答：因为我没有用 Android Studio 之类的编译器，所以项目结构看起来就很奇怪，如果要放到 Android Studio 之类编译器的话，嗯，可能得你自己重新编辑一下项目结构了，还有依赖也是


**5.** 问：为什么叫 VieY？
 - 答：上一个浏览器叫 Vie（也叫 VieK），我懒得起个新名，就直接在 Vie 后面加上我以前一个好朋友 Y 的名字算了


**6.** 问：为什么不上架应用市场？
 - 答：因为好麻烦啊，写这个是纯兴趣爱好，有没有人用其实都行，写这个的过程我就挺高兴了


**7.** 问：为什么不做 TOFU 而是直接全部信任证书？
 - 答：后面可能会加上吧，以前我不知道有这东西，直接无视了

**8.** 问：为什么使用 iApp Studio 而不是常规的编译器？
 - 答：因为它支持中文，而且还能在手机上直接打包我的 Java 程序，躺着床上没事干的时候就能写两行，挺方便的

**9.** 问：怎么联系作者 / 反馈问题？
 - 答：下面有联系方式啦

---


## 9. 联系方式

如果您
- 不知道怎么使用
- 想要反馈问题
- 希望添加新功能
- 想获取最新版本源码（有时候可能会忘记上传源码，可以联系我提醒一下）

请按照以下途径联系我：
- QQ → 2171802813
- email → [VieBrowser@hotmail.com](mailto:VieBrowser@hotmail.com) （注意没有 Y）
- 酷安 → [@呆毛飘啊飘](https://www.coolapk.com/u/1318094)
- TG → @Kawaii_R

[VieY 浏览器官网](https://palhube666.wodemo.net/) / [下载途径①](https://dmpap.lanzouw.com/b02dc84ch) / [下载途径②](https://www.123pan.com/s/UML9jv-fk4bh.html) / [下载途径③](https://t.me/Kawaii_V)