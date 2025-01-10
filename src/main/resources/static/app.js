document.addEventListener('DOMContentLoaded', function() {
    var messageInput = document.getElementById('chat-message-input');
    var sendButton = document.getElementById('chat-message-send');
    var chatMessages = document.querySelector('.chat-messages');

    var stompClient = null;

    function connect() {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function(frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/public', function(message) {
                showMessage(JSON.parse(message.body));
            });
            stompClient.send("/app/chat.addUser", {}, JSON.stringify({'sender': username}));
        });
    }

    function sendMessage() {
        var message = messageInput.value.trim();
        if (message) {
            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({'content': message, 'recipient': recipientUsername}));
            messageInput.value = '';
        }
    }

    function showMessage(message) {
        var messageElement = document.createElement('div');
        messageElement.classList.add('message');
        messageElement.textContent = message.sender + ': ' + message.content;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function login() {
        username = prompt('Please enter your username:');
        recipientUsername = prompt('Please enter the recipient username:');
        connect();
    }

    login();

    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            sendMessage();
        }
    });
});

