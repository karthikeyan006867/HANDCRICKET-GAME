const express = require('express');
const { Pool } = require('pg');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Neon PostgreSQL connection
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: {
    rejectUnauthorized: false
  }
});

app.use(cors());
app.use(express.json());
app.use(express.static('../'));

// Initialize database tables
async function initDB() {
  try {
    await pool.query(`
      CREATE TABLE IF NOT EXISTS players (
        id SERIAL PRIMARY KEY,
        username VARCHAR(50) UNIQUE NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        total_games INTEGER DEFAULT 0,
        total_wins INTEGER DEFAULT 0,
        total_losses INTEGER DEFAULT 0,
        total_ties INTEGER DEFAULT 0,
        highest_score INTEGER DEFAULT 0,
        total_runs INTEGER DEFAULT 0,
        win_streak INTEGER DEFAULT 0,
        max_win_streak INTEGER DEFAULT 0,
        last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
    
    await pool.query(`
      CREATE TABLE IF NOT EXISTS game_history (
        id SERIAL PRIMARY KEY,
        player_id INTEGER REFERENCES players(id),
        player_score INTEGER NOT NULL,
        computer_score INTEGER NOT NULL,
        result VARCHAR(10) NOT NULL,
        difficulty VARCHAR(20) NOT NULL,
        played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
    
    console.log('✅ Database tables initialized successfully');
  } catch (error) {
    console.error('❌ Error initializing database:', error);
  }
}

// Check if username exists
app.get('/api/check-username/:username', async (req, res) => {
  try {
    const { username } = req.params;
    const result = await pool.query(
      'SELECT id, username FROM players WHERE LOWER(username) = LOWER($1)',
      [username]
    );
    
    if (result.rows.length > 0) {
      res.json({ exists: true, player: result.rows[0] });
    } else {
      res.json({ exists: false });
    }
  } catch (error) {
    console.error('Error checking username:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Register new player
app.post('/api/register', async (req, res) => {
  try {
    const { username } = req.body;
    
    if (!username || username.trim().length < 2) {
      return res.status(400).json({ error: 'Username must be at least 2 characters' });
    }
    
    // Check if username already exists
    const existing = await pool.query(
      'SELECT id FROM players WHERE LOWER(username) = LOWER($1)',
      [username.trim()]
    );
    
    if (existing.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists! Please choose another.' });
    }
    
    // Create new player
    const result = await pool.query(
      'INSERT INTO players (username) VALUES ($1) RETURNING *',
      [username.trim()]
    );
    
    console.log('🎮 New player registered:', username);
    res.status(201).json({ success: true, player: result.rows[0] });
  } catch (error) {
    console.error('Error registering player:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Login existing player
app.post('/api/login', async (req, res) => {
  try {
    const { username } = req.body;
    
    const result = await pool.query(
      'SELECT * FROM players WHERE LOWER(username) = LOWER($1)',
      [username.trim()]
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Player not found' });
    }
    
    // Update last played
    await pool.query(
      'UPDATE players SET last_played = CURRENT_TIMESTAMP WHERE id = $1',
      [result.rows[0].id]
    );
    
    console.log('🎮 Player logged in:', username);
    res.json({ success: true, player: result.rows[0] });
  } catch (error) {
    console.error('Error logging in:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Save game result
app.post('/api/save-game', async (req, res) => {
  try {
    const { playerId, playerScore, computerScore, result, difficulty } = req.body;
    
    // Save to game history
    await pool.query(
      `INSERT INTO game_history (player_id, player_score, computer_score, result, difficulty)
       VALUES ($1, $2, $3, $4, $5)`,
      [playerId, playerScore, computerScore, result, difficulty]
    );
    
    // Get current player stats
    const currentStats = await pool.query(
      'SELECT * FROM players WHERE id = $1',
      [playerId]
    );
    
    if (currentStats.rows.length === 0) {
      return res.status(404).json({ error: 'Player not found' });
    }
    
    const player = currentStats.rows[0];
    
    // Calculate new stats
    const newTotalGames = player.total_games + 1;
    const newTotalRuns = player.total_runs + playerScore;
    const newHighestScore = Math.max(player.highest_score, playerScore);
    
    let newWins = player.total_wins;
    let newLosses = player.total_losses;
    let newTies = player.total_ties;
    let newWinStreak = player.win_streak;
    let newMaxWinStreak = player.max_win_streak;
    
    if (result === 'win') {
      newWins++;
      newWinStreak++;
      newMaxWinStreak = Math.max(newMaxWinStreak, newWinStreak);
    } else if (result === 'loss') {
      newLosses++;
      newWinStreak = 0;
    } else {
      newTies++;
    }
    
    // Update player stats
    await pool.query(
      `UPDATE players SET 
        total_games = $1,
        total_wins = $2,
        total_losses = $3,
        total_ties = $4,
        highest_score = $5,
        total_runs = $6,
        win_streak = $7,
        max_win_streak = $8,
        last_played = CURRENT_TIMESTAMP
       WHERE id = $9`,
      [newTotalGames, newWins, newLosses, newTies, newHighestScore, newTotalRuns, newWinStreak, newMaxWinStreak, playerId]
    );
    
    console.log(`🎮 Game saved for player ${playerId}: ${result}`);
    res.json({ success: true });
  } catch (error) {
    console.error('Error saving game:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Get player stats
app.get('/api/player/:id', async (req, res) => {
  try {
    const { id } = req.params;
    
    const result = await pool.query(
      'SELECT * FROM players WHERE id = $1',
      [id]
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Player not found' });
    }
    
    res.json(result.rows[0]);
  } catch (error) {
    console.error('Error getting player:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Get leaderboard (rankings)
app.get('/api/leaderboard', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT 
        id,
        username,
        total_games,
        total_wins,
        total_losses,
        total_ties,
        highest_score,
        total_runs,
        max_win_streak,
        ROUND(CASE WHEN total_games > 0 THEN (total_wins::DECIMAL / total_games) * 100 ELSE 0 END, 1) as win_rate
      FROM players 
      WHERE total_games > 0
      ORDER BY total_wins DESC, win_rate DESC, highest_score DESC
      LIMIT 50
    `);
    
    res.json(result.rows);
  } catch (error) {
    console.error('Error getting leaderboard:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Get player game history
app.get('/api/history/:playerId', async (req, res) => {
  try {
    const { playerId } = req.params;
    
    const result = await pool.query(
      `SELECT * FROM game_history 
       WHERE player_id = $1 
       ORDER BY played_at DESC 
       LIMIT 20`,
      [playerId]
    );
    
    res.json(result.rows);
  } catch (error) {
    console.error('Error getting history:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Get player rank
app.get('/api/rank/:playerId', async (req, res) => {
  try {
    const { playerId } = req.params;
    
    const result = await pool.query(`
      SELECT COUNT(*) + 1 as rank
      FROM players p1
      WHERE p1.total_wins > (SELECT total_wins FROM players WHERE id = $1)
         OR (p1.total_wins = (SELECT total_wins FROM players WHERE id = $1) 
             AND p1.highest_score > (SELECT highest_score FROM players WHERE id = $1))
    `, [playerId]);
    
    const totalPlayers = await pool.query('SELECT COUNT(*) FROM players WHERE total_games > 0');
    
    res.json({ 
      rank: parseInt(result.rows[0].rank), 
      totalPlayers: parseInt(totalPlayers.rows[0].count) 
    });
  } catch (error) {
    console.error('Error getting rank:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Health check
app.get('/api/health', async (req, res) => {
  try {
    await pool.query('SELECT 1');
    res.json({ status: 'ok', database: 'connected' });
  } catch (error) {
    res.status(500).json({ status: 'error', database: 'disconnected' });
  }
});

// Initialize DB and start server
initDB().then(() => {
  app.listen(PORT, () => {
    console.log(`🚀 Server running on http://localhost:${PORT}`);
    console.log('📊 Database connected to Neon PostgreSQL');
  });
});
