document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const loginContainer = document.getElementById('login-container');
    const registerContainer = document.getElementById('register-container');
    const chatApp = document.getElementById('chat-app');
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    const chatMessages = document.getElementById('chat-messages');
    const onlineUsers = document.getElementById('online-users');
    const currentUser = document.getElementById('current-user');
    const chatRecipient = document.getElementById('chat-recipient');

    // Navigation between login and register
    document.getElementById('show-register').addEventListener('click', (e) => {
        e.preventDefault();
        loginContainer.style.display = 'none';
        registerContainer.style.display = 'block';
    });

    document.getElementById('show-login').addEventListener('click', (e) => {
        e.preventDefault();
        registerContainer.style.display = 'none';
        loginContainer.style.display = 'block';
    });

    // WebSocket and State Management
    let stompClient = null;
    let username = null;
    let selectedRecipient = null;

    // Connect to WebSocket
    function connect(token) {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({
            'Authorization': token
        }, function(frame) {
            console.log('Connected: ' + frame);

            // Subscribe to public channel
            stompClient.subscribe('/topic/public', function(message) {
                showMessage(JSON.parse(message.body));
            });

            // Subscribe to private messages
            stompClient.subscribe('/user/queue/private', function(message) {
                showMessage(JSON.parse(message.body));
            });

            // Subscribe to user status updates
            stompClient.subscribe('/topic/users', function(message) {
                updateOnlineUsers(JSON.parse(message.body));
            });

            // Announce user presence
            stompClient.send("/app/chat.addUser", {}, JSON.stringify({
                'sender': username,
                'type': 'JOIN'
            }));
        });
    }

    // Handle Login
    document.getElementById('login-form').addEventListener('submit', function(e) {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        })
            .then(response => response.json())
            .then(data => {
                if (data.token) {
                    loginSuccess(username, data.token);
                } else {
                    alert('Login failed: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Login failed');
            });
    });

    // Handle Registration
    document.getElementById('register-form').addEventListener('submit', function(e) {
        e.preventDefault();
        const username = document.getElementById('reg-username').value;
        const password = document.getElementById('reg-password').value;
        const confirmPassword = document.getElementById('reg-confirm-password').value;

        if (password !== confirmPassword) {
            alert('Passwords do not match!');
            return;
        }

        fetch('/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Registration successful! Please login.');
                    registerContainer.style.display = 'none';
                    loginContainer.style.display = 'block';
                } else {
                    alert('Registration failed: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Registration failed');
            });
    });

    function loginSuccess(user, token) {
        username = user;
        currentUser.textContent = username;
        loginContainer.style.display = 'none';
        chatApp.style.display = 'flex';
        connect(token);
    }

    // Message Handling
    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    function sendMessage() {
        const messageContent = messageInput.value.trim();
        if (messageContent && stompClient) {
            const chatMessage = {
                sender: username,
                content: messageContent,
                recipient: selectedRecipient,
                timestamp: new Date(),
                type: 'CHAT'
            };

            if (selectedRecipient) {
                stompClient.send("/app/chat.private", {}, JSON.stringify(chatMessage));
            } else {
                stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
            }

            messageInput.value = '';
        }
    }

    function showMessage(message) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message');

        // Add sent/received styling
        if (message.sender === username) {
            messageElement.classList.add('sent');
        } else {
            messageElement.classList.add('received');
        }

        const content = document.createElement('div');
        content.classList.add('message-content');
        content.textContent = `${message.sender}: ${message.content}`;

        const timestamp = document.createElement('div');
        timestamp.classList.add('message-timestamp');
        timestamp.textContent = new Date(message.timestamp).toLocaleTimeString();

        messageElement.appendChild(content);
        messageElement.appendChild(timestamp);
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function updateOnlineUsers(users) {
        onlineUsers.innerHTML = '';
        users.forEach(user => {
            if (user !== username) {
                const li = document.createElement('li');
                li.textContent = user;
                li.addEventListener('click', () => selectRecipient(user));
                onlineUsers.appendChild(li);
            }
        });
    }

    function selectRecipient(user) {
        selectedRecipient = user;
        chatRecipient.textContent = user || 'Everyone';
        document.querySelectorAll('#online-users li').forEach(li => {
            li.classList.remove('selected');
            if (li.textContent === user) {
                li.classList.add('selected');
            }
        });
    }
});
