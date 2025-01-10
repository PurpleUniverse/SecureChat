let stompClient = null;
let username = null;
let selectedRecipient = null;

function connect() {
    username = document.getElementById('current-user').textContent;

    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({
            sender: username,
            type: 'JOIN'
        })
    );
}

function onError(error) {
    console.log('Could not connect to WebSocket server. Please refresh this page to try again!');
}

function sendMessage(event) {
    const messageInput = document.getElementById('message-input');
    const messageContent = messageInput.value.trim();

    if(messageContent && stompClient) {
        const chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT',
            recipient: selectedRecipient
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}

function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    const messageElement = document.createElement('div');
    messageElement.classList.add('message');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
        updateUserList(message.sender, true);
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
        updateUserList(message.sender, false);
    } else {
        messageElement.classList.add(message.sender === username ? 'sent' : 'received');
        const textElement = document.createElement('p');
        textElement.textContent = message.sender + ": " + message.content;
        messageElement.appendChild(textElement);

        const timestampElement = document.createElement('span');
        timestampElement.classList.add('timestamp');
        timestampElement.textContent = new Date().toLocaleTimeString();
        messageElement.appendChild(timestampElement);
    }

    const chatMessages = document.getElementById('chat-messages');
    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function updateUserList(username, add) {
    const userList = document.getElementById('online-users');
    if(add) {
        if(!document.getElementById(username)) {
            const userElement = document.createElement('li');
            userElement.setAttribute('id', username);
            userElement.textContent = username;
            userElement.onclick = () => selectRecipient(username);
            userList.appendChild(userElement);
        }
    } else {
        const userElement = document.getElementById(username);
        if(userElement) {
            userList.removeChild(userElement);
        }
    }
}

function selectRecipient(username) {
    selectedRecipient = username;
    document.getElementById('chat-recipient').textContent = username || 'Everyone';
    document.querySelectorAll('#online-users li').forEach(li => {
        li.classList.remove('selected');
        if(li.textContent === username) {
            li.classList.add('selected');
        }
    });
}

// Event Listeners
document.addEventListener('DOMContentLoaded', function() {
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');

    connect();

    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', function(event) {
        if(event.key === 'Enter') {
            sendMessage(event);
        }
    });
});
