# 聊天室项目

本项目是一个简单的聊天室应用程序，支持匿名和实名聊天。它包含服务器端和客户端组件，服务器端使用 Java 构建，客户端使用 HTML、CSS 和 JavaScript 构建。

## 功能

- 使用预定义的用户列表进行用户认证
- 支持多个客户端同时连接
- 实时消息传递，支持广播和私聊消息
- 匿名和实名聊天模式
- 聊天室界面使用动态渐变背景

## 前提条件

- 已安装 Java 开发工具包 (JDK)
- 已安装 Maven 以管理依赖
- 使用网络浏览器运行客户端界面

## 快速开始

### 服务器设置

1. **克隆仓库：**

   ```bash
   git clone git@github.com:lalalala5678/bupt-java-homework.git
   cd <你克隆的项目位置>
   ```

2. **将 users.txt 文件放置在项目根目录：**

   `users.txt` 文件应包含用户和密码列表，格式如下：

   ```plaintext
   user1 password1
   user2 password2
   ...
   ```

3. **编译服务器代码：**

   导航到 `src` 目录并使用以下命令编译 Java 服务器代码：

   ```bash
   cd src
   javac -cp ".;libs/gson-2.11.0.jar" com/example/chatserver/ChatServer.java
   ```

4. **运行服务器：**

   使用以下命令启动服务器：

   ```bash
   java -cp ".;libs/gson-2.11.0.jar" com.example.chatserver.ChatServer
   ```

   服务器将启动并监听 8000 端口。

### 客户端设置

1. **打开客户端界面：**

   在网络浏览器中打开 `index.html` 文件。

2. **登录：**

   输入 `users.txt` 文件中的用户名和密码进行登录。如果凭证正确，您将进入聊天室。

3. **使用聊天室：**

   - **发送消息：** 在输入框中输入消息并点击“发送”按钮，将其广播给所有用户。
   - **私聊消息：** 要发送私聊消息，以 `@用户名` 开头输入消息（例如，`@user2 你好！`）。

### 命令：

- `@@list` - 列出所有在线用户。
- `@@quit` - 退出聊天室。
- `@@showanonymous` - 显示当前是否处于匿名模式。
- `@@anonymous` - 切换匿名和实名模式。

## 项目结构

```
├── libs
│   └── gson-2.11.0.jar
├── src
│   ├── com
│   │   └── example
│   │       └── chatserver
│   │           └── ChatServer.java
│   └── users.txt
├── index.html
├── script.js
└── styles.css
```

## 依赖

Gson: 一个用于将 JSON 转换为 Java 对象和将 Java 对象转换为 JSON 的 Java 库。请确保在 `libs` 目录中有 `gson-2.11.0.jar` 文件。
