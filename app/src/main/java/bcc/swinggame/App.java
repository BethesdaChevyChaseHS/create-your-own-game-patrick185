package bcc.swinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(FarmingGame::new);
    }
}

class FarmingGame extends JFrame {
    private enum State { EMPTY, WEED, PLANTED, PEST, GROWN, WITHERED }
    private State state = State.EMPTY;
    private int score = 0;
    private int seeds = 5;
    private Timer witherTimer;
    private final Random random = new Random();

    private JButton plantButton;
    private JButton growButton;
    private JButton harvestButton;
    private JButton clearButton;
    private JButton storeButton;
    private JButton getSeedsButton;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JLabel seedsLabel;

    // Gameplay modifiers from store items
    private boolean fertilizer = false;
    private boolean goldenHoe = false;
    private boolean wateringCan = false;
    private boolean scarecrow = false;
    private boolean weedMat = false;

    // Cosmetic/fun items
    private boolean redBarn = false;
    private boolean tractor = false;
    private boolean windmill = false;
    private boolean farmDog = false;
    private boolean flowerBed = false;

    // Base variables
    private int growthTime = 5000;
    private int weedChance = 10;
    private int pestChance = 25;
    private int seedCost = 1;

    public FarmingGame() {
        setTitle("Fun Farming Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Field is empty. Plant a seed!", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        add(statusLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        plantButton = new JButton("Plant");
        growButton = new JButton("Grow");
        harvestButton = new JButton("Harvest");
        clearButton = new JButton("Clear");
        storeButton = new JButton("Farm Store");
        getSeedsButton = new JButton("Get Seeds");

        plantButton.addActionListener(e -> plantSeed());
        growButton.addActionListener(e -> growCrop());
        harvestButton.addActionListener(e -> harvestCrop());
        clearButton.addActionListener(e -> clearObstacle());
        storeButton.addActionListener(e -> openStore());
        getSeedsButton.addActionListener(e -> {
            seeds += 3;
            seedsLabel.setText("Seeds: " + seeds);
            statusLabel.setText("You received 3 seeds!");
        });

        buttonPanel.add(plantButton);
        buttonPanel.add(growButton);
        buttonPanel.add(harvestButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(storeButton);
        buttonPanel.add(getSeedsButton);

        add(buttonPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(1, 2));
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        seedsLabel = new JLabel("Seeds: 5", SwingConstants.CENTER);
        infoPanel.add(scoreLabel);
        infoPanel.add(seedsLabel);

        add(infoPanel, BorderLayout.SOUTH);

        updateButtons();

        setSize(700, 220);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void plantSeed() {
        if (state != State.EMPTY) {
            penalize("Can't plant now!");
            return;
        }
        if (seeds < seedCost) {
            statusLabel.setText("Not enough seeds! Harvest or get more.");
            return;
        }
        seeds -= seedCost;
        seedsLabel.setText("Seeds: " + seeds);
        if (!weedMat && random.nextInt(100) < weedChance) {
            state = State.WEED;
            statusLabel.setText("Weeds appeared! Clear them first.");
        } else {
            state = State.PLANTED;
            statusLabel.setText("Seed planted! Grow it.");
        }
        updateButtons();
    }

    private void growCrop() {
        if (state == State.PLANTED) {
            if (!scarecrow && random.nextInt(100) < pestChance) {
                state = State.PEST;
                statusLabel.setText("Pests attacked! Clear them before harvesting.");
            } else {
                state = State.GROWN;
                statusLabel.setText("Crop grown! Harvest quickly before it withers!");
                startWitherTimer();
            }
        } else {
            penalize("Can't grow now!");
        }
        updateButtons();
    }

    private void harvestCrop() {
        if (state == State.GROWN) {
            stopWitherTimer();
            state = State.EMPTY;
            int scoreGain = goldenHoe ? 2 : 1;
            int seedGain = fertilizer ? 2 : 1;
            score += scoreGain;
            seeds += seedGain;
            statusLabel.setText("Harvested! Field is empty. Plant again.");
            scoreLabel.setText("Score: " + score);
            seedsLabel.setText("Seeds: " + seeds);
        } else if (state == State.WITHERED) {
            state = State.EMPTY;
            statusLabel.setText("Withered crop cleared. Plant again.");
        } else {
            penalize("Can't harvest now!");
        }
        updateButtons();
    }

    private void clearObstacle() {
        if (state == State.WEED) {
            state = State.EMPTY;
            statusLabel.setText("Weeds cleared! Plant a seed.");
        } else if (state == State.PEST) {
            state = State.GROWN;
            statusLabel.setText("Pests cleared! Now harvest quickly!");
            startWitherTimer();
        } else if (state == State.WITHERED) {
            state = State.EMPTY;
            statusLabel.setText("Withered crop cleared. Plant again.");
        } else {
            penalize("Nothing to clear!");
        }
        updateButtons();
    }

    private void penalize(String message) {
        score--;
        if (score < 0) score = 0;
        statusLabel.setText(message + " -1 point.");
        scoreLabel.setText("Score: " + score);
    }

    private void startWitherTimer() {
        stopWitherTimer();
        if (wateringCan) return; // Crops never wither!
        int time = growthTime;
        Timer timer = new Timer(time, e -> {
            if (state == State.GROWN) {
                state = State.WITHERED;
                statusLabel.setText("Crop withered! Clear it.");
                updateButtons();
                penalize("Crop withered!");
            }
        });
        witherTimer = timer;
        timer.setRepeats(false);
        timer.start();
    }

    private void stopWitherTimer() {
        if (witherTimer != null) {
            witherTimer.stop();
            witherTimer = null;
        }
    }

    private void updateButtons() {
        plantButton.setEnabled(state == State.EMPTY);
        growButton.setEnabled(state == State.PLANTED);
        harvestButton.setEnabled(state == State.GROWN || state == State.WITHERED);
        clearButton.setEnabled(state == State.WEED || state == State.PEST || state == State.WITHERED);
        storeButton.setEnabled(true);
        getSeedsButton.setEnabled(true);
    }

    private void openStore() {
        JDialog store = new JDialog(this, "Farm Store", true);
        store.setLayout(new GridLayout(11, 1));

        JButton fertilizerBtn = new JButton("Fertilizer (+1 seed per harvest, 8 pts)" + (fertilizer ? " (OWNED)" : ""));
        JButton goldenHoeBtn = new JButton("Golden Hoe (+2 score per harvest, 10 pts)" + (goldenHoe ? " (OWNED)" : ""));
        JButton wateringCanBtn = new JButton("Watering Can (Crops never wither, 12 pts)" + (wateringCan ? " (OWNED)" : ""));
        JButton scarecrowBtn = new JButton("Scarecrow (No pests, 7 pts)" + (scarecrow ? " (OWNED)" : ""));
        JButton weedMatBtn = new JButton("Weed Mat (No weeds, 7 pts)" + (weedMat ? " (OWNED)" : ""));
        JButton redBarnBtn = new JButton("GTA+ (Premium Subscription, 3 pts)" + (redBarn ? " (OWNED)" : ""));
        JButton tractorBtn = new JButton("Tank (Show them whos boss. 4 pts)" + (tractor ? " (OWNED)" : ""));
        JButton windmillBtn = new JButton("Dropshipping company (Start Dropshipping, 4 pts)" + (windmill ? " (OWNED)" : ""));
        JButton farmDogBtn = new JButton("Guard Pig (Hire muscle, 2 pts)" + (farmDog ? " (OWNED)" : ""));
        JButton flowerBedBtn = new JButton("Ticket to The Real World  (Take the red pill, 2 pts)" + (flowerBed ? " (OWNED)" : ""));

        fertilizerBtn.setEnabled(!fertilizer && score >= 8);
        goldenHoeBtn.setEnabled(!goldenHoe && score >= 10);
        wateringCanBtn.setEnabled(!wateringCan && score >= 12);
        scarecrowBtn.setEnabled(!scarecrow && score >= 7);
        weedMatBtn.setEnabled(!weedMat && score >= 7);
        redBarnBtn.setEnabled(!redBarn && score >= 3);
        tractorBtn.setEnabled(!tractor && score >= 4);
        windmillBtn.setEnabled(!windmill && score >= 4);
        farmDogBtn.setEnabled(!farmDog && score >= 2);
        flowerBedBtn.setEnabled(!flowerBed && score >= 2);

        fertilizerBtn.addActionListener(e -> {
            if (score >= 8 && !fertilizer) {
                score -= 8;
                fertilizer = true;
                scoreLabel.setText("Score: " + score);
                store.dispose();
                statusLabel.setText("Fertilizer bought! More seeds per harvest.");
            }
        });
        goldenHoeBtn.addActionListener(e -> {
            if (score >= 10 && !goldenHoe) {
                score -= 10;
                goldenHoe = true;
                scoreLabel.setText("Score: " + score);
                store.dispose();
                statusLabel.setText("Golden Hoe bought! More score per harvest.");
            }
        });
        wateringCanBtn.addActionListener(e -> {
            if (score >= 12 && !wateringCan) {
                score -= 12;
                wateringCan = true;
                scoreLabel.setText("Score: " + score);
                store.dispose();
                statusLabel.setText("Watering Can bought! Crops never wither.");
            }
        });
        scarecrowBtn.addActionListener(e -> {
            if (score >= 7 && !scarecrow) {
                score -= 7;
                scarecrow = true;
                scoreLabel.setText("Score: " + score);
                store.dispose();
                statusLabel.setText("Scarecrow bought! No more pests.");
            }
        });
        weedMatBtn.addActionListener(e -> {
            if (score >= 7 && !weedMat) {
                score -= 7;
                weedMat = true;
                scoreLabel.setText("Score: " + score);
                store.dispose();
                statusLabel.setText("Weed Mat bought! No more weeds.");
            }
        });
        redBarnBtn.addActionListener(e -> {
            if (score >= 3 && !redBarn) {
                score -= 3;
                redBarn = true;
                scoreLabel.setText("Score: " + score);
                store.dispose();
                statusLabel.setText("Red Barn bought! Your farm looks great.");
            }
        });
        tractorBtn.addActionListener(e -> {
            if (score >= 4 && !tractor) {
                score -= 4;
                tractor = true;
                scoreLabel.setText("Score: " + score);
                store.dispose();
                statusLabel.setText("Tractor bought! Vroom vroom.");
            }
        });
        windmillBtn.addActionListener(e -> {
            if (score >= 4 && !windmill) {
                score -= 4;
                windmill = true;
                scoreLabel.setText("Score: " + score);
                store.dispose();
                statusLabel.setText("Windmill bought! Breezy.");
            }
        });
        farmDogBtn.addActionListener(e -> {
            if (score >= 2 && !farmDog) {
                score -= 2;
                farmDog = true;
                scoreLabel.setText("Score: " + score);
                store.dispose();
                statusLabel.setText("Farm Dog bought! Woof!");
            }
        });
        flowerBedBtn.addActionListener(e -> {
            if (score >= 2 && !flowerBed) {
                score -= 2;
                flowerBed = true;
                scoreLabel.setText("Score: " + score);
                store.dispose();
                statusLabel.setText("Flower Bed bought! Beautiful.");
            }
        });

        store.add(fertilizerBtn);
        store.add(goldenHoeBtn);
        store.add(wateringCanBtn);
        store.add(scarecrowBtn);
        store.add(weedMatBtn);
        store.add(redBarnBtn);
        store.add(tractorBtn);
        store.add(windmillBtn);
        store.add(farmDogBtn);
        store.add(flowerBedBtn);

        JButton close = new JButton("Close");
        close.addActionListener(e -> store.dispose());
        store.add(close);

        store.setSize(400, 500);
        store.setLocationRelativeTo(this);
        store.setVisible(true);
    }
}