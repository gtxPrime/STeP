/**
 * Educon MoTA Unified Portal - JAGO AI Voice & Multilingual Chatbot
 * Voice input via Web Speech API, RAG over MoTA scheme guidelines, Text-To-Speech
 */

window.EduconJago = (function() {
  let isListening = false;
  let recognition = null;
  let synth = window.speechSynthesis;
  const conversationHistory = [];

  function init() {
    setupSpeechRecognition();
    setupEventListeners();
  }

  function setupSpeechRecognition() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognition) {
      recognition = new SpeechRecognition();
      recognition.continuous = false;
      recognition.interimResults = false;
      recognition.lang = 'en-IN'; // Also handles Indian accents and Hinglish

      recognition.onstart = () => {
        isListening = true;
        updateMicButtonState(true);
      };

      recognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        const inputEl = document.getElementById('jago-input-text');
        if (inputEl) {
          inputEl.value = transcript;
          handleSendMessage();
        }
      };

      recognition.onerror = (event) => {
        console.warn('Speech recognition error:', event.error);
        isListening = false;
        updateMicButtonState(false);
      };

      recognition.onend = () => {
        isListening = false;
        updateMicButtonState(false);
      };
    }
  }

  function setupEventListeners() {
    const sendBtn = document.getElementById('btn-jago-send');
    const micBtn = document.getElementById('btn-jago-mic');
    const inputEl = document.getElementById('jago-input-text');

    if (sendBtn) {
      sendBtn.addEventListener('click', handleSendMessage);
    }
    if (micBtn) {
      micBtn.addEventListener('click', toggleVoiceRecognition);
    }
    if (inputEl) {
      inputEl.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
          e.preventDefault();
          handleSendMessage();
        }
      });
    }

    // Preset suggested chips
    document.querySelectorAll('.jago-suggestion-chip').forEach(chip => {
      chip.addEventListener('click', (e) => {
        const query = e.currentTarget.textContent.trim();
        askQuestion(query);
      });
    });
  }

  function updateMicButtonState(listening) {
    const micBtn = document.getElementById('btn-jago-mic');
    if (!micBtn) return;
    if (listening) {
      micBtn.classList.add('mic-active');
      micBtn.innerHTML = ' Listening...';
    } else {
      micBtn.classList.remove('mic-active');
      micBtn.innerHTML = '️ Speak';
    }
  }

  function toggleVoiceRecognition() {
    if (!recognition) {
      EduconApp.showToast("Voice speech recognition not supported in this browser. You can type your query below!");
      return;
    }
    if (isListening) {
      recognition.stop();
    } else {
      try {
        recognition.start();
      } catch (e) {
        console.warn(e);
      }
    }
  }

  async function handleSendMessage() {
    const inputEl = document.getElementById('jago-input-text');
    if (!inputEl) return;
    const msg = inputEl.value.trim();
    if (!msg) return;

    inputEl.value = '';
    await askQuestion(msg);
  }

  async function askQuestion(text) {
    appendMessage('user', text);

    const typingId = appendTypingIndicator();
    const chatContainer = document.getElementById('jago-messages-scroll');
    if (chatContainer) chatContainer.scrollTop = chatContainer.scrollHeight;

    const result = await EduconMoTa AI.chatJago(text, conversationHistory);

    removeTypingIndicator(typingId);
    appendMessage('jago', result.reply, result.source);

    if (chatContainer) chatContainer.scrollTop = chatContainer.scrollHeight;

    conversationHistory.push({ role: 'user', text });
    conversationHistory.push({ role: 'model', text: result.reply });
  }

  function appendMessage(sender, text, sourceLabel) {
    const container = document.getElementById('jago-messages-scroll');
    if (!container) return;

    const isUser = sender === 'user';
    const msgDiv = document.createElement('div');
    msgDiv.className = `chat-bubble-wrapper ${isUser ? 'user-side' : 'jago-side'}`;

    const formattedText = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                              .replace(/\n/g, '<br/>');

    msgDiv.innerHTML = `
      <div class="chat-bubble ${isUser ? 'bubble-user' : 'bubble-jago'}">
        <div class="bubble-header">
          <span class="bubble-sender">${isUser ? 'You' : 'JAGO Assistant (MoTA AI)'}</span>
          ${sourceLabel ? `<span class="bubble-source-tag">${sourceLabel}</span>` : ''}
        </div>
        <div class="bubble-text">${formattedText}</div>
        ${!isUser ? `
          <div class="bubble-actions">
            <button class="btn btn-ghost btn-xs" onclick="EduconJago.speakText(this)" data-text="${encodeURIComponent(text)}"> Read Aloud</button>
            <button class="btn btn-ghost btn-xs" onclick="navigator.clipboard.writeText('${text.replace(/'/g, "\\'")}'); EduconApp.showToast('Copied answer!');"> Copy</button>
          </div>
        ` : ''}
      </div>
    `;

    container.appendChild(msgDiv);
    container.scrollTop = container.scrollHeight;
  }

  function appendTypingIndicator() {
    const container = document.getElementById('jago-messages-scroll');
    if (!container) return null;

    const id = 'typing-' + Date.now();
    const div = document.createElement('div');
    div.id = id;
    div.className = 'chat-bubble-wrapper jago-side';
    div.innerHTML = `
      <div class="chat-bubble bubble-jago">
        <div class="typing-dots">
          <span></span><span></span><span></span>
        </div>
      </div>
    `;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
    return id;
  }

  function removeTypingIndicator(id) {
    if (!id) return;
    const el = document.getElementById(id);
    if (el) el.remove();
  }

  function speakText(btn) {
    if (!synth) return;
    const rawText = decodeURIComponent(btn.dataset.text);
    synth.cancel(); // Stop current speech
    const utterance = new SpeechSynthesisUtterance(rawText);
    utterance.rate = 1.0;
    utterance.pitch = 1.0;
    synth.speak(utterance);
    btn.textContent = '⏹ Stop Voice';
    utterance.onend = () => {
      btn.textContent = ' Read Aloud';
    };
  }

  return {
    init,
    askQuestion,
    speakText
  };
})();
