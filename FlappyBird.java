
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

// Abstract class for game objects
abstract class GameObject {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Image img;

    public GameObject(int x, int y, int width, int height, Image img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
    }

    // Abstract method for drawing objects
    public abstract void draw(Graphics g);

    // Getters and Setters
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

// Bird class inheriting from GameObject
class Bird extends GameObject {
    private int velocityY;
    private final int gravity;

    public Bird(int x, int y, int width, int height, Image img) {
        super(x, y, width, height, img);
        velocityY = 0;
        gravity = 1;
    }

    public void flap() {
        velocityY = -9;
    }

    public void move() {
        velocityY += gravity;
        y += velocityY;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
    }
}

// Pipe class inheriting from GameObject
class Pipe extends GameObject {
    private int velocityX;
    boolean passed;

    public Pipe(int x, int y, int width, int height, Image img, int velocityX) {
        super(x, y, width, height, img);
        this.velocityX = velocityX;
        this.passed = false;
    }

    public void move() {
        x += velocityX;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
    }
}

// Interface for movable game objects
interface Moveable {
    void move();
}

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private final int boardWidth = 560;
    private final int boardHeight = 640;

    // Images
    private Image backgroundImg;
    private Image birdImg;
    private Image topPipeImg;
    private Image bottomPipeImg;

    // Bird
    private Bird bird;

    // Pipes
    private ArrayList<Pipe> pipes;
    private Timer placePipeTimer;

    // Game state
    private Timer gameLoop;
    private boolean gameOver;
    private boolean gameStarted;
    private boolean winner;
    private double score;

    public FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Initialize bird
        bird = new Bird(boardWidth / 8, boardWidth / 2, 34, 24, birdImg);

        // Initialize pipes
        pipes = new ArrayList<Pipe>();

        // Place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        // Game loop timer
        gameLoop = new Timer(1000 / 60, this);
    }

    private void placePipes() {
        int pipeX = boardWidth;
        int pipeY = 0;
        int pipeWidth = 64; // Scaled by 1/6
        int pipeHeight = 512;

        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(pipeX, randomPipeY, pipeWidth, pipeHeight, topPipeImg, -4);
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(pipeX, topPipe.getY() + pipeHeight + openingSpace, pipeWidth, pipeHeight, bottomPipeImg, -4);
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Draw bird
        bird.draw(g);

        // Draw pipes
        for (Pipe pipe : pipes) {
            pipe.draw(g);
        }

        // Draw scoreboard
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            if (winner) {
                g.drawString("Congratulations!", boardWidth / 2 - 130, boardHeight / 2 - 30);
                g.drawString("**Winner**", boardWidth / 2 - 80, boardHeight / 2 + 10);
            } else {
                g.drawString("Game Over: " + (int) score, 10, 35);
            }
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }

        // Draw start text if game not started
        if (!gameStarted) {
            g.setColor(Color.black);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            String startText = "Start the Game";
            int textWidth = g.getFontMetrics().stringWidth(startText);
            g.drawString(startText, boardWidth / 2 - textWidth / 2, boardHeight / 2);
        }
    }

    public void move() {
        if (!gameStarted) {
            return;
        }

        // Move bird
        bird.move();

        // Move pipes
        for (Pipe pipe : pipes) {
            pipe.move();
        }

        // Check collisions
        for (Pipe pipe : pipes) {
            if (birdCollision(bird, pipe)) {
                gameOver = true;
                winner = false;
            }
            if (!pipe.passed && bird.getX() > pipe.getX() + pipe.getWidth()) {
                score += 0.5; // Increase score when passing each pipe
                pipe.passed = true;
            }
        }

        // Check if bird hits ground
        if (bird.getY() > boardHeight - bird.getHeight()) {
            gameOver = true;
            winner = false;
        }

        // Check if win condition (score 10 points)
        if (score >= 10) {
            gameOver = true;
            winner = true;
        }
    }

    private boolean birdCollision(Bird bird, Pipe pipe) {
        return bird.getX() < pipe.getX() + pipe.getWidth() &&
                bird.getX() + bird.getWidth() > pipe.getX() &&
                bird.getY() < pipe.getY() + pipe.getHeight() &&
                bird.getY() + bird.getHeight() > pipe.getY();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();

        // Game over check
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
            showGameOverDialog();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true;
                gameLoop.start();
                placePipeTimer.start();
            }
            bird.flap(); // Bird flaps on space key press

            if (gameOver) {
                restartGame();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    private void restartGame() {
        // Reset game state
        bird.setY(boardWidth / 2);
        pipes.clear();
        gameOver = false;
        winner = false;
        score = 0;
        gameStarted = false;
        repaint();
        gameLoop.start();
        placePipeTimer.start();
    }

    private void showGameOverDialog() {
        String message = winner ? "Congratulations\n**Winner**" : "Game Over: " + (int) score;
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        restartGame();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Flappy Bird");
                FlappyBird game = new FlappyBird();
                frame.add(game);
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
