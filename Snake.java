import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

class Main {
  public static void main(String[] args) {
    System.setProperty("sun.awt.noerasebackground", "true");
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Canvas canvas = new Canvas();
    JLabel helpLabel = new JLabel("Press 'hjkl' to move");
    helpLabel.setPreferredSize(new Dimension(100, 50));
    helpLabel.setHorizontalAlignment(SwingConstants.CENTER);
    JFrame frame = new JFrame();
    frame.setTitle("Silly snake");
    frame.setSize(500, 500);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    new Timer(
            1000 / Canvas.REFRESH_RATE,
            (e) -> {
              SwingUtilities.invokeLater(
                  () -> {
                    canvas.play();
                    toolkit.sync();
                  });
            })
        .start();
    frame.add(canvas, BorderLayout.CENTER);
    frame.add(helpLabel, BorderLayout.SOUTH);
    frame.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent e) {
            if (!canvas.snake.moved) {
              canvas.snake.moved = true;
              try {
                canvas.snake.nextMove = Direction.from(e.getKeyChar());
              } catch (IllegalArgumentException ex) {
                System.out.println("Press either : i, j, k, l");
              }
            }
          }
        });
  }
}

class Canvas extends JPanel {
  public static final int REFRESH_RATE = 10;

  public static final int HEIGHT = 500;

  public static final int WIDTH = 500;

  public static final int GRID_SIZE = 10;

  public static final int SQUARE_WIDTH = 50;

  Snake snake = new Snake();

  Square food = new Square(4, 4, null, Color.PINK);

  public Canvas() {
    super();
    setSize(new Dimension(HEIGHT, WIDTH));
    setBackground(Color.black);
  }

  public void play() {
    Square tail = snake.move();
    if (tail != null) {
      boolean ate = snake.eat(food);
      if (ate) {
        snake.parts.push(tail);
        snake.sortedParts.add(tail);
        food = new Square(4, 4, null, Color.PINK);
      }
    } else {
      snake.init();
    }
    repaint();
    snake.moved = false;
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    drawGrid(g);
    snake.draw(g);
    food.draw(g);
  }

  private void drawGrid(Graphics g) {
    g.setColor(Color.RED);
    int lines = HEIGHT / SQUARE_WIDTH;
    for (int i = 0; i <= lines; i++) {
      int start = i * SQUARE_WIDTH;
      g.drawLine(0, start, WIDTH, start);
    }

    for (int i = 0; i <= lines; i++) {
      int start = i * SQUARE_WIDTH;
      g.drawLine(start, 0, start, HEIGHT);
    }
  }
}

interface Drawable {
  public void draw(Graphics g);
}

class Square implements Drawable {
  int x, y;
  Square prev;
  Color color;

  public Square(int x, int y, Square prev, Color color) {
    this.x = x;
    this.y = y;
    this.prev = prev;
    this.color = color;
  }

  @Override
  public void draw(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    if (color != null) {
      g2.setColor(color);
    } else {
      g2.setColor(Color.GREEN);
    }
    g2.fillRect(
        x * Canvas.SQUARE_WIDTH, y * Canvas.SQUARE_WIDTH, Canvas.SQUARE_WIDTH, Canvas.SQUARE_WIDTH);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Square) {
      System.out.println(this + " " + (Square) obj);
      Square other = (Square) obj;
      System.out.println(other.x == this.x && other.y == this.y);
      return (other.x == this.x && other.y == this.y);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Integer.valueOf(x).hashCode() + Integer.valueOf(y).hashCode();
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + " )";
  }
}

class Snake implements Drawable {
  boolean moved = false;

  Direction nextMove = Direction.RIGHT;

  LinkedList<Square> parts;
  Set<Square> sortedParts;

  public Snake() {
    init();
  }

  public void init() {
    parts = new LinkedList<>();
    Square s1 = new Square(0, 0, null, null);
    Square s2 = new Square(0, 1, s1, null);
    parts.addLast(s1);
    parts.addLast(s2);
    sortedParts = new HashSet<>();
    sortedParts.add(s1);
    sortedParts.add(s2);
  }

  public boolean eat(Square food) {
    Square head = parts.peekLast();
    if (head.equals(food)) {
      return true;
    }
    return false;
  }

  @Override
  public void draw(Graphics g) {
    parts.forEach(s -> s.draw(g));
  }

  public Square move() {
    Square tail = parts.pop();
    sortedParts.remove(tail);
    Square _square = parts.peekLast();
    Square square = nextMove.move(_square);
    System.out.println(sortedParts.contains(square));
    if (sortedParts.contains(square)) {
      return null;
    }
    this.parts.addLast(square);
    this.sortedParts.add(square);
    System.out.println("MOVED ( " + square.x + "," + square.y + ") => " + nextMove);
    return tail;
  }
}

interface HandleMove {
  public void handle(Square current, Square prev);
}

enum Direction implements HandleMove {
  UP {
    public void handle(Square current, Square prev) {
      boolean cond = false;
      if (prev != null) {
        int val = current.y - 1;
        if (val < 0) {
          val += 10;
        }
        cond = val == prev.y ? true : false;
      }
      if (cond) {
        current.y = (current.y + 1) % 10;
      } else if (current.y == 0) {
        current.y = Canvas.GRID_SIZE - 1;
      } else {
        current.y--;
      }
    }
  },
  DOWN {
    public void handle(Square current, Square prev) {
      boolean cond = false;
      if (prev != null) {
        int val = (current.y + 1) % 10;
        cond = val == prev.y ? true : false;
      }
      if (cond) {
        current.y--;
        if (current.y < 0) {
          current.y = 9;
        }
      } else if (current.y == Canvas.GRID_SIZE - 1) {
        current.y = 0;
      } else {
        current.y = current.y + 1;
      }
    }
  },
  LEFT {
    public void handle(Square current, Square prev) {
      if (current.x == 0) {
        current.x = Canvas.GRID_SIZE - 1;
      } else {
        current.x = current.x - 1;
      }
    }
  },
  RIGHT {
    public void handle(Square current, Square prev) {
      if (current.x == Canvas.GRID_SIZE - 1) {
        current.x = 0;
      } else {
        current.x = current.x + 1;
      }
    }
  };

  public Square move(Square _square) {
    Square square = new Square(_square.x, _square.y, _square, null);
    Square prev = _square.prev;
    this.handle(square, prev);
    return square;
  }

  public static Direction from(char c) {
    switch (c) {
      case 'k':
        return UP;
      case 'j':
        return DOWN;
      case 'h':
        return LEFT;
      case 'l':
        return RIGHT;
      default:
        throw new IllegalArgumentException("press the right key idiot");
    }
  }
}
