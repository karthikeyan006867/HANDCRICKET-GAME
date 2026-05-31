# 🏏 Odd or Even Cricket Game - Ultimate Edition

A stunning, feature-rich Odd or Even Cricket Game with Neon database integration, player rankings, and multiple difficulty levels including the impossible **Nightmare Mode**!

![Game Preview](https://img.shields.io/badge/Version-2.0-blue) ![License](https://img.shields.io/badge/License-MIT-green) ![Database](https://img.shields.io/badge/Database-Neon_PostgreSQL-purple)

## ✨ Features

### 🎮 Game Features
- **🪙 Animated Coin Toss** - Beautiful flip animation with sound effects
- **🏏 Batting & Bowling** - Full cricket experience with innings
- **🎯 Number Grid** - Easy-to-use button interface (0-10)
- **📱 Responsive Design** - Works perfectly on all devices

### 🧠 Difficulty Levels
- **😊 Easy** - Random computer moves
- **😐 Medium** - Computer learns from your patterns
- **😈 Hard** - Advanced AI prediction
- **💀 Nightmare** - IMPOSSIBLE TO WIN! Computer cheats to ensure you lose

### 👤 Player System
- **User Registration** - Unique usernames stored in database
- **Login System** - Your progress is saved
- **📊 Player Stats** - Track games, wins, losses, highest scores
- **🏆 Leaderboard** - Compete with other players globally

### 🎨 Themes & UI
- **🌙 Dark Theme** - Default sleek dark mode
- **☀️ Light Theme** - Clean light interface
- **💜 Purple Theme** - Royal purple vibes
- **🌈 Rainbow Theme** - Secret unlock (click theme 9 times!)
- **Beautiful Animations** - Confetti, shake effects, glowing elements
- **😀 Emoji Support** - Fun emojis throughout

### 💾 Database Integration
- **Neon PostgreSQL** - Cloud database for persistent storage
- **Game History** - All games saved with details
- **Real-time Rankings** - Updated after each game

## 📁 Project Structure

```bash
HANDCRICKET-GAME/
├── index.html          # Main game (all-in-one HTML)
├── README.md           # Documentation
├── LICENSE             # MIT License
├── vercel.json          # Vercel routing config
├── package.json         # Root dependencies for Vercel API
├── api/
│   └── index.js         # Vercel serverless API entry
└── backend/
    ├── server.js       # Express API server
  ├── public_leaderboard.json # Public fallback leaderboard
    ├── package.json    # Node.js dependencies
    └── .env            # Database configuration
```

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/HANDCRICKET-GAME.git
cd HANDCRICKET-GAME
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Configure Database
The `.env` file is pre-configured with Neon PostgreSQL. For your own database:
```env
DATABASE_URL=postgresql://user:password@host/database?sslmode=require
PORT=3000
```

### 4. Start the Server
```bash
npm start
```

### 5. Open the Game
Open `http://localhost:3000` in your browser.

## ☁️ Deploy to Vercel + Neon

1. **Push to GitHub** and import the repo into Vercel.
2. **Framework Preset:** Other.
3. **Environment Variables** in Vercel:
  - `DATABASE_URL` = your Neon pooled connection string (keep it secret)
4. **Deploy** — Vercel will serve `index.html` and run the API at `/api/*`.

If the database is unavailable, the app falls back to `backend/public_leaderboard.json` so the leaderboard remains public.

## 🎮 How to Play

1. **Enter Your Name** - Create a unique username or login
2. **Coin Toss** - Choose HEAD or TAILS
3. **Select Role** - If you win, choose Batting or Bowling
4. **Play Numbers** - Click numbers 0-10 or use keyboard
5. **Score Runs** - If batting, your number = runs (unless OUT)
6. **Get Wickets** - If bowling, match computer's number to get them OUT
7. **Win the Game** - Score more than the computer!

### ⌨️ Keyboard Shortcuts
- `0-9` keys - Quick number selection
- `Enter` - Play the round
- Numbers appear on the grid as you type

## 🏆 Ranking System

Players are ranked by:
1. Total Wins
2. Win Rate Percentage
3. Highest Score
4. Best Win Streak

View rankings anytime by clicking the 🏆 button!

## 💀 Nightmare Mode

**Warning**: This mode is designed to be IMPOSSIBLE!

The computer will:
- Match your number 70% of the time when you're batting (OUT!)
- Never match your number when batting (scoring 7-10 every time)
- Basically cheat to ensure you lose

Only the bravest players attempt this mode! 😈

## 🛠️ API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/check-username/:name` | GET | Check if username exists |
| `/api/register` | POST | Register new player |
| `/api/login` | POST | Login existing player |
| `/api/save-game` | POST | Save game result |
| `/api/player/:id` | GET | Get player stats |
| `/api/leaderboard` | GET | Get top 50 players |
| `/api/rank/:id` | GET | Get player's rank |
| `/api/history/:id` | GET | Get game history |
| `/api/health` | GET | Check server status |

## 🎨 Customization

### Adding New Themes
Add CSS variables in the style section:
```css
body.mytheme {
  --bg-color: #your-color;
  --primary: #your-primary;
  /* ... */
}
```

### Modifying Difficulty
Edit the `generateComputerMove()` function in the script section.

## 📝 Database Schema

### Players Table
```sql
CREATE TABLE players (
  id SERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  total_games INTEGER DEFAULT 0,
  total_wins INTEGER DEFAULT 0,
  total_losses INTEGER DEFAULT 0,
  highest_score INTEGER DEFAULT 0,
  max_win_streak INTEGER DEFAULT 0,
  ...
);
```

### Game History Table
```sql
CREATE TABLE game_history (
  id SERIAL PRIMARY KEY,
  player_id INTEGER REFERENCES players(id),
  player_score INTEGER NOT NULL,
  computer_score INTEGER NOT NULL,
  result VARCHAR(10),
  difficulty VARCHAR(20),
  played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file.

## 🙏 Credits

- **Sound Effects**: [Mixkit](https://mixkit.co/)
- **Font**: [Google Fonts - Poppins](https://fonts.google.com/specimen/Poppins)
- **Database**: [Neon](https://neon.tech/)
- **Icons**: Native Emoji

---

Made with ❤️ and 🏏

**Enjoy the game! May the odds be ever in your favor... unless you're in Nightmare mode! 💀**

1. Open `index.html` in a web browser
2. Choose HEAD or TAILS for the coin toss
3. Select whether to bat or bowl first
4. Enter numbers 0-10 to play each round
5. Try to avoid matching the computer's number when batting
6. Try to match the computer's number when bowling

## Technical Features

- **Object-Oriented JavaScript**: Clean class-based architecture
- **Separation of Concerns**: HTML structure, CSS styling, and JS logic in separate files
- **Modern CSS**: CSS custom properties (variables) for theming
- **Responsive Design**: Mobile-friendly layout
- **Progressive Enhancement**: Works without JavaScript for basic functionality

## Browser Compatibility

- Modern browsers supporting ES6+ features
- HTML5 Canvas support required for screenshot functionality
- Web Share API for enhanced sharing (optional)

## Installation

1. Clone or download the project files
2. Open `index.html` in a web browser
3. No additional setup required - it's a client-side only application

## Development

To modify the game:

- Edit `styles.css` for visual changes
- Modify `script.js` for game logic changes
- Update `index.html` for structural changes

The code is well-commented and organized for easy maintenance and extension.
