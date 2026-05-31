package me.karthikeyang.handcricket;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String API_BASE_URL = "https://handcricket-game.karthikeyang.me";
    private static final String PREFS = "handcricket_prefs";
    private static final String KEY_PLAYER = "current_player";
    private static final String KEY_INPUT_MODE = "input_mode";

    private final GameEngine engine = new GameEngine();
    private ApiClient api;

    private LinearLayout loginScreen;
    private LinearLayout gameScreen;
    private EditText usernameInput;
    private TextView loginError;
    private TextView displayName;
    private TextView displayRank;
    private TextView messageBox;
    private TextView gameStatus;
    private TextView targetDisplay;

    private LinearLayout difficultyBox;
    private LinearLayout tossSection;
    private LinearLayout choiceSection;
    private LinearLayout gameSection;
    private LinearLayout buttonInputWrapper;
    private LinearLayout keyboardInputWrapper;

    private TextView playerChoiceDisplay;
    private TextView computerChoiceDisplay;
    private TextView playerScore;
    private TextView computerScore;
    private EditText keyboardInput;
    private Button btnModeButtons;
    private Button btnModeKeyboard;

    private int selectedNumber = -1;
    private JSONObject currentPlayer;
    private boolean keyboardMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = new ApiClient(API_BASE_URL);

        loginScreen = findViewById(R.id.login_screen);
        gameScreen = findViewById(R.id.game_screen);
        usernameInput = findViewById(R.id.username_input);
        loginError = findViewById(R.id.login_error);
        displayName = findViewById(R.id.display_name);
        displayRank = findViewById(R.id.display_rank);
        messageBox = findViewById(R.id.message_box);
        gameStatus = findViewById(R.id.game_status);
        targetDisplay = findViewById(R.id.target_display);

        difficultyBox = findViewById(R.id.difficulty_box);
        tossSection = findViewById(R.id.toss_section);
        choiceSection = findViewById(R.id.choice_section);
        gameSection = findViewById(R.id.game_section);
        buttonInputWrapper = findViewById(R.id.button_input_wrapper);
        keyboardInputWrapper = findViewById(R.id.keyboard_input_wrapper);

        playerChoiceDisplay = findViewById(R.id.player_choice_display);
        computerChoiceDisplay = findViewById(R.id.computer_choice_display);
        playerScore = findViewById(R.id.player_score);
        computerScore = findViewById(R.id.computer_score);
        keyboardInput = findViewById(R.id.keyboard_input);
        btnModeButtons = findViewById(R.id.btn_mode_buttons);
        btnModeKeyboard = findViewById(R.id.btn_mode_keyboard);

        Button startBtn = findViewById(R.id.start_button);
        Button loginLeaderboard = findViewById(R.id.login_leaderboard_button);
        Button logoutBtn = findViewById(R.id.logout_button);
        Button difficultyToggle = findViewById(R.id.difficulty_toggle);
        Button leaderboardBtn = findViewById(R.id.leaderboard_button);
        Button statsBtn = findViewById(R.id.stats_button);
        Button headBtn = findViewById(R.id.head_btn);
        Button tailsBtn = findViewById(R.id.tails_btn);
        Button battingBtn = findViewById(R.id.batting_btn);
        Button bowlingBtn = findViewById(R.id.bowling_btn);
        Button playBtn = findViewById(R.id.play_button);
        Button newGameBtn = findViewById(R.id.new_game_button);

        startBtn.setOnClickListener(v -> handleLogin());
        loginLeaderboard.setOnClickListener(v -> showLeaderboard());
        logoutBtn.setOnClickListener(v -> logout());
        difficultyToggle.setOnClickListener(v -> toggleDifficulty());
        leaderboardBtn.setOnClickListener(v -> showLeaderboard());
        statsBtn.setOnClickListener(v -> showStats());

        headBtn.setOnClickListener(v -> toss("head"));
        tailsBtn.setOnClickListener(v -> toss("tails"));
        battingBtn.setOnClickListener(v -> setChoice("batting"));
        bowlingBtn.setOnClickListener(v -> setChoice("bowling"));
        playBtn.setOnClickListener(v -> playRound());
        newGameBtn.setOnClickListener(v -> resetGame());
        btnModeButtons.setOnClickListener(v -> setInputMode(false));
        btnModeKeyboard.setOnClickListener(v -> setInputMode(true));

        setupNumberButtons();
        restoreInputMode();
        restoreSession();
    }

    private void setupNumberButtons() {
        int[] ids = new int[]{R.id.num_0, R.id.num_1, R.id.num_2, R.id.num_3, R.id.num_4,
                R.id.num_5, R.id.num_6, R.id.num_7, R.id.num_8, R.id.num_9, R.id.num_10};
        for (int i = 0; i < ids.length; i++) {
            int val = i == 10 ? 10 : i;
            Button b = findViewById(ids[i]);
            b.setBackgroundResource(R.drawable.bg_button);
            b.setTextColor(getResources().getColor(R.color.text_light));
            b.setOnClickListener(v -> {
                selectedNumber = val;
                playerChoiceDisplay.setText(String.valueOf(val));
                Toast.makeText(this, "Selected " + val, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void restoreInputMode() {
        String mode = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_INPUT_MODE, "buttons");
        keyboardMode = "keyboard".equals(mode);
        setInputMode(keyboardMode);
    }

    private void setInputMode(boolean useKeyboard) {
        keyboardMode = useKeyboard;
        buttonInputWrapper.setVisibility(useKeyboard ? View.GONE : View.VISIBLE);
        keyboardInputWrapper.setVisibility(useKeyboard ? View.VISIBLE : View.GONE);
        btnModeButtons.setBackgroundResource(useKeyboard ? R.drawable.bg_button : R.drawable.bg_button_success);
        btnModeKeyboard.setBackgroundResource(useKeyboard ? R.drawable.bg_button_success : R.drawable.bg_button);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(KEY_INPUT_MODE, useKeyboard ? "keyboard" : "buttons")
                .apply();
    }

    private void restoreSession() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_PLAYER, null);
        if (raw != null) {
            try {
                currentPlayer = new JSONObject(raw);
                showGameScreen();
                updatePlayerDisplay();
            } catch (JSONException ignored) {
            }
        }
    }

    private void handleLogin() {
        String username = usernameInput.getText().toString().trim();
        if (username.length() < 2) {
            showError("Please enter a name (at least 2 characters)");
            return;
        }

        if (!NetworkUtil.isOnline(this)) {
            offlineLogin(username);
            return;
        }

        api.get("/api/check-username/" + ApiClient.encode(username), new ApiClient.JsonCallback() {
            @Override
            public void onSuccess(String body) {
                try {
                    JSONObject check = new JSONObject(body);
                    boolean exists = check.optBoolean("exists", false);
                    if (exists) {
                        loginExisting(username);
                    } else {
                        registerNew(username);
                    }
                } catch (JSONException e) {
                    offlineLogin(username);
                }
            }

            @Override
            public void onError(Exception error) {
                offlineLogin(username);
            }
        });
    }

    private void loginExisting(String username) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("username", username);
            api.post("/api/login", payload, new ApiClient.JsonCallback() {
                @Override
                public void onSuccess(String body) {
                    try {
                        JSONObject data = new JSONObject(body);
                        currentPlayer = data.optJSONObject("player");
                        savePlayer();
                        showGameScreen();
                        updatePlayerDisplay();
                    } catch (JSONException e) {
                        offlineLogin(username);
                    }
                }

                @Override
                public void onError(Exception error) {
                    offlineLogin(username);
                }
            });
        } catch (JSONException e) {
            offlineLogin(username);
        }
    }

    private void registerNew(String username) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("username", username);
            api.post("/api/register", payload, new ApiClient.JsonCallback() {
                @Override
                public void onSuccess(String body) {
                    try {
                        JSONObject data = new JSONObject(body);
                        currentPlayer = data.optJSONObject("player");
                        savePlayer();
                        showGameScreen();
                        updatePlayerDisplay();
                    } catch (JSONException e) {
                        offlineLogin(username);
                    }
                }

                @Override
                public void onError(Exception error) {
                    offlineLogin(username);
                }
            });
        } catch (JSONException e) {
            offlineLogin(username);
        }
    }

    private void offlineLogin(String username) {
        try {
            currentPlayer = new JSONObject();
            currentPlayer.put("id", System.currentTimeMillis());
            currentPlayer.put("username", username);
            currentPlayer.put("total_games", 0);
            currentPlayer.put("total_wins", 0);
            currentPlayer.put("total_losses", 0);
            currentPlayer.put("highest_score", 0);
            currentPlayer.put("max_win_streak", 0);
            savePlayer();
            Toast.makeText(this, "Offline mode", Toast.LENGTH_SHORT).show();
            showGameScreen();
            updatePlayerDisplay();
        } catch (JSONException ignored) {
        }
    }

    private void showError(String message) {
        loginError.setText(message);
        loginError.setVisibility(View.VISIBLE);
        loginError.postDelayed(() -> loginError.setVisibility(View.GONE), 2500);
    }

    private void showGameScreen() {
        loginScreen.setVisibility(View.GONE);
        gameScreen.setVisibility(View.VISIBLE);
        resetGame();
    }

    private void updatePlayerDisplay() {
        if (currentPlayer == null) return;
        displayName.setText(currentPlayer.optString("username", "Player"));

        if (!NetworkUtil.isOnline(this)) {
            displayRank.setText("Rank: #-- (offline)");
            return;
        }

        int id = currentPlayer.optInt("id", 0);
        api.get("/api/rank/" + id, new ApiClient.JsonCallback() {
            @Override
            public void onSuccess(String body) {
                try {
                    JSONObject data = new JSONObject(body);
                    int rank = data.optInt("rank", 0);
                    int total = data.optInt("totalPlayers", 0);
                    displayRank.setText("Rank: #" + rank + " of " + total);
                } catch (JSONException e) {
                    displayRank.setText("Rank: #--");
                }
            }

            @Override
            public void onError(Exception error) {
                displayRank.setText("Rank: #--");
            }
        });
    }

    private void toggleDifficulty() {
        difficultyBox.setVisibility(difficultyBox.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        findViewById(R.id.easy_btn).setOnClickListener(v -> setDifficulty(GameEngine.Difficulty.EASY));
        findViewById(R.id.medium_btn).setOnClickListener(v -> setDifficulty(GameEngine.Difficulty.MEDIUM));
        findViewById(R.id.hard_btn).setOnClickListener(v -> setDifficulty(GameEngine.Difficulty.HARD));
    }

    private void setDifficulty(GameEngine.Difficulty difficulty) {
        engine.difficulty = difficulty;
        difficultyBox.setVisibility(View.GONE);
        Toast.makeText(this, "Difficulty: " + difficulty.name(), Toast.LENGTH_SHORT).show();
    }

    private void toss(String playerToss) {
        String comp = Math.random() < 0.5 ? "head" : "tails";
        if (playerToss.equals(comp)) {
            messageBox.setText("You won the toss. Choose Batting or Bowling.");
            tossSection.setVisibility(View.GONE);
            choiceSection.setVisibility(View.VISIBLE);
        } else {
            String compRole = Math.random() < 0.5 ? "batting" : "bowling";
            engine.phase = compRole.equals("batting") ? GameEngine.Phase.BOWLING : GameEngine.Phase.BATTING;
            messageBox.setText("Computer won and chose to " + compRole + ". You are " + (engine.phase == GameEngine.Phase.BATTING ? "batting" : "bowling") + ".");
            tossSection.setVisibility(View.GONE);
            startGame();
        }
    }

    private void setChoice(String choice) {
        engine.phase = choice.equals("batting") ? GameEngine.Phase.BATTING : GameEngine.Phase.BOWLING;
        choiceSection.setVisibility(View.GONE);
        startGame();
    }

    private void startGame() {
        gameSection.setVisibility(View.VISIBLE);
        String phaseText = engine.phase == GameEngine.Phase.BATTING ? "BATTING" : "BOWLING";
        gameStatus.setText("You are " + phaseText + (engine.secondInnings ? " (2nd Innings)" : ""));
        if (engine.secondInnings) {
            int target = engine.phase == GameEngine.Phase.BATTING ? engine.computerScore + 1 : engine.playerScore + 1;
            targetDisplay.setVisibility(View.VISIBLE);
            targetDisplay.setText("Target: " + target);
        } else {
            targetDisplay.setVisibility(View.GONE);
        }
    }

    private void playRound() {
        int val = getPlayerValue();
        if (val < 0) return;
        engine.recordPlayerMove(val);
        int comp = engine.generateComputerMove(val);
        boolean isOut = val == comp;

        playerChoiceDisplay.setText(String.valueOf(val));
        computerChoiceDisplay.setText(String.valueOf(comp));

        if (engine.phase == GameEngine.Phase.BATTING) {
            if (isOut) {
                messageBox.setText("OUT! Both chose " + val);
                handleOut();
            } else {
                int runs = val == 0 ? comp : val;
                engine.playerScore += runs;
                updateScores();
                messageBox.setText("+" + runs + " runs");
                if (engine.secondInnings && engine.playerScore > engine.computerScore) {
                    endGame();
                }
            }
        } else {
            if (isOut) {
                messageBox.setText("WICKET! Both chose " + val);
                handleOut();
            } else {
                engine.computerScore += comp;
                updateScores();
                messageBox.setText("Computer scored " + comp + " runs");
                if (engine.secondInnings && engine.computerScore > engine.playerScore) {
                    endGame();
                }
            }
        }
    }

    private int getPlayerValue() {
        if (keyboardMode) {
            String raw = keyboardInput.getText().toString().trim();
            if (raw.isEmpty()) {
                Toast.makeText(this, "Enter a number 0-10", Toast.LENGTH_SHORT).show();
                return -1;
            }
            try {
                int v = Integer.parseInt(raw);
                if (v < 0 || v > 10) {
                    Toast.makeText(this, "Enter 0-10 only", Toast.LENGTH_SHORT).show();
                    return -1;
                }
                keyboardInput.setText("");
                return v;
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a number 0-10", Toast.LENGTH_SHORT).show();
                return -1;
            }
        }

        if (selectedNumber < 0) {
            Toast.makeText(this, "Select a number 0-10", Toast.LENGTH_SHORT).show();
            return -1;
        }
        return selectedNumber;
    }

    private void handleOut() {
        if (!engine.secondInnings) {
            engine.secondInnings = true;
            engine.phase = engine.phase == GameEngine.Phase.BATTING ? GameEngine.Phase.BOWLING : GameEngine.Phase.BATTING;
            startGame();
        } else {
            endGame();
        }
    }

    private void updateScores() {
        playerScore.setText(String.valueOf(engine.playerScore));
        computerScore.setText(String.valueOf(engine.computerScore));
    }

    private void endGame() {
        String result;
        if (engine.playerScore > engine.computerScore) {
            result = "win";
            messageBox.setText("YOU WIN! " + engine.playerScore + " - " + engine.computerScore);
        } else if (engine.playerScore < engine.computerScore) {
            result = "loss";
            messageBox.setText("YOU LOSE! " + engine.playerScore + " - " + engine.computerScore);
        } else {
            result = "tie";
            messageBox.setText("TIE! " + engine.playerScore + " - " + engine.computerScore);
        }

        saveGameResult(result);
        gameSection.setVisibility(View.GONE);
        tossSection.setVisibility(View.VISIBLE);
    }

    private void saveGameResult(String result) {
        if (!NetworkUtil.isOnline(this) || currentPlayer == null) return;
        try {
            JSONObject payload = new JSONObject();
            payload.put("playerId", currentPlayer.optInt("id", 0));
            payload.put("playerScore", engine.playerScore);
            payload.put("computerScore", engine.computerScore);
            payload.put("result", result);
            payload.put("difficulty", engine.difficulty.name().toLowerCase());
            api.post("/api/save-game", payload, new ApiClient.JsonCallback() {
                @Override
                public void onSuccess(String body) { }

                @Override
                public void onError(Exception error) { }
            });
        } catch (JSONException ignored) { }
    }

    private void resetGame() {
        engine.reset();
        messageBox.setText("Welcome! Choose HEAD or TAILS to start the toss.");
        playerChoiceDisplay.setText("-");
        computerChoiceDisplay.setText("-");
        updateScores();
        selectedNumber = -1;
        if (keyboardInput != null) keyboardInput.setText("");
        gameSection.setVisibility(View.GONE);
        choiceSection.setVisibility(View.GONE);
        tossSection.setVisibility(View.VISIBLE);
        targetDisplay.setVisibility(View.GONE);
    }

    private void showLeaderboard() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_leaderboard, null);
        TextView status = view.findViewById(R.id.leaderboard_status);
        RecyclerView list = view.findViewById(R.id.leaderboard_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        builder.setView(view);
        builder.setTitle("Leaderboard");

        AlertDialog dialog = builder.create();
        dialog.show();

        if (!NetworkUtil.isOnline(this)) {
            status.setVisibility(View.VISIBLE);
            status.setText("You are offline");
            list.setAdapter(new LeaderboardAdapter(new JSONArray()));
            return;
        }

        api.get("/api/leaderboard", new ApiClient.JsonCallback() {
            @Override
            public void onSuccess(String body) {
                try {
                    JSONArray data = new JSONArray(body);
                    list.setAdapter(new LeaderboardAdapter(data));
                } catch (JSONException e) {
                    status.setVisibility(View.VISIBLE);
                    status.setText("Unable to load leaderboard");
                }
            }

            @Override
            public void onError(Exception error) {
                status.setVisibility(View.VISIBLE);
                status.setText("Unable to load leaderboard");
            }
        });
    }

    private void showStats() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_stats, null);
        TextView statsText = view.findViewById(R.id.stats_text);
        builder.setView(view);
        builder.setTitle("Stats");
        AlertDialog dialog = builder.create();
        dialog.show();

        if (!NetworkUtil.isOnline(this)) {
            statsText.setText("You are offline");
            return;
        }

        if (currentPlayer == null) {
            statsText.setText("No player");
            return;
        }

        int id = currentPlayer.optInt("id", 0);
        api.get("/api/player/" + id, new ApiClient.JsonCallback() {
            @Override
            public void onSuccess(String body) {
                try {
                    JSONObject data = new JSONObject(body);
                    int games = data.optInt("total_games", 0);
                    int wins = data.optInt("total_wins", 0);
                    int losses = data.optInt("total_losses", 0);
                    int best = data.optInt("highest_score", 0);
                    int streak = data.optInt("max_win_streak", 0);
                    int winRate = games > 0 ? Math.round((wins * 100f) / games) : 0;

                    String text = "Games: " + games + "\nWins: " + wins + "\nLosses: " + losses +
                            "\nWin Rate: " + winRate + "%\nBest: " + best + "\nBest Streak: " + streak;
                    statsText.setText(text);
                } catch (JSONException e) {
                    statsText.setText("Unable to load stats");
                }
            }

            @Override
            public void onError(Exception error) {
                statsText.setText("Unable to load stats");
            }
        });
    }

    private void logout() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_PLAYER).apply();
        currentPlayer = null;
        loginScreen.setVisibility(View.VISIBLE);
        gameScreen.setVisibility(View.GONE);
    }

    private void savePlayer() {
        if (currentPlayer == null) return;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_PLAYER, currentPlayer.toString()).apply();
    }
}
