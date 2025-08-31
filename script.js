// Hand Cricket Game JavaScript

class HandCricketGame {
  constructor() {
    this.gameURL = "https://handcricket-game-g-in.onecompiler.app";
    this.difficulty = 'easy';
    this.themeClickCount = 0;
    this.p_run = 0;
    this.c_run = 0;
    this.playerChoice = '';
    this.gamePhase = 'toss';
    this.secondPhase = false;
    this.prevMoves = [];
    this.sameChoiceCount = 0;
    this.lastPlayerInput = null;
    
    this.initializeElements();
    this.bindEvents();
  }

  initializeElements() {
    this.playerRunInput = document.getElementById('player-run');
    this.playerScoreDisplay = document.getElementById('player-score');
    this.computerScoreDisplay = document.getElementById('computer-score');
    this.playerChoiceDisplay = document.getElementById('player-choice-display');
    this.computerChoiceDisplay = document.getElementById('computer-choice-display');
    this.messageBox = document.getElementById('message-box');
    this.gameStatus = document.getElementById('game-status');
    this.shareBtn = document.getElementById('share-btn');
    this.screenshotBtn = document.getElementById('screenshot-btn');
    this.container = document.getElementById('main-container');
  }

  bindEvents() {
    document.addEventListener("keydown", (e) => {
      if (e.key === "Enter") this.playRound();
      if (e.key === "Shift") this.resetGame();
    });
  }

  toggleTheme() {
    const body = document.body;
    this.themeClickCount++;
    if (this.themeClickCount === 9) {
      body.className = 'rainbow';
      this.showPopup("🌈 Rainbow Theme Unlocked!");
    } else {
      const themes = ['dark', 'light', 'purple'];
      const current = themes.indexOf(body.className);
      body.className = themes[(current + 1) % themes.length];
    }
  }

  toggleDifficulty() {
    const box = document.getElementById("difficulty-box");
    box.classList.toggle("hidden");
  }

  setDifficulty(level) {
    this.difficulty = level;
    document.getElementById("difficulty-box").classList.add("hidden");
    ["easy", "medium", "hard"].forEach(id => {
      document.getElementById(`${id}-btn`).classList.remove("selected");
    });
    document.getElementById(`${level}-btn`).classList.add("selected");
  }

  toss(playerToss) {
    const compToss = Math.random() < 0.5 ? 'head' : 'tails';
    if (playerToss === compToss) {
      this.messageBox.innerHTML = `You chose <strong>${playerToss}</strong>, Computer chose <strong>${compToss}</strong>.<br>You won the toss! Choose to Bat or Bowl.`;
      document.getElementById('toss-section').classList.add('hidden');
      document.getElementById('choice-section').classList.remove('hidden');
    } else {
      const compRole = Math.random() < 0.5 ? 'batting' : 'bowling';
      this.playerChoice = (compRole === 'batting') ? 'bowling' : 'batting';
      this.gamePhase = this.playerChoice;
      this.messageBox.innerHTML = `Computer won the toss and chose to ${compRole}. You are ${this.playerChoice}.`;
      document.getElementById('toss-section').classList.add('hidden');
      setTimeout(() => this.startGame(), 1500);
    }
  }

  setChoice(choice) {
    this.playerChoice = choice;
    this.gamePhase = choice;
    document.getElementById('choice-section').classList.add('hidden');
    this.startGame();
  }

  startGame() {
    document.getElementById('game-section').classList.remove('hidden');
    this.gameStatus.textContent = `You are ${this.playerChoice}${this.secondPhase ? " (2nd Innings)" : ""}`;
    this.playerRunInput.focus();
  }

  generateComputerMove() {
    if (this.difficulty === 'easy') return Math.floor(Math.random() * 11);
    
    if (this.difficulty === 'medium') {
      const bias = Math.random() < 0.3 ? 
        this.prevMoves[this.prevMoves.length - 1] || Math.floor(Math.random() * 11) : 
        Math.floor(Math.random() * 11);
      return Math.max(0, Math.min(10, bias));
    }
    
    if (this.difficulty === 'hard') {
      const freq = {};
      this.prevMoves.forEach(n => freq[n] = (freq[n] || 0) + 1);
      const sorted = Object.entries(freq).sort((a, b) => b[1] - a[1]);
      const predict = sorted.length ? parseInt(sorted[0][0]) : Math.floor(Math.random() * 11);
      return Math.max(0, Math.min(10, predict + (Math.random() < 0.5 ? 0 : (Math.random() < 0.5 ? 1 : -1))));
    }
  }

