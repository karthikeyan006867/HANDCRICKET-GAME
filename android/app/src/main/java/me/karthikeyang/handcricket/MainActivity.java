package me.karthikeyang.handcricket;

import android.app.AlertDialog;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;

import java.util.Random;

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
    private static final String KEY_THEME = "theme";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_VIBRATION = "vibration_enabled";

    private final GameEngine engine = new GameEngine();
    private ApiClient api;

    private LinearLayout loginScreen;
    private LinearLayout gameScreen;
    private FrameLayout root;
    private FrameLayout effectLayer;
    private EditText usernameInput;
    private TextView loginError;
    private TextView loginTitle;
    private TextView loginSubtitle;
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
    private Button themeToggle;
    private Button soundToggle;
    private Button vibrationToggle;

    private int selectedNumber = -1;
    private JSONObject currentPlayer;
    private boolean keyboardMode = false;
    private String currentTheme = "dark";
    private boolean soundEnabled = true;
    private boolean vibrationEnabled = true;
    private ToneGenerator toneGenerator;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = new ApiClient(API_BASE_URL);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);

        loginScreen = findViewById(R.id.login_screen);
        gameScreen = findViewById(R.id.game_screen);
        root = findViewById(R.id.root);
        effectLayer = findViewById(R.id.effect_layer);
        usernameInput = findViewById(R.id.username_input);
        loginError = findViewById(R.id.login_error);
        loginTitle = findViewById(R.id.login_title);
        loginSubtitle = findViewById(R.id.login_subtitle);
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
        themeToggle = findViewById(R.id.theme_toggle);
        soundToggle = findViewById(R.id.sound_toggle);
        vibrationToggle = findViewById(R.id.vibration_toggle);

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
        themeToggle.setOnClickListener(v -> cycleTheme());
        soundToggle.setOnClickListener(v -> toggleSound());
        vibrationToggle.setOnClickListener(v -> toggleVibration());

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
        restoreSettings();
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
                playTone(ToneGenerator.TONE_PROP_BEEP, 60);
                vibrateOnce(30);
            });
        }
    }

    private void restoreSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        currentTheme = prefs.getString(KEY_THEME, "dark");
        soundEnabled = prefs.getBoolean(KEY_SOUND, true);
        vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true);
        applyTheme();
        updateToggleLabels();
    }

    private void cycleTheme() {
        String[] themes = new String[]{"dark", "light", "purple", "rainbow"};
        int index = 0;
        for (int i = 0; i < themes.length; i++) {
            if (themes[i].equals(currentTheme)) {
                index = (i + 1) % themes.length;
                break;
            }
        }
        currentTheme = themes[index];
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_THEME, currentTheme).apply();
        applyTheme();
    }

    private void applyTheme() {
        int textPrimary = getResources().getColor(R.color.text_light);
        int textDim = getResources().getColor(R.color.text_dim);

        if ("light".equals(currentTheme)) {
            root.setBackgroundColor(getResources().getColor(R.color.bg_light));
            loginScreen.setBackgroundResource(R.drawable.bg_container_light);
            gameScreen.setBackgroundResource(R.drawable.bg_container_light);
            textPrimary = getResources().getColor(R.color.text_dark);
            textDim = getResources().getColor(R.color.text_dim_dark);
        } else if ("purple".equals(currentTheme)) {
            root.setBackgroundColor(getResources().getColor(R.color.bg_purple));
            loginScreen.setBackgroundResource(R.drawable.bg_container_purple);
            gameScreen.setBackgroundResource(R.drawable.bg_container_purple);
        } else if ("rainbow".equals(currentTheme)) {
            root.setBackgroundResource(R.drawable.bg_root_rainbow);
            loginScreen.setBackgroundResource(R.drawable.bg_container);
            gameScreen.setBackgroundResource(R.drawable.bg_container);
        } else {
            root.setBackgroundColor(getResources().getColor(R.color.bg_dark));
            loginScreen.setBackgroundResource(R.drawable.bg_container);
            gameScreen.setBackgroundResource(R.drawable.bg_container);
        }

        loginTitle.setTextColor(textPrimary);
        loginSubtitle.setTextColor(textDim);
        messageBox.setTextColor(textPrimary);
        gameStatus.setTextColor(textPrimary);
        targetDisplay.setTextColor(getResources().getColor(R.color.accent));
        displayName.setTextColor(textPrimary);
    }

    private void toggleSound() {
        soundEnabled = !soundEnabled;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_SOUND, soundEnabled).apply();
        updateToggleLabels();
    }

    private void toggleVibration() {
        vibrationEnabled = !vibrationEnabled;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_VIBRATION, vibrationEnabled).apply();
        updateToggleLabels();
        if (vibrationEnabled) vibrateOnce(60);
    }

    private void updateToggleLabels() {
        soundToggle.setText(soundEnabled ? "Sound On" : "Sound Off");
        vibrationToggle.setText(vibrationEnabled ? "Vibrate On" : "Vibrate Off");
        themeToggle.setText("Theme: " + currentTheme);
    }

    private void playTone(int tone, int durationMs) {
        if (!soundEnabled || toneGenerator == null) return;
        toneGenerator.startTone(tone, durationMs);
    }

    private void vibrateOnce(int ms) {
        if (!vibrationEnabled || vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(ms);
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
        findViewById(R.id.nightmare_btn).setOnClickListener(v -> setDifficulty(GameEngine.Difficulty.NIGHTMARE));
    }

    private void setDifficulty(GameEngine.Difficulty difficulty) {
        engine.difficulty = difficulty;
        difficultyBox.setVisibility(View.GONE);
        if (difficulty == GameEngine.Difficulty.NIGHTMARE) {
            Toast.makeText(this, "Nightmare mode: expect the impossible", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Difficulty: " + difficulty.name(), Toast.LENGTH_SHORT).show();
        }
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
                playTone(ToneGenerator.TONE_PROP_NACK, 120);
                vibrateOnce(120);
                showFloatingText("OUT!", false);
                handleOut();
            } else {
                int runs = val == 0 ? comp : val;
                engine.playerScore += runs;
                updateScores();
                messageBox.setText("+" + runs + " runs");
                playTone(runs >= 6 ? ToneGenerator.TONE_PROP_BEEP2 : ToneGenerator.TONE_PROP_BEEP, 80);
                vibrateOnce(40);
                showFloatingText("+" + runs, true);
                if (engine.secondInnings && engine.playerScore > engine.computerScore) {
                    endGame();
                }
            }
        } else {
            if (isOut) {
                messageBox.setText("WICKET! Both chose " + val);
                playTone(ToneGenerator.TONE_PROP_NACK, 120);
                vibrateOnce(120);
                showFloatingText("WICKET!", true);
                handleOut();
            } else {
                engine.computerScore += comp;
                updateScores();
                messageBox.setText("Computer scored " + comp + " runs");
                playTone(ToneGenerator.TONE_PROP_BEEP, 60);
                showFloatingText("-" + comp, false);
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
        animateShake(root);
    }

    private void updateScores() {
        playerScore.setText(String.valueOf(engine.playerScore));
        computerScore.setText(String.valueOf(engine.computerScore));
        animatePulse(playerScore);
        animatePulse(computerScore);
    }

    private void animatePulse(View view) {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        view.startAnimation(pulse);
    }

    private void animateShake(View view) {
        if (view == null) return;
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        view.startAnimation(shake);
    }

    private void showFloatingText(String text, boolean positive) {
        if (effectLayer == null) return;
        final TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(20);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(positive ? getResources().getColor(R.color.success) : getResources().getColor(R.color.danger));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        effectLayer.addView(tv, lp);

        effectLayer.post(() -> {
            tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int x = (effectLayer.getWidth() - tv.getMeasuredWidth()) / 2;
            int y = effectLayer.getHeight() / 3;
            tv.setX(x);
            tv.setY(y);

            AnimatorSet set = new AnimatorSet();
            ObjectAnimator move = ObjectAnimator.ofFloat(tv, "translationY", 0f, -140f);
            ObjectAnimator fade = ObjectAnimator.ofFloat(tv, "alpha", 1f, 0f);
            set.playTogether(move, fade);
            set.setDuration(900);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    effectLayer.removeView(tv);
                }
            });
            set.start();
        });
    }

    private void showConfetti() {
        if (effectLayer == null) return;
        Random rng = new Random();
        int[] colors = new int[]{
                getResources().getColor(R.color.primary),
                getResources().getColor(R.color.secondary),
                getResources().getColor(R.color.accent),
                getResources().getColor(R.color.success),
                getResources().getColor(R.color.danger)
        };

        int count = 24;
        for (int i = 0; i < count; i++) {
            View piece = new View(this);
            int size = 8 + rng.nextInt(10);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
            piece.setLayoutParams(lp);
            piece.setBackgroundColor(colors[rng.nextInt(colors.length)]);
            effectLayer.addView(piece);

            int startX = rng.nextInt(Math.max(effectLayer.getWidth(), 1));
            piece.setX(startX);
            piece.setY(-20);

            float endY = effectLayer.getHeight() + 200f;
            ObjectAnimator fall = ObjectAnimator.ofFloat(piece, "translationY", 0f, endY);
            ObjectAnimator spin = ObjectAnimator.ofFloat(piece, "rotation", 0f, 360f + rng.nextInt(360));
            ObjectAnimator fade = ObjectAnimator.ofFloat(piece, "alpha", 1f, 0f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(fall, spin, fade);
            set.setDuration(1200 + rng.nextInt(900));
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    effectLayer.removeView(piece);
                }
            });
            set.start();
        }
    }

    private void endGame() {
        String result;
        if (engine.playerScore > engine.computerScore) {
            result = "win";
            messageBox.setText("YOU WIN! " + engine.playerScore + " - " + engine.computerScore);
            playTone(ToneGenerator.TONE_PROP_ACK, 200);
            vibrateOnce(160);
            showConfetti();
            showFloatingText("VICTORY!", true);
        } else if (engine.playerScore < engine.computerScore) {
            result = "loss";
            messageBox.setText("YOU LOSE! " + engine.playerScore + " - " + engine.computerScore);
            playTone(ToneGenerator.TONE_PROP_NACK, 200);
            vibrateOnce(160);
            showFloatingText("TRY AGAIN", false);
        } else {
            result = "tie";
            messageBox.setText("TIE! " + engine.playerScore + " - " + engine.computerScore);
            playTone(ToneGenerator.TONE_PROP_BEEP, 140);
            showFloatingText("TIE", true);
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
