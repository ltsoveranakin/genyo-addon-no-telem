package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.utils.GenyoChatUtils;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.resource.Resource;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Einstein extends GenyoModule {

    public Einstein() {
        super(Genyo.MISC, "einstein", "natural selection of society");
        readEinstein();
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> interval = sgGeneral.add(new IntSetting.Builder()
        .name("Time Interval")
        .description("The time between the executions of the Final Solution (in minutes)")
        .min(1)
        .defaultValue(5)
        .max(20)
        .sliderRange(1, 20)
        .onChanged(this::changeCooldown)
        .build()
    );

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Difficulty")
        .description("You can be honest here (beta only includes easy questions)")
        .defaultValue(Mode.Sigma)
        .build()
    );

    private final Setting<Boolean> goodbye = sgGeneral.add(new BoolSetting.Builder()
        .name("Say goodbye")
        .description("If you enter an incorrect answer you say something before you can't.")
        .defaultValue(true)
        .build()
    );

    // Things
    private final Identifier file = Identifier.of(Genyo.MOD_ID, "einstein/einstein.yml");
    private final List<Entry> entries = new ArrayList<>();
    private final Random random = new Random();

    // Cooldown
    private int cooldown = interval.get(); // default: 300, testing: 5
    private final Timer timer = new CacheTimer();

    // Game things
    private final Timer answerTimer = new CacheTimer();
    private Entry currentEntry;
    private boolean inGame = false;
    private int remainingTime = 15;

    @Override
    public void onActivate() {
        resetDefaults();
    }

    @Override
    public void onDeactivate() {
        resetDefaults();
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.world == null && mc.player == null) return;

        if (timer.passed(cooldown * 60000) && !inGame) {
            startGame();
            resetGame();
        }

        if (inGame) { // In-game checks
            if (answerTimer.passed(15000)) {
                endGame(false);
            } else if (answerTimer.getElapsedTime() - ((15 - remainingTime) * 1000L) >= 1000) {
                remainingTime -= 1;
                GenyoChatUtils.sendMessage(String.valueOf(remainingTime), "genyo-einstein-remaining");
            }
        }
    }

    private void startGame() {
        inGame = true;

        if (mode.get() == Mode.Beta) {
            List<Entry> easyEntries = entries.stream()
                .filter(e -> e.difficulty.equals(Entry.Difficulty.Easy))
                .toList();

            currentEntry = easyEntries.get(random.nextInt(easyEntries.size()));
        } else {
            currentEntry = entries.get(random.nextInt(entries.size()));
        }

        if (currentEntry == null) return;

        String question = currentEntry.question;
        List<String> answers = currentEntry.answers;
        String output = getOutput(question, answers);

        GenyoChatUtils.sendMessage(output);
        answerTimer.reset();
    }

    private static @NotNull String getOutput(String question, List<String> answers) {
        String output = "";

        output += Formatting.GRAY + "Answer or crash :D" + Formatting.RESET + "\n\n";

        // I don't want to display the difficulty
        output += "" + Formatting.GREEN + Formatting.BOLD + question + "\n";
        output += Formatting.DARK_GRAY + "(A) " + Formatting.GRAY + answers.get(0) + " ";
        output += Formatting.DARK_GRAY + "(B) " + Formatting.GRAY + answers.get(1) + " ";
        output += Formatting.DARK_GRAY + "(C) " + Formatting.GRAY + answers.get(2) + " ";
        output += Formatting.DARK_GRAY + "(D) " + Formatting.GRAY + answers.get(3) + "\n\n";

        output += Formatting.RESET + "" + Formatting.GRAY + "Answer with the correct letter in chat!\nYou have 15 seconds.";
        return output;
    }

    public void endGame(boolean correct) {
        if (correct) {
            inGame = false;
            String output = "";

            output += Formatting.GREEN + "Correct :D";

            GenyoChatUtils.sendMessage(output);
        } else {
            inGame = false;
            resetDefaults();

            if (goodbye.get()) ChatUtils.sendPlayerMsg("I feel like leaving.");

            mc.close();
        }
    }

    public String getCorrectChoice() {
        return currentEntry.correctChoice.toString();
    }

    private void readEinstein() {
        Yaml yaml = new Yaml();

        try {
            Resource resource = mc.getResourceManager().getResource(file).orElseThrow();
            InputStream inputStream = resource.getInputStream();

            HashMap yamlMap = yaml.load(inputStream);

            List<?> keys = yamlMap.keySet().stream().toList();
            for (Object o : keys) {

                // Initialize the things
                String question;
                List<String> answers;
                String correctAnswer;
                Entry.Difficulty difficulty;

                // The decoding
                HashMap value = (HashMap) yamlMap.get(o);

                // question
                question = value.get("question").toString();

                // answers
                HashMap entryAnswers = (HashMap) value.get("answers");
                String answerA = entryAnswers.get("A").toString();
                String answerB = entryAnswers.get("B").toString();
                String answerC = entryAnswers.get("C").toString();
                String answerD = entryAnswers.get("D").toString();
                answers = List.of(answerA, answerB, answerC, answerD);

                // correct answer
                correctAnswer = value.get("correct").toString();

                // difficulty
                difficulty = Entry.Difficulty.valueOf(value.get("difficulty").toString());

                // Entry
                Entry entry = new Entry(question, answers, correctAnswer, difficulty);
                entries.add(entry);
            }
        } catch (Exception exception) {
            Genyo.LOG.error(exception.getMessage());
            sendError("Couldn't read file. Send logs to wuritz pls.");
        }
    }

    private static class Entry {
        private final String question;
        private final ArrayList<String> answers;
        private final Choices correctChoice;
        private final String correctAnswer; // remains here in case i wanna display the correct answer
        private final Difficulty difficulty;

        public Entry(String question, List<String> answers, String correctAnswer, Difficulty difficulty) {
            this.question = question;
            this.answers = new ArrayList<>(answers);
            this.correctAnswer = correctAnswer;

            this.correctChoice = switch (answers.indexOf(correctAnswer)) {
                case 0 -> Choices.A;
                case 1 -> Choices.B;
                case 2 -> Choices.C;
                case 3 -> Choices.D;
                default -> null;
            };

            this.difficulty = difficulty;
        }

        private enum Choices {
            A, B, C, D
        }

        private enum Difficulty {
            Easy, Hard
        }
    }

    public boolean isInGame() {
        return inGame;
    }

    private void resetGame() {
        timer.reset();
    }

    private void resetDefaults() {
        timer.reset();
        answerTimer.reset();
        currentEntry = null;
        inGame = false;
        remainingTime = 15;
    }

    private void changeCooldown(int newValue) {
        cooldown = newValue;
    }

    private enum Mode {
        Beta, Sigma
    }
}
