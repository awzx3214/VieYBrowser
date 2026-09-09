# VieYBrowser - A Smolnet Browser for Android

![VieY Browser Icon](/icon.png)

[简体中文](/README_zh.md) | English

[VieY Browser Official Website](https://palhube666.wodemo.net/) / [Download Link ①](https://dmpap.lanzouw.com/b02dc84ch) / [Download Link ②](https://www.123pan.com/s/UML9jv-fk4bh.html) / [Download Link ③](https://t.me/Kawaii_V)

## 1. Project Overview

Welcome to download / use Vie Browser

VieY Browser is an open-source lightweight Android browser developed by 呆毛飘啊飘 (Daimao Piao A Piao).

VieY is a fork of my Vie project, which removes superfluous features while retaining and enhancing functionality related to the small net (smolnet).

The installation package is approximately 10 MB in size.

---


## 2. Features

- Supports smolnet identity/client certificates for requests
- Supports normal web page browsing
- Pull-down to refresh pages
- Long press on bottom action bar for shortcut functions
- Full-screen video playback
- Supports web page use of location, microphone, and camera
- Supports external applications accessing internal files via local storage
- Minimum support for Android 5

---


## 3. Comparison with Other Android smolnet Browsers

|Comparison Item|VieY|Vie|Lagrange|Zapri|portal.mozz.us|
|---|---|---|---|---|---|
|Size|≈10MB|≈5MB|≈20MB|≈20MB|0MB|
|Platform|Android|Android|Multi-platform|Android|Multi-platform|
|Client Certificates|Supports .bks .p12 .pem .crt/.key|Supports .bks .p12|Supports .pem|Not supported|Supports .pem|
|Multi-language|English/Chinese|Chinese only|Multi-language (but Chinese display is broken)|English only|English only|
|Strengths|Supports many protocols, can browse normal web pages|Supports Tampermonkey scripts, usable as daily browser|Supports multi-platform use|Simple and convenient|Accesses smolnet via HTTP proxy|
|Weaknesses|Too barebones|Chinese only|Outdated UI|Cannot set client certificates|Unstable; unusable if server goes down|
|http(s)://|✔ Supported|✔ Supported|✘ Not supported|✘ Not supported|✔ Supported|
|gemini://|✔ Supported|✔ Supported|✔ Supported|✔ Supported|✔ Supported|
|gopher(s)://|✔ Supported|✔ Supported|✔ Supported|✘ Not supported|✔ Supported|
|finger://|✔ Supported|✔ Supported|✔ Supported|✔ Supported|✔ Supported|
|nex://|✔ Supported|✔ Supported|✔ Supported|✘ Not supported|✔ Supported|
|text://|✔ Supported|✔ Supported|✘ Not supported|✔ Supported|✔ Supported|
|spartan://|✔ Supported|✔ Supported|✔ Supported|✘ Not supported|✔ Supported|
|kepler(s)://|✔ Supported|✔ Supported|✘ Not supported|✘ Not supported|✘ Not supported|
|scorpion(s)://|✔ Supported|✔ Supported|✘ Not supported|✘ Not supported|✘ Not supported|
|titan://|✔ Supported|✔ Supported|✔ Supported|✘ Not supported|✔ Supported|
|misfin://|✔ Supported|✔ Supported|✔ Supported|✘ Not supported|✘ Not supported|

---


## 4. Todo List

### Features

- Improve night mode
- Multi-level bookmarks
- Unify UI layout
- Supplement protocol support
- Translate page content
- Modify in-app fonts
- Support data: blob: file downloads
- Modular parsing of text/gemini, text/scroll, and other file types
- Support download functionality for various smolnet protocols

---


## 5. Protocol Support

### Protocols Supported by VieY Browser

Below are the protocols VieY can support, along with the smolnet protocols I am aware of (ordered by implementation time).

"Available" means basic interactive functionality is supported.

If you know of any smolnet protocols not listed here, please contact me!

- Http://
  - [x] Available
  - [x] Https:// (Http over TLS)
- File://
  - [x] Available
- Gemini://
  - [x] Available
    - [x] Upload using Titan:// sibling protocol
    - [ ] Upload using Nimigem:// sibling protocol
    - [ ] Upload using Inimeg:// sibling protocol
    - [ ] Upload using Iapetus:// sibling protocol
  - [x] Supports client certificates
- Finger://
  - [x] Available
  - [ ] Auto-convert text to links
  - [ ] Fingers:// (Finger over TLS)
- Gopher://
  - [x] Available
    - [x] Text rendering
    - [x] File download
  - [x] Gopher+
  - [x] Gophers:// (Gopher over TLS)
  - [ ] GopherVR
  - [ ] /_TCP raw data stream
- Spartan://
  - [x] Available
  - [ ] File upload
- Nex://
  - [x] Available
    - [x] Upload using Nps:// sibling protocol
- Text://
  - [x] Available
  - [x] data: link access
- Scorpion://
  - [x] Available
    - [x] R sub-protocol
    - [ ] S sub-protocol
    - [ ] M sub-protocol
    - [ ] I sub-protocol
      - Due to lack of test links, although other sub-protocol code has been written, correctness cannot be guaranteed, so currently only the R sub-protocol is supported.
  - [x] Supports client certificates
  - [x] Scorpions:// (Scorpion over TLS)
- Kepler://
  - [x] Available
  - [x] Supports client certificates
  - [x] Keplers:// (Kepler over TLS)
  - [ ] Page caching
    - The documentation was exhausting to read, so this wasn't implemented, though an interface has been reserved and should be implemented later.
- Titan://
  - [x] Available
    - [x] Upload text
    - [x] Upload files
  - [x] Supports client certificates
  - [ ] ;edit parameter
- Misfin://
  - [x] Available
    - [x] Misfin(A)
      - Due to lack of test links, although implemented, correctness cannot be guaranteed.
    - [x] Misfin(B)
    - [x] Misfin(C)
      - Misfin(C) is written according to draft 9.
  - [x] Supports client certificates
- Scroll://
  - [x] Available
    - When rendering custom ordered lists, some inline markup errors may occur that do not conform to specifications.
  - [x] Language list
  - [x] Metadata requests
  - [x] Supports client certificates
  - [ ] Streaming document rendering
- Molerat://
  - [x] Available
    - Due to lack of test links, correctness is not guaranteed.
  - [x] Page caching
- Nps://
  - [x] Available
- Guppy://
  - [ ] Available
    - Due to lack of test links, although basic code has been written, support is not currently planned.
- Cso://
  - [ ] Available
    - Code is written, but unsure whether to include it.
- Fsp://
  - [ ] Available
    - Under research.
- Nimigem://
  - [ ] Available
    - Under research.
- Mark://
  - [ ] Available
    - Protocol requires QUIC; adding it could double the app size.
- Terse://
  - [ ] Available
    - Difficult to implement; may never be realized.
- Inimeg://
  - [ ] 可用
    - Has specification documents, but deprecated
- Iapetus://
  - [ ] 可用
    - Has specification documents, but deprecated
- Mercury://
  - [ ] Available
    - Only a proposal exists, no specification document.
- Gequilla://
  - [ ] Available
    - Only a proposal exists, no specification document.
- Fist://
  - [ ] Available
    - Only a proposal exists, no specification document.


### Protocols Supported by Vie Browser

If you need support for:

rslsync://, text://, scroll://, scorpion://, molerat://, titan://, misfin://, btsync://, webdav://, webdavs://, davs://, dav://, ircs://, irc://, snmp://, ldap://, cso://, ed2k://, view-source:, freenet:, content://, intent:, android-app://, scp://, http://, https://, mqtts://, mqtt+ssl://, mqtt+ws://, mqtt+wss://, file://, about:, javascript:, news://, fs2you://, bc://, spartan://, nex://, gophers://, gopher://, gemini://, finger://, buss://, curl://, geo:, smsto:, mmsto:, mms:, mailto:, tel:, sms:, qqdl://, flashget://, thunder://, coaps://, magnet:, jsonfeed://, atom://, coap://, mqtt://, rss://, feed://, dns://, whois://, qotd://, daytime://, time://, rtmp://, rtsp://, smtp://, ftps://, sftp://, tftp://, ftp://, nntp://, ntp://, viek://, ws://, wss://

protocols, and if you are a Simplified Chinese user, you may choose Vie Browser.

---


## 6. User Guide

### User Guide

**1.** You can use this app as a normal browser.

**2.** Long press each button on the bottom bar for corresponding actions.

**3.** Settings support importing client certificates in multiple formats.


### Developer Guide

**1.** **Please note:** This project uses iApp Studio for packaging, so the project structure differs from traditional project structures!

**2.** If you need to compile with another compiler, you must first modify the project structure!

**3.** Except for the .java files in the kawaii.viey.browser.xy folder, all other files are classes under the kawaii.viey.browser package — be sure to move them accordingly!

**4.** iApp Studio includes built-in dependencies such as AndroidX, so there is no need to import them again. If compiling in another compiler, you must add AndroidX and other dependencies yourself!

**5.** This project uses the Apache License 2.0 open source license and will later apply for software copyright, which will be protected under the "Copyright Law of the People's Republic of China"!
  - Permissions: Commercial use, modification, privatization, redistribution
  - Requirements: Retain original copyright notice; mark changes in modified files
  - Patent grant: Automatically grants users patent usage rights
  - Prohibitions: Using the project name for promotion (unless permitted)
  - That is, you may use it for free and may create your own closed-source software after use.
  - However, you must retain my copyright information.
  - If making secondary modifications based on this app, you must place "This project is based on the open-source VieY Browser by Daimao Piao A Piao, original project open-source address: https://gitee.com/awzx3214/VieYBrowser/ " in a visible location such as the Acknowledgments / Open Source License section.
  - If only using part of the code, simply indicate in the open source license section that this project was used.

---


## 7. Tech Stack

- Android Java + HTML/JavaScript
- iApp Studio packaging
- Custom link parsing
- Modular implementation

---


## 8. Simple Q&A

**1.** Q: Why are there so few features?
 - A: Because this is specifically written for smolnet access. That said, it's not ruled out that I might rewrite all of Vie's features in Java on top of this later, since Vie is too laggy.


**2.** Q: What is smolnet?
 - A: I'm not sure how to put it — maybe ask an AI, it explains in detail. Simply put, it's an ad-free, plain-text web.


**3.** Q: Why is there no code comments?
 - A: Because I do this as a hobby, and I'm still a student with limited time (honestly, I'm just lazy). Some code was directly copied from my previous project "Vie Browser", like HTML files and some Java files. There's so much of it — maybe just consider VieY Browser as closed-source software.


