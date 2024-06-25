document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("login-form");
    const chatSection = document.getElementById("chat-section");
    const loginSection = document.getElementById("login-section");
    const messageForm = document.getElementById("message-form");
    const messageInput = document.getElementById("message-input");
    const messages = document.getElementById("messages");

    let username;
    let currentMessageCount = 0;
    let anonymousMode = false;

    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        username = document.getElementById("username").value;
        const password = document.getElementById("password").value;

        try {
            const response = await fetch("http://localhost:8000/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
            });

            const result = await response.json();

            if (result.success) {
                loginSection.style.display = "none";
                chatSection.style.display = "block";
                startChat();
            } else {
                alert("用户名或密码错误，请重新输入。");
            }
        } catch (error) {
            console.error("登录失败：", error);
        }
    });

    messageForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const message = messageInput.value.trim();

        if (message === "") return;

        if (message.startsWith("@@")) {
            handleCommand(message.substring(2).trim());
        } else if (message.startsWith("@")) {
            const [targetUser, ...msgParts] = message.split(" ");
            const privateMessage = msgParts.join(" ");
            sendMessage({ type: "private-message", to: targetUser.substring(1), message: privateMessage });
        } else {
            sendMessage({ type: "message", message: message });
        }

        messageInput.value = "";
    });

    function startChat() {
        setInterval(async () => {
            try {
                const response = await fetch(`http://localhost:8000/messages?count=${currentMessageCount}`);
                const result = await response.json();

                const newMessages = result.newMessages;
                newMessages.forEach(msg => {
                    addMessage(msg.username, msg.message);
                });

                currentMessageCount = result.totalMessages;
            } catch (error) {
                console.error("获取消息失败：", error);
            }
        }, 1000);
    }

    function sendMessage(message) {
        const sender = anonymousMode ? "匿名" : username;
        fetch("http://localhost:8000/send", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: `username=${encodeURIComponent(sender)}&message=${encodeURIComponent(message.message)}`
        }).catch(error => console.error("发送消息失败：", error));
    }

    function addMessage(user, message) {
        if (user !== username && message.startsWith("@") && !message.startsWith(`@${username}`)) {
            return;
        }

        const messageElement = document.createElement("div");
        messageElement.textContent = `${user}: ${message}`;
        messages.appendChild(messageElement);
        messages.scrollTop = messages.scrollHeight;
    }

    function handleCommand(command) {
        switch (command) {
            case "list":
                listOnlineUsers();
                break;
            case "quit":
                logout();
                break;
            case "showanonymous":
                alert(`当前聊天方式：${anonymousMode ? "匿名" : "实名"}`);
                break;
            case "anonymous":
                anonymousMode = !anonymousMode;
                alert(`已切换为${anonymousMode ? "匿名" : "实名"}聊天`);
                break;
            default:
                alert(`未知命令：${command}`);
        }
    }

    async function listOnlineUsers() {
        try {
            const response = await fetch("http://localhost:8000/list");
            const onlineUsers = await response.json();
            alert("当前在线用户: " + onlineUsers.join(", "));
        } catch (error) {
            console.error("获取在线用户列表失败：", error);
        }
    }

    async function logout() {
        try {
            const response = await fetch("http://localhost:8000/logout", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: `username=${encodeURIComponent(username)}`
            });
    
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
    
            const result = await response.json();
    
            if (result.success) {
                alert("已成功退出系统。");
                loginSection.style.display = "block";
                chatSection.style.display = "none";
            } else {
                alert("退出系统失败，请重试。");
            }
        } catch (error) {
            console.error("退出系统失败：", error);
        }
    }
});
