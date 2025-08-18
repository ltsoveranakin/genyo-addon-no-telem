package com.genyo.systems.modules.misc;

import com.genyo.GenyoAddon;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.utils.GenyoChatUtils;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.resource.Resource;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Einstein extends GenyoModule {

    public Einstein() {
        super(GenyoAddon.MISC, "einstein", "natural selection for society");
        readEinstein();
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> goodbye = sgGeneral.add(new BoolSetting.Builder()
        .name("Say goodbye")
        .description("If you enter an incorrect answer you say something before you can't.")
        .defaultValue(true)
        .build()
    );

    // Things
    private final Identifier file = Identifier.of(GenyoAddon.MOD_ID, "einstein/einstein.yml");
    private final List<Entry> entries = new ArrayList<>();
    private final Random random = new Random();

    // Cooldown
    private int cooldown = 5; // default: 300
    private final Timer timer = new CacheTimer();

    // Game things
    private final Timer answerTimer = new CacheTimer();
    private Entry currentEntry;
    private boolean inGame = false;
    private int remainingTime = 14;

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

        if (timer.passed(cooldown * 1000) && !inGame) {
            game();
            inGame = true;
            resetGame();
        }

        if (inGame) { // In-game checks
            if (answerTimer.passed(15000)) {
                incorrect();
            } else if (answerTimer.getElapsedTime() - ((15 - remainingTime) * 1000L) >= 1000) {
                remainingTime -= 1;
                GenyoChatUtils.sendMessage(String.valueOf(remainingTime), "genyo-einstein-remaining");
            }
        }
    }

    private void game() {
        currentEntry = entries.get(random.nextInt(entries.size()));
        if (currentEntry == null) return;

        String question = currentEntry.question;
        List<String> answers = currentEntry.answers;
        String output = "";

        output += Formatting.GRAY + "Answer or crash :D" + Formatting.RESET + "\n\n";

        output += "" + Formatting.GREEN + Formatting.BOLD + question + "\n";
        output += Formatting.DARK_GRAY + "(A) " + Formatting.GRAY + answers.get(0) + " ";
        output += Formatting.DARK_GRAY + "(B) " + Formatting.GRAY + answers.get(1) + " ";
        output += Formatting.DARK_GRAY + "(C) " + Formatting.GRAY + answers.get(2) + " ";
        output += Formatting.DARK_GRAY + "(D) " + Formatting.GRAY + answers.get(3) + "\n\n";

        output += Formatting.RESET + "" + Formatting.GRAY + "Answer with the correct letter in chat!\nYou have 15 seconds.";

        GenyoChatUtils.sendMessage(output);
        answerTimer.reset();
    }

    public void correct() {
        inGame = false;
        String output = "";

        output += Formatting.GREEN + " Correct :D";

        GenyoChatUtils.sendMessage(output);
    }

    public void incorrect() {
        inGame = false;
        resetDefaults();

        if (goodbye.get()) ChatUtils.sendPlayerMsg("I feel like leaving.");

        mc.close();
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
                // The things
                String question;
                List<String> answers;
                String correctAnswer;

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

                // Entry
                entries.add(new Entry(question, answers, correctAnswer));
            }
        } catch (Exception exception) {
            GenyoAddon.LOG.info(exception.getMessage());
        }
    }

    private static class Entry {
        private final String question;
        private final ArrayList<String> answers;
        private final Choices correctChoice;
        private final String correctAnswer;

        public Entry(String question, List<String> answers, String correctAnswer) {
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
        }

        public String getQuestion() {
            return question;
        }

        public List<String> getAnswers() {
            return answers;
        }

        public Choices getCorrectChoice() {
            return correctChoice;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        private enum Choices {
            A, B, C, D
        }
    }

    public boolean isInGame() {
        return inGame;
    }

    private void resetGame() {
        timer.reset();
        //cooldown = random.nextInt(120, 1800);
        cooldown = random.nextInt(5, 10);
    }

    private void resetDefaults() {
        timer.reset();
        answerTimer.reset();
        cooldown = 5; // default: 300
        currentEntry = null;
        inGame = false;
        remainingTime = 15;
    }
}