**4.** Q: Why is the project structure so weird?
 - A: Because I didn't use Android Studio or similar compilers, so the project structure looks unusual. If you want to put it into Android Studio or the like, well, you might have to restructure the project and dependencies yourself.


**5.** Q: Why is it called VieY?
 - A: The previous browser was called Vie (also VieK). I was too lazy to come up with a new name, so I just appended the name Y from an old friend to Vie.


**6.** Q: Why not list it on app stores?
 - A: Because it's such a hassle. I write this purely as a hobby — it doesn't matter if anyone uses it or not. The process of writing it already makes me happy.


**7.** Q: Why not implement TOFU and instead trust all certificates outright?
 - A: Might add it later. I didn't know about this before and simply ignored it.

**8.** Q: Why use iApp Studio instead of a regular compiler?
 - A: Because it supports Chinese, and it can package my Java programs directly on a mobile phone. I can write a couple lines of code while lying in bed when I'm free, which is pretty convenient.

**9.** Q: How can I contact the author / report issues?
 - A: Contact info is below.

---


## 9. Contact Information

If you:
- Don't know how to use it
- Want to report issues
- Hope to add new features
- Want to get the latest source code (sometimes I may forget to upload source code; feel free to remind me)

Please reach out via the following channels:
- QQ → 2171802813
- email → [VieBrowser@hotmail.com](mailto:VieBrowser@hotmail.com) （Vie without Y）
- Coolapk → [@呆毛飘啊飘](https://www.coolapk.com/u/1318094)
- TG → @Kawaii_R

[VieY Browser Official Website](https://palhube666.wodemo.net/) / [Download Link ①](https://dmpap.lanzouw.com/b02dc84ch) / [Download Link ②](https://www.123pan.com/s/UML9jv-fk4bh.html) / [Download Link ③](https://t.me/Kawaii_V)