  playRound() {
    const val = parseInt(this.playerRunInput.value);
    if (isNaN(val) || val < 0 || val > 10) {
      this.messageBox.innerText = "Enter a number between 0 and 10.";
      return;
    }

    if (val === this.lastPlayerInput) {
      this.sameChoiceCount++;
      if (this.sameChoiceCount === 3) this.showPopup("⚠ Warning: Don't repeat the same number!");
      if (this.sameChoiceCount === 4) this.showPopup("😬 Be careful! Repeating again will get you OUT!");
    } else {
      this.sameChoiceCount = 1;
      this.lastPlayerInput = val;
    }

    if (this.sameChoiceCount >= 5 && this.gamePhase === 'batting') {
      this.messageBox.innerHTML = "❌ You used the same number 5 times! You're out!";
      document.getElementById('losinghorn').play();
      this.animateOut();
      if (!this.secondPhase) {
        this.secondPhase = true;
        this.playerChoice = 'bowling';
        this.gamePhase = 'bowling';
        setTimeout(() => this.startGame(), 1500);
      } else this.endGame();
      this.playerRunInput.value = '';
      return;
    }

    const comp = this.generateComputerMove();
    const isOut = val === comp;
    this.prevMoves.push(val);
    this.playerChoiceDisplay.textContent = val;
    this.computerChoiceDisplay.textContent = comp;

    if (this.gamePhase === 'batting') {
      if (isOut) {
        document.getElementById('losinghorn').play();
        this.animateOut();
        if (!this.secondPhase) {
          this.secondPhase = true;
          this.playerChoice = 'bowling';
          this.gamePhase = 'bowling';
          this.messageBox.innerText = "You are out! Now Bowling – Defend your score!";
          setTimeout(() => this.startGame(), 1000);
        } else this.endGame();
      } else {
        this.p_run += val === 0 ? comp : val;
        this.updateScores();
        if (this.secondPhase && this.p_run > this.c_run) this.win();
      }
    } else {
      if (isOut) {
        document.getElementById('losinghorn').play();
        this.animateOut();
        if (!this.secondPhase) {
          this.secondPhase = true;
          this.playerChoice = 'batting';
          this.gamePhase = 'batting';
          this.messageBox.innerText = "Computer is out! Now Batting – Chase the score!";
          setTimeout(() => this.startGame(), 1000);
        } else this.endGame();
      } else {
        this.c_run += comp;
        this.updateScores();
        if (this.secondPhase && this.c_run > this.p_run) this.lose();
      }
    }

    this.playerRunInput.value = '';
  }

  updateScores() {
    this.playerScoreDisplay.textContent = this.p_run;
    this.computerScoreDisplay.textContent = this.c_run;
  }

  endGame() {
    let result = `🏁 Game Over!<br>Your Score: <strong>${this.p_run}</strong><br>Computer Score: <strong>${this.c_run}</strong><br>`;
    if (this.p_run > this.c_run) {
      result += "🎉 You win!";
      this.playSound('win');
    } else if (this.c_run > this.p_run) {
      result += "💔 You lose!";
      this.playSound('lose');
    } else {
      result += "🤝 It's a tie!";
    }
    this.messageBox.innerHTML = result;
    document.getElementById('game-section').classList.add('hidden');
  }

  win() {
    this.endGame();
  }

  lose() {
    this.endGame();
  }

  playSound(type) {
    document.getElementById(type === 'win' ? 'clapssound' : 'losinghorn').play();
  }

  resetGame() {
    this.p_run = 0;
    this.c_run = 0;
    this.secondPhase = false;
    this.gamePhase = 'toss';
    this.prevMoves = [];
    this.sameChoiceCount = 0;
    this.lastPlayerInput = null;
    this.updateScores();
    this.playerChoiceDisplay.textContent = "-";
    this.computerChoiceDisplay.textContent = "-";
    this.messageBox.textContent = "Welcome! Choose HEAD or TAILS to start the toss.";
    this.gameStatus.textContent = '';
    document.getElementById('game-section').classList.add('hidden');
    document.getElementById('toss-section').classList.remove('hidden');
    document.getElementById('choice-section').classList.add('hidden');
    this.shareBtn.classList.add('hidden');
  }

  showPopup(message) {
    const popup = document.createElement("div");
    popup.className = "rainbow-popup";
    popup.innerText = message;
    document.getElementById("popup-container").appendChild(popup);
    setTimeout(() => popup.remove(), 6000);
  }

  animateOut() {
    this.container.classList.add("shake");
    setTimeout(() => this.container.classList.remove("shake"), 600);
  }

  captureAndShare() {
    console.log("captureAndShare() called");
    this.screenshotBtn.disabled = true;

    html2canvas(document.getElementById('main-container'), {
      useCORS: true,
      allowTaint: true,
      logging: true,
    })
    .then(canvas => {
      console.log("html2canvas succeeded");
      try {
        this.shareImageWhatsApp(canvas.toDataURL('image/png'));
      } catch (shareError) {
        console.error("Error sharing via WhatsApp:", shareError);
        alert("Error sharing. See console.");
      }
    })
    .catch(error => {
      console.error("html2canvas failed:", error);
      alert("Screenshot failed. See console.");
    })
    .finally(() => {
      this.screenshotBtn.disabled = false;
    });
  }

  shareImageWhatsApp(imgData) {
    console.log("shareImageWhatsApp() called");

    if (!imgData) {
      console.error("No image data to share.");
      alert("No image data to share.");
      return;
    }

    if (navigator.share) {
      console.log("Web Share API is supported");
      const byteString = atob(imgData.split(',')[1]);
      const mimeString = imgData.split(',')[0].split(':')[1].split(';')[0];
      const ab = new ArrayBuffer(byteString.length);
      const ia = new Uint8Array(ab);
      for (let i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
      }
      const blob = new Blob([ab], { type: mimeString });
      const file = new File([blob], "game_screenshot.png", { type: mimeString });

      navigator.share({
        files: [file],
        title: 'Odd or Even Cricket Game Result',
        text: 'Check out my score!',
      })
      .then(() => console.log('Shared successfully'))
      .catch((error) => console.error('Error sharing using Web Share API:', error));
    } else {
      console.log("Web Share API not supported");
      window.open(imgData, '_blank');
    }
  }
}

// Initialize the game when DOM is loaded
let game;
document.addEventListener('DOMContentLoaded', () => {
  game = new HandCricketGame();
});

// Global functions for HTML onclick handlers
function toggleTheme() {
  game.toggleTheme();
}

function toggleDifficulty() {
  game.toggleDifficulty();
}

function setDifficulty(level) {
  game.setDifficulty(level);
}

function toss(playerToss) {
  game.toss(playerToss);
}

function setChoice(choice) {
  game.setChoice(choice);
}

function playRound() {
  game.playRound();
}

function resetGame() {
  game.resetGame();
}

function captureAndShare() {
  game.captureAndShare();
}